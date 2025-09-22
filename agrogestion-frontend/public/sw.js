// Service Worker para AgroCloud PWA
const CACHE_NAME = 'agrocloud-v1.0.0';
const STATIC_CACHE = 'agrocloud-static-v1.0.0';
const DYNAMIC_CACHE = 'agrocloud-dynamic-v1.0.0';
const API_CACHE = 'agrocloud-api-v1.0.0';

// Archivos estáticos para cache
const STATIC_FILES = [
  '/',
  '/index.html',
  '/manifest.json',
  '/offline.html',
  '/icons/agrocloud-logo.svg',
  '/icons/icon-192x192.png',
  '/icons/icon-512x512.png'
];

// Rutas de API para cache
const API_ROUTES = [
  '/api/fields',
  '/api/plots',
  '/api/crops',
  '/api/inputs',
  '/api/machinery',
  '/api/labors',
  '/api/reports'
];

// Instalación del Service Worker
self.addEventListener('install', (event) => {
  console.log('🔧 [SW] Instalando Service Worker...');
  
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then((cache) => {
        console.log('📦 [SW] Cacheando archivos estáticos...');
        return cache.addAll(STATIC_FILES);
      })
      .then(() => {
        console.log('✅ [SW] Service Worker instalado correctamente');
        return self.skipWaiting();
      })
      .catch((error) => {
        console.error('❌ [SW] Error durante la instalación:', error);
      })
  );
});

// Activación del Service Worker
self.addEventListener('activate', (event) => {
  console.log('🚀 [SW] Activando Service Worker...');
  
  event.waitUntil(
    caches.keys()
      .then((cacheNames) => {
        return Promise.all(
          cacheNames.map((cacheName) => {
            // Limpiar caches antiguos
            if (cacheName !== STATIC_CACHE && 
                cacheName !== DYNAMIC_CACHE && 
                cacheName !== API_CACHE) {
              console.log('🗑️ [SW] Eliminando cache antiguo:', cacheName);
              return caches.delete(cacheName);
            }
          })
        );
      })
      .then(() => {
        console.log('✅ [SW] Service Worker activado');
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
    event.respondWith(cacheFirst(request, STATIC_CACHE));
    return;
  }
  
  // Estrategia para API calls
  if (request.method === 'GET' && isApiRequest(url.pathname)) {
    event.respondWith(networkFirst(request, API_CACHE));
    return;
  }
  
  // Estrategia para otros recursos
  if (request.method === 'GET') {
    event.respondWith(networkFirst(request, DYNAMIC_CACHE));
    return;
  }
  
  // Para peticiones POST/PUT/DELETE, solo hacer fetch sin cache
  if (['POST', 'PUT', 'DELETE'].includes(request.method)) {
    event.respondWith(fetch(request));
    return;
  }
});

// Estrategia Cache First para archivos estáticos
async function cacheFirst(request, cacheName) {
  try {
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      console.log('📦 [SW] Sirviendo desde cache:', request.url);
      return cachedResponse;
    }
    
    const networkResponse = await fetch(request);
    if (networkResponse.ok) {
      const cache = await caches.open(cacheName);
      cache.put(request, networkResponse.clone());
    }
    return networkResponse;
  } catch (error) {
    console.error('❌ [SW] Error en cacheFirst:', error);
    return new Response('Error de red', { status: 503 });
  }
}

// Estrategia Network First para API y recursos dinámicos (solo GET)
async function networkFirst(request, cacheName) {
  // Solo hacer cache de peticiones GET
  if (request.method !== 'GET') {
    return fetch(request);
  }
  
  try {
    const networkResponse = await fetch(request);
    if (networkResponse.ok) {
      const cache = await caches.open(cacheName);
      cache.put(request, networkResponse.clone());
    }
    return networkResponse;
  } catch (error) {
    console.log('📡 [SW] Sin conexión, sirviendo desde cache:', request.url);
    const cachedResponse = await caches.match(request);
    if (cachedResponse) {
      return cachedResponse;
    }
    
    // Si no hay cache y es una página, intentar servir la aplicación principal
    if (request.destination === 'document') {
      const cachedApp = await caches.match('/index.html');
      if (cachedApp) {
        return cachedApp;
      }
      // Solo mostrar offline.html si realmente no hay nada en cache
      return caches.match('/offline.html');
    }
    
    return new Response('Sin conexión', { status: 503 });
  }
}

// Verificar si es un archivo estático
function isStaticFile(pathname) {
  return STATIC_FILES.includes(pathname) ||
         pathname.startsWith('/icons/') ||
         pathname.startsWith('/assets/') ||
         pathname.endsWith('.js') ||
         pathname.endsWith('.css') ||
         pathname.endsWith('.svg') ||
         pathname.endsWith('.png') ||
         pathname.endsWith('.jpg') ||
         pathname.endsWith('.ico');
}

// Verificar si es una petición de API
function isApiRequest(pathname) {
  return API_ROUTES.some(route => pathname.startsWith(route)) ||
         pathname.startsWith('/api/');
}

// Background Sync para sincronización offline
self.addEventListener('sync', (event) => {
  console.log('🔄 [SW] Background sync iniciado:', event.tag);
  
  if (event.tag === 'sync-offline-data') {
    event.waitUntil(syncOfflineData());
  }
});

// Sincronizar datos offline
async function syncOfflineData() {
  try {
    console.log('🔄 [SW] Sincronizando datos offline...');
    
    // Obtener datos offline del IndexedDB
    const offlineData = await getOfflineData();
    
    if (offlineData.length === 0) {
      console.log('✅ [SW] No hay datos offline para sincronizar');
      return;
    }
    
    // Procesar cada acción offline
    for (const data of offlineData) {
      try {
        await syncOfflineAction(data);
        await removeOfflineData(data.id);
      } catch (error) {
        console.error('❌ [SW] Error sincronizando acción:', error);
      }
    }
    
    console.log('✅ [SW] Sincronización completada');
    
    // Notificar a los clientes
    const clients = await self.clients.matchAll();
    clients.forEach(client => {
      client.postMessage({
        type: 'SYNC_COMPLETED',
        data: { success: true }
      });
    });
    
  } catch (error) {
    console.error('❌ [SW] Error en sincronización:', error);
  }
}

// Sincronizar una acción offline específica
async function syncOfflineAction(data) {
  const { method, url, body, headers } = data;
  
  const requestOptions = {
    method,
    headers: headers || {
      'Content-Type': 'application/json'
    }
  };
  
  if (body && method !== 'GET') {
    requestOptions.body = JSON.stringify(body);
  }
  
  const response = await fetch(url, requestOptions);
  
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
  }
  
  return response;
}

// Obtener datos offline del IndexedDB (simulado)
async function getOfflineData() {
  // En una implementación real, esto accedería al IndexedDB
  // Por ahora, simulamos datos offline
  return [];
}

// Remover datos offline del IndexedDB (simulado)
async function removeOfflineData(id) {
  // En una implementación real, esto eliminaría del IndexedDB
  console.log('🗑️ [SW] Eliminando datos offline:', id);
}

// Push notifications
self.addEventListener('push', (event) => {
  console.log('📱 [SW] Push notification recibida');
  
  const options = {
    body: event.data ? event.data.text() : 'Nueva notificación de AgroCloud',
    icon: '/icons/icon-192x192.png',
    badge: '/icons/icon-192x192.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    },
    actions: [
      {
        action: 'explore',
        title: 'Ver detalles',
        icon: '/icons/icon-192x192.png'
      },
      {
        action: 'close',
        title: 'Cerrar',
        icon: '/icons/icon-192x192.png'
      }
    ]
  };
  
  event.waitUntil(
    self.registration.showNotification('AgroCloud', options)
  );
});

// Click en notificación
self.addEventListener('notificationclick', (event) => {
  console.log('👆 [SW] Notificación clickeada:', event.action);
  
  event.notification.close();
  
  if (event.action === 'explore') {
    event.waitUntil(
      clients.openWindow('/')
    );
  }
});

// Mensajes del cliente
self.addEventListener('message', (event) => {
  console.log('💬 [SW] Mensaje recibido:', event.data);
  
  const { type, data } = event.data;
  
  switch (type) {
    case 'SKIP_WAITING':
      self.skipWaiting();
      break;
      
    case 'GET_CACHE_INFO':
      getCacheInfo().then(info => {
        event.ports[0].postMessage(info);
      });
      break;
      
    case 'CLEAR_CACHE':
      clearCache().then(() => {
        event.ports[0].postMessage({ success: true });
      });
      break;
      
    case 'SYNC_NOW':
      syncOfflineData().then(() => {
        event.ports[0].postMessage({ success: true });
      });
      break;
  }
});

// Obtener información del cache
async function getCacheInfo() {
  const cacheNames = await caches.keys();
  const cacheInfo = {};
  
  for (const cacheName of cacheNames) {
    const cache = await caches.open(cacheName);
    const keys = await cache.keys();
    cacheInfo[cacheName] = keys.length;
  }
  
  return cacheInfo;
}

// Limpiar cache
async function clearCache() {
  const cacheNames = await caches.keys();
  await Promise.all(
    cacheNames.map(cacheName => caches.delete(cacheName))
  );
  console.log('🗑️ [SW] Cache limpiado');
}

console.log('🔧 [SW] Service Worker cargado');
