import React, { useCallback, useEffect, useRef, useState } from 'react';
import { TerraDraw, TerraDrawPolygonMode, TerraDrawRenderMode } from 'terra-draw';
import { TerraDrawGoogleMapsAdapter } from 'terra-draw-google-maps-adapter';
import FieldWeatherButton from './FieldWeatherButton';
import { loadGoogleMaps, GOOGLE_MAPS_CONFIG } from '../config/googleMaps';

declare global {
  interface Window {
    google: any;
  }
}

function anilloGeoJsonACoordenadas(coordsGeo: [number, number][]): { lat: number; lng: number }[] {
  if (!coordsGeo || coordsGeo.length < 1) return [];
  const sinDuplicado =
    coordsGeo.length > 1 &&
    coordsGeo[0][0] === coordsGeo[coordsGeo.length - 1][0] &&
    coordsGeo[0][1] === coordsGeo[coordsGeo.length - 1][1]
      ? coordsGeo.slice(0, -1)
      : coordsGeo;
  const [a, b] = sinDuplicado[0];
  const esLngLat = Math.abs(a) <= 180 && Math.abs(b) <= 90;
  return sinDuplicado.map(([x, y]) => (esLngLat ? { lat: y, lng: x } : { lat: x, lng: y }));
}

function parsearCoordenadasIniciales(json: string | null | undefined): { lat: number; lng: number }[] {
  if (!json || !json.trim()) return [];
  try {
    const arr = JSON.parse(json) as unknown;
    if (!Array.isArray(arr)) return [];
    return arr
      .filter((p) => p && typeof p.lat === 'number' && typeof p.lng === 'number')
      .map((p) => ({ lat: p.lat, lng: p.lng }));
  } catch {
    return [];
  }
}

export interface EditorMapaUbicacionProps {
  abierto: boolean;
  coordenadasJson: string | null;
  onCoordenadasJsonChange: (json: string | null) => void;
  /** Etiqueta para el botón de clima (por defecto «Establecimiento»). */
  nombreCampoClima?: string;
}

const EditorMapaUbicacion: React.FC<EditorMapaUbicacionProps> = ({
  abierto,
  coordenadasJson,
  onCoordenadasJsonChange,
  nombreCampoClima = 'Establecimiento',
}) => {
  const mapRef = useRef<HTMLDivElement>(null);
  const [listo, setListo] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [latManual, setLatManual] = useState('');
  const [lngManual, setLngManual] = useState('');
  const terraRef = useRef<TerraDraw | null>(null);
  const mapInstRef = useRef<any>(null);
  const poligonoLecturaRef = useRef<any>(null);
  const onCoordenadasJsonChangeRef = useRef(onCoordenadasJsonChange);
  const [revisionMapa, setRevisionMapa] = useState(0);
  const [terraListo, setTerraListo] = useState(false);
  const [perimetroDibujoActivo, setPerimetroDibujoActivo] = useState(false);

  const destruirMapa = useCallback(() => {
    try {
      if (poligonoLecturaRef.current) {
        poligonoLecturaRef.current.setMap(null);
        poligonoLecturaRef.current = null;
      }
    } catch {
      /* ignore */
    }
    try {
      if (terraRef.current) {
        terraRef.current.stop();
        terraRef.current = null;
      }
    } catch {
      /* ignore */
    }
    terraRef.current = null;
    mapInstRef.current = null;
    if (mapRef.current) {
      mapRef.current.innerHTML = '';
    }
  }, []);

  useEffect(() => {
    let cancelado = false;
    loadGoogleMaps()
      .then(() => {
        if (!cancelado) setListo(true);
      })
      .catch(() => {
        if (!cancelado) setError('No se pudo cargar Google Maps. Revisá la clave en configuración.');
      });
    return () => {
      cancelado = true;
    };
  }, []);

  useEffect(() => {
    onCoordenadasJsonChangeRef.current = onCoordenadasJsonChange;
  }, [onCoordenadasJsonChange]);

  useEffect(() => {
    if (abierto) {
      setRevisionMapa((r) => r + 1);
    }
  }, [abierto]);

  const volverModoEstaticoSeguro = useCallback((instancia: TerraDraw) => {
    requestAnimationFrame(() => {
      try {
        if (terraRef.current === instancia) {
          instancia.setMode('static');
        }
      } catch {
        /* La instancia pudo destruirse si el mapa se remontó */
      }
    });
  }, []);

  useEffect(() => {
    if (!abierto || !listo || !mapRef.current || !window.google) return;

    let cancelado = false;
    destruirMapa();
    setTerraListo(false);
    setPerimetroDibujoActivo(false);

    const existentes = parsearCoordenadasIniciales(coordenadasJson);
    const centroPorDefecto = GOOGLE_MAPS_CONFIG.DEFAULT_CENTER;

    const montarMapa = (centro: { lat: number; lng: number }, zoomInicial: number) => {
      if (cancelado || !mapRef.current) return;

      const mapInstance = new window.google.maps.Map(mapRef.current, {
        center: centro,
        zoom: zoomInicial,
        mapTypeId: 'satellite',
        mapTypeControl: true,
        streetViewControl: false,
        fullscreenControl: true,
        disableDoubleClickZoom: true,
      });
      mapInstRef.current = mapInstance;

      if (existentes.length > 2) {
        poligonoLecturaRef.current = new window.google.maps.Polygon({
          paths: existentes,
          strokeColor: '#ca8a04',
          strokeOpacity: 0.9,
          strokeWeight: 2,
          fillColor: '#facc15',
          fillOpacity: 0.25,
          map: mapInstance,
        });
        const bounds = new window.google.maps.LatLngBounds();
        existentes.forEach((c) => bounds.extend(new window.google.maps.LatLng(c.lat, c.lng)));
        mapInstance.fitBounds(bounds);
      } else {
        poligonoLecturaRef.current = null;
        if (existentes.length === 1) {
          new window.google.maps.Marker({
            position: existentes[0],
            map: mapInstance,
          });
        }
      }

      const adapter = new TerraDrawGoogleMapsAdapter({
        map: mapInstance,
        lib: window.google.maps,
        coordinatePrecision: 9,
      });
      const drawInstance = new TerraDraw({
        adapter,
        modes: [
          new TerraDrawPolygonMode({
            showCoordinatePoints: true,
            styles: {
              fillColor: '#ca8a04',
              outlineColor: '#a16207',
              outlineWidth: 2,
            },
          }),
          new TerraDrawRenderMode({ modeName: 'static', styles: {} } as ConstructorParameters<typeof TerraDrawRenderMode>[0]),
        ],
      });

      const iniciar = () => {
        if (cancelado) return;
        drawInstance.start();
        drawInstance.setMode('static');
        terraRef.current = drawInstance;
        setTerraListo(true);
      };
      window.google.maps.event.addListenerOnce(mapInstance, 'idle', iniciar);

      drawInstance.on('finish', (id: string | number, context: { action?: string }) => {
        if (context?.action !== 'draw') return;
        const snapshot = drawInstance.getSnapshot();
        const poligono = snapshot.find((f: any) => f.id === id || f.geometry?.type === 'Polygon');
        if (!poligono?.geometry?.coordinates?.[0]) return;
        const coordsGeo = poligono.geometry.coordinates[0] as [number, number][];
        const coordenadas = anilloGeoJsonACoordenadas(coordsGeo);
        if (coordenadas.length < 3) return;
        onCoordenadasJsonChangeRef.current(JSON.stringify(coordenadas));
        setPerimetroDibujoActivo(false);
        try {
          if (poligonoLecturaRef.current) {
            poligonoLecturaRef.current.setMap(null);
            poligonoLecturaRef.current = null;
          }
        } catch {
          /* ignore */
        }
        volverModoEstaticoSeguro(drawInstance);
      });
    };

    if (existentes.length > 0) {
      const centro =
        existentes.length > 2
          ? {
              lat: existentes.reduce((s, c) => s + c.lat, 0) / existentes.length,
              lng: existentes.reduce((s, c) => s + c.lng, 0) / existentes.length,
            }
          : existentes[0];
      const zoom = existentes.length > 2 ? 14 : 16;
      montarMapa(centro, zoom);
    } else if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          if (cancelado) return;
          montarMapa({ lat: pos.coords.latitude, lng: pos.coords.longitude }, 16);
        },
        () => {
          if (cancelado) return;
          montarMapa(centroPorDefecto, 16);
        },
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 300000 }
      );
    } else {
      montarMapa(centroPorDefecto, 16);
    }

    return () => {
      cancelado = true;
      setTerraListo(false);
      setPerimetroDibujoActivo(false);
      destruirMapa();
    };
  }, [abierto, listo, revisionMapa, destruirMapa, volverModoEstaticoSeguro]);

  const centrarEnMiUbicacion = () => {
    setError(null);
    const mapa = mapInstRef.current;
    if (!mapa) return;
    if (!navigator.geolocation) {
      setError('Tu navegador no permite geolocalización.');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        mapa.setCenter({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        mapa.setZoom(16);
      },
      () => {
        setError('No se pudo obtener tu ubicación. Revisá permisos del navegador.');
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 }
    );
  };

  const empezarDibujoPerimetro = () => {
    setError(null);
    const td = terraRef.current;
    if (!td) return;
    try {
      if (poligonoLecturaRef.current) {
        poligonoLecturaRef.current.setMap(null);
        poligonoLecturaRef.current = null;
      }
    } catch {
      /* ignore */
    }
    try {
      td.clear();
    } catch {
      /* ignore */
    }
    try {
      td.setMode('polygon');
      setPerimetroDibujoActivo(true);
    } catch {
      setError('No se pudo activar el dibujo del perímetro. Intentá de nuevo.');
    }
  };

  const aplicarPuntoManual = () => {
    const lat = parseFloat(latManual.replace(',', '.'));
    const lng = parseFloat(lngManual.replace(',', '.'));
    if (Number.isNaN(lat) || Number.isNaN(lng)) {
      setError('Latitud y longitud numéricas obligatorias.');
      return;
    }
    setError(null);
    onCoordenadasJsonChange(JSON.stringify([{ lat, lng }]));
    setPerimetroDibujoActivo(false);
    setRevisionMapa((r) => r + 1);
  };

  const limpiar = () => {
    setError(null);
    onCoordenadasJsonChange(null);
    setLatManual('');
    setLngManual('');
    setPerimetroDibujoActivo(false);
    setRevisionMapa((r) => r + 1);
  };

  const coordsList = parsearCoordenadasIniciales(coordenadasJson);

  const estiloBoton = (principal?: boolean): React.CSSProperties => ({
    padding: '8px 14px',
    borderRadius: 6,
    border: principal ? 'none' : '1px solid #d1d5db',
    background: principal ? '#15803d' : '#fff',
    color: principal ? '#fff' : '#374151',
    cursor: 'pointer',
    fontSize: 14,
    fontWeight: 500,
    boxShadow: principal ? '0 1px 3px rgba(0,0,0,0.2)' : undefined,
  });

  return (
    <div style={{ marginTop: 8 }}>
      <p style={{ fontSize: '0.85rem', color: '#4b5563', marginBottom: 8 }}>
        Sin coordenadas guardadas, el mapa se centra en tu ubicación del navegador (si permitís el permiso). Para el
        perímetro: pulsá <strong>Dibujar perímetro</strong>, hacé <strong>un clic por cada vértice</strong> y{' '}
        <strong>doble clic</strong> para cerrar el polígono. También podés usar un solo punto con las coordenadas
        manuales abajo.
      </p>
      {error && (
        <div style={{ color: '#b91c1c', fontSize: '0.85rem', marginBottom: 8 }} role="alert">
          {error}
        </div>
      )}
      <div style={{ position: 'relative' }}>
        <div
          ref={mapRef}
          style={{
            width: '100%',
            height: 360,
            borderRadius: 8,
            border: '1px solid #e5e7eb',
            background: '#f3f4f6',
          }}
        />
        <div
          style={{
            position: 'absolute',
            top: 10,
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 2,
            display: 'flex',
            flexWrap: 'wrap',
            gap: 8,
            justifyContent: 'center',
            maxWidth: '96%',
          }}
        >
          <button type="button" style={estiloBoton()} onClick={centrarEnMiUbicacion} disabled={!terraListo}>
            Centrar en mi ubicación
          </button>
          <button type="button" style={estiloBoton(true)} onClick={empezarDibujoPerimetro} disabled={!terraListo}>
            Dibujar perímetro
          </button>
        </div>
        {perimetroDibujoActivo && (
          <div
            style={{
              position: 'absolute',
              bottom: 12,
              left: '50%',
              transform: 'translateX(-50%)',
              zIndex: 2,
              padding: '8px 14px',
              backgroundColor: 'rgba(0,0,0,0.78)',
              color: '#fff',
              borderRadius: 6,
              fontSize: 13,
              maxWidth: '92%',
              textAlign: 'center',
            }}
          >
            Perímetro: un clic por esquina → <strong>doble clic</strong> para cerrar.
          </div>
        )}
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 10, alignItems: 'center' }}>
        <button type="button" onClick={limpiar} style={{ padding: '6px 12px', borderRadius: 6, border: '1px solid #d1d5db' }}>
          Limpiar ubicación
        </button>
        {coordsList.length > 0 && <FieldWeatherButton fieldName={nombreCampoClima} coordinates={coordsList} />}
      </div>
      <div style={{ marginTop: 14, paddingTop: 12, borderTop: '1px solid #e5e7eb' }}>
        <p style={{ fontSize: '0.8rem', color: '#6b7280', marginBottom: 6 }}>Coordenadas manuales (un punto)</p>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
          <input
            type="text"
            placeholder="Latitud (ej: -34.60)"
            value={latManual}
            onChange={(e) => setLatManual(e.target.value)}
            style={{ padding: 8, borderRadius: 6, border: '1px solid #d1d5db', minWidth: 140 }}
          />
          <input
            type="text"
            placeholder="Longitud (ej: -58.38)"
            value={lngManual}
            onChange={(e) => setLngManual(e.target.value)}
            style={{ padding: 8, borderRadius: 6, border: '1px solid #d1d5db', minWidth: 140 }}
          />
          <button type="button" onClick={aplicarPuntoManual} style={{ padding: '8px 14px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 6 }}>
            Aplicar punto
          </button>
        </div>
      </div>
    </div>
  );
};

export default EditorMapaUbicacion;
