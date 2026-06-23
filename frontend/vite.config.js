import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // SSE 流式响应不缓冲
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.url === '/api/chat/stream') {
              proxyReq.setHeader('Connection', 'keep-alive')
            }
          })
        }
      }
    }
  }
})
