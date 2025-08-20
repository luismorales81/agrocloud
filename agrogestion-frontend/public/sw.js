const CACHE_NAME = 'agrocloud-v1.0.0';
const STATIC_CACHE = 'agrocloud-static-v1.0.0';
const DYNAMIC_CACHE = 'agrocloud-dynamic-v1.0.0';

// Archivos estáticos para cache
const STATIC_FILES = [
  '/',
  '/index.html',
  '/manifest.json',
  '/static/js/bundle.js',
  '/static/css/main.css',
  '/icons/icon-192x192.png',
  '/icons/icon-512x512.png'
];

// Rutas de API que deben funcionar offline
const OFFLINE_API_ROUTES = [
  '/api/campos',
  '/api/lotes',
  '/api/insumos',
  '/api/labores',
  '/api/maquinaria'
];

// Instalación del Service Worker
self.addEventListener('install', (event) => {
  console.log('Service Worker: Instalando...');
  
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then((cache) => {
        console.log('Service Worker: Cacheando archivos estáticos');
        return cache.addAll(STATIC_FILES);
      })
      .then(() => {
        console.log('Service Worker: Instalación completada');
        return self.skipWaiting();
      })
  );
});

// Activación del Service Worker
self.addEventListener('activate', (event) => {
  console.log('Service Worker: Activando...');
  
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            if (cacheName !== STATIC_CACHE && cacheName !== DYNAMIC_CACHE) {
              console.log('Service Worker: Eliminando cache antiguo:', cacheName);
              return caches.delete(cacheName);
            }
          })
        );
      })
      .then(() => {
        console.log('Service Worker: Activación completada');
        return self.clients.claim();
      })
  );
});

// Interceptar peticiones
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // Estrategia para archivos estáticos
  if (request.method === 'GET' && isStaticFile(url.pathname)) {
    event.respondWith(cacheFirst(request));
  }
  // Estrategia para API calls
  else if (request.method === 'GET' && url.pathname.startsWith('/api/')) {
    event.respondWith(networkFirst(request));
  }
  // Estrategia para otras peticiones
  else {
    event.respondWith(networkFirst(request));
  }
});

// Estrategia Cache First para archivos estáticos
async function cacheFirst(request) {
  try {
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      return cachedResponse;
    }
    
    const networkResponse = await fetch(request);
    if (networkResponse.ok) {
      const cache = await caches.open(STATIC_CACHE);
      cache.put(request, networkResponse.clone());
    }
    return networkResponse;
  } catch (error) {
    console.log('Cache First fallback:', error);
    return new Response('Error de conexión', { status: 503 });
  }
}

// Estrategia Network First para API calls
async function networkFirst(request) {
  try {
    // Intentar red primero
    const networkResponse = await fetch(request);
    
    if (networkResponse.ok) {
      // Cachear respuesta exitosa
      const cache = await caches.open(DYNAMIC_CACHE);
      cache.put(request, networkResponse.clone());
    }
    
    return networkResponse;
  } catch (error) {
    console.log('Network First fallback - usando cache:', error);
    
    // Si no hay red, buscar en cache
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      return cachedResponse;
    }
    
    // Si no hay cache, devolver respuesta offline
    return getOfflineResponse(request);
  }
}

// Verificar si es archivo estático
function isStaticFile(pathname) {
  return pathname.includes('.js') || 
         pathname.includes('.css') || 
         pathname.includes('.png') || 
         pathname.includes('.jpg') || 
         pathname.includes('.ico') ||
         pathname.includes('.json') ||
         pathname === '/' ||
         pathname === '/index.html';
}

// Respuesta offline personalizada
function getOfflineResponse(request) {
  const url = new URL(request.url);
  
  // Para rutas de API específicas, devolver datos mock
  if (url.pathname.startsWith('/api/')) {
    return new Response(JSON.stringify(getOfflineData(url.pathname)), {
      headers: { 'Content-Type': 'application/json' },
      status: 200
    });
  }
  
  // Para otras rutas, devolver página offline
  return new Response(`
    <!DOCTYPE html>
    <html lang="es">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>AgroCloud - Sin Conexión</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          min-height: 100vh;
          margin: 0;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          text-align: center;
          padding: 20px;
        }
        .offline-icon {
          font-size: 4rem;
          margin-bottom: 1rem;
        }
        h1 {
          margin-bottom: 1rem;
        }
        p {
          margin-bottom: 2rem;
          opacity: 0.9;
        }
        .retry-btn {
          background: rgba(255,255,255,0.2);
          border: 2px solid white;
          color: white;
          padding: 12px 24px;
          border-radius: 8px;
          cursor: pointer;
          font-size: 1rem;
          transition: all 0.3s;
        }
        .retry-btn:hover {
          background: rgba(255,255,255,0.3);
        }
      </style>
    </head>
    <body>
      <div class="offline-icon">📡</div>
      <h1>Sin Conexión</h1>
      <p>No tienes conexión a internet. Algunas funciones están disponibles offline.</p>
      <button class="retry-btn" onclick="window.location.reload()">
        Reintentar Conexión
      </button>
    </body>
    </html>
  `, {
    headers: { 'Content-Type': 'text/html' },
    status: 200
  });
}

// Datos mock para funcionamiento offline
function getOfflineData(pathname) {
  switch (pathname) {
    case '/api/campos':
      return [
        { id: 1, nombre: 'Campo Norte', ubicacion: 'Ruta 9, Km 45', area_hectareas: 150.00 },
        { id: 2, nombre: 'Campo Sur', ubicacion: 'Ruta 9, Km 47', area_hectareas: 80.00 }
      ];
    
    case '/api/lotes':
      return [
        { id: 1, nombre: 'Lote A1', area_hectareas: 50.00, estado: 'EN_CULTIVO' },
        { id: 2, nombre: 'Lote B1', area_hectareas: 30.00, estado: 'DISPONIBLE' }
      ];
    
    case '/api/insumos':
      return [
        { id: 1, nombre: 'Semilla Soja DM 53i54', stock_actual: 100, stock_minimo: 50 },
        { id: 2, nombre: 'Fertilizante NPK', stock_actual: 25, stock_minimo: 30 }
      ];
    
    case '/api/labores':
      return [
        { id: 1, nombre: 'Siembra Lote A1', estado: 'COMPLETADA', fecha_labor: '2024-11-15' },
        { id: 2, nombre: 'Fertilización Lote B1', estado: 'PLANIFICADA', fecha_labor: '2024-11-20' }
      ];
    
    case '/api/maquinaria':
      return [
        { id: 1, nombre: 'Tractor John Deere 5075E', estado: 'activo', kilometros_uso: 12500.50 },
        { id: 2, nombre: 'Cosechadora New Holland CR8.90', estado: 'activo', kilometros_uso: 8500.25 }
      ];
    
    default:
      return { message: 'Datos no disponibles offline' };
  }
}

// Sincronización en background
self.addEventListener('sync', (event) => {
  if (event.tag === 'background-sync') {
    event.waitUntil(doBackgroundSync());
  }
});

// Función de sincronización en background
async function doBackgroundSync() {
  try {
    console.log('Service Worker: Sincronizando datos en background...');
    
    // Aquí se implementaría la lógica de sincronización
    // Por ejemplo, enviar datos guardados en IndexedDB al servidor
    
    const db = await openDB();
    const pendingData = await db.getAll('pendingData');
    
    for (const data of pendingData) {
      try {
        await fetch(data.url, {
          method: data.method,
          headers: data.headers,
          body: data.body
        });
        
        // Eliminar dato sincronizado
        await db.delete('pendingData', data.id);
      } catch (error) {
        console.log('Error sincronizando dato:', error);
      }
    }
    
    console.log('Service Worker: Sincronización completada');
  } catch (error) {
    console.log('Error en sincronización background:', error);
  }
}

// Función helper para abrir IndexedDB
function openDB() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open('AgroCloudDB', 1);
    
    request.onerror = () => reject(request.error);
    request.onsuccess = () => resolve(request.result);
    
    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      
      // Crear store para datos pendientes
      if (!db.objectStoreNames.contains('pendingData')) {
        const store = db.createObjectStore('pendingData', { keyPath: 'id', autoIncrement: true });
        store.createIndex('timestamp', 'timestamp', { unique: false });
      }
      
      // Crear store para cache de datos
      if (!db.objectStoreNames.contains('dataCache')) {
        const store = db.createObjectStore('dataCache', { keyPath: 'key' });
        store.createIndex('timestamp', 'timestamp', { unique: false });
      }
    };
  });
}
