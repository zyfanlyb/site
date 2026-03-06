import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/site', // 设置基础路径，用于部署在子路径下
  server: {
    proxy: {
      // 代理所有以/api开头的请求
      '/api': {
        target: 'http://localhost:11006', // 后端服务地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
    port: 12003,  // 固定端口号
    host: '0.0.0.0',  // 允许局域网访问
    open: false,  // 自动打开浏览器
    strictPort: true,  // 端口被占用时直接退出，不自动换端口
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  }
})
