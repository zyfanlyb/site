<template>
  <a-modal
    v-model:open="showModal"
    :title="title"
    :confirm-loading="confirmLoading"
    :footer="null"
    :width="700"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="菜单名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入菜单名称" :disabled="isDetail" />
      </a-form-item>

      <a-form-item label="菜单类型" name="type">
        <a-select v-model:value="formState.type" placeholder="请选择菜单类型" :disabled="isDetail">
          <a-select-option value="0">目录</a-select-option>
          <a-select-option value="1">菜单</a-select-option>
          <a-select-option value="2">按钮</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="父级菜单" name="parentId">
        <a-tree-select :disabled="isDetail"
          v-model:value="formState.parentId"
          style="width: 100%"
          :tree-data="menuTreeData"
          placeholder="请选择父级菜单"
          :fieldNames="{ children: 'children', label: 'name', key: 'id', value: 'id' }"
          allow-clear
        />
      </a-form-item>

      <a-form-item 
        v-if="formState.type !== '2'" 
        label="菜单路径" 
        name="path"
      >
        <a-input v-model:value="formState.path" placeholder="请输入菜单路径" :disabled="isDetail" />
      </a-form-item>

      <a-form-item 
        v-if="formState.type === '1'" 
        label="组件路径" 
        name="component"
      >
        <a-input v-model:value="formState.component" placeholder="请输入组件路径" :disabled="isDetail" />
      </a-form-item>

      <a-form-item label="权限标识" name="perms">
        <a-input v-model:value="formState.perms" placeholder="请输入权限标识" :disabled="isDetail" />
      </a-form-item>

      <a-form-item label="排序" name="sort">
        <a-input-number :disabled="isDetail"
          v-model:value="formState.sort" 
          placeholder="请输入排序" 
          style="width: 100%"
          :min="0"
        />
      </a-form-item>

      <a-form-item label="菜单图标" name="icon">
        <IconSelector v-model:value="formState.icon" :disabled="isDetail" />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-switch :disabled="isDetail"
          v-model:checked="formState.status" 
          checked-children="启用"
          un-checked-children="禁用"
        />
      </a-form-item>

      <a-form-item 
        v-if="formState.type === '1'" 
        label="路由参数" 
        name="query"
      >
        <a-input v-model:value="formState.query" placeholder="请输入路由参数" :disabled="isDetail" />
      </a-form-item>

      <template v-if="isDetail">
        <a-form-item label="创建时间">
          <a-input v-model:value="formState.createTime" disabled />
        </a-form-item>
        <a-form-item label="更新时间">
          <a-input v-model:value="formState.updateTime" disabled />
        </a-form-item>
        <a-form-item label="创建人">
          <a-input v-model:value="formState.createBy" disabled />
        </a-form-item>
        <a-form-item label="更新人">
          <a-input v-model:value="formState.updateBy" disabled />
        </a-form-item>
      </template>
    </a-form>
    <div>
      <div style="display: flex; justify-content: flex-end; gap: 16px; padding-top: 16px;">
        <a-button style="width: 80px;" @click="handleCancel">关闭</a-button>
        <a-button v-if="!isDetail" style="width: 80px;" type="primary" :loading="confirmLoading" @click="handleOk">{{ isEdit ? '保存' : '确定' }}</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, defineProps, defineEmits, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import IconSelector from '@/components/IconSelector.vue'
import { useAuthStore } from '@/stores/auth'
const showModal = ref(false)
import { post } from '@/utils/request'

const emits = defineEmits(['refresh', 'update:visible', 'update:open'])
const formRef = ref()
const confirmLoading = ref(false)
const menuTreeData = ref([])
const authStore = useAuthStore()

const loadMenuList = async () => {
  try {
    const res = await post('/sysMenu/listAll')
    menuTreeData.value = buildTree(res.data)
  } catch (error) {
    console.error(error)
    message.error('获取菜单列表失败')
  }
}

const buildTree = (menus) => {
  const menuMap = menus.reduce((map, menu) => {
    map[menu.id] = { ...menu, children: [] }
    return map
  }, {})

  const tree = []
  menus.forEach(menu => {
    if (!menu.parentId || menu.parentId === 0) {
      tree.push(menuMap[menu.id])
    } else if (menuMap[menu.parentId]) {
      menuMap[menu.parentId].children.push(menuMap[menu.id])
    }
  })
  sortTreeBySort(tree)
  return tree
}

/** 按 sort 字段递归排序树节点（根节点及各级子节点） */
const sortTreeBySort = (nodes) => {
  if (!nodes || !nodes.length) return
  nodes.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  nodes.forEach(node => {
    if (node.children && node.children.length) {
      sortTreeBySort(node.children)
    }
  })
}

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  // 兼容旧版本
  visible: {
    type: Boolean,
    default: false
  },
  currentId: {
    type: Number,
    default: null
  },
  menuTreeData: {
    type: Array,
    default: () => []
  }
})

const open = (id = null, modalMode = 'add') => {
  showModal.value = true
  emits('update:open', true)
  emits('update:visible', true)
  mode.value = modalMode
  loadMenuList()
  
  if (id) {
    title.value = modalMode === 'detail' ? '菜单详情' : '编辑菜单'
    loadMenuInfo(id)
  } else {
    title.value = '新增菜单'
    // 手动重置formState所有字段
    Object.assign(formState, {
      id: null,
      name: '',
      type: '0',
      parentId: null,
      path: '',
      component: '',
      perms: '',
      sort: 0,
      icon: '',
      status: true,
      query: '',
      createTime: '',
      updateTime: '',
      createBy: '',
      updateBy: ''
    })
    formRef.value?.resetFields()
  }
}

const loadMenuInfo = async (id) => {
  try {
    const res = await post(`/sysMenu/info/${id}`)
    Object.assign(formState, res.data)
  } catch (error) {
    console.error(error)
    message.error('获取菜单详情失败')
  }
}

const mode = ref('add')
const title = ref('新增菜单')

const isDetail = computed(() => mode.value === 'detail')
const isEdit = computed(() => mode.value === 'edit')

const formState = reactive({
  id: null,
  name: '',
  type: '0',
  parentId: null,
  path: '',
  component: '',
  perms: '',
  sort: 0,
  icon: '',
  status: true,
  query: '',
  createTime: '',
  updateTime: '',
  createBy: '',
  updateBy: ''
})

const rules = reactive({
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'blur' }]
})

watch(() => props.currentId, async (val) => {
  if (val) {
    title.value = '编辑菜单'
    try {
      const res = await post(`/sysMenu/info/${val}`)
      Object.assign(formState, res.data)
    } catch (error) {
      console.error(error)
      message.error('获取菜单详情失败')
    }
  } else {
    title.value = '新增菜单'
    formRef.value?.resetFields()
  }
})

// 清空表单数据
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formState, {
    id: null,
    name: '',
    type: '0',
    parentId: null,
    path: '',
    component: '',
    perms: '',
    sort: 0,
    icon: '',
    status: true,
    query: '',
    createTime: '',
    updateTime: '',
    createBy: '',
    updateBy: ''
  })
}

const handleOk = async () => {
  try {
    await formRef.value.validate()
    confirmLoading.value = true
    // 根据是否有ID调用不同接口
    const url = formState.id ? '/sysMenu/update' : '/sysMenu/insert'
    await post(url, { data: formState })
    // 菜单保存成功后，刷新用户菜单，确保 Home.vue 侧边栏菜单图标能立即生效
    try {
      await authStore.fetchUserMenus()
    } catch (e) {
      console.error('刷新用户菜单失败', e)
    }
    message.success('操作成功')
    emits('refresh')
    handleCancel()
  } catch (error) {
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  showModal.value = false
  emits('update:open', false)
  // 兼容旧版本
  emits('update:visible', false)
}

// 监听模态框关闭，每次关闭时清空表单
watch(showModal, (newVal) => {
  if (!newVal) {
    // 模态框关闭时清空表单
    resetForm()
  }
})

defineExpose({
  open
})
</script>