import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';

interface Empresa {
  id: number;
  nombre: string;
  cuit?: string;
  emailContacto?: string;
  telefonoContacto?: string;
  direccion?: string;
  estado: 'ACTIVO' | 'INACTIVO' | 'PENDIENTE';
  fechaInicioTrial?: string;
  fechaFinTrial?: string;
  activo: boolean;
  fechaCreacion: string;
  fechaActualizacion: string;
}

interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  usuarioEmail: string;
  usuarioNombre: string;
  empresaId: number;
  empresaNombre: string;
  cuit?: string;
  rol: 'ADMINISTRADOR' | 'JEFE_CAMPO' | 'JEFE_FINANCIERO' | 'OPERARIO' | 'CONSULTOR_EXTERNO' | 
       // Roles legacy para retrocompatibilidad
       'ASESOR' | 'CONTADOR' | 'TECNICO' | 'LECTURA' | 'PRODUCTOR';
  estado: 'ACTIVO' | 'INACTIVO' | 'PENDIENTE';
  fechaInicio: string;
  fechaFin: string;
  creadoPorId?: number;
  creadoPorEmail?: string;
}

interface EmpresaContextType {
  empresaActiva: Empresa | null;
  /** Id de la empresa activa; null si no hay selección */
  empresaId: number | null;
  empresasUsuario: UsuarioEmpresa[];
  rolUsuario: string | null;
  /** True cuando mis-empresas validó y fijó la empresa activa (listo para X-Company-Id). */
  empresaContextoListo: boolean;
  cambiarEmpresa: (empresaId: number) => Promise<void>;
  cargarEmpresasUsuario: () => Promise<UsuarioEmpresa[]>;
  // Nuevos roles
  esAdministrador: () => boolean;
  esJefeCampo: () => boolean;
  esJefeFinanciero: () => boolean;
  esOperario: () => boolean;
  esConsultorExterno: () => boolean;
  // Roles legacy (mantener para retrocompatibilidad)
  esAsesor: () => boolean;
  esContador: () => boolean;
  esTecnico: () => boolean;
  esSoloLectura: () => boolean;
  esProductor: () => boolean;
  // Permisos
  tienePermisoEscritura: () => boolean;
  tienePermisoAdministracion: () => boolean;
  tienePermisoFinanciero: () => boolean;
  loading: boolean;
  error: string | null;
}

const EmpresaContext = createContext<EmpresaContextType | undefined>(undefined);

interface EmpresaProviderProps {
  children: ReactNode;
}

export const EmpresaProvider: React.FC<EmpresaProviderProps> = ({ children }) => {
  const { user, loading: authCargando } = useAuth();

  const [empresaActiva, setEmpresaActiva] = useState<Empresa | null>(null);
  const [empresasUsuario, setEmpresasUsuario] = useState<UsuarioEmpresa[]>([]);
  const [rolUsuario, setRolUsuario] = useState<string | null>(null);
  const [empresaContextoListo, setEmpresaContextoListo] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const cargarEmpresasUsuario = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      setEmpresaContextoListo(false);
      // Evitar que el interceptor envíe X-Company-Id obsoleto hasta validar mis-empresas
      localStorage.removeItem('empresaIdValidada');

      const response = await api.get('/v1/empresas/mis-empresas');
      const empresas = response.data;
      
      console.log('🔍 [EmpresaContext] Datos recibidos de mis-empresas:', empresas);
      
      setEmpresasUsuario(empresas);

      if (empresas.length === 0) {
        setEmpresaActiva(null);
        setRolUsuario(null);
        setEmpresaContextoListo(false);
        localStorage.removeItem('empresaActiva');
        localStorage.removeItem('empresaIdValidada');
        localStorage.removeItem('rolUsuario');
        localStorage.removeItem('campanaActiva');
        return empresas;
      }

      // Validar empresa guardada contra las empresas reales del usuario (evita X-Company-Id obsoleto)
      let empresaSeleccionada: UsuarioEmpresa | undefined;
      const empresaGuardada = localStorage.getItem('empresaActiva');
      if (empresaGuardada) {
        try {
          const parsed = JSON.parse(empresaGuardada) as Empresa;
          empresaSeleccionada = empresas.find((e: UsuarioEmpresa) => e.empresaId === parsed.id);
        } catch {
          // ignorar JSON inválido
        }
      }
      if (!empresaSeleccionada) {
        empresaSeleccionada = empresas[0];
      }
      if (empresaSeleccionada) {
        await cambiarEmpresaConDatos(empresaSeleccionada);
      }

      return empresas;
      
    } catch (error) {
      console.error('Error cargando empresas del usuario:', error);
      setError('Error al cargar las empresas del usuario');
      return []; // Retornar array vacío en caso de error
    } finally {
      setLoading(false);
    }
  }, []);

  // Restaurar empresa al volver a entrar (sesión por cookie + user en localStorage, sin token)
  useEffect(() => {
    if (authCargando) {
      return;
    }
    if (user) {
      cargarEmpresasUsuario();
    } else {
      setEmpresaActiva(null);
      setEmpresasUsuario([]);
      setRolUsuario(null);
      setEmpresaContextoListo(false);
    }
  }, [user?.id, authCargando, cargarEmpresasUsuario]);

  const cambiarEmpresaConDatos = async (empresaData: UsuarioEmpresa) => {
    try {
      setLoading(true);
      setError(null);
      
      
      // Actualizar empresa activa y rol
      // Crear objeto Empresa desde UsuarioEmpresa
      const empresa: Empresa = {
        id: empresaData.empresaId,
        nombre: empresaData.empresaNombre,
        estado: empresaData.estado,
        activo: empresaData.estado === 'ACTIVO',
        fechaCreacion: empresaData.fechaInicio,
        fechaActualizacion: empresaData.fechaInicio
      };
      setEmpresaActiva(empresa);
      setRolUsuario(empresaData.rol);
      
      // Guardar en localStorage para persistencia
      localStorage.setItem('empresaActiva', JSON.stringify(empresa));
      localStorage.setItem('empresaIdValidada', String(empresaData.empresaId));
      localStorage.setItem('rolUsuario', empresaData.rol);
      setEmpresaContextoListo(true);
      
      // Actualizar el token con el contexto de empresa
      // TODO: Implementar endpoint en backend si es necesario
      // await api.post('/empresas/cambiar-contexto', { empresaId: empresaData.id });
      
      
    } catch (error) {
      console.error('Error cambiando empresa:', error);
      setError(error instanceof Error ? error.message : 'Error al cambiar empresa');
    } finally {
      setLoading(false);
    }
  };

  const cambiarEmpresa = async (empresaIdDestino: number) => {
    try {
      setLoading(true);
      setError(null);

      const usuarioEmpresa = empresasUsuario.find((ue) => ue.empresaId === empresaIdDestino);

      if (!usuarioEmpresa) {
        throw new Error('No tienes acceso a esta empresa');
      }

      await cambiarEmpresaConDatos(usuarioEmpresa);
      
    } catch (error) {
      console.error('Error cambiando empresa:', error);
      setError('Error al cambiar de empresa');
    } finally {
      setLoading(false);
    }
  };

  // Métodos de verificación de permisos - NUEVOS ROLES
  const esAdministrador = () => rolUsuario === 'ADMINISTRADOR';
  const esJefeCampo = () => rolUsuario === 'JEFE_CAMPO' || 
                            rolUsuario === 'PRODUCTOR' || 
                            rolUsuario === 'ASESOR' || 
                            rolUsuario === 'TECNICO';
  const esJefeFinanciero = () => rolUsuario === 'JEFE_FINANCIERO' || 
                                  rolUsuario === 'CONTADOR';
  const esOperario = () => rolUsuario === 'OPERARIO';
  const esConsultorExterno = () => rolUsuario === 'CONSULTOR_EXTERNO' || 
                                     rolUsuario === 'LECTURA';

  // Métodos legacy (mantener para retrocompatibilidad)
  const esAsesor = () => rolUsuario === 'ASESOR' || rolUsuario === 'JEFE_CAMPO';
  const esContador = () => rolUsuario === 'CONTADOR' || rolUsuario === 'JEFE_FINANCIERO';
  const esTecnico = () => rolUsuario === 'TECNICO' || rolUsuario === 'JEFE_CAMPO';
  const esSoloLectura = () => rolUsuario === 'LECTURA' || rolUsuario === 'CONSULTOR_EXTERNO';
  const esProductor = () => rolUsuario === 'PRODUCTOR' || rolUsuario === 'JEFE_CAMPO';

  const tienePermisoEscritura = (): boolean => {
    // Solo ADMINISTRADOR y JEFE_CAMPO tienen permisos de escritura en operaciones
    return Boolean(rolUsuario && (
      rolUsuario === 'ADMINISTRADOR' || 
      esJefeCampo()
    ));
  };

  const tienePermisoAdministracion = () => {
    return esAdministrador();
  };

  const tienePermisoFinanciero = () => {
    return esAdministrador() || esJefeFinanciero();
  };

  // No hidratar empresa desde localStorage: evita X-Company-Id obsoleto antes de mis-empresas.

  const value: EmpresaContextType = {
    empresaActiva,
    empresaId: empresaActiva?.id ?? null,
    empresasUsuario,
    rolUsuario,
    empresaContextoListo,
    cambiarEmpresa,
    cargarEmpresasUsuario,
    // Nuevos roles
    esAdministrador,
    esJefeCampo,
    esJefeFinanciero,
    esOperario,
    esConsultorExterno,
    // Roles legacy
    esAsesor,
    esContador,
    esTecnico,
    esSoloLectura,
    esProductor,
    // Permisos
    tienePermisoEscritura,
    tienePermisoAdministracion,
    tienePermisoFinanciero,
    loading,
    error
  };

  
  return (
    <EmpresaContext.Provider value={value}>
      {children}
    </EmpresaContext.Provider>
  );
};

export const useEmpresa = (): EmpresaContextType => {
  const context = useContext(EmpresaContext);
  if (context === undefined) {
    throw new Error('useEmpresa debe usarse dentro de EmpresaProvider');
  }
  return context;
};

export default EmpresaContext;
