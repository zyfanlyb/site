import { reactive } from 'vue'

/**
 * 全局分页配置
 */
export const defaultPaginationConfig = {
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: false,
  showTotal: total => `共 ${total} 条`,
  locale: {
    items_per_page: ' / 页',
  },
}

/**
 * 创建分页响应式对象
 * @param {Object} overrides - 可选的覆盖配置
 * @returns {Object} 响应式分页对象
 */
export function usePagination(overrides = {}) {
  return reactive({
    ...defaultPaginationConfig,
    ...overrides,
  })
}

/**
 * 创建分页变更处理函数
 * @param {Object} pagination - 分页响应式对象
 * @param {Function} fetchData - 数据获取函数
 * @returns {Function} 分页变更处理函数
 */
export function createPaginationHandler(pagination, fetchData) {
  return (pag, filters, sorter) => {
    if (pag) {
      const oldPageSize = pagination.pageSize
      if (pag.current !== undefined) {
        pagination.current = pag.current
      }
      if (pag.pageSize !== undefined) {
        pagination.pageSize = pag.pageSize
        // 当改变每页大小时，重置到第一页
        if (oldPageSize !== pag.pageSize) {
          pagination.current = 1
        }
      }
    }
    fetchData()
  }
}

/**
 * 重置分页到第一页
 * @param {Object} pagination - 分页响应式对象
 */
export function resetPagination(pagination) {
  pagination.current = 1
}
