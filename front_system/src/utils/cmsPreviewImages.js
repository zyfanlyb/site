import { getCmsFilePreviewUrl } from '@/utils/file'

function isProbablyRemoteUrl(url) {
  return /^(https?:)?\/\//i.test(url)
}

function isAlreadyPreviewUrl(url) {
  return url.includes('/file/preview')
}

function isBlobOrDataUrl(url) {
  return url.startsWith('blob:') || url.startsWith('data:')
}

function normalizeObjectName(src) {
  if (!src) return ''
  const s = String(src).trim()
  if (!s) return ''

  // md-editor-v3 里有时会输出相对路径 /yyyy/MM/dd/xxx.png
  if (s.startsWith('/')) return s.slice(1)
  return s
}

/**
 * 将 rootEl 内的 <img> 的 src（objectName）替换为 /file/preview 对应的 blob URL。
 * 适用于“需要 token 鉴权的图片预览”，避免 <img> 无法携带 Authorization Header 的问题。
 */
export async function hydrateCmsPreviewImages(rootEl) {
  if (!rootEl?.querySelectorAll) return

  const imgs = Array.from(rootEl.querySelectorAll('img'))
  if (!imgs.length) return

  await Promise.all(
    imgs.map(async (img) => {
      try {
        const src = img.getAttribute('src') || ''
        if (!src) return
        if (isBlobOrDataUrl(src)) return
        if (isProbablyRemoteUrl(src)) return
        if (isAlreadyPreviewUrl(src)) return
        if (img.dataset?.cmsPreviewHydrated === '1') return

        const objectName = normalizeObjectName(src)
        if (!objectName) return

        img.dataset.cmsPreviewHydrated = '1'
        const blobUrl = await getCmsFilePreviewUrl(objectName)
        if (blobUrl) {
          img.setAttribute('src', blobUrl)
        }
      } catch (e) {
        // 单个图片失败不影响整体渲染
      }
    })
  )
}

