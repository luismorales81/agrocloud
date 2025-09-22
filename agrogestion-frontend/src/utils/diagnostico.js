/**
 * Utilidad de diagnóstico para problemas de conectividad
 */

export const diagnosticarProblemas = () => {
  const problemas = [];
  const soluciones = [];

  // Verificar si estamos en localhost
  if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    problemas.push('🔍 Diagnóstico de conectividad local');
    
    // Verificar puerto del backend
    const backendUrl = 'http://localhost:8080';
    problemas.push(`📡 Intentando conectar a: ${backendUrl}`);
    
    // Verificar si el backend responde
    fetch(`${backendUrl}/api/health`)
      .then(response => {
        if (response.ok) {
          console.log('✅ Backend responde correctamente');
          problemas.push('✅ Backend está funcionando');
        } else {
          console.log('⚠️ Backend responde pero con error:', response.status);
          problemas.push(`⚠️ Backend responde con error: ${response.status}`);
        }
      })
      .catch(error => {
        console.log('❌ Error conectando al backend:', error.message);
        problemas.push('❌ Backend no responde');
        
        // Agregar soluciones específicas
        soluciones.push('🔧 Posibles soluciones:');
        soluciones.push('1. Verificar que el backend esté ejecutándose');
        soluciones.push('2. Ejecutar: .\\iniciar-backend-debug.bat');
        soluciones.push('3. Verificar que MySQL esté ejecutándose en XAMPP');
        soluciones.push('4. Verificar que el puerto 8080 no esté bloqueado');
        soluciones.push('5. Revisar logs del backend en agrogestion-backend/backend.log');
      });
  }

  return { problemas, soluciones };
};

export const mostrarDiagnosticoEnConsola = () => {
  console.log('🔧 [DIAGNÓSTICO] Iniciando verificación de conectividad...');
  
  const { problemas, soluciones } = diagnosticarProblemas();
  
  console.log('📋 Problemas detectados:');
  problemas.forEach(problema => console.log(`  ${problema}`));
  
  if (soluciones.length > 0) {
    console.log('💡 Soluciones sugeridas:');
    soluciones.forEach(solucion => console.log(`  ${solucion}`));
  }
  
  console.log('🔧 [DIAGNÓSTICO] Verificación completada');
};

// Función para verificar el estado del backend
export const verificarEstadoBackend = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/health', {
      method: 'GET',
      timeout: 5000
    });
    
    if (response.ok) {
      console.log('✅ [BACKEND] Estado: Funcionando correctamente');
      return { estado: 'ok', mensaje: 'Backend funcionando' };
    } else {
      console.log(`⚠️ [BACKEND] Estado: Error ${response.status}`);
      return { estado: 'error', mensaje: `Error ${response.status}` };
    }
  } catch (error) {
    console.log('❌ [BACKEND] Estado: No responde');
    console.log('❌ [BACKEND] Error:', error.message);
    
    // Información adicional para debugging
    console.log('🔍 [DEBUG] Información adicional:');
    console.log('  - URL intentada: http://localhost:8080/api/health');
    console.log('  - Tipo de error:', error.name);
    console.log('  - Código de error:', error.code);
    
    return { 
      estado: 'no_responde', 
      mensaje: 'Backend no responde',
      error: error.message,
      soluciones: [
        '1. Verificar que el backend esté ejecutándose',
        '2. Ejecutar el script: .\\iniciar-backend-debug.bat',
        '3. Verificar que MySQL esté ejecutándose en XAMPP',
        '4. Revisar logs del backend',
        '5. Verificar configuración de firewall'
      ]
    };
  }
};

// Función para mostrar información de debugging en la consola
export const mostrarInfoDebugging = (error) => {
  console.group('🔧 [DEBUG] Información de debugging');
  console.log('📅 Timestamp:', new Date().toISOString());
  console.log('🌐 URL actual:', window.location.href);
  console.log('🔗 User Agent:', navigator.userAgent);
  console.log('📡 Conexión:', navigator.onLine ? 'Online' : 'Offline');
  
  if (error) {
    console.log('❌ Error:', error.message);
    console.log('🏷️ Tipo:', error.name);
    console.log('📊 Código:', error.code);
    console.log('📋 Stack:', error.stack);
  }
  
  console.log('💡 Para más información, ejecuta: .\\diagnostico-backend.bat');
  console.groupEnd();
};
