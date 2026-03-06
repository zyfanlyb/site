/**
 * 文件工具函数
 */
import service from '@/utils/request'

// 缓存blob URL，避免重复请求
const blobUrlCache = new Map()

/**
 * 获取文件预览URL（带token认证）
 * @param {string} objectName - 文件路径（objectName，格式：yyyy/MM/dd/uuid.extension）
 * @returns {Promise<string>} 预览URL（blob URL）
 */
export async function getFilePreviewUrl(objectName) {
  if (!objectName) {
    return ''
  }
  
  // 如果缓存中有，直接返回
  if (blobUrlCache.has(objectName)) {
    return blobUrlCache.get(objectName)
  }
  
  try {
    const url = `/file/preview`

    // 使用 POST 请求并通过表单参数传递文件名（参数名：fileName）
    const formData = new FormData()
    formData.append('fileName', objectName)
    
    // 通过axios请求文件内容，会自动携带token
    const response = await service.post(url, formData, {
      responseType: 'blob'
    })
    
    // 创建blob URL
    const blobUrl = URL.createObjectURL(response.data)
    
    // 缓存blob URL
    blobUrlCache.set(objectName, blobUrl)
    
    return blobUrl
  } catch (error) {
    console.error('获取文件预览失败:', error)
    return ''
  }
}

/**
 * 获取文件预览URL（同步版本，用于需要立即返回URL的场景）
 * @param {string} objectName - 文件路径
 * @returns {string} 预览URL
 * @deprecated 请使用异步版本的getFilePreviewUrl
 */
export function getFilePreviewUrlSync(objectName) {
  if (!objectName) {
    return ''
  }
  
  const baseURL = import.meta.env.VITE_APP_BASE_API || ''
  // 仅用于构建 URL，这里也将参数名改为 fileName 以与接口保持一致
  return `${baseURL}/file/preview?fileName=${encodeURIComponent(objectName)}`
}
