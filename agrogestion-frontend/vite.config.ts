import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { copyFileSync } from 'fs'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // Plugin personalizado para copiar manifest.json
    {
      name: 'copy-manifest',
      closeBundle() {
        try {
          copyFileSync(
            path.resolve(__dirname, 'public/manifest.json'),
            path.resolve(__dirname, 'dist/manifest.json')
          )
          console.log('✅ manifest.json copiado a dist/')
        } catch (error) {
          console.error('❌ Error copiando manifest.json:', error)
        }
      }
    }
  ],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
})