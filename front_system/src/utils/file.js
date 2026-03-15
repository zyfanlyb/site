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
export async function getAuthFilePreviewUrl(objectName) {
  if (!objectName) {
    return ''
  }
  
  // 如果缓存中有，直接返回
  if (blobUrlCache.has(objectName)) {
    return blobUrlCache.get(objectName)
  }
  
  try {
    const url = `/file/auth/preview`

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

const cmsBlobUrlCache = new Map()

/**
 * 获取 CMS 文件预览 URL（FileController /file/preview，带 token）
 * @param {string} objectName - 文件路径（objectName，如 yyyy/MM/dd/uuid.jpg）
 * @returns {Promise<string>} 预览 blob URL
 */
export async function getCmsFilePreviewUrl(objectName) {
  if (!objectName) return ''
  if (cmsBlobUrlCache.has(objectName)) return cmsBlobUrlCache.get(objectName)
  try {
    const response = await service.get('/file/preview', {
      params: { objectName },
      responseType: 'blob'
    })
    const blobUrl = URL.createObjectURL(response.data)
    cmsBlobUrlCache.set(objectName, blobUrl)
    return blobUrl
  } catch (e) {
    console.error('CMS 文件预览失败:', e)
    return ''
  }
}
