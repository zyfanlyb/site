<template>
  <div class="icon-selector">
    <a-input
      v-model:value="inputValue"
      placeholder="请选择图标"
      :disabled="disabled"
      readonly
      @click="showModal = true"
    >
      <template #prefix>
        <component v-if="selectedIconComponent" :is="selectedIconComponent" />
        <span v-else style="color: #bfbfbf;">图标</span>
      </template>
      <template #suffix>
        <SearchOutlined @click.stop="showModal = true" style="cursor: pointer;" />
      </template>
    </a-input>
    
    <a-modal
      v-model:open="showModal"
      title="选择图标"
      :width="600"
      :footer="null"
    >
      <a-input
        v-model:value="searchText"
        placeholder="搜索图标"
        style="margin-bottom: 16px;"
      >
        <template #prefix>
          <SearchOutlined />
        </template>
      </a-input>
      
      <div class="icon-list">
        <div
          v-for="icon in filteredIcons"
          :key="icon.name"
          class="icon-item"
          :class="{ active: selectedIconName === icon.name }"
          @click="selectIcon(icon.name)"
        >
          <component :is="icon.component" />
          <span class="icon-name">{{ icon.name }}</span>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import * as Icons from '@ant-design/icons-vue'
import { SearchOutlined } from '@ant-design/icons-vue'

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

const showModal = ref(false)
const searchText = ref('')
const selectedIconName = ref(props.value || '')

// 常用图标列表（去重）
const iconList = [
  // 基础图标
  'HomeOutlined', 'AppstoreOutlined', 'MenuOutlined', 'SettingOutlined', 'DashboardOutlined',
  // 用户相关
  'UserOutlined', 'TeamOutlined', 'UserAddOutlined', 'UserDeleteOutlined', 'ContactsOutlined',
  'UsergroupAddOutlined', 'UsergroupDeleteOutlined', 'UserSwitchOutlined', 'SolutionOutlined',
  'IdcardOutlined',
  // 文件相关
  'FileOutlined', 'FolderOutlined', 'FolderOpenOutlined', 'FileTextOutlined', 'FileSearchOutlined',
  'FileSyncOutlined', 'FileProtectOutlined', 'FileExcelOutlined', 'FilePdfOutlined', 'FileWordOutlined',
  'FileImageOutlined', 'FileZipOutlined', 'FileUnknownOutlined', 'FolderAddOutlined', 'FolderViewOutlined',
  // 编辑相关
  'EditOutlined', 'DeleteOutlined', 'CopyOutlined', 'SaveOutlined', 'FormOutlined',
  // 操作相关
  'PlusOutlined', 'MinusOutlined', 'CheckOutlined', 'CloseOutlined', 'SearchOutlined',
  'ReloadOutlined', 'DownloadOutlined', 'UploadOutlined', 'SwapOutlined', 'RetweetOutlined',
  // 导航相关
  'LeftOutlined', 'RightOutlined', 'UpOutlined', 'DownOutlined', 'ArrowLeftOutlined',
  'ArrowRightOutlined', 'ArrowUpOutlined', 'ArrowDownOutlined',
  // 状态相关
  'CheckCircleOutlined', 'CloseCircleOutlined', 'ExclamationCircleOutlined', 'InfoCircleOutlined',
  'WarningOutlined', 'QuestionCircleOutlined',
  // 数据相关
  'TableOutlined', 'DatabaseOutlined', 'BarChartOutlined', 'PieChartOutlined', 'LineChartOutlined',
  'AreaChartOutlined',
  // 系统相关
  'DesktopOutlined', 'LaptopOutlined', 'MobileOutlined', 'TabletOutlined', 'CloudOutlined',
  'ServerOutlined', 'ApiOutlined', 'ControlOutlined', 'FunctionOutlined', 'CodeSandboxOutlined',
  'CodeOutlined', 'BugOutlined', 'ToolOutlined', 'ExperimentOutlined',
  // 其他常用
  'MailOutlined', 'PhoneOutlined', 'MessageOutlined', 'NotificationOutlined', 'BellOutlined',
  'CalendarOutlined', 'ClockCircleOutlined', 'HistoryOutlined', 'LockOutlined', 'UnlockOutlined',
  'KeyOutlined', 'SafetyOutlined', 'EyeOutlined', 'EyeInvisibleOutlined', 'FilterOutlined',
  'SortAscendingOutlined', 'SortDescendingOutlined', 'PoweroffOutlined', 'LogoutOutlined',
  'LoginOutlined', 'ShoppingCartOutlined', 'ShoppingOutlined', 'GiftOutlined', 'HeartOutlined',
  'StarOutlined', 'LikeOutlined', 'DislikeOutlined', 'ShareAltOutlined', 'LinkOutlined',
  'GlobalOutlined', 'EnvironmentOutlined', 'CameraOutlined', 'PictureOutlined', 'VideoCameraOutlined',
  'SoundOutlined', 'PlayCircleOutlined', 'PauseCircleOutlined', 'StopOutlined', 'ThunderboltOutlined',
  'FireOutlined', 'RocketOutlined', 'BulbOutlined', 'BookOutlined', 'ReadOutlined',
  'TagsOutlined', 'TagOutlined', 'FlagOutlined', 'TrophyOutlined', 'CrownOutlined',
  'DiamondOutlined', 'GoldOutlined', 'BankOutlined', 'ShopOutlined', 'CarOutlined',
  'BuildOutlined', 'MedicineBoxOutlined', 'SafetyCertificateOutlined', 'ProjectOutlined',
  'PartitionOutlined', 'ClusterOutlined', 'DeploymentUnitOutlined', 'AppstoreAddOutlined'
]

// 去重并过滤存在的图标
const availableIcons = computed(() => {
  const uniqueIcons = [...new Set(iconList)]
  return uniqueIcons
    .filter(name => Icons[name])
    .map(name => ({
      name,
      component: Icons[name]
    }))
})

// 过滤图标
const filteredIcons = computed(() => {
  if (!searchText.value) {
    return availableIcons.value
  }
  const search = searchText.value.toLowerCase()
  return availableIcons.value.filter(icon => 
    icon.name.toLowerCase().includes(search)
  )
})

// 选中的图标组件
const selectedIconComponent = computed(() => {
  const iconName = selectedIconName.value
  if (!iconName || typeof iconName !== 'string' || iconName.trim() === '') {
    return null
  }
  const IconComponent = Icons[iconName]
  return IconComponent || null
})

// 输入框显示的值
const inputValue = computed({
  get: () => selectedIconName.value,
  set: (val) => {
    selectedIconName.value = val || ''
    emit('update:value', val || '')
    emit('change', val || '')
  }
})

// 选择图标
const selectIcon = (iconName) => {
  selectedIconName.value = iconName
  emit('update:value', iconName)
  emit('change', iconName)
  showModal.value = false
  searchText.value = ''
}

// 监听外部值变化
watch(() => props.value, (newVal) => {
  const newValue = newVal || ''
  if (newValue !== selectedIconName.value) {
    selectedIconName.value = newValue
  }
}, { immediate: true, deep: true })
</script>

<style scoped>
.icon-selector {
  width: 100%;
}

.icon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.icon-item:hover {
  border-color: #7dd3fc;
  background-color: #f0f9ff;
}

.icon-item.active {
  border-color: #7dd3fc;
  background-color: #e0f2fe;
}

.icon-item :deep(svg) {
  font-size: 24px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.icon-item:hover :deep(svg),
.icon-item.active :deep(svg) {
  color: #38bdf8;
}

.icon-name {
  font-size: 12px;
  color: #cbd5e1;
  text-align: center;
  word-break: break-all;
}

.icon-item:hover .icon-name,
.icon-item.active .icon-name {
  color: #38bdf8;
}
</style>