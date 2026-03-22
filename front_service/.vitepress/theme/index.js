import { nextTick } from 'vue'
import DefaultTheme from 'vitepress/theme'
import BlogList from '../components/BlogList.vue'
import AgentAuthGate from '../components/AgentAuthGate.vue'
import HomeContactChannels from './HomeContactChannels.vue'
import './custom.css'
import AuthGateLayout from './AuthGateLayout.vue'

export default {
  extends: DefaultTheme,
  Layout: AuthGateLayout,
  enhanceApp({ app, router }) {
    app.component('BlogList', BlogList)
    app.component('AgentAuthGate', AgentAuthGate)
    app.component('HomeContactChannels', HomeContactChannels)

    if (typeof window === 'undefined') return

    const HOME_TOP = 0

    let snapping = false
    let scrollEndTimer = null
    let snapSettleFallbackId = null

    const isHomePage = () => {
      const p = window.location.pathname.replace(/\/+$/, '')
      return p === '' || p === '/index.html'
    }

    const getAboutEl = () => document.getElementById('about')
    const getContactEl = () => document.getElementById('contact')
    const getFooterEl = () => document.querySelector('footer.VPFooter, footer')
    const getWrapEl = () => document.querySelector('.home-smooth-wrap')

    /** 该区块顶边还在视口偏下 → 仍算「上一屏」 */
    const stillBelowFold = (el) => {
      if (!el) return false
      const r = el.getBoundingClientRect()
      return r.top > window.innerHeight * 0.4
    }

    const hasReachedScreenTop = (el) => {
      if (!el) return false
      return el.getBoundingClientRect().top <= 140
    }

    const isBetweenScreens = (el) =>
      Boolean(el) && !stillBelowFold(el) && !hasReachedScreenTop(el)

    const isFooterEnteringView = () => {
      const footerEl = getFooterEl()
      if (!footerEl) return false
      const r = footerEl.getBoundingClientRect()
      return r.top < window.innerHeight + 80
    }

    const updateFooterNearClass = () => {
      const wrap = getWrapEl()
      if (!wrap) return
      wrap.classList.toggle('home-footer-near', isFooterEnteringView())
    }

    const getMaxScrollTop = () => {
      const h = Math.max(
        document.documentElement.scrollHeight,
        document.body.scrollHeight,
      )
      return Math.max(0, h - window.innerHeight)
    }

    const isNearBottom = (px = 64) =>
      window.scrollY >= getMaxScrollTop() - px

    /**
     * 关于我：按「是否处在阅读区」切换 .home-about--in-view。
     * 从第三屏滚回时 #about 在视口上方，stillBelowFold 仍为 false，旧逻辑不会 remove，导致再上滚无过渡。
     */
    const isAboutInProminentView = () => {
      const about = getAboutEl()
      if (!about) return false
      const r = about.getBoundingClientRect()
      const vh = window.innerHeight
      const visible = Math.min(r.bottom, vh) - Math.max(r.top, 0)
      if (visible <= 0) return false
      const ratio = visible / Math.max(1, Math.min(r.height, vh))
      const anchored = r.top <= vh * 0.55 && r.bottom >= vh * 0.26
      return ratio >= 0.2 || anchored
    }

    const syncAboutRevealFromScroll = () => {
      if (!isHomePage()) return
      const about = getAboutEl()
      if (!about) return
      if (isAboutInProminentView()) {
        about.classList.add('home-about--in-view')
      } else {
        about.classList.remove('home-about--in-view')
      }
    }

    const syncAllHomeRevealsFromScroll = () => {
      syncAboutRevealFromScroll()
    }

    let aboutRevealScrollRaf = null
    const onScrollAboutReveal = () => {
      if (!isHomePage()) return
      if (aboutRevealScrollRaf != null) return
      aboutRevealScrollRaf = window.requestAnimationFrame(() => {
        aboutRevealScrollRaf = null
        if (!isHomePage()) return
        syncAllHomeRevealsFromScroll()
      })
    }

    let aboutRevealIo = null
    const bindAboutIntersectionObserver = () => {
      const about = getAboutEl()
      if (!about) return
      if (!aboutRevealIo) {
        aboutRevealIo = new IntersectionObserver(
          () => {
            syncAllHomeRevealsFromScroll()
          },
          { threshold: [0, 0.08, 0.12, 0.2, 0.35, 0.5] },
        )
      }
      aboutRevealIo.disconnect()
      aboutRevealIo.observe(about)
    }

    const teardownHomeRevealEnv = () => {
      document.documentElement.classList.remove('home-about-tech-js')
      aboutRevealIo?.disconnect()
      const about = getAboutEl()
      if (about) {
        about.classList.remove('home-about--in-view')
      }
    }

    const setupHomeRevealEnv = () => {
      if (!isHomePage()) return
      document.documentElement.classList.add('home-about-tech-js')
      bindAboutIntersectionObserver()
      scheduleAboutRevealChecks()
    }

    const syncHomeRevealEnvForRoute = async () => {
      await nextTick()
      await nextTick()
      if (isHomePage()) {
        setupHomeRevealEnv()
      } else {
        teardownHomeRevealEnv()
      }
    }

    const runHomeRevealAfterRoute = () => {
      void syncHomeRevealEnvForRoute()
    }
    router.onAfterRouteChanged = runHomeRevealAfterRoute
    router.onAfterRouteChange = runHomeRevealAfterRoute
    void syncHomeRevealEnvForRoute()

    window.addEventListener('vp-home-reveal-kick', () => {
      void syncHomeRevealEnvForRoute()
    })

    const scheduleAboutRevealChecks = () => {
      syncAllHomeRevealsFromScroll()
      window.requestAnimationFrame(() => {
        syncAllHomeRevealsFromScroll()
        window.requestAnimationFrame(syncAllHomeRevealsFromScroll)
      })
      ;[60, 180, 420, 680, 900].forEach((ms) => {
        window.setTimeout(syncAllHomeRevealsFromScroll, ms)
      })
    }

    const clearSnapSettleWatch = () => {
      if (snapSettleFallbackId != null) {
        window.clearTimeout(snapSettleFallbackId)
        snapSettleFallbackId = null
      }
    }

    /** 程序化滚动结束后解除 snapping，并同步第二屏 reveal */
    const completeSnapWhenScrollSettles = (maxWaitMs) => {
      clearSnapSettleWatch()
      let done = false
      const finish = () => {
        if (done) return
        done = true
        clearSnapSettleWatch()
        window.removeEventListener('scrollend', onScrollEnd)
        snapping = false
        if (isHomePage()) syncAllHomeRevealsFromScroll()
      }
      const onScrollEnd = () => finish()
      window.addEventListener('scrollend', onScrollEnd, { passive: true })
      snapSettleFallbackId = window.setTimeout(finish, maxWaitMs)
    }

    const scrollToPageBottom = () => {
      if (snapping) return
      snapping = true
      const max = getMaxScrollTop()
      window.scrollTo({ top: max, left: 0, behavior: 'smooth' })
      completeSnapWhenScrollSettles(1200)
      if (isHomePage()) scheduleAboutRevealChecks()
    }

    const snapToTop = () => {
      if (snapping) return
      snapping = true
      window.history.replaceState(null, '', window.location.pathname)
      window.scrollTo({ top: HOME_TOP, behavior: 'smooth' })
      completeSnapWhenScrollSettles(950)
      if (isHomePage()) scheduleAboutRevealChecks()
    }

    const snapToEl = (el, hashId) => {
      if (snapping || !el) return
      snapping = true
      const path = window.location.pathname
      window.history.replaceState(
        null,
        '',
        hashId ? `${path}#${hashId}` : path,
      )
      el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      completeSnapWhenScrollSettles(950)
      if (isHomePage()) scheduleAboutRevealChecks()
    }

    const snapToAbout = () => snapToEl(getAboutEl(), 'about')
    const snapToContact = () => snapToEl(getContactEl(), 'contact')

    window.addEventListener('scroll', onScrollAboutReveal, { passive: true })

    /** 向下箭头：1→2→3 屏，第三屏后再点去页脚（与 stillBelowFold 判定一致） */
    const handleArrowAction = () => {
      const aboutEl = getAboutEl()
      const contactEl = getContactEl()
      if (!aboutEl) return
      if (stillBelowFold(aboutEl)) {
        snapToAbout()
        return
      }
      if (contactEl && stillBelowFold(contactEl)) {
        snapToContact()
        return
      }
      scrollToPageBottom()
    }

    document.addEventListener(
      'click',
      (e) => {
        const el = e.target?.closest?.('.home-scroll-indicator')
        if (!el) return
        if (!isHomePage()) return
        e.preventDefault()
        e.stopPropagation()
        e.stopImmediatePropagation?.()
        handleArrowAction()
      },
      true,
    )

    window.addEventListener(
      'wheel',
      (e) => {
        if (!isHomePage()) return
        const aboutEl = getAboutEl()
        if (!aboutEl) return
        const contactEl = getContactEl()

        updateFooterNearClass()

        if (snapping) {
          e.preventDefault()
          return
        }
        if (Math.abs(e.deltaY) < Math.abs(e.deltaX)) return

        const footerVisible = isFooterEnteringView()
        const nearBottom = isNearBottom()

        /**
         * 从页脚/接近文档底部往上滚：
         * - 若 #contact 仍在视口「正常阅读区」（短页面时 scrollY 也会 nearBottom），应回第二屏 #about，
         *   不能仍 snapToContact（已在第三屏会 noop + preventDefault → 滚轮像卡住）。
         * - 若 #contact 已滚到视口上方，才是从页脚回到第三屏。
         */
        if ((footerVisible || nearBottom) && e.deltaY < 0) {
          e.preventDefault()
          if (contactEl) {
            const r = contactEl.getBoundingClientRect()
            const thirdStillInView =
              r.top >= -100 &&
              r.bottom > Math.min(200, window.innerHeight * 0.25)
            if (thirdStillInView) snapToAbout()
            else snapToContact()
          } else {
            snapToAbout()
          }
          return
        }

        if (nearBottom && e.deltaY > 0) return

        // —— 首屏 ↔ #about ——
        if (stillBelowFold(aboutEl)) {
          if (e.deltaY > 0) {
            e.preventDefault()
            snapToAbout()
          } else if (e.deltaY < 0 && isBetweenScreens(aboutEl)) {
            e.preventDefault()
            snapToTop()
          }
          return
        }

        // —— 有第三屏：#about ↔ #contact ——
        if (contactEl) {
          if (stillBelowFold(contactEl)) {
            if (e.deltaY > 0) {
              e.preventDefault()
              snapToContact()
              return
            }
            if (e.deltaY < 0) {
              const ra = aboutEl.getBoundingClientRect()
              const nearAboutStart =
                ra.top >= -24 && ra.top <= 140 && window.scrollY > 16
              if (nearAboutStart) {
                e.preventDefault()
                snapToTop()
              }
            }
            return
          }

          if (isBetweenScreens(contactEl)) {
            if (e.deltaY > 0) {
              e.preventDefault()
              snapToContact()
            } else {
              e.preventDefault()
              snapToAbout()
            }
            return
          }

          // 已在 #contact 屏：向上滚回 #about（放宽顶边判定，避免只剩「拖滚动条」）
          if (e.deltaY < 0) {
            const rm = contactEl.getBoundingClientRect()
            const canStepBackToAbout = rm.top >= -120 && window.scrollY > 16
            if (canStepBackToAbout) {
              e.preventDefault()
              snapToAbout()
            }
          }
          return
        }

        // —— 无第三屏：等同原两屏逻辑 ——
        if (e.deltaY < 0) {
          const r = aboutEl.getBoundingClientRect()
          const nearAboutStart =
            r.top >= -24 && r.top <= 140 && window.scrollY > 16
          if (nearAboutStart) {
            e.preventDefault()
            snapToTop()
          }
        }
      },
      { passive: false },
    )

    window.addEventListener(
      'scroll',
      () => {
        if (!isHomePage()) return
        if (snapping) return

        updateFooterNearClass()
        syncAllHomeRevealsFromScroll()

        if (scrollEndTimer) window.clearTimeout(scrollEndTimer)
        scrollEndTimer = window.setTimeout(() => {
          const aboutEl = getAboutEl()
          const contactEl = getContactEl()
          if (!aboutEl) return
          if (isNearBottom()) return
          if (isFooterEnteringView() && isNearBottom(120)) return
          if (stillBelowFold(aboutEl)) return

          const y = window.scrollY

          // 停在 hero 与 #about 之间 → 吸附到较近一屏
          if (isBetweenScreens(aboutEl)) {
            const aboutTopDoc =
              aboutEl.getBoundingClientRect().top + window.scrollY
            const midpoint = (HOME_TOP + aboutTopDoc) / 2
            const next = y < midpoint ? HOME_TOP : aboutTopDoc
            if (Math.abs(y - next) < 6) return
            if (next === HOME_TOP) snapToTop()
            else snapToAbout()
            return
          }

          // 停在 #about 与 #contact 之间
          if (contactEl && hasReachedScreenTop(aboutEl)) {
            if (stillBelowFold(contactEl)) return
            if (isBetweenScreens(contactEl)) {
              const aboutTopDoc =
                aboutEl.getBoundingClientRect().top + window.scrollY
              const contactTopDoc =
                contactEl.getBoundingClientRect().top + window.scrollY
              const midpoint = (aboutTopDoc + contactTopDoc) / 2
              const next = y < midpoint ? aboutTopDoc : contactTopDoc
              if (Math.abs(y - next) < 6) return
              if (next === aboutTopDoc) snapToAbout()
              else snapToContact()
            }
          }
        }, 120)
      },
      { passive: true },
    )
  },
}
