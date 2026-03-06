<template>
  <a-modal
    v-model:open="visible"
    title="菜单绑定"
    width="800px"
    :confirm-loading="confirmLoading"
    ok-text="确认"
    cancel-text="取消"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-table
      :columns="columns"
      :row-key="record => record.id"
      :data-source="menuList"
        :row-selection="{
          selectedRowKeys: selectedMenuIds,
          onChange: onTreeCheck,
          checkStrictly: true
        }"
      :expand-icon-column-index="0"
      :default-expand-all-rows="true"
      size="small"
      bordered
      class="menu-select-table"
    >
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="text ? 'green' : 'red'">
            {{ text ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'type'">
          <span>{{ getTypeText(text) }}</span>
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<style scoped>
.menu-select-table {
  padding: 12px;
  background: #fff;
}

.menu-select-table :deep(.ant-table-row-level-0) {
  border-left: 2px solid #1890ff;
}

.menu-select-table :deep(.ant-table-row-level-1) {
  border-left: 2px solid #52c41a;
}

.menu-select-table :deep(.ant-table-row-level-2) {
  border-left: 2px solid #faad14;
}

.menu-select-table :deep(.ant-table-row-level-3) {
  border-left: 2px solid #f5222d;
}

.menu-select-table :deep(.ant-table-row:hover > td) {
  background-color: #f5f5f5;
}

.menu-select-table :deep(.ant-table-selection-column) {
  width: 60px;
  padding-right: 12px;
}

.menu-select-table :deep(.ant-table-row-expand-icon) {
  margin-right: 8px;
}
</style>

<script setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { post } from '@/utils/request'

const emit = defineEmits(['success'])
const visible = ref(false)
const confirmLoading = ref(false)
const selectedMenuIds = ref([])
const menuList = ref([])
const currentRoleId = ref(null)

const getTypeText = (type) => {
  const typeMap = {
    '0': '目录',
    '1': '菜单',
    '2': '按钮'
  }
  return typeMap[type] || type
}

const columns = [
  {
    title: '菜单名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '权限标识',
    dataIndex: 'perms',
    key: 'perms',
  },
  {
    title: '类型',
    key: 'type',
    dataIndex: 'type',
  },
  {
    title: '状态',
    key: 'status',
    dataIndex: 'status',
  }
]

// 重构后的选中逻辑，支持父子联动和半选状态
const onTreeCheck = (checkedKeys, { node, checked }) => {
  console.log('Tree checked keys raw:', checkedKeys)
  console.log('Previous selected IDs:', selectedMenuIds.value)
  console.log('Current node:', node)

  // 获取节点及其所有子节点ID
  const getNodeAndChildIds = (n) => {
    const ids = [n.id]
    if (n.children) {
      n.children.forEach(child => {
        ids.push(...getNodeAndChildIds(child))
      })
    }
    return ids
  }

  // 处理选中状态
  let newSelected = [...checkedKeys]

  // 处理当前节点及其子节点
  if (node) {
    const nodeIds = getNodeAndChildIds(node)
    if (checked) {
      // 添加当前节点及其子节点
      newSelected = [...new Set([...newSelected, ...nodeIds])]
    } else {
      // 移除当前节点及其子节点
      newSelected = newSelected.filter(id => !nodeIds.includes(id))
    }

    // 处理父节点半选状态
    const processParent = (parentNode) => {
      if (!parentNode) return

      const siblingIds = parentNode.children.map(c => c.id)
      const selectedSiblings = newSelected.filter(id => siblingIds.includes(id))

      if (selectedSiblings.length === 0) {
        // 无选中兄弟节点，移除父节点
        newSelected = newSelected.filter(id => id !== parentNode.id)
      } else if (selectedSiblings.length < siblingIds.length) {
        // 部分选中，添加父节点（半选状态）
        if (!newSelected.includes(parentNode.id)) {
          newSelected.push(parentNode.id)
        }
      }

      // 继续处理上层父节点
      processParent(parentNode.parent)
    }

    // 从当前节点的父节点开始处理
    processParent(node.parent)
  }

  selectedMenuIds.value = newSelected
  console.log('Updated selected IDs with hierarchy:', selectedMenuIds.value)
  console.log('Menu list structure:', menuList.value)
  
  // 调试：验证父菜单ID是否被正确包含
  const parentMenuIds = []
  const findParentIds = (menu) => {
    if (menu.parentId && !newSelected.includes(menu.parentId)) {
      parentMenuIds.push(menu.parentId)
    }
    if (menu.children) {
      menu.children.forEach(child => findParentIds(child))
    }
  }
  menuList.value.forEach(menu => findParentIds(menu))
  console.log('自动包含的父菜单IDs:', parentMenuIds)
}

const fetchMenuList = async () => {
  try {
    const res = await post('/sysMenu/page', {
      pageSize: 1000
    })
    console.log('Raw menu data:', res.data)
    menuList.value = res.data.map(item => ({
      ...item,
      key: item.id, // 确保tree-data中每个节点有key字段
      title: item.name
    }))
  } catch (error) {
    console.error(error)
    message.error('获取菜单列表失败')
  }
}

const open = (roleId, selectedIds = []) => {
  currentRoleId.value = roleId
  selectedMenuIds.value = selectedIds
  visible.value = true
  fetchMenuList()
}

const handleOk = async () => {
  confirmLoading.value = true
  try {
    console.log('Current role ID:', currentRoleId.value)
    console.log('Selected menu IDs before submit:', selectedMenuIds.value)

    console.log('All selected IDs before filter:', selectedMenuIds.value)
    console.log('Menu list IDs:', menuList.value.map(m => m.id))

    // 创建菜单ID到类型的映射
    const menuTypeMap = {}
    menuList.value.forEach(menu => {
      menuTypeMap[menu.id] = menu.type
      if (menu.children) {
        menu.children.forEach(child => {
          menuTypeMap[child.id] = child.type
        })
      }
    })

    // 只筛选类型为'2'(按钮)的菜单ID
    // const buttonMenuIds = [...new Set(selectedMenuIds.value || [])]
    //   .filter(id => menuTypeMap[id] === '2')
    const buttonMenuIds = selectedMenuIds.value

    console.log('Filtered button menu IDs:', buttonMenuIds)

    const params = {
      data: {
        roleId: currentRoleId.value,
        menuIds: buttonMenuIds
      }
    }
    console.log('提交的菜单IDs:', params.data.menuIds)

    console.log('Final params to API:', params)

    const response = await post('/sysRoleMenu/update', params)
    console.log('Full response:', response)
    console.log('API response:', response)
    visible.value = false
    emit('success')
    message.success('菜单绑定成功')
  } catch (error) {
    console.error(error)
    message.error('菜单绑定失败')
  } finally {
    confirmLoading.value = false
  }
}

const handleCancel = () => {
  visible.value = false
}

defineExpose({ open })
</script>
