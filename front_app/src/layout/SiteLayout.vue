<script setup>
import { computed } from 'vue'
import { useRoute, RouterLink, RouterView } from 'vue-router'

const route = useRoute()
const isHome = computed(() => route.path === '/')

const navItems = [
  { text: '首页', to: '/' },
  { text: '博客', to: '/dynamic/blogs' },
  { text: '项目经历', to: '/dynamic/projects' },
]
</script>

<template>
  <div class="VPApp" :class="{ VPHome: isHome }">
    <header class="VPNavBar" role="banner">
      <div class="VPNavBarTitle">
        <RouterLink class="title" to="/">
          <span>zyfan</span>
        </RouterLink>
      </div>
      <nav class="VPNavBarMenu" aria-label="主导航">
        <RouterLink
          v-for="it in navItems"
          :key="it.to"
          class="VPNavBarMenuLink"
          :to="it.to"
          active-class="active"
        >
          {{ it.text }}
        </RouterLink>
      </nav>
    </header>

    <main class="VPContent" role="main">
      <RouterView />
    </main>

    <footer class="VPFooter">
      <div class="VPFooterMessage" v-html="`zyfan · 全栈工程师 · <a href=&quot;mailto:2938668548@qq.com&quot;>2938668548@qq.com</a>`" />
      <div class="VPFooterCopyright">
        © 2026 版权与声明：本站内容仅代表个人观点，转载/引用请注明出处，如有侵权请邮件联系删除。
      </div>
    </footer>
  </div>
</template>

