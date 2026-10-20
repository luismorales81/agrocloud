import { defineConfig, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { copyFileSync, createReadStream, existsSync, mkdirSync } from 'fs'

const raizSitio = path.resolve(__dirname, '../sitio-publico')

function sitioPublico(): Plugin {
  const tipos: Record<string, string> = {
    '/': 'text/html; charset=utf-8',
    '/estilos.css': 'text/css; charset=utf-8',
    '/logo.png': 'image/png',
  }

  const servir = (req: { url?: string }, res: { setHeader: (nombre: string, valor: string) => void; end: (dato?: string) => void }, next: () => void) => {
    const ruta = (req.url || '').split('?')[0]
    if (!(ruta in tipos)) {
      next()
      return
    }
    const archivo = ruta === '/' ? 'index.html' : ruta.slice(1)
    const completo = path.join(raizSitio, archivo)
    if (!existsSync(completo)) {
      next()
      return
    }
    res.setHeader('Content-Type', tipos[ruta])
    createReadStream(completo).pipe(res as unknown as NodeJS.WritableStream)
  }

  return {
    name: 'sitio-publico',
    configureServer(server) {
      server.middlewares.use(servir)
    },
    closeBundle() {
      if (!existsSync(raizSitio)) {
        return
      }
      const destino = path.resolve(__dirname, 'dist/sitio')
      mkdirSync(destino, { recursive: true })
      for (const nombre of ['index.html', 'estilos.css', 'logo.png']) {
        copyFileSync(path.join(raizSitio, nombre), path.join(destino, nombre))
      }
    },
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    sitioPublico(),
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
        // No partir lucide/axios/vendor a mano: genera ciclos
        // ("Cannot access 'X' before initialization") en el JS minificado.
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }
          if (
            /[\\/]node_modules[\\/](react|react-dom|scheduler|react-router|react-router-dom)[\\/]/.test(
              id
            )
          ) {
            return 'react-vendor'
          }
        },
        chunkFileNames: 'assets/[name]-[hash].js',
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      },
    },
  },
})