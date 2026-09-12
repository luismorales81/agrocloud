import React, { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import { ModuleId, ModuleInfo } from '../types/module.types';
import api from '../../services/api';
import { getModuleById } from '../../modules';
import { useAuth } from '../../contexts/AuthContext';
import { useEmpresa } from '../../contexts/EmpresaContext';
import {
  fusionarModulosApiConCatalogo,
  modulosDesdeCatalogoFrontend,
} from '../utils/modulosDisponibles';

interface ModuleContextType {
  currentModule: ModuleId | null;
  setCurrentModule: (module: ModuleId | null) => void;
  availableModules: ModuleInfo[];
  loading: boolean;
  moduleInfo: ModuleInfo | null;
}

const ModuleContext = createContext<ModuleContextType | undefined>(undefined);

interface ModuleProviderProps {
  children: ReactNode;
}

export const ModuleProvider: React.FC<ModuleProviderProps> = ({ children }) => {
  const { user, loading: authCargando } = useAuth();
  const { empresaId, loading: empresaCargando, empresaContextoListo } = useEmpresa();

  const [currentModule, setCurrentModuleState] = useState<ModuleId | null>(() => {
    const saved = localStorage.getItem('currentModule');
    return (saved as ModuleId) || null;
  });

  const [availableModules, setAvailableModules] = useState<ModuleInfo[]>([]);
  const [loading, setLoading] = useState(true);

  // Esperar sesión y empresa activa antes de consultar /modules/available (cabecera X-Company-Id)
  useEffect(() => {
    if (authCargando || empresaCargando) {
      return;
    }

    const cargarModulos = async () => {
      if (!user) {
        setAvailableModules(modulosDesdeCatalogoFrontend());
        setLoading(false);
        return;
      }

      if (!empresaId || !empresaContextoListo) {
        setAvailableModules([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const response = await api.get('/modules/available').catch((error) => {
          console.warn('No se pudieron cargar módulos del backend, usando catálogo local:', error.message);
          return { data: [] as Record<string, unknown>[] };
        });

        const datos = Array.isArray(response?.data) ? response.data : [];
        if (datos.length > 0) {
          setAvailableModules(fusionarModulosApiConCatalogo(datos));
        } else {
          setAvailableModules(modulosDesdeCatalogoFrontend());
        }
      } catch (error) {
        console.error('Error cargando módulos:', error);
        setAvailableModules(modulosDesdeCatalogoFrontend());
      } finally {
        setLoading(false);
      }
    };

    cargarModulos();
  }, [authCargando, empresaCargando, user?.id, empresaId, empresaContextoListo]);

  useEffect(() => {
    if (!loading && availableModules.length === 1 && !currentModule) {
      const singleModule = availableModules[0];
      setCurrentModuleState(singleModule.id);
      localStorage.setItem('currentModule', singleModule.id);
    }
  }, [loading, availableModules, currentModule]);

  useEffect(() => {
    if (!loading && currentModule && !availableModules.some((m) => m.id === currentModule)) {
      setCurrentModuleState(null);
      localStorage.removeItem('currentModule');
    }
  }, [loading, currentModule, availableModules]);

  const setCurrentModule = (module: ModuleId | null) => {
    setCurrentModuleState(module);
    if (module) {
      localStorage.setItem('currentModule', module);
    } else {
      localStorage.removeItem('currentModule');
    }
  };

  const moduleInfo = useMemo((): ModuleInfo | null => {
    if (!currentModule) {
      return null;
    }
    const encontrado = availableModules.find((m) => m.id === currentModule);
    if (encontrado) {
      return encontrado;
    }
    const cfg = getModuleById(currentModule);
    if (cfg) {
      return {
        id: cfg.id,
        nombre: cfg.nombre,
        descripcion: cfg.descripcion,
        icono: cfg.icono,
        color: cfg.color,
        habilitado: true,
      };
    }
    return null;
  }, [currentModule, availableModules]);

  return (
    <ModuleContext.Provider
      value={{
        currentModule,
        setCurrentModule,
        availableModules: availableModules.filter((m) => m.habilitado),
        loading: loading || authCargando || empresaCargando,
        moduleInfo,
      }}
    >
      {children}
    </ModuleContext.Provider>
  );
};

export const useModule = (): ModuleContextType => {
  const context = useContext(ModuleContext);
  if (context === undefined) {
    throw new Error('useModule debe ser usado dentro de un ModuleProvider');
  }
  return context;
};
