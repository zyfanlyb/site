<template>
  <transition name="welcome-fade">
    <div v-if="show" class="welcome-container">
      <!-- 水墨晕染背景 -->
      <div class="ink-bg">
        <div class="ink-blob ink-blob-1"></div>
        <div class="ink-blob ink-blob-2"></div>
        <div class="ink-blob ink-blob-3"></div>
      </div>

      <!-- 浮动墨点 -->
      <div class="ink-particles">
        <div
          v-for="i in 12"
          :key="i"
          class="ink-dot"
          :style="getInkDotStyle(i)"
        ></div>
      </div>

      <div class="welcome-content">
        <!-- 逐字显现的欢迎文字 -->
        <div class="welcome-text">
          <h1 class="welcome-title">
            <span
              v-for="(char, idx) in 'welcome'"
              :key="idx"
              class="char"
              :class="{ 'char-visible': animateTitle && charVisible[idx] }"
              :style="{ animationDelay: `${idx * 0.08}s` }"
            >{{ char }}</span>
          </h1>
          <p class="welcome-subtitle" :class="{ 'animate-in': animateSubtitle }">
            正在为您准备
          </p>
        </div>

        <!-- 毛笔笔触风格加载条 -->
        <div class="welcome-loader" :class="{ 'animate-in': animateLoader }">
          <div class="brush-track">
            <div class="brush-fill"></div>
          </div>
          <div class="loader-hint">加载中</div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, computed } from 'vue';

const props = defineProps({
  show: { type: Boolean, default: false },
  duration: { type: Number, default: 2000 },
  ready: { type: Boolean, default: false }
});

const emit = defineEmits(['complete']);

const animateTitle = ref(false);
const animateSubtitle = ref(false);
const animateLoader = ref(false);
const startTime = ref(0);
const readyTime = ref(0);
let completeTimer = null;

// 逐字显示：根据 animateTitle 延迟计算每个字是否可见
const charVisible = ref([false, false, false, false, false, false, false]);
watch(animateTitle, (val) => {
  if (val) {
    charVisible.value = [false, false, false, false, false, false, false];
    'welcome'.split('').forEach((_, i) => {
      setTimeout(() => {
        charVisible.value = [...charVisible.value.slice(0, i), true, ...charVisible.value.slice(i + 1)];
      }, i * 120);
    });
  } else {
    charVisible.value = [false, false, false, false, false, false, false];
  }
});

const getInkDotStyle = (index) => {
  const left = (index * 8.5) % 95 + 2;
  const delay = (index * 0.15) % 2.5;
  const size = 3 + (index % 2) * 2;
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    width: `${size}px`,
    height: `${size}px`,
  };
};

const triggerComplete = () => {
  if (completeTimer) {
    clearTimeout(completeTimer);
    completeTimer = null;
  }
  emit('complete');
};

watch(() => props.show, (newVal) => {
  if (newVal) {
    startTime.value = Date.now();
    readyTime.value = 0;
    setTimeout(() => { animateTitle.value = true; }, 80);
    setTimeout(() => { animateSubtitle.value = true; }, 400);
    setTimeout(() => { animateLoader.value = true; }, 600);
    if (props.ready) {
      readyTime.value = Date.now();
      const elapsed = readyTime.value - startTime.value;
      const remaining = Math.max(0, props.duration - elapsed);
      completeTimer = setTimeout(triggerComplete, remaining);
    }
  } else {
    animateTitle.value = false;
    animateSubtitle.value = false;
    animateLoader.value = false;
    if (completeTimer) {
      clearTimeout(completeTimer);
      completeTimer = null;
    }
  }
});

watch(() => props.ready, (newVal) => {
  if (newVal && props.show && !readyTime.value) {
    readyTime.value = Date.now();
    const elapsed = readyTime.value - startTime.value;
    const remaining = Math.max(0, props.duration - elapsed);
    if (completeTimer) clearTimeout(completeTimer);
    completeTimer = setTimeout(triggerComplete, remaining);
  }
});
</script>

<style scoped>
.welcome-container {
  position: fixed;
  inset: 0;
  background-image: linear-gradient(to top, #fad0c4 0%, #ffd1ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  overflow: hidden;
}

.ink-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ink-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: inkFloat 8s ease-in-out infinite;
}

.ink-blob-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(251, 113, 133, 0.45) 0%, transparent 70%);
  top: -10%;
  left: -5%;
  animation-delay: 0s;
}

.ink-blob-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(244, 114, 182, 0.4) 0%, transparent 70%);
  bottom: -15%;
  right: -5%;
  animation-delay: -3s;
}

.ink-blob-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(253, 186, 116, 0.45) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
}

@keyframes inkFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(20px, -30px) scale(1.05); }
  66% { transform: translate(-15px, 20px) scale(0.95); }
}

.ink-particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.ink-dot {
  position: absolute;
  background: rgba(236, 72, 153, 0.45);
  border-radius: 50%;
  top: -10px;
  animation: inkRise 4s ease-in-out infinite;
  box-shadow: 0 0 8px rgba(236, 72, 153, 0.3);
}

@keyframes inkRise {
  0% { transform: translateY(0) scale(0); opacity: 0; }
  15% { opacity: 0.8; }
  85% { opacity: 0.8; }
  100% { transform: translateY(110vh) scale(1); opacity: 0; }
}

.welcome-content {
  position: relative;
  text-align: center;
  z-index: 1;
}

.welcome-text {
  margin-bottom: 48px;
}

.welcome-title {
  font-family: "LXGW WenKai", serif;
  font-size: 64px;
  font-weight: 700;
  color: #4a3f3f;
  margin: 0 0 20px 0;
  letter-spacing: 0.2em;
  display: flex;
  justify-content: center;
  gap: 4px;
}

.welcome-title .char {
  display: inline-block;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  text-shadow: 0 2px 12px rgba(236, 72, 153, 0.25);
}

.welcome-title .char.char-visible {
  opacity: 1;
  transform: translateY(0);
}

.welcome-subtitle {
  font-size: 18px;
  font-weight: 300;
  color: rgba(74, 63, 63, 0.85);
  margin: 0;
  letter-spacing: 0.3em;
  opacity: 0;
  transform: translateY(12px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.welcome-subtitle.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.welcome-loader {
  opacity: 0;
  transform: translateY(16px);
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.welcome-loader.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.brush-track {
  width: 200px;
  height: 6px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 3px;
  overflow: hidden;
  margin: 0 auto 12px;
}

.brush-fill {
  height: 100%;
  width: 0;
  background: linear-gradient(90deg, #f472b6, #ec4899);
  border-radius: 3px;
  animation: brushStroke 1.8s ease-in-out infinite;
}

@keyframes brushStroke {
  0% { width: 0%; }
  50% { width: 100%; }
  100% { width: 0%; }
}

.loader-hint {
  font-size: 13px;
  color: rgba(74, 63, 63, 0.7);
  letter-spacing: 0.2em;
}

.welcome-fade-enter-active,
.welcome-fade-leave-active {
  transition: opacity 0.5s ease;
}

.welcome-fade-enter-from,
.welcome-fade-leave-to {
  opacity: 0;
}
</style>
