<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import {
  MailOutlined,
  PhoneOutlined,
  WechatOutlined,
} from '@ant-design/icons-vue'

/**
 * 图标来源（二选一）：
 * 1) iconfont：在 front_service/.env 设置 VITE_ICONFONT_CSS=你的「Font class」在线 css 地址；
 *    可选 VITE_ICONFONT_BASE_CLASS（默认 iconfont）、VITE_ICONFONT_PHONE / WECHAT / MAIL 类名。
 * 2) 未配置时：使用 @ant-design/icons-vue（PhoneOutlined / WechatOutlined / MailOutlined）。
 */
const vf = import.meta.env.VITE_ICONFONT_CSS
const useIconfont = Boolean(vf && String(vf).trim())

const iconfontBase = import.meta.env.VITE_ICONFONT_BASE_CLASS || 'iconfont'
const iconfontPhone = import.meta.env.VITE_ICONFONT_PHONE || 'icon-dianhua'
const iconfontWechat = import.meta.env.VITE_ICONFONT_WECHAT || 'icon-weixin'
const iconfontMail = import.meta.env.VITE_ICONFONT_MAIL || 'icon-youxiang'

function iconfontClasses(extra) {
  return [iconfontBase, extra].filter(Boolean)
}

/**
 * 手机 / 邮箱：非链接；方形容器内默认仅图标，悬停/聚焦显示纯文字。
 * 微信：点击弹二维码。.vitepress/public/wechat-qr.png
 */
const rawBase = import.meta.env.BASE_URL || '/'
const base = rawBase.endsWith('/') ? rawBase : `${rawBase}/`
const wechatQrFile = 'wechat-qr.png'

const CONTACT = {
  phone: '19971049361',
  email: '2938668548@qq.com',
}

const qrSrc = `${base}${wechatQrFile}`

const wechatOpen = ref(false)
const qrFailed = ref(false)

function openWechat() {
  qrFailed.value = false
  wechatOpen.value = true
}

function closeWechat() {
  wechatOpen.value = false
}

/** 复制到剪贴板（优先 Clipboard API，降级 execCommand） */
async function copyToClipboard(text) {
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
      return true
    }
  } catch {
    /* fall through */
  }
  try {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.setAttribute('readonly', '')
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(ta)
    return ok
  } catch {
    return false
  }
}

/** 复制成功后的短暂提示：'phone' | 'email' | null */
const copyFlash = ref(null)
let copyFlashTimer = 0

function flashCopy(kind) {
  copyFlash.value = kind
  window.clearTimeout(copyFlashTimer)
  copyFlashTimer = window.setTimeout(() => {
    copyFlash.value = null
  }, 1600)
}

async function onCopyPhone() {
  const text = CONTACT.phone.replace(/\s+/g, '')
  if (await copyToClipboard(text)) flashCopy('phone')
}

async function onCopyEmail() {
  if (await copyToClipboard(CONTACT.email)) flashCopy('email')
}

function onKeydown(e) {
  if (e.key === 'Escape') closeWechat()
}

watch(wechatOpen, (open) => {
  if (typeof document === 'undefined') return
  document.body.style.overflow = open ? 'hidden' : ''
})

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  window.clearTimeout(copyFlashTimer)
  document.body.style.overflow = ''
})
</script>

<template>
  <div
    class="home-contact-channels"
    role="group"
    aria-label="联系方式（图标入口）"
  >
    <button
      type="button"
      class="home-contact-channels__btn home-contact-channels__peekable home-contact-channels__tile home-contact-channels__tile--phone"
      :class="{ 'is-copy-feedback': copyFlash === 'phone' }"
      :aria-label="`手机号 ${CONTACT.phone.replace(/\s+/g, '')}，点击复制`"
      @click="onCopyPhone"
    >
      <span class="home-contact-channels__icon" aria-hidden="true">
        <i
          v-if="useIconfont"
          class="home-contact-channels__iconfont"
          :class="iconfontClasses(iconfontPhone)"
        />
        <PhoneOutlined v-else class="home-contact-channels__icon-antd" />
      </span>
      <span class="home-contact-channels__peek">{{
        copyFlash === 'phone' ? '已复制' : CONTACT.phone
      }}</span>
    </button>

    <button
      type="button"
      class="home-contact-channels__btn home-contact-channels__tile home-contact-channels__tile--wechat"
      aria-label="查看微信二维码"
      aria-haspopup="dialog"
      :aria-expanded="wechatOpen"
      @click="openWechat"
    >
      <span class="home-contact-channels__icon" aria-hidden="true">
        <i
          v-if="useIconfont"
          class="home-contact-channels__iconfont"
          :class="iconfontClasses(iconfontWechat)"
        />
        <WechatOutlined v-else class="home-contact-channels__icon-antd" />
      </span>
    </button>

    <button
      type="button"
      class="home-contact-channels__btn home-contact-channels__peekable home-contact-channels__tile home-contact-channels__tile--mail"
      :class="{ 'is-copy-feedback': copyFlash === 'email' }"
      :aria-label="`邮箱 ${CONTACT.email}，点击复制`"
      @click="onCopyEmail"
    >
      <span class="home-contact-channels__icon" aria-hidden="true">
        <i
          v-if="useIconfont"
          class="home-contact-channels__iconfont"
          :class="iconfontClasses(iconfontMail)"
        />
        <MailOutlined v-else class="home-contact-channels__icon-antd" />
      </span>
      <span
        class="home-contact-channels__peek home-contact-channels__peek--email"
        >{{
        copyFlash === 'email' ? '已复制' : CONTACT.email
      }}</span>
    </button>
  </div>

  <Teleport to="body">
    <div v-if="wechatOpen" class="home-contact-modal-root">
      <div
        class="home-contact-modal-mask"
        aria-hidden="true"
        @click="closeWechat"
      />
      <div
        class="home-contact-modal-wrap"
        role="dialog"
        aria-modal="true"
        aria-labelledby="home-contact-modal-title"
      >
        <div class="home-contact-modal-content" @click.stop>
          <div class="home-contact-modal-header">
            <span id="home-contact-modal-title" class="home-contact-modal-title">
              微信扫一扫
            </span>
          </div>
          <button
            type="button"
            class="home-contact-modal-close"
            aria-label="关闭"
            @click="closeWechat"
          >
            <span class="home-contact-modal-close-x" aria-hidden="true">×</span>
          </button>
          <div class="home-contact-modal-body">
            <div class="home-contact-modal-body-inner">
              <img
                :src="qrSrc"
                alt="微信二维码"
                class="home-contact-modal-qr"
                decoding="async"
                @error="qrFailed = true"
              />
              <p v-if="qrFailed" class="home-contact-modal-fallback">
                未找到二维码图片。请将文件放到
                <code>.vitepress/public/wechat-qr.png</code>
                后重新构建。
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
