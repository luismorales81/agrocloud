// Función para mostrar notificaciones
export const showNotification = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') => {
  console.log(`🔔 [Notification] ${type.toUpperCase()}: ${message}`);
  
  // Aquí puedes implementar tu sistema de notificaciones preferido
  // Por ejemplo: toast, alert, o un sistema personalizado
  
  // Por ahora solo mostramos en consola
  // En el futuro puedes integrar con librerías como:
  // - react-toastify
  // - react-hot-toast
  // - @headlessui/react
};
