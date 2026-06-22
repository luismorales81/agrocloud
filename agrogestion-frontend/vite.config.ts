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
        timeout: 120000,
        proxyTimeout: 120000,
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
    dedupe: ['react', 'react-dom', 'react-router', 'react-router-dom'],
  },
  optimizeDeps: {
    include: ['react', 'react-dom', 'react-router-dom'],
  },
  build: {
    chunkSizeWarningLimit: 700,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // React, React-DOM y React-Router en un chunk (carga crítica)
            if (id.includes('react-router') || id.includes('react-dom') || id.includes('/react/')) {
              return 'react-vendor';
            }
            // MUI y Emotion (gran librería)
            if (id.includes('@mui') || id.includes('@emotion')) {
              return 'mui';
            }
            // Axios (muy usada)
            if (id.includes('axios')) {
              return 'axios';
            }
            // Recharts (gráficos, solo en algunas vistas)
            if (id.includes('recharts')) {
              return 'recharts';
            }
            // Lucide icons
            if (id.includes('lucide-react')) {
              return 'lucide';
            }
            // Headless UI y Heroicons
            if (id.includes('@headlessui') || id.includes('@heroicons')) {
              return 'ui-icons';
            }
            // Google Maps (solo en vistas que usan mapa)
            if (id.includes('@googlemaps') || id.includes('google.maps')) {
              return 'maps';
            }
            // Resto de dependencias
            return 'vendor';
          }
        },
        chunkFileNames: 'assets/[name]-[hash].js',
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      },
    },
  },
})