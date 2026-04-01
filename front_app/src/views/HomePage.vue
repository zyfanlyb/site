<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import HomeShowcaseStrip from '../components/HomeShowcaseStrip.vue'
import HomeContactChannels from '../components/HomeContactChannels.vue'
import { setupHomeScrollAndReveal } from '../work/homeScroll'

let cleanup = null
onMounted(() => {
  cleanup = setupHomeScrollAndReveal()
})

onBeforeUnmount(() => {
  cleanup?.()
  cleanup = null
})
</script>

<template>
  <div class="VPHome">
    <section class="VPHomeHero VPHero">
      <div class="container">
        <div class="main">
          <h1 class="VPHeroName">Web全栈·智能体</h1>
          <p class="VPHeroText">追求行业领先技术</p>
          <p class="VPHeroTagline">
            欢迎来到zyfan的个人小站，我致力于每一个可落地的需求，朝着将智能体带入商业和生活的方向而努力
          </p>

          <div class="home-hero-actions-extra">
            <HomeShowcaseStrip />
          </div>
        </div>
      </div>
    </section>

    <div class="home-smooth-wrap">
      <section id="about" class="home-about home-screen" aria-labelledby="about-heading">
        <div class="home-about__inner">
          <h1 id="about-heading" class="home-about__headline home-about__reveal">关于我</h1>
          <p class="home-about__lead home-about__reveal">
            简单介绍下我自己，我是一名全栈主后端开发，技术栈涉及 Web端·桌面端·小程序，目前在杭州发展工作，四年分布式微服务架构开发经验，开发测试部署全流程，目前的研究方向是AI智能体，已有多个智能体项目实际交付经验。
          </p>
          <div class="home-about__top-rule" aria-hidden="true"></div>
          <div class="home-about-grid">
            <div class="home-about__col">
              <h3 class="home-about__label home-about__reveal">后端技术栈</h3>
              <p class="home-about__stream home-about__tech-reveal">Java / Spring MVC / Spring Boot</p>
              <p class="home-about__stream home-about__tech-reveal">Spring Cloud / Spring Cloud Alibaba / Nacos / Gateway</p>
              <p class="home-about__stream home-about__tech-reveal">Spring Ai / Spring Ai Alibaba / Agent Framework</p>
              <p class="home-about__stream home-about__tech-reveal">Nginx</p>
              <p class="home-about__stream home-about__tech-reveal">Netty</p>
              <p class="home-about__stream home-about__tech-reveal">C++ / Qt</p>
              <p class="home-about__stream home-about__tech-reveal">Modbus / Jt808 / Jt809 / Jt1078</p>
              <p class="home-about__stream home-about__tech-reveal">Mysql / MangoDB / Redis / ElasticSearch</p>
              <p class="home-about__stream home-about__tech-reveal">RabbitMQ / RocketMQ</p>
              <p class="home-about__stream home-about__tech-reveal">Xxljob</p>
              <span class="home-about__col-rail" aria-hidden="true"></span>
            </div>
            <div class="home-about__col">
              <h3 class="home-about__label home-about__reveal">前端技术栈</h3>
              <p class="home-about__stream home-about__tech-reveal">Vue2 / Vue3</p>
              <p class="home-about__stream home-about__tech-reveal">Electron</p>
              <p class="home-about__stream home-about__tech-reveal">uni-app</p>
              <p class="home-about__stream home-about__tech-reveal">C++ / Qt</p>
              <p class="home-about__stream home-about__tech-reveal">Ant Design / Element UI</p>
              <p class="home-about__stream home-about__tech-reveal">ECharts</p>
              <p class="home-about__stream home-about__tech-reveal">Vite Press</p>
            </div>
          </div>
        </div>
      </section>

      <section id="contact" class="home-contact home-screen" aria-labelledby="contact-heading">
        <div class="home-about__inner home-contact__wrap">
          <header class="home-contact__intro">
            <h1 id="contact-heading" class="home-about__headline home-contact__title">联系我</h1>
            <p class="home-contact__scenes" aria-label="联系场景">
              <span>商务合作</span>
              <span class="home-contact__scenes-sep" aria-hidden="true">·</span>
              <span>技术交流</span>
            </p>
            <p class="home-contact__lead">
              如果你有定制开发、架构咨询或智能体应用方面的需求，欢迎与我咨询。技术问题也欢迎交流，我会在方便时第一时间回复。扫码添加我的微信，或点击复制电话或邮箱与我联系
            </p>
          </header>
          <div class="home-contact__top-rule" aria-hidden="true"></div>
          <HomeContactChannels />
        </div>
      </section>

      <button type="button" class="home-scroll-indicator">
        <span class="home-scroll-indicator__chevron home-scroll-indicator__chevron--1" aria-hidden="true"></span>
        <span class="home-scroll-indicator__chevron home-scroll-indicator__chevron--2" aria-hidden="true"></span>
        <span class="home-scroll-indicator__chevron home-scroll-indicator__chevron--3" aria-hidden="true"></span>
      </button>
    </div>
  </div>
</template>

