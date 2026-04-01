import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import './styles/vp-vars.css'
import './styles/custom.css'
import './styles/app.css'

function injectIconfontCss() {
  if (typeof document === 'undefined') return
  const href = (import.meta.env.VITE_ICONFONT_CSS || '').trim()
  if (!href) return
  const id = 'iconfont-css'
  if (document.getElementById(id)) return
  const link = document.createElement('link')
  link.id = id
  link.rel = 'stylesheet'
  link.href = href
  document.head.appendChild(link)
}

injectIconfontCss()

createApp(App).use(router).mount('#app')

