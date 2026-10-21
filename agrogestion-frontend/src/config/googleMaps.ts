// Configuración de Google Maps para AgroCloud.
// La clave no vive en el bundle: se pide al backend con la sesión iniciada.
import api from '../services/api';

let claveMapaEnMemoria: string | null = null;

async function obtenerClaveMapa(): Promise<string> {
  if (claveMapaEnMemoria) {
    return claveMapaEnMemoria;
  }
  const { data } = await api.get<{ clave?: string }>('/mapas/clave');
  const clave = data?.clave?.trim() ?? '';
  if (!clave) {
    throw new Error('El servidor no tiene configurada la clave de mapas');
  }
  claveMapaEnMemoria = clave;
  return clave;
}

export const GOOGLE_MAPS_CONFIG = {
  
  // Configuración por defecto del mapa
  DEFAULT_CENTER: { lat: -34.6118, lng: -58.3960 }, // Buenos Aires
  DEFAULT_ZOOM: 15,
  
  // Estilos del polígono
  POLYGON_STYLES: {
    fillColor: '#4CAF50',
    fillOpacity: 0.3,
    strokeWeight: 2,
    strokeColor: '#4CAF50',
    editable: true,
    draggable: true
  },
  
  // Configuración del Drawing Manager
  DRAWING_MANAGER_OPTIONS: {
    drawingMode: null, // Se establece dinámicamente
    drawingControl: true,
    drawingControlOptions: {
      position: 1, // TOP_CENTER
      drawingModes: ['polygon']
    }
  }
};

/**
 * Carga la API de Google Maps (una sola vez por página).
 * El callback opcional se ejecuta al éxito (compatibilidad con cultivos).
 * Devuelve una promesa para uso con async/await (p. ej. avícola huevos).
 */
export const loadGoogleMaps = (callback?: () => void): Promise<void> => {
  const promesa = new Promise<void>((resolver, rechazar) => {
    const alExito = () => {
      try {
        callback?.();
      } finally {
        resolver();
      }
    };

    const alFallo = () => {
      rechazar(new Error('No se pudo cargar Google Maps'));
    };

    // Verificar si ya está cargado
    if (window.google && window.google.maps) {
      setTimeout(alExito, 100);
      return;
    }

    // Verificar si ya se está cargando
    if (document.querySelector('script[src*="maps.googleapis.com"]')) {
      let intentos = 0;
      const maxIntentos = 300; // ~30 s
      const esperarCarga = () => {
        intentos += 1;
        if (window.google && window.google.maps) {
          setTimeout(alExito, 100);
        } else if (intentos >= maxIntentos) {
          alFallo();
        } else {
          setTimeout(esperarCarga, 100);
        }
      };
      esperarCarga();
      return;
    }

    obtenerClaveMapa()
      .then((claveApi) => agregarScriptMapas(claveApi, alExito, alFallo))
      .catch(() => {
        console.warn('No se pudo obtener la clave de mapas del servidor. Los mapas no estarán disponibles.');
        alFallo();
      });
  });

  if (callback !== undefined) {
    void promesa.catch(() => {
      /* Compatibilidad: cultivos solo usan callback; evitar rechazo no manejado. */
    });
  }

  return promesa;
};

function agregarScriptMapas(
  claveApi: string,
  alExito: () => void,
  alFallo: () => void
): void {
    const script = document.createElement('script');
    // geometry para computeArea. Dibujo con TerraDraw (drawing deprecado mayo 2026)
    script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(claveApi)}&libraries=geometry&callback=initGoogleMaps&loading=async&v=weekly`;
    script.async = true;
    script.defer = true;

    // Función global de callback con manejo de errores mejorado
    window.initGoogleMaps = () => {
      try {
        if (window.google && window.google.maps) {
          console.log('✅ Google Maps cargado exitosamente');
          setTimeout(alExito, 200);
        } else {
          console.error('❌ Google Maps no se inicializó correctamente');
          console.warn('Google Maps no disponible. Algunas funcionalidades pueden estar limitadas.');
          alFallo();
        }
      } catch (error) {
        console.error('❌ Error en callback de Google Maps:', error);
        console.warn('Google Maps no disponible. Algunas funcionalidades pueden estar limitadas.');
        alFallo();
      }
    };

    script.onerror = () => {
      console.error('❌ Error cargando script de Google Maps');
      console.warn('⚠️ Google Maps no está disponible. Posibles causas:');
      console.warn('   - La API key no es válida');
      console.warn('   - La facturación no está habilitada en Google Cloud');
      console.warn('   - Problemas de conectividad');
      console.warn('   La aplicación continuará funcionando sin mapas.');
      alFallo();
    };

    script.onload = () => {
      console.log('📦 Script de Google Maps cargado');
    };

    document.head.appendChild(script);
}

// Declaraciones de tipos para TypeScript
declare global {
  interface Window {
    google: any;
    initGoogleMaps: () => void;
  }
}
