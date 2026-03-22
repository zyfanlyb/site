<script setup>
/**
 * 首屏装饰：三栏能力展示，仅 hover 动效，无链接、无业务逻辑
 */
const items = [
  {
    title: 'Web 全栈开发',
    subtitle: 'FULL STACK DEVELOPMENT',
    desc: '基于现代技术栈构建高可用系统，从前端交互到后端架构，将复杂业务沉淀为可维护、可演进的产品实现。',
  },
  {
    title: '智能体应用开发',
    subtitle: 'AGENT APPLICATIONS',
    desc: '定制化 Agent 落地与编排，让智能体嵌入业务流程，在可控边界内完成从感知到执行的闭环。',
  },
  {
    title: '游戏开发',
    subtitle: 'GAME DEVELOPMENT',
    desc: '独立向创作，学习并自研游戏，从建模到开发全流程。',
  },
]
</script>

<template>
  <section
    class="home-showcase-strip"
    aria-label="能力方向展示（装饰性）"
  >
    <div
      v-for="(it, i) in items"
      :key="i"
      class="home-showcase-card"
    >
      <div class="home-showcase-card__icon-wrap" aria-hidden="true">
        <!-- 1: 浏览器 + 代码 -->
        <svg
          v-if="i === 0"
          class="home-showcase-card__svg"
          viewBox="0 0 64 64"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <rect
            x="9"
            y="13"
            width="46"
            height="38"
            rx="4"
            stroke="currentColor"
            stroke-width="2"
          />
          <path d="M9 23h46" stroke="currentColor" stroke-width="2" />
          <circle cx="16" cy="18" r="2" fill="currentColor" />
          <circle cx="24" cy="18" r="2" fill="currentColor" />
          <path
            d="m23 37-5-5 5-5M36 42l5-10-5-10"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <!-- 2: 智能体 — 中心节点 + 多向连接（编排 / 多 Agent） -->
        <svg
          v-else-if="i === 1"
          class="home-showcase-card__svg"
          viewBox="0 0 64 64"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M32 20v10M32 34v10M20 32h10M34 32h10"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <circle
            cx="32"
            cy="32"
            r="9"
            stroke="currentColor"
            stroke-width="2"
          />
          <circle cx="32" cy="14" r="4.5" stroke="currentColor" stroke-width="2" />
          <circle cx="32" cy="50" r="4.5" stroke="currentColor" stroke-width="2" />
          <circle cx="14" cy="32" r="4.5" stroke="currentColor" stroke-width="2" />
          <circle cx="50" cy="32" r="4.5" stroke="currentColor" stroke-width="2" />
        </svg>
        <!-- 3: 游戏 — 手柄 -->
        <svg
          v-else
          class="home-showcase-card__svg"
          viewBox="0 0 64 64"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <rect
            x="9"
            y="24"
            width="46"
            height="22"
            rx="11"
            stroke="currentColor"
            stroke-width="2"
          />
          <path
            d="M21 33h7M24.5 29.5v7"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <circle cx="40" cy="31" r="2.75" fill="currentColor" />
          <circle cx="46" cy="36" r="2.75" fill="currentColor" />
        </svg>
      </div>
      <h3 class="home-showcase-card__title">{{ it.title }}</h3>
      <p class="home-showcase-card__sub">{{ it.subtitle }}</p>
      <p class="home-showcase-card__desc">{{ it.desc }}</p>
    </div>
  </section>
</template>
