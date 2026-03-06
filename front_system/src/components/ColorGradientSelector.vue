<template>
  <div class="color-gradient-selector">
    <a-radio-group v-model:value="mode" :disabled="disabled" style="margin-bottom: 12px">
      <a-radio-button value="solid">单色</a-radio-button>
      <a-radio-button value="gradient">渐变</a-radio-button>
    </a-radio-group>

    <!-- 单色模式 -->
    <div v-if="mode === 'solid'" class="color-mode">
      <div class="color-input-wrapper">
        <input 
          type="color" 
          v-model="solidColor" 
          :disabled="disabled"
          @change="handleSolidColorChange"
          class="color-picker-input"
        />
        <a-input 
          v-model:value="solidColor" 
          :disabled="disabled"
          placeholder="请输入颜色值，如: #ffffff"
          @change="handleSolidColorChange"
          style="flex: 1"
        />
      </div>
    </div>

    <!-- 渐变模式 -->
    <div v-if="mode === 'gradient'" class="color-mode">
      <a-flex>
        <div class="gradient-preview" :style="{ background: gradientValue }">
        </div>
        <a-input
            :value="gradientValue"
            @update:value="updateGradientValue"
            :disabled="disabled"
            placeholder="如: linear-gradient(135deg, #FFB6C1 0%, #FFC0CB 100%)"
        />
      </a-flex>

    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:value', 'change'])

// 检测当前值的类型
const detectMode = (value) => {
  if (!value || value.trim() === '') {
    return 'solid'
  }
  // 检查是否是 linear-gradient 格式
  if (value.includes('linear-gradient') || value.includes('gradient')) {
    return 'gradient'
  }
  // 默认当作单色
  return 'solid'
}

const initialMode = detectMode(props.value)
const mode = ref(initialMode)
// 确保单色模式时，如果没有值或值为空，默认为白色
const getInitialSolidColor = () => {
  if (initialMode === 'solid') {
    const val = props.value || ''
    return val.trim() === '' ? '#ffffff' : val
  }
  return '#ffffff'
}
const solidColor = ref(getInitialSolidColor())
const gradientValue = ref(initialMode === 'gradient' ? (props.value || '') : '')

const handleSolidColorChange = () => {
  isInternalUpdate = true
  emit('update:value', solidColor.value || '')
  emit('change', solidColor.value || '')
}

const updateGradientValue = (val) => {
  gradientValue.value = val || ''
  isInternalUpdate = true
  emit('update:value', gradientValue.value)
  emit('change', gradientValue.value)
}

const handleGradientChange = () => {
  isInternalUpdate = true
  emit('update:value', gradientValue.value || '')
  emit('change', gradientValue.value || '')
}

// 监听模式切换
watch(mode, (newMode) => {
  if (newMode === 'solid') {
    // 切换到单色模式时，如果当前值是渐变，清空或使用默认值
    if (gradientValue.value && gradientValue.value.includes('linear-gradient')) {
      solidColor.value = '#ffffff'
      gradientValue.value = ''
    }
    handleSolidColorChange()
  } else if (newMode === 'gradient') {
    // 切换到渐变模式时，如果当前值是单色，清空渐变值，但不立即触发更新
    if (solidColor.value && !solidColor.value.includes('linear-gradient')) {
      if (!gradientValue.value) {
        gradientValue.value = ''
      }
    }
    // 如果有渐变值，使用它；否则使用空值
    handleGradientChange()
  }
})

// 一个标志，用来防止循环更新
let isInternalUpdate = false

// 监听外部值变化
watch(() => props.value, (newVal) => {
  // 如果是内部更新触发的，跳过
  if (isInternalUpdate) {
    isInternalUpdate = false
    return
  }
  
  const newValue = (newVal || '').trim()
  
  if (newValue === '') {
    if (mode.value !== 'solid') {
      mode.value = 'solid'
    }
    // 确保单色为空时设置为白色
    if (solidColor.value !== '#ffffff' || solidColor.value === '') {
      solidColor.value = '#ffffff'
      handleSolidColorChange()
    }
    gradientValue.value = ''
    return
  }
  
  const detectedMode = detectMode(newValue)
  
  // 只有在模式变化时才更新 mode
  if (detectedMode !== mode.value) {
    mode.value = detectedMode
  }
  
  if (detectedMode === 'solid') {
    if (solidColor.value !== newValue) {
      solidColor.value = newValue
    }
  } else if (detectedMode === 'gradient') {
    if (gradientValue.value !== newValue) {
      gradientValue.value = newValue
    }
  }
}, { immediate: true })
</script>

<style scoped>
.color-gradient-selector {
  width: 100%;
}

.color-mode {
  min-height: 40px;
}

.color-preview,
.gradient-preview {
  margin-top: 12px;
  margin-right: 12px;
  width: 60px;
  height: 40px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 12px;
}

.color-preview:hover,
.gradient-preview:hover {
  border-color: #40a9ff;
}

.empty-preview {
  background: #f5f5f5;
  color: #999;
}

.color-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}

.color-picker-input {
  width: 50px;
  height: 32px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  padding: 0;
  background: none;
}

.color-picker-input:hover:not(:disabled) {
  border-color: #40a9ff;
}

.color-picker-input:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.color-preview span {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.color-preview.empty-preview span {
  color: #999;
  text-shadow: none;
}
</style>
