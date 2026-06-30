import React, { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import { ModuleId, ModuleInfo } from '../types/module.types';
import api from '../../services/api';
import { getModuleById } from '../../modules';
import { useAuth } from '../../contexts/AuthContext';
import { useEmpresa } from '../../contexts/EmpresaContext';

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
  const { empresaId } = useEmpresa();

  const [currentModule, setCurrentModuleState] = useState<ModuleId | null>(() => {
    // Intentar recuperar el módulo del localStorage
    const saved = localStorage.getItem('currentModule');
    const module = (saved as ModuleId) || null;
    console.log('ModuleContext: Inicializando con módulo del localStorage', { saved, module });
    return module;
  });

  const [availableModules, setAvailableModules] = useState<ModuleInfo[]>([]);
  const [loading, setLoading] = useState(true);

  // Recargar módulos cuando cambia la sesión o la empresa activa (cabecera X-Company-Id en el backend)
  useEffect(() => {
    if (authCargando) {
      return;
    }

    const cargarModulos = async () => {
      // Verificar si hay token de autenticación
      const token = localStorage.getItem('token');
      if (!token || !user) {
        // Sin sesión: no llamar al backend; lista mínima para pantallas que no requieren módulos completos
        setAvailableModules([
          {
            id: 'cultivos',
            nombre: 'Cultivos',
            descripcion: 'Gestión de campos, lotes, cultivos y labores',
            icono: 'Wheat',
            color: '#10b981',
            habilitado: true,
          },
          {
            id: 'porcinos',
            nombre: 'Porcinos',
            descripcion: 'Gestión de producción porcina',
            icono: 'PiggyBank',
            color: '#f59e0b',
            habilitado: true,
          },
        ]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        const response = await api.get('/modules/available').catch((error) => {
          // Si el endpoint falla (401, 404, etc.), usar módulos por defecto
          console.warn('No se pudieron cargar módulos del backend, usando módulos por defecto:', error.message);
          return { data: [] };
        });

        if (response && response.data && response.data.length > 0) {
          // Filtrar y normalizar módulos del backend
          const modulosBackend = response.data;
          
          // Normalizar IDs: convertir "crops" -> "cultivos", "pigs" -> "porcinos"
          const modulosNormalizados: ModuleInfo[] = modulosBackend.map((m: Record<string, unknown>) => {
            let idNormalizado = String(m.id ?? '');
            if (idNormalizado === 'crops') idNormalizado = 'cultivos';
            if (idNormalizado === 'pigs') idNormalizado = 'porcinos';
            if (idNormalizado === 'avicola_crianza' || idNormalizado === 'AVICOLA_CRIANZA') {
              idNormalizado = 'avicola-crianza';
            }
            if (idNormalizado === 'avicola_huevos' || idNormalizado === 'AVICOLA_HUEVOS') {
              idNormalizado = 'avicola-huevos';
            }
            if (idNormalizado === 'avicola_carne' || idNormalizado === 'AVICOLA_CARNE') {
              idNormalizado = 'avicola-crianza';
            }
            if (idNormalizado === 'avicola_ponedoras' || idNormalizado === 'AVICOLA_PONEDORAS') {
              idNormalizado = 'avicola-ponedoras';
            }
            if (idNormalizado === 'FEEDLOT' || idNormalizado === 'feedlot') {
              idNormalizado = 'feedlot';
            }
            if (idNormalizado === 'LECHERIA' || idNormalizado === 'lecheria') {
              idNormalizado = 'lecheria';
            }
            const id = idNormalizado as ModuleId;
            const cfg = getModuleById(id);
            return {
              id,
              nombre: cfg?.nombre ?? String(m.nombre ?? ''),
              descripcion: cfg?.descripcion ?? String(m.descripcion ?? ''),
              icono: cfg?.icono ?? 'Package',
              color: cfg?.color ?? String(m.color ?? '#10b981'),
              habilitado: m.habilitado !== false,
            };
          });
          
          // Eliminar duplicados por ID
          const modulosUnicos = modulosNormalizados.reduce((acc: any[], curr: any) => {
            if (!acc.find((m: any) => m.id === curr.id)) {
              acc.push(curr);
            }
            return acc;
          }, []);
          
          // Verificar si están ambos módulos principales
          const tieneCultivos = modulosUnicos.some((m: any) => m.id === 'cultivos');
          const tienePorcinos = modulosUnicos.some((m: any) => m.id === 'porcinos');
          
          // Si falta alguno, agregarlo
          const modulosCompletos = [...modulosUnicos];
          if (!tieneCultivos) {
            modulosCompletos.push({
              id: 'cultivos',
              nombre: 'Cultivos',
              descripcion: 'Gestión de campos, lotes, cultivos y labores',
              icono: 'Wheat',
              color: '#10b981',
              habilitado: true,
            });
          }
          if (!tienePorcinos) {
            modulosCompletos.push({
              id: 'porcinos',
              nombre: 'Porcinos',
              descripcion: 'Gestión de producción porcina',
              icono: 'PiggyBank',
              color: '#f59e0b',
              habilitado: true,
            });
          }
          setAvailableModules(modulosCompletos);
        } else {
          // Módulos por defecto si el backend no responde o retorna vacío
          setAvailableModules([
            {
              id: 'cultivos',
              nombre: 'Cultivos',
              descripcion: 'Gestión de campos, lotes, cultivos y labores',
              icono: '🌾',
              color: '#10b981',
              habilitado: true,
            },
            {
              id: 'porcinos',
              nombre: 'Porcinos',
              descripcion: 'Gestión de producción porcina',
              icono: '🐷',
              color: '#f59e0b',
              habilitado: true,
            },
          ]);
        }
      } catch (error) {
        console.error('Error cargando módulos:', error);
        // Módulos por defecto en caso de error - siempre mostrar ambos
        setAvailableModules([
          {
            id: 'cultivos',
            nombre: 'Cultivos',
            descripcion: 'Gestión de campos, lotes, cultivos y labores',
            icono: 'Wheat',
            color: '#10b981',
            habilitado: true,
          },
          {
            id: 'porcinos',
            nombre: 'Porcinos',
            descripcion: 'Gestión de producción porcina',
            icono: 'PiggyBank',
            color: '#f59e0b',
            habilitado: true,
          },
        ]);
      } finally {
        setLoading(false);
      }
    };

    cargarModulos();
  }, [authCargando, user?.id, empresaId]);

  // Si solo hay un módulo disponible, seleccionarlo automáticamente
  useEffect(() => {
    if (!loading && availableModules.length === 1 && !currentModule) {
      const singleModule = availableModules[0];
      setCurrentModuleState(singleModule.id);
      localStorage.setItem('currentModule', singleModule.id);
    }
  }, [loading, availableModules, currentModule]);

  const setCurrentModule = (module: ModuleId | null) => {
    console.log('ModuleContext: setCurrentModule llamado con', module);
    setCurrentModuleState(module);
    if (module) {
      localStorage.setItem('currentModule', module);
      console.log('ModuleContext: Módulo guardado en localStorage:', module);
    } else {
      localStorage.removeItem('currentModule');
    }
  };

  // Calcular moduleInfo usando useMemo para evitar recálculos innecesarios
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
    console.warn('ModuleContext: Módulo no reconocido', {
      currentModule,
      availableModules: availableModules.map((m) => m.id),
    });
    return null;
  }, [currentModule, availableModules]);

  console.log('ModuleContext: Estado actual', { 
    currentModule, 
    moduleInfo: moduleInfo ? { id: moduleInfo.id, nombre: moduleInfo.nombre } : null, 
    availableModules: availableModules.length,
    availableModulesIds: availableModules.map(m => m.id),
    loading 
  });

  return (
    <ModuleContext.Provider
      value={{
        currentModule,
        setCurrentModule,
        availableModules: availableModules.filter((m) => m.habilitado),
        loading,
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

