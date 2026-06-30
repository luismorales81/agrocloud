import { useCallback, useState } from 'react';
import weatherService from '../services/weatherService';

export interface ValoresClima {
  temperatura: number | null;
  humedad: number | null;
}

function redondearUnDecimal(valor: number): number {
  return Math.round(valor * 10) / 10;
}

export function useRellenarClimaDesdeCoordenadas() {
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const consultarClima = useCallback(async (latitud: number | null | undefined, longitud: number | null | undefined): Promise<ValoresClima | null> => {
    if (latitud == null || longitud == null) {
      setError('No hay coordenadas del establecimiento para consultar el clima.');
      return null;
    }
    try {
      setCargando(true);
      setError(null);
      const datos = await weatherService.getWeatherByCoordinates(latitud, longitud);
      const t = datos.current.temperature;
      const h = datos.current.humidity;
      return {
        temperatura: Number.isFinite(t) ? redondearUnDecimal(t) : null,
        humedad: Number.isFinite(h) ? redondearUnDecimal(h) : null,
      };
    } catch (err: unknown) {
      const mensaje = err instanceof Error ? err.message : 'No se pudo consultar el clima.';
      setError(mensaje);
      return null;
    } finally {
      setCargando(false);
    }
  }, []);

  return { consultarClima, cargando, error, limpiarError: () => setError(null) };
}
