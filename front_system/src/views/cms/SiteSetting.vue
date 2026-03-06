<template>
  <div class="cms-setting">
    <a-card title="站点设置（Site Setting）" :bordered="false">
      <a-alert
        type="info"
        show-icon
        message="这里保存的是全站配置 JSON（标题、logo、导航、社交链接、页脚等）。前台通过 /noAuth/cms/siteSetting 读取。"
        style="margin-bottom: 12px"
      />

      <a-space style="margin-bottom: 12px">
        <a-button type="primary" @click="loadSetting">刷新</a-button>
        <a-button :loading="saving" type="primary" @click="saveSetting" v-if="canEdit">保存</a-button>
      </a-space>

      <a-form layout="vertical">
        <a-form-item label="settings_json">
          <a-textarea
            v-model:value="settingsJson"
            :auto-size="{ minRows: 14, maxRows: 32 }"
            placeholder='例如：{"title":"我的网站","nav":[{"text":"博客","href":"/blog"}]}'
          />
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'
import { usePermission } from '@/composables/usePermission'

const { checkPermission } = usePermission()
const canEdit = computed(() => checkPermission('cms:site:edit'))

const settingsJson = ref('{}')
const saving = ref(false)

const loadSetting = async () => {
  try {
    const res = await post('/cms/siteSetting', { data: null })
    // 兼容后端 getMapping 返回结构：res.data 为对象
    settingsJson.value = res?.data?.settingsJson || '{}'
  } catch (e) {
    console.error(e)
    message.error('获取站点设置失败')
  }
}

const saveSetting = async () => {
  if (!canEdit.value) return
  try {
    // basic json validation
    JSON.parse(settingsJson.value || '{}')
  } catch (e) {
    message.error('JSON 格式不正确')
    return
  }
  saving.value = true
  try {
    await post('/cms/siteSetting/update', { data: settingsJson.value })
    message.success('保存成功')
    await loadSetting()
  } catch (e) {
    console.error(e)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSetting()
})
</script>

<style scoped>
.cms-setting {
  padding: 12px;
}
</style>

