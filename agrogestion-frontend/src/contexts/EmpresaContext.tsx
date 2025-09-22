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
  rol: 'ADMINISTRADOR' | 'ASESOR' | 'OPERARIO' | 'CONTADOR' | 'TECNICO' | 'LECTURA' | 'PRODUCTOR';
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
  esAdministrador: () => boolean;
  esAsesor: () => boolean;
  esOperario: () => boolean;
  esContador: () => boolean;
  esTecnico: () => boolean;
  esSoloLectura: () => boolean;
  esProductor: () => boolean;
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
      setEmpresaActiva(empresaData);
      setRolUsuario(empresaData.rol);
      
      // Guardar en localStorage para persistencia
      localStorage.setItem('empresaActiva', JSON.stringify(empresaData));
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

  // Métodos de verificación de permisos
  const esAdministrador = () => rolUsuario === 'ADMINISTRADOR';
  const esAsesor = () => rolUsuario === 'ASESOR';
  const esOperario = () => rolUsuario === 'OPERARIO';
  const esContador = () => rolUsuario === 'CONTADOR';
  const esTecnico = () => rolUsuario === 'TECNICO';
  const esSoloLectura = () => rolUsuario === 'LECTURA';
  const esProductor = () => rolUsuario === 'PRODUCTOR';

  const tienePermisoEscritura = () => {
    return rolUsuario && !esSoloLectura() && rolUsuario !== 'LECTURA';
  };

  const tienePermisoAdministracion = () => {
    return esAdministrador() || esAsesor();
  };

  const tienePermisoFinanciero = () => {
    return esAdministrador() || esAsesor() || esContador() || esProductor();
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
    esAdministrador,
    esAsesor,
    esOperario,
    esContador,
    esTecnico,
    esSoloLectura,
    esProductor,
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
    throw new Error('useEmpresa debe ser usado dentro de un EmpresaProvider');
  }
  return context;
};

export default EmpresaContext;
