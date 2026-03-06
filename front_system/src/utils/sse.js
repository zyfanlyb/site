/**
 * 智能体流式问答 SSE 请求
 * 调用 /chat/stream，传入 threadId（数据库ID，可选）与 question，消费 text/event-stream 流
 * 
 * 后端返回格式：
 * - 第一个数据块：{"type":"meta","threadId":xxx,"threadUuid":"xxx"}\n\n
 * - 后续数据块：{"type":"text","data":"文本内容"}\n\n
 * 
 * 注意：实际可能收到SSE格式的数据，如 data:data:{"type":"text","data":"..."}\n\n
 */
const baseURL = import.meta.env.VITE_APP_BASE_API || ''

/**
 * @param {number|null} threadId 对话线程ID（数据库主键ID，可选，null表示新建对话）
 * @param {string} question 用户问题
 * @param { (chunk: string) => void } onChunk 每收到一段文本回调（只接收文本内容，不包含JSON结构）
 * @param { (meta: {threadId: number, threadUuid: string}) => void } onMeta 收到元数据回调（第一个数据块）
 * @param { (err?: Error) => void } onEnd 结束回调（正常或异常）
 * @param {AbortController} abortController 可选的 AbortController，用于中断请求
 * @returns {{abort: () => void, reader: ReadableStreamDefaultReader|null}} 返回中断函数和 reader 对象
 */
export function chatStreamFetch(threadId, question, onChunk, onMeta, onEnd, abortController = null) {
  const url = `${baseURL}/chat/stream`
  const token = localStorage.getItem('user_token')
  const params = new URLSearchParams({ question })
  if (threadId != null) {
    params.append('threadId', threadId.toString())
  }

  // 如果没有传入 abortController，创建一个新的
  const controller = abortController || new AbortController()
  let reader = null
  let isAborted = false

  // 立即返回可中断的对象
  const result = {
    abort: () => {
      isAborted = true
      try {
        controller.abort()
        if (reader) {
          reader.cancel().catch(() => {})
        }
      } catch (e) {
        // 忽略错误
      }
    },
    reader: null
  }

  // 异步处理流
  ;(async () => {
    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          ...(token ? { Authorization: token } : {})
        },
        body: params.toString(),
        signal: controller.signal
      })

      if (!res.ok) {
        if (!isAborted) {
          onEnd(new Error(`HTTP ${res.status}`))
        }
        return
      }

      reader = res.body.getReader()
      result.reader = reader
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      let metaReceived = false
      let ended = false // 标记是否已经调用过onEnd

      try {
        while (true) {
          // 检查是否已被中断
          if (isAborted) {
            break
          }

          const { done, value } = await reader.read()
          if (done) break
          
          buffer += decoder.decode(value, { stream: true })
          
          // 按 \n\n 分割数据块（后端每个JSON对象以 \n\n 结尾）
          const parts = buffer.split('\n\n')
          buffer = parts.pop() || '' // 保留最后一个可能不完整的数据块
          
          for (const part of parts) {
            if (isAborted) break
            if (!part.trim()) continue
            
            let text = part.trim()
            
            // 移除所有可能的SSE格式前缀 data: 或 data:data: 等
            while (text.startsWith('data:')) {
              text = text.substring(5).trim()
            }
            
            if (!text) continue
            
            // 尝试解析JSON
            let json = null
            try {
              json = JSON.parse(text)
            } catch (e) {
              // 如果直接解析失败，尝试从文本中提取JSON对象
              // 查找第一个 { 到最后一个 } 之间的内容
              const firstBrace = text.indexOf('{')
              const lastBrace = text.lastIndexOf('}')
              if (firstBrace >= 0 && lastBrace > firstBrace) {
                try {
                  json = JSON.parse(text.substring(firstBrace, lastBrace + 1))
                } catch (e2) {
                  // 解析失败，跳过这个数据块
                  continue
                }
              } else {
                // 没有找到JSON对象，跳过
                continue
              }
            }
            
            if (!json || typeof json !== 'object') continue
            
            // 处理元数据
            if (json.type === 'meta' && json.threadId) {
              if (!metaReceived && !isAborted) {
                onMeta({ threadId: json.threadId, threadUuid: json.threadUuid })
                metaReceived = true
              }
              continue
            }
            
            // 处理文本内容 - 关键：只提取data字段的文本内容
            if (json.type === 'text' && json.data != null && json.data !== undefined) {
              const textContent = String(json.data)
              if (textContent && !isAborted) {
                onChunk(textContent)
              }
              continue
            }
            
            // 处理错误
            if (json.error) {
              if (!ended && !isAborted) {
                ended = true
                onEnd(new Error(String(json.error)))
              }
              return
            }
            
            // 如果有data字段（兼容其他类型），也提取
            if (json.data != null && json.data !== undefined) {
              const textContent = String(json.data)
              if (textContent && !isAborted) {
                onChunk(textContent)
              }
            }
          }
        }
        
        // 处理最后剩余的数据
        if (buffer.trim() && !isAborted) {
          let text = buffer.trim()
          while (text.startsWith('data:')) {
            text = text.substring(5).trim()
          }
          if (text) {
            let json = null
            try {
              json = JSON.parse(text)
            } catch (e) {
              const firstBrace = text.indexOf('{')
              const lastBrace = text.lastIndexOf('}')
              if (firstBrace >= 0 && lastBrace > firstBrace) {
                try {
                  json = JSON.parse(text.substring(firstBrace, lastBrace + 1))
                } catch (e2) {
                  // 忽略
                }
              }
            }
            
            if (json && typeof json === 'object' && !isAborted) {
              if (json.type === 'meta' && json.threadId && !metaReceived) {
                onMeta({ threadId: json.threadId, threadUuid: json.threadUuid })
              } else if (json.type === 'text' && json.data != null && json.data !== undefined) {
                onChunk(String(json.data))
              } else if (json.data != null && json.data !== undefined) {
                onChunk(String(json.data))
              }
            }
          }
        }
        
        // 正常结束
        if (!ended && !isAborted) {
          ended = true
          onEnd()
        }
      } catch (e) {
        // 异常结束
        if (!ended && !isAborted) {
          ended = true
          // 如果是 AbortError，不调用 onEnd
          if (e.name !== 'AbortError') {
            onEnd(e)
          }
        }
      } finally {
        // 确保reader被释放
        try {
          if (reader) {
            reader.releaseLock()
          }
        } catch (e) {
          // 忽略释放锁的错误
        }
      }
    } catch (e) {
      // 外层异常处理
      // 如果是 AbortError，说明是主动中断，不需要调用 onEnd
      if (e.name !== 'AbortError' && !isAborted) {
        onEnd(e)
      }
    }
  })()

  return result
}
