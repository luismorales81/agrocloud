import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './style.css';

console.log('🚀 [Main] Iniciando aplicación AgroCloud...');

// Service Worker completamente deshabilitado
console.log('⚠️ [Main] Service Worker completamente deshabilitado');

// Desregistrar cualquier service worker existente
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(function(registrations) {
    for(let registration of registrations) {
      registration.unregister();
      console.log('✅ [Main] Service Worker desregistrado');
    }
  });
}

// Verificar configuración de API
const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
console.log('🔧 [Main] URL de API configurada:', apiUrl);

// Verificar si hay datos de usuario guardados
const savedUser = localStorage.getItem('user');
const savedToken = localStorage.getItem('token');
if (savedUser || savedToken) {
  console.log('🔧 [Main] Datos de sesión encontrados en localStorage');
} else {
  console.log('ℹ️ [Main] No hay datos de sesión guardados');
}

// Verificar conectividad
console.log('🔧 [Main] Estado de conectividad:', navigator.onLine ? 'Online' : 'Offline');

// Renderizar aplicación
console.log('🔧 [Main] Renderizando aplicación...');

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

console.log('✅ [Main] Aplicación renderizada exitosamente');
