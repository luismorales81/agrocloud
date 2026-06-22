import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import api from '../services/api';
import { useEmpresa } from './EmpresaContext';
import { useAuth } from './AuthContext';

export interface Campana {
  id: number;
  empresaId: number;
  codigo: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  estado: 'BORRADOR' | 'ACTIVA' | 'CERRADA';
  esDefault: boolean;
}

interface CampanaContextType {
  campanaActiva: Campana | null;
  campanaId: number | null;
  campanas: Campana[];
  cambiarCampana: (campanaId: number) => Promise<void>;
  recargarCampanas: () => Promise<void>;
  loading: boolean;
  error: string | null;
}

const CampanaContext = createContext<CampanaContextType | undefined>(undefined);

export const CampanaProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { empresaId, empresasUsuario, loading: empresaLoading } = useEmpresa();
  const { user, loading: authLoading } = useAuth();
  const [campanaActiva, setCampanaActiva] = useState<Campana | null>(null);
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const empresaValida = Boolean(
    empresaId != null && empresasUsuario.some((e) => e.empresaId === empresaId)
  );

  const recargarCampanas = useCallback(async () => {
    const token = localStorage.getItem('token');
    const autenticado = Boolean(token && user);

    if (!empresaId || !autenticado || !empresaValida) {
      if (!empresaId) {
        setCampanas([]);
        setCampanaActiva(null);
        localStorage.removeItem('campanaActiva');
      }
      return;
    }
    try {
      setLoading(true);
      setError(null);
      const [listaRes, activaRes] = await Promise.all([
        api.get('/v1/campanas'),
        api.get('/v1/campanas/activa'),
      ]);
      setCampanas(listaRes.data ?? []);
      const activa: Campana = activaRes.data;
      if (activa) {
        setCampanaActiva(activa);
        localStorage.setItem('campanaActiva', JSON.stringify(activa));
      }
    } catch (e) {
      console.error('Error cargando campañas:', e);
      setError('Error al cargar campañas');
    } finally {
      setLoading(false);
    }
  }, [empresaId, user, empresaValida]);

  useEffect(() => {
    if (!empresaId) {
      setCampanaActiva(null);
      localStorage.removeItem('campanaActiva');
    }
  }, [empresaId]);

  useEffect(() => {
    if (authLoading || empresaLoading) {
      return;
    }
    recargarCampanas();
  }, [recargarCampanas, authLoading, empresaLoading]);

  const cambiarCampana = async (campanaId: number) => {
    const campana = campanas.find((c) => c.id === campanaId);
    if (!campana) {
      throw new Error('Campaña no encontrada');
    }
    setCampanaActiva(campana);
    localStorage.setItem('campanaActiva', JSON.stringify(campana));
  };

  const value: CampanaContextType = {
    campanaActiva,
    campanaId: campanaActiva?.id ?? null,
    campanas,
    cambiarCampana,
    recargarCampanas,
    loading,
    error,
  };

  return <CampanaContext.Provider value={value}>{children}</CampanaContext.Provider>;
};

export const useCampana = (): CampanaContextType => {
  const ctx = useContext(CampanaContext);
  if (!ctx) {
    throw new Error('useCampana debe usarse dentro de CampanaProvider');
  }
  return ctx;
};

export default CampanaContext;
