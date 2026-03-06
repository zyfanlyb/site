<template>
  <transition name="welcome-fade">
    <div v-if="show" class="welcome-container">
      <div class="welcome-content">
        <!-- 欢迎文字 -->
        <div class="welcome-text">
          <h1 class="welcome-title" :class="{ 'animate-in': animateTitle }">
            welcome
          </h1>
        </div>
        
        <!-- 装饰性动画元素 -->
        <div class="welcome-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
          <div class="decoration-circle circle-4"></div>
          <div class="decoration-circle circle-5"></div>
        </div>
        
        <!-- 粒子效果背景 -->
        <div class="particles">
          <div class="particle" v-for="i in 20" :key="i" :style="getParticleStyle(i)"></div>
        </div>
        
        <!-- 加载指示器 -->
        <div class="welcome-loader" :class="{ 'animate-in': animateLoader }">
          <div class="loader-dot"></div>
          <div class="loader-dot"></div>
          <div class="loader-dot"></div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  duration: {
    type: Number,
    default: 2000 // 默认2秒（最小显示时间）
  },
  ready: {
    type: Boolean,
    default: false // 资源是否已准备好
  }
});

const emit = defineEmits(['complete']);

const animateTitle = ref(false);
const animateSubtitle = ref(false);
const animateLoader = ref(false);
const startTime = ref(0);
const readyTime = ref(0);
let completeTimer = null;

// 生成粒子样式
const getParticleStyle = (index) => {
  const delay = (index * 0.1) % 2;
  const left = (index * 7.5) % 100;
  const size = 4 + (index % 3) * 2;
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    width: `${size}px`,
    height: `${size}px`,
  };
};

// 触发完成事件
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
    
    // 依次触发动画
    setTimeout(() => {
      animateTitle.value = true;
    }, 100);
    
    setTimeout(() => {
      animateSubtitle.value = true;
    }, 300);
    
    setTimeout(() => {
      animateLoader.value = true;
    }, 500);
    
    // 如果已经准备好，等待最小显示时间后完成
    if (props.ready) {
      readyTime.value = Date.now();
      const elapsed = readyTime.value - startTime.value;
      const remaining = Math.max(0, props.duration - elapsed);
      completeTimer = setTimeout(triggerComplete, remaining);
    } else {
      // 如果还没准备好，等待准备好后再处理
      completeTimer = setTimeout(triggerComplete, props.duration);
    }
  } else {
    // 重置动画状态
    animateTitle.value = false;
    animateSubtitle.value = false;
    animateLoader.value = false;
    if (completeTimer) {
      clearTimeout(completeTimer);
      completeTimer = null;
    }
  }
});

// 监听 ready 状态变化
watch(() => props.ready, (newVal) => {
  if (newVal && props.show && !readyTime.value) {
    readyTime.value = Date.now();
    const elapsed = readyTime.value - startTime.value;
    const remaining = Math.max(0, props.duration - elapsed);
    
    // 清除之前的定时器
    if (completeTimer) {
      clearTimeout(completeTimer);
    }
    
    // 等待剩余的最小显示时间后完成
    completeTimer = setTimeout(triggerComplete, remaining);
  }
});
</script>

<style scoped>
.welcome-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(to top, #fad0c4 0%, #ffd1ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  overflow: hidden;
}

.welcome-content {
  position: relative;
  text-align: center;
  z-index: 1;
}

/* 欢迎文字动画 */
.welcome-text {
  margin-bottom: 60px;
}

.welcome-title {
  font-size: 56px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px 0;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: 2px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.welcome-title.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.welcome-subtitle {
  font-size: 20px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: 4px;
}

.welcome-subtitle.animate-in {
  opacity: 1;
  transform: translateY(0);
}

/* 装饰性圆圈 - 涟漪扩散效果 */
.welcome-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.6);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  visibility: hidden;
  will-change: transform, opacity;
  backface-visibility: hidden;
}

.circle-1 {
  width: 300px;
  height: 300px;
  animation: ripple 3.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) infinite backwards;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  animation: ripple 3.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) infinite backwards;
  animation-delay: 0.7s;
}

.circle-3 {
  width: 300px;
  height: 300px;
  animation: ripple 3.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) infinite backwards;
  animation-delay: 1.4s;
}

.circle-4 {
  width: 300px;
  height: 300px;
  animation: ripple 3.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) infinite backwards;
  animation-delay: 2.1s;
}

.circle-5 {
  width: 300px;
  height: 300px;
  animation: ripple 3.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) infinite backwards;
  animation-delay: 2.8s;
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
    visibility: hidden;
  }
  3% {
    opacity: 0.8;
    visibility: visible;
  }
  50% {
    opacity: 0.4;
    visibility: visible;
  }
  100% {
    transform: translate(-50%, -50%) scale(3.5);
    opacity: 0;
    visibility: hidden;
  }
}

/* 加载指示器 */
.welcome-loader {
  display: flex;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.welcome-loader.animate-in {
  opacity: 1;
  transform: translateY(0);
}

.loader-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ffffff;
  animation: dotBounce 1.4s ease-in-out infinite;
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.4);
}

.loader-dot:nth-child(1) {
  animation-delay: 0s;
}

.loader-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.loader-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.6;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 粒子效果 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: rgba(236, 72, 153, 0.4);
  border-radius: 50%;
  animation: particleFloat 3s ease-in-out infinite;
  top: -10px;
  box-shadow: 0 2px 6px rgba(236, 72, 153, 0.3);
}

@keyframes particleFloat {
  0% {
    transform: translateY(0) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(110vh) scale(1);
    opacity: 0;
  }
}

/* 淡入淡出过渡 */
.welcome-fade-enter-active,
.welcome-fade-leave-active {
  transition: opacity 0.5s ease;
}

.welcome-fade-enter-from,
.welcome-fade-leave-to {
  opacity: 0;
}
</style>
