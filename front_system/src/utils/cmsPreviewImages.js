import { getCmsFilePreviewUrl } from '@/utils/file'

function isBlobOrDataUrl(url) {
  return url.startsWith('blob:') || url.startsWith('data:')
}

/** 非本站存储路径的外链（http/https），<img> 直接展示即可 */
function isExternalImageUrl(src) {
  const s = String(src || '').trim()
  if (!/^(https?:)?\/\//i.test(s)) return false
  // 即使是完整 URL，只要指向「预览接口」，也不能让浏览器直连（无 token），需走下方解析
  if (s.includes('/file/preview')) return false
  return true
}

/**
 * 从 img.src 得到 objectName（MinIO 对象路径，如 yyyy/MM/dd/xxx.png）
 * - Markdown 里常见：/yyyy/MM/dd/xxx.png 或 yyyy/MM/dd/xxx.png
 * - 若误把「预览接口地址」写进 src（含 /file/preview?objectName=），从 query 取出 objectName，
 *   仍用 axios 带 Authorization 拉 blob，避免预览失败
 */
function resolveObjectNameFromImgSrc(raw) {
  const s = String(raw || '').trim()
  if (!s) return ''

  if (s.includes('/file/preview')) {
    try {
      const u = new URL(s, typeof window !== 'undefined' ? window.location.origin : 'http://localhost')
      const on = u.searchParams.get('objectName')
      if (on) return decodeURIComponent(on)
    } catch (_) {
      /* ignore */
    }
    return ''
  }

  // md-editor-v3 里有时会输出相对路径 /yyyy/MM/dd/xxx.png
  if (s.startsWith('/')) return s.replace(/^\/+/, '')
  return s
}

/** 正文未保存前的本地插图占位（与 site_cms 合并逻辑一致） */
const INLINE_PLACEHOLDER = /^__CMS_INLINE_(\d+)__$/

function normalizeImgSrcForMatch(raw) {
  const s = String(raw || '').trim()
  if (!s) return ''
  try {
    return decodeURIComponent(s)
  } catch {
    return s
  }
}

/**
 * 将 rootEl 内的 <img> 的 src（objectName）替换为 /file/preview 对应的 blob URL。
 * 适用于“需要 token 鉴权的图片预览”，避免 <img> 无法携带 Authorization Header 的问题。
 *
 * @param {HTMLElement} rootEl
 * @param {{ resolveInlinePlaceholder?: (src: string) => string | null | Promise<string | null> }} [options]
 *   未保存正文的插图占位 {@code __CMS_INLINE_n__}，由调用方提供本地 File 对应的 blob URL
 */
export async function hydrateCmsPreviewImages(rootEl, options = {}) {
  if (!rootEl?.querySelectorAll) return

  const imgs = Array.from(rootEl.querySelectorAll('img'))
  if (!imgs.length) return

  const resolveInline = options?.resolveInlinePlaceholder

  await Promise.all(
    imgs.map(async (img) => {
      try {
        const srcRaw = img.getAttribute('src') || ''
        if (!srcRaw) return
        if (isBlobOrDataUrl(srcRaw)) return
        if (img.dataset?.cmsPreviewHydrated === '1') return

        const normalized = normalizeImgSrcForMatch(srcRaw)
        if (resolveInline && INLINE_PLACEHOLDER.test(normalized)) {
          const maybe = resolveInline(normalized)
          const blobUrl = maybe && typeof maybe.then === 'function' ? await maybe : maybe
          if (blobUrl) {
            img.dataset.cmsPreviewHydrated = '1'
            img.setAttribute('src', blobUrl)
          }
          return
        }

        // 外链图（非本站 objectName、非预览接口）不处理
        if (isExternalImageUrl(srcRaw)) return

        const objectName = resolveObjectNameFromImgSrc(srcRaw)
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

