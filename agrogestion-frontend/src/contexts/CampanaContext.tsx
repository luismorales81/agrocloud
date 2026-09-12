import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import api from '../services/api';
import { useEmpresa } from './EmpresaContext';
import { useAuth } from './AuthContext';
import { normalizarCampana, normalizarListaCampanas } from '../core/utils/campanaApi';

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
  registrarCampanaCreada: (campana: Campana) => void;
  loading: boolean;
  error: string | null;
}

const CampanaContext = createContext<CampanaContextType | undefined>(undefined);

export const CampanaProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { empresaId, loading: empresaLoading, empresaContextoListo } = useEmpresa();
  const { user, loading: authLoading } = useAuth();
  const [campanaActiva, setCampanaActiva] = useState<Campana | null>(null);
  const [campanas, setCampanas] = useState<Campana[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const registrarCampanaCreada = useCallback((campana: Campana) => {
    setCampanas((prev) => {
      if (prev.some((c) => c.id === campana.id)) {
        return prev;
      }
      return [campana, ...prev];
    });
  }, []);

  const recargarCampanas = useCallback(async () => {
    const autenticado = Boolean(user);
    const listo = Boolean(empresaId != null && empresaContextoListo);

    if (!empresaId || !autenticado || !listo) {
      if (!empresaId || !listo) {
        setCampanas([]);
        setCampanaActiva(null);
        localStorage.removeItem('campanaActiva');
      }
      return;
    }

    try {
      setLoading(true);
      setError(null);

      let lista: Campana[] = [];
      let activa: Campana | null = null;

      try {
        const listaRes = await api.get('/v1/campanas');
        lista = normalizarListaCampanas(listaRes.data);
      } catch (e) {
        console.error('Error listando campañas:', e);
        setError('Error al cargar el listado de campañas');
      }

      try {
        const activaRes = await api.get('/v1/campanas/activa');
        activa = normalizarCampana(activaRes.data);
      } catch (e) {
        console.error('Error obteniendo campaña activa:', e);
        activa = lista.find((c) => c.estado === 'ACTIVA') ?? lista[0] ?? null;
      }

      setCampanas(lista);

      const seleccionGuardada = localStorage.getItem('campanaActiva');
      let preferida: Campana | null = null;
      if (seleccionGuardada) {
        try {
          const parsed = normalizarCampana(JSON.parse(seleccionGuardada));
          if (parsed && lista.some((c) => c.id === parsed.id)) {
            preferida = parsed;
          }
        } catch {
          // ignorar JSON inválido
        }
      }

      const campanaFinal =
        (preferida && preferida.estado !== 'CERRADA' ? preferida : null) ??
        (activa && activa.estado !== 'CERRADA' ? activa : null) ??
        lista.find((c) => c.estado === 'ACTIVA') ??
        lista.find((c) => c.estado !== 'CERRADA') ??
        null;
      if (campanaFinal) {
        setCampanaActiva(campanaFinal);
        localStorage.setItem('campanaActiva', JSON.stringify(campanaFinal));
      } else {
        setCampanaActiva(null);
        localStorage.removeItem('campanaActiva');
      }
    } catch (e: unknown) {
      console.error('Error listando campañas:', e);
      const axiosErr = e as { response?: { data?: { message?: string; error?: string } } };
      const detalle = axiosErr.response?.data?.message || axiosErr.response?.data?.error;
      setError(detalle ? `Error al cargar campañas: ${detalle}` : 'Error al cargar campañas');
    } finally {
      setLoading(false);
    }
  }, [empresaId, empresaContextoListo, user]);

  useEffect(() => {
    if (!empresaId) {
      setCampanaActiva(null);
      setCampanas([]);
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
    let campana = campanas.find((c) => c.id === campanaId);
    if (!campana) {
      try {
        const listaRes = await api.get('/v1/campanas');
        const lista = normalizarListaCampanas(listaRes.data);
        setCampanas(lista);
        campana = lista.find((c) => c.id === campanaId);
      } catch (e) {
        console.error('Error al resolver campaña para selector:', e);
      }
    }
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
    registrarCampanaCreada,
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
