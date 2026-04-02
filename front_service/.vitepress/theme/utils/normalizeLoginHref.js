/** 登录跳转地址规范化（与 AuthGateLayout / 历史逻辑一致） */
export function normalizeLoginHref(href) {
  const s = String(href || '').trim()
  if (!s) return '/auth/login'

  if (s.startsWith('http://') || s.startsWith('https://')) return s
  if (s.startsWith('//')) return s
  if (s.startsWith('/')) return s

  return '/' + s
}
