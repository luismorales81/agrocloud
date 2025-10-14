import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import api from '../services/api';

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
  empresasUsuario: UsuarioEmpresa[];
  rolUsuario: string | null;
  cambiarEmpresa: (empresaId: number) => Promise<void>;
  cargarEmpresasUsuario: () => Promise<void>;
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
  
  const [empresaActiva, setEmpresaActiva] = useState<Empresa | null>(null);
  const [empresasUsuario, setEmpresasUsuario] = useState<UsuarioEmpresa[]>([]);
  const [rolUsuario, setRolUsuario] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Cargar empresas del usuario al inicializar
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      cargarEmpresasUsuario();
    }
  }, []);

  const cargarEmpresasUsuario = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await api.get('/api/v1/empresas/mis-empresas');
      const empresas = response.data;
      
      console.log('🔍 [EmpresaContext] Datos recibidos de mis-empresas:', empresas);
      
      setEmpresasUsuario(empresas);
      
      // Si hay empresas y no hay empresa activa, seleccionar la primera
      if (empresas.length > 0 && !empresaActiva) {
        const primeraEmpresa = empresas[0];
        await cambiarEmpresaConDatos(primeraEmpresa);
      }
      
      return empresas; // Retornar las empresas cargadas
      
    } catch (error) {
      console.error('Error cargando empresas del usuario:', error);
      setError('Error al cargar las empresas del usuario');
      return []; // Retornar array vacío en caso de error
    } finally {
      setLoading(false);
    }
  };

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
      localStorage.setItem('rolUsuario', empresaData.rol);
      
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

  const cambiarEmpresa = async (empresaId: number) => {
    try {
      setLoading(true);
      setError(null);
      
      // Buscar la empresa en las empresas del usuario
      const usuarioEmpresa = empresasUsuario.find(ue => ue.id === empresaId);
      
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

  // Cargar empresa activa desde localStorage al inicializar
  useEffect(() => {
    const empresaGuardada = localStorage.getItem('empresaActiva');
    const rolGuardado = localStorage.getItem('rolUsuario');
    
    if (empresaGuardada && rolGuardado) {
      try {
        setEmpresaActiva(JSON.parse(empresaGuardada));
        setRolUsuario(rolGuardado);
      } catch (error) {
        console.error('Error cargando empresa desde localStorage:', error);
      }
    }
  }, []);

  const value: EmpresaContextType = {
    empresaActiva,
    empresasUsuario,
    rolUsuario,
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

export const useEmpresa = (): EmpresaContextType | undefined => {
  const context = useContext(EmpresaContext);
  return context;
};

export default EmpresaContext;
