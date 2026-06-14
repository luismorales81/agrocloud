/**
 * Hook personalizado para gestión de Madres
 */
import { useState, useEffect } from 'react';
import { madresService } from '../services/madresService';
import type { Madre, FiltrosMadres } from '../types';

export const useMadres = (filtros?: FiltrosMadres) => {
  const [madres, setMadres] = useState<Madre[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const cargarMadres = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await madresService.listar(filtros);
      setMadres(data);
    } catch (err: any) {
      setError(err.message || 'Error al cargar las madres');
      console.error('Error cargando madres:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarMadres();
  }, [JSON.stringify(filtros)]);

  const crearMadre = async (madreData: any) => {
    try {
      const nuevaMadre = await madresService.crear(madreData);
      await cargarMadres();
      return nuevaMadre;
    } catch (err: any) {
      throw new Error(err.message || 'Error al crear la madre');
    }
  };

  const actualizarMadre = async (id: number, madreData: Partial<Madre>) => {
    try {
      await madresService.actualizar(id, madreData);
      await cargarMadres();
    } catch (err: any) {
      throw new Error(err.message || 'Error al actualizar la madre');
    }
  };

  const eliminarMadre = async (id: number) => {
    try {
      await madresService.eliminar(id);
      await cargarMadres();
    } catch (err: any) {
      throw new Error(err.message || 'Error al eliminar la madre');
    }
  };

  return {
    madres,
    loading,
    error,
    cargarMadres,
    crearMadre,
    actualizarMadre,
    eliminarMadre,
  };
};

