import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    tailwindcss(),
    react(),
  ],
  define: {
    global: 'window',
  },
  server: {
    port: 5173,
    host: true, // Allows access across LAN physical devices
    allowedHosts: true, // Allows Cloudflare Tunnel and public domain hostnames
    proxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/ws-devclash': {
        target: 'http://localhost:8085',
        ws: true,
        changeOrigin: true,
      },
    },
  },
})
