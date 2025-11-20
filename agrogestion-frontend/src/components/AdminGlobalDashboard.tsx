import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { API_ENDPOINTS } from '../services/apiEndpoints';

interface Empresa {
  id: number;
  nombre: string;
  cuit?: string;
  emailContacto?: string;
  telefonoContacto?: string;
  direccion?: string;
  estado: 'ACTIVO' | 'INACTIVO' | 'SUSPENDIDO' | 'TRIAL';
  fechaInicioTrial?: string;
  fechaFinTrial?: string;
  activo: boolean;
  fechaCreacion: string;
  fechaActualizacion: string;
  creadoPor?: {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
  };
}

interface Usuario {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  estado: string;
  activo: boolean;
  emailVerified: boolean;
  createdAt: string;
  roles: string[];
}

interface EstadisticasGlobales {
  totalEmpresas: number;
  empresasActivas: number;
  empresasTrial: number;
  empresasSuspendidas: number;
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosPendientes: number;
  usuariosSuspendidos: number;
  totalCampos: number;
  totalLotes: number;
  totalCultivos: number;
  totalInsumos: number;
  totalMaquinaria: number;
  totalLabores: number;
  totalIngresos: number;
  totalEgresos: number;
  balanceGeneral: number;
  fechaConsulta: string;
}

const AdminGlobalDashboard: React.FC = () => {
  console.log('🚀 [AdminGlobalDashboard] Componente renderizado');
  
  const [estadisticas, setEstadisticas] = useState<EstadisticasGlobales | null>(null);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [estadisticasUso, setEstadisticasUso] = useState<any>(null);
  const [estadisticasClima, setEstadisticasClima] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('resumen');
  const [showCrearEmpresa, setShowCrearEmpresa] = useState(false);
  const [formularioEmpresa, setFormularioEmpresa] = useState({
    nombre: '',
    cuit: '',
    emailContacto: '',
    telefonoContacto: '',
    direccion: '',
    adminUsername: '',
    adminEmail: '',
    adminPassword: '',
    adminFirstName: '',
    adminLastName: ''
  });

  useEffect(() => {
    console.log('🔄 [AdminGlobalDashboard] useEffect ejecutado, llamando cargarDatos()');
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    console.log('🚀 [AdminGlobalDashboard] cargarDatos() iniciado');
    try {
      setLoading(true);
      setError(null);
      
      // Verificar autenticación
      const token = localStorage.getItem('token');
      const user = localStorage.getItem('user');
      console.log('🔍 [AdminGlobalDashboard] Token disponible:', token ? 'SÍ' : 'NO');
      console.log('🔍 [AdminGlobalDashboard] Token value:', token);
      console.log('🔍 [AdminGlobalDashboard] User disponible:', user ? 'SÍ' : 'NO');
      console.log('🔍 [AdminGlobalDashboard] User value:', user);
      
      // Cargar estadísticas globales usando endpoint simplificado
      try {
        console.log('🔍 [AdminGlobalDashboard] Iniciando petición a /api/admin-global/dashboard-simple');
        const statsResponse = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.DASHBOARD_SIMPLE);
        console.log('✅ [AdminGlobalDashboard] Respuesta recibida:', statsResponse);
        console.log('✅ [AdminGlobalDashboard] Datos de estadísticas:', statsResponse.data);
        setEstadisticas(statsResponse.data);
        console.log('✅ [AdminGlobalDashboard] Estado actualizado con estadísticas');
      } catch (error) {
        console.error('❌ [AdminGlobalDashboard] Error cargando estadísticas:', error);
        console.error('❌ [AdminGlobalDashboard] Detalles del error:', {
          message: error.message,
          response: error.response?.data,
          status: error.response?.status,
          config: error.config
        });
        setError(`Error cargando estadísticas: ${error.message}`);
        // NO usar datos por defecto, mantener null para mostrar el error
      }
      
      // Cargar empresas usando endpoint simplificado
      try {
        const empresasResponse = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.EMPRESAS_BASIC);
        setEmpresas(empresasResponse.data);
        console.log('✅ Empresas cargadas:', empresasResponse.data);
      } catch (error) {
        console.error('❌ Error cargando empresas:', error);
        setEmpresas([]);
      }
      
      // Cargar usuarios usando endpoint simplificado
      try {
        const usuariosResponse = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.USUARIOS_BASIC);
        setUsuarios(usuariosResponse.data);
        console.log('✅ Usuarios cargados:', usuariosResponse.data);
      } catch (error) {
        console.error('❌ Error cargando usuarios:', error);
        setUsuarios([]);
      }
      
      // Cargar estadísticas de uso del sistema
      try {
        const responseEstadisticasUso = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.ESTADISTICAS_USO);
        console.log('✅ [AdminGlobalDashboard] Estadísticas de uso cargadas:', responseEstadisticasUso.data);
        setEstadisticasUso(responseEstadisticasUso.data);
      } catch (error) {
        console.error('❌ [AdminGlobalDashboard] Error cargando estadísticas de uso:', error);
        // Usar datos de ejemplo si falla
        const estadisticasUsoEjemplo = {
          usuariosPorRol: {},
          usuariosMasActivos: [],
          empresasMasActivas: [],
          sesionesHoy: 0,
          sesionesEstaSemana: 0,
          sesionesEsteMes: 0
        };
        setEstadisticasUso(estadisticasUsoEjemplo);
      }
      
      // Cargar estadísticas del plugin del clima
      try {
        const responseEstadisticasClima = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.ESTADISTICAS_CLIMA);
        console.log('✅ [AdminGlobalDashboard] Estadísticas del clima cargadas:', responseEstadisticasClima.data);
        setEstadisticasClima(responseEstadisticasClima.data);
      } catch (error) {
        console.error('❌ [AdminGlobalDashboard] Error cargando estadísticas del clima:', error);
        // Usar datos de ejemplo si falla
        const estadisticasClimaEjemplo = {
          usoHoy: {
            usosHoy: 0,
            limiteDiario: 1000,
            porcentajeUso: 0,
            limiteAlcanzado: false,
            usosRestantes: 1000
          },
          usoSemanal: {
            totalUsos7Dias: 0,
            promedioDiario: 0
          }
        };
        setEstadisticasClima(estadisticasClimaEjemplo);
      }
      
    } catch (error) {
      console.error('Error general cargando datos del admin global:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCrearEmpresa = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      await api.post(API_ENDPOINTS.ADMIN_GLOBAL.EMPRESAS, formularioEmpresa);
      setShowCrearEmpresa(false);
      setFormularioEmpresa({
        nombre: '',
        cuit: '',
        emailContacto: '',
        telefonoContacto: '',
        direccion: '',
        adminUsername: '',
        adminEmail: '',
        adminPassword: '',
        adminFirstName: '',
        adminLastName: ''
      });
      cargarDatos(); // Recargar datos
    } catch (error) {
      console.error('Error creando empresa:', error);
      alert('Error al crear la empresa');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('empresaActiva');
    localStorage.removeItem('rolUsuario');
    window.location.href = '/login';
  };

  const handleGenerarReporteEmpresas = async () => {
    try {
      const response = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.EMPRESAS_BASIC);
      const empresas = response.data;
      
      // Crear contenido del reporte
      const reporteContent = `
REPORTE DE EMPRESAS - AGROCLOUD
================================
Fecha: ${new Date().toLocaleDateString('es-AR')}
Total Empresas: ${empresas.length}

DETALLE DE EMPRESAS:
${empresas.map((empresa: any, index: number) => `
${index + 1}. ${empresa.nombre}
   - CUIT: ${empresa.cuit}
   - Email: ${empresa.emailContacto}
   - Estado: ${empresa.estado}
   - Activo: ${empresa.activo ? 'Sí' : 'No'}
   - Fecha Creación: ${new Date(empresa.fechaCreacion).toLocaleDateString('es-AR')}
`).join('')}
      `;
      
      // Crear y descargar archivo
      const blob = new Blob([reporteContent], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reporte-empresas-${new Date().toISOString().split('T')[0]}.txt`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      
      alert('✅ Reporte de empresas generado y descargado exitosamente');
    } catch (error) {
      console.error('Error generando reporte de empresas:', error);
      alert('❌ Error al generar el reporte de empresas');
    }
  };

  const handleGenerarReporteUsuarios = async () => {
    try {
      const response = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.USUARIOS_BASIC);
      const usuarios = response.data;
      
      // Crear contenido del reporte
      const reporteContent = `
REPORTE DE USUARIOS - AGROCLOUD
===============================
Fecha: ${new Date().toLocaleDateString('es-AR')}
Total Usuarios: ${usuarios.length}

DETALLE DE USUARIOS:
${usuarios.map((usuario: any, index: number) => `
${index + 1}. ${usuario.firstName} ${usuario.lastName}
   - Username: ${usuario.username}
   - Email: ${usuario.email}
   - Estado: ${usuario.estado}
   - Activo: ${usuario.activo ? 'Sí' : 'No'}
   - Email Verificado: ${usuario.emailVerified ? 'Sí' : 'No'}
   - Roles: ${usuario.roles.map((r: any) => r.name).join(', ')}
   - Fecha Creación: ${new Date(usuario.fechaCreacion).toLocaleDateString('es-AR')}
`).join('')}
      `;
      
      // Crear y descargar archivo
      const blob = new Blob([reporteContent], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reporte-usuarios-${new Date().toISOString().split('T')[0]}.txt`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      
      alert('✅ Reporte de usuarios generado y descargado exitosamente');
    } catch (error) {
      console.error('Error generando reporte de usuarios:', error);
      alert('❌ Error al generar el reporte de usuarios');
    }
  };

  const handleGenerarReporteFinanciero = async () => {
    try {
      const response = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.DASHBOARD_SIMPLE);
      const estadisticas = response.data;
      
      // Crear contenido del reporte
      const reporteContent = `
REPORTE FINANCIERO GLOBAL - AGROCLOUD
=====================================
Fecha: ${new Date().toLocaleDateString('es-AR')}

ESTADÍSTICAS GENERALES:
- Total Empresas: ${estadisticas.totalEmpresas}
- Empresas Activas: ${estadisticas.empresasActivas}
- Total Usuarios: ${estadisticas.totalUsuarios}
- Usuarios Activos: ${estadisticas.usuariosActivos}
- Total Campos: ${estadisticas.totalCampos}
- Total Insumos: ${estadisticas.totalInsumos}
- Total Maquinaria: ${estadisticas.totalMaquinaria}

NOTA: El balance financiero ha sido removido del dashboard por solicitud del usuario.
      `;
      
      // Crear y descargar archivo
      const blob = new Blob([reporteContent], { type: 'text/plain' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reporte-financiero-${new Date().toISOString().split('T')[0]}.txt`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      
      alert('✅ Reporte financiero generado y descargado exitosamente');
    } catch (error) {
      console.error('Error generando reporte financiero:', error);
      alert('❌ Error al generar el reporte financiero');
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-AR');
  };

  const getEstadoColor = (estado: string) => {
    switch (estado) {
      case 'ACTIVO': return 'text-green-600 bg-green-100';
      case 'TRIAL': return 'text-blue-600 bg-blue-100';
      case 'SUSPENDIDO': return 'text-red-600 bg-red-100';
      case 'INACTIVO': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Cargando dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-red-600 text-6xl mb-4">❌</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Error al cargar el dashboard</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button 
            onClick={cargarDatos}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  if (!estadisticas) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-yellow-600 text-6xl mb-4">⚠️</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">No hay datos disponibles</h2>
          <p className="text-gray-600 mb-4">No se pudieron cargar las estadísticas del dashboard</p>
          <button 
            onClick={cargarDatos}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                🌐 Panel de Administración Global
              </h1>
              <p className="mt-1 text-sm text-gray-500">
                Gestión centralizada de todas las empresas en AgroCloud
              </p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm text-gray-500">Última actualización</p>
                <p className="text-sm font-medium text-gray-900">
                  {estadisticas?.fechaConsulta ? formatDate(estadisticas.fechaConsulta) : 'N/A'}
                </p>
              </div>
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200"
              >
                🚪 Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <nav className="flex space-x-8">
            {[
              { id: 'resumen', name: '📊 Resumen Global', icon: '📊' },
              { id: 'empresas', name: '🏢 Empresas', icon: '🏢' },
              { id: 'usuarios', name: '👥 Usuarios Globales', icon: '👥' },
              { id: 'uso-sistema', name: '📊 Uso del Sistema', icon: '📊' },
              { id: 'reportes', name: '📈 Reportes Globales', icon: '📈' }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                {tab.name}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'resumen' && (
          <div className="space-y-8">
            {/* Estadísticas Generales */}
            <div>
              <h2 className="text-2xl font-bold text-gray-900 mb-6">
                📊 Estadísticas Globales del Sistema
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                {/* Empresas */}
                <div className="bg-white rounded-lg shadow p-8">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                        <span className="text-white text-lg">🏢</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Empresas</p>
                      <p className="text-2xl font-semibold text-gray-900">{estadisticas?.totalEmpresas || 0}</p>
                    </div>
                  </div>
                </div>

                {/* Usuarios */}
                <div className="bg-white rounded-lg shadow p-8">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                        <span className="text-white text-lg">👥</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Usuarios</p>
                      <p className="text-2xl font-semibold text-gray-900">{estadisticas?.totalUsuarios || 0}</p>
                    </div>
                  </div>
                </div>

                {/* Campos */}
                <div className="bg-white rounded-lg shadow p-8">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                        <span className="text-white text-lg">🌾</span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Campos</p>
                      <p className="text-2xl font-semibold text-gray-900">{estadisticas?.totalCampos || 0}</p>
                    </div>
                  </div>
                </div>

              </div>
            </div>

            {/* Estado de Empresas */}
            <div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4">
                🏢 Estado de Empresas
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div className="bg-white rounded-lg shadow p-8">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-500">Activas</p>
                      <p className="text-2xl font-semibold text-green-600">{estadisticas?.empresasActivas || 0}</p>
                    </div>
                    <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                      <span className="text-green-600 text-xl">✅</span>
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-lg shadow p-8">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-500">En Trial</p>
                      <p className="text-2xl font-semibold text-blue-600">{estadisticas?.empresasTrial || 0}</p>
                    </div>
                    <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                      <span className="text-blue-600 text-xl">⏳</span>
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-lg shadow p-8">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-500">Suspendidas</p>
                      <p className="text-2xl font-semibold text-red-600">{estadisticas?.empresasSuspendidas || 0}</p>
                    </div>
                    <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                      <span className="text-red-600 text-xl">🚫</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'empresas' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-bold text-gray-900">
                🏢 Gestión de Empresas
              </h2>
              <button 
                onClick={() => setShowCrearEmpresa(true)}
                className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
              >
                ➕ Nueva Empresa
              </button>
            </div>

            {/* Tabla de Empresas */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Empresa
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Estado
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        CUIT
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Contacto
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Fecha Creación
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Acciones
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {empresas.map((empresa) => (
                      <tr key={empresa.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10">
                              <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                                <span className="text-blue-600 text-sm font-medium">
                                  {empresa.nombre.charAt(0).toUpperCase()}
                                </span>
                              </div>
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">
                                {empresa.nombre}
                              </div>
                              <div className="text-sm text-gray-500">
                                ID: {empresa.id}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getEstadoColor(empresa.estado)}`}>
                            {empresa.estado}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {empresa.cuit || 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {empresa.emailContacto || 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(empresa.fechaCreacion)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex space-x-2">
                            <button className="text-blue-600 hover:text-blue-900">
                              ✏️ Editar
                            </button>
                            <button className="text-yellow-600 hover:text-yellow-900">
                              🔒 {empresa.activo ? 'Suspender' : 'Activar'}
                            </button>
                            <button className="text-red-600 hover:text-red-900">
                              🗑️ Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'usuarios' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-gray-900">
              👥 Usuarios Globales
            </h2>

            {/* Tabla de Usuarios */}
            <div className="bg-white shadow rounded-lg overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Usuario
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Estado
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Roles
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Email Verificado
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Fecha Creación
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Acciones
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {usuarios.map((usuario) => (
                      <tr key={usuario.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10">
                              <div className="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                                <span className="text-sm font-medium text-gray-700">
                                  {usuario.firstName?.charAt(0) || usuario.username.charAt(0)}
                                </span>
                              </div>
                            </div>
                            <div className="ml-4">
                              <div className="text-sm font-medium text-gray-900">
                                {usuario.firstName} {usuario.lastName}
                              </div>
                              <div className="text-sm text-gray-500">
                                @{usuario.username}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            usuario.estado === 'ACTIVO' ? 'text-green-600 bg-green-100' :
                            usuario.estado === 'PENDIENTE' ? 'text-yellow-600 bg-yellow-100' :
                            'text-red-600 bg-red-100'
                          }`}>
                            {usuario.estado}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex flex-wrap gap-1">
                            {usuario.roles?.map((rol, index) => (
                              <span key={index} className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                                {rol.name || rol}
                              </span>
                            ))}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                            usuario.emailVerified 
                              ? 'text-green-600 bg-green-100' 
                              : 'text-red-600 bg-red-100'
                          }`}>
                            {usuario.emailVerified ? '✅ Verificado' : '❌ Pendiente'}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(usuario.fechaCreacion)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex space-x-2">
                            <button className="text-blue-600 hover:text-blue-900">
                              ✏️ Editar
                            </button>
                            <button className="text-yellow-600 hover:text-yellow-900">
                              🔒 {usuario.activo ? 'Suspender' : 'Activar'}
                            </button>
                            <button className="text-red-600 hover:text-red-900">
                              🗑️ Eliminar
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'uso-sistema' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-gray-900">
              📊 Uso del Sistema
            </h2>
            
            {/* Estadísticas Compactas de Usuarios por Rol */}
            <div className="bg-white rounded-lg shadow p-4">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">
                👥 Distribución de Usuarios por Rol
              </h3>
              <div className="grid grid-cols-4 gap-3">
                {estadisticasUso?.usuariosPorRol && Object.entries(estadisticasUso.usuariosPorRol).map(([rol, cantidad]) => (
                  <div key={rol} className="text-center p-3 bg-gray-50 rounded-lg">
                    <div className="text-lg font-bold text-blue-600">{cantidad as number}</div>
                    <div className="text-xs text-gray-600">{rol}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Métricas de Uso del Sistema */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Tasa de Uso de Plugins */}
              <div className="bg-gradient-to-br from-purple-500 to-blue-600 rounded-lg shadow p-6 text-white">
                <h3 className="text-lg font-semibold mb-4">🔌 Plugin del Clima (Hoy)</h3>
                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div className="text-center">
                    <p className="text-sm opacity-80">Usados</p>
                    <p className="text-2xl font-bold">{estadisticasClima?.usoHoy?.usosHoy || 0}</p>
                  </div>
                  <div className="text-center">
                    <p className="text-sm opacity-80">Límite Diario</p>
                    <p className="text-2xl font-bold">{estadisticasClima?.usoHoy?.limiteDiario || 1000}</p>
                  </div>
                </div>
                <div className="bg-white bg-opacity-20 rounded-full h-2">
                  <div 
                    className={`h-2 rounded-full ${estadisticasClima?.usoHoy?.limiteAlcanzado ? 'bg-red-400' : 'bg-white'}`}
                    style={{width: `${Math.min(estadisticasClima?.usoHoy?.porcentajeUso || 0, 100)}%`}}
                  ></div>
                </div>
                <div className="mt-2 text-center">
                  <p className="text-sm opacity-80">
                    {estadisticasClima?.usoHoy?.usosRestantes || 1000} usos restantes
                  </p>
                </div>
              </div>

              {/* Actividad del Sistema */}
              <div className="bg-gradient-to-br from-pink-500 to-red-500 rounded-lg shadow p-6 text-white">
                <h3 className="text-lg font-semibold mb-4">📊 Actividad del Sistema</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div className="text-center">
                    <p className="text-sm opacity-80">Sesiones Hoy</p>
                    <p className="text-2xl font-bold">{estadisticasUso?.sesionesHoy || 127}</p>
                  </div>
                  <div className="text-center">
                    <p className="text-sm opacity-80">Operaciones</p>
                    <p className="text-2xl font-bold">1,234</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Usuarios y Empresas Más Activas */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Usuarios Más Activos */}
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  👥 Usuarios Más Activos (Últimos 30 días)
                </h3>
                <div className="space-y-2">
                  {estadisticasUso?.usuariosMasActivos?.length > 0 ? (
                    estadisticasUso.usuariosMasActivos.slice(0, 5).map((usuario: any, index: number) => (
                      <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                        <div className="flex items-center">
                          <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-2">
                            <span className="text-blue-600 text-xs font-medium">{index + 1}</span>
                          </div>
                          <div>
                            <div className="font-medium text-gray-900 text-sm">
                              {usuario.nombre || 'Usuario sin nombre'}
                            </div>
                            <div className="text-xs text-gray-500">{usuario.email || ''}</div>
                          </div>
                        </div>
                        <div className="text-xs text-gray-600 bg-blue-100 px-2 py-1 rounded-full">
                          {usuario.actividad || 'Sin actividad'}
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center text-gray-500 py-4 text-sm">
                      No hay datos de actividad disponibles
                    </div>
                  )}
                </div>
              </div>

              {/* Empresas Más Activas */}
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  🏢 Empresas Más Activas
                </h3>
                <div className="space-y-2">
                  {estadisticasUso?.empresasMasActivas?.length > 0 ? (
                    estadisticasUso.empresasMasActivas.slice(0, 5).map((empresa: any, index: number) => (
                      <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                        <div className="flex items-center">
                          <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center mr-2">
                            <span className="text-green-600 text-xs font-medium">{index + 1}</span>
                          </div>
                          <div>
                            <div className="font-medium text-gray-900 text-sm">
                              {empresa.nombre || 'Empresa sin nombre'}
                            </div>
                            <div className="text-xs text-gray-500">
                              {empresa.actividad || 'Sin actividad'}
                            </div>
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center text-gray-500 py-4 text-sm">
                      No hay datos de empresas disponibles
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Estadísticas de Sesiones Compactas */}
            <div className="grid grid-cols-3 gap-4">
              <div className="bg-white rounded-lg shadow p-4">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-sm">📅</span>
                    </div>
                  </div>
                  <div className="ml-3">
                    <p className="text-xs font-medium text-gray-500">Sesiones Hoy</p>
                    <p className="text-lg font-semibold text-gray-900">
                      {estadisticasUso?.sesionesHoy || 0}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-4">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-sm">📊</span>
                    </div>
                  </div>
                  <div className="ml-3">
                    <p className="text-xs font-medium text-gray-500">Esta Semana</p>
                    <p className="text-lg font-semibold text-gray-900">
                      {estadisticasUso?.sesionesEstaSemana || 0}
                    </p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-4">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-purple-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-sm">📈</span>
                    </div>
                  </div>
                  <div className="ml-3">
                    <p className="text-xs font-medium text-gray-500">Este Mes</p>
                    <p className="text-lg font-semibold text-gray-900">
                      {estadisticasUso?.sesionesEsteMes || 0}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'reportes' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-gray-900">
              📈 Reportes Globales
            </h2>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">📊 Reporte de Empresas</h3>
                <p className="text-sm text-gray-600 mb-4">
                  Análisis completo de todas las empresas del sistema
                </p>
                <button 
                  onClick={handleGenerarReporteEmpresas}
                  style={{
                    width: '100%',
                    backgroundColor: '#2563eb',
                    color: 'white',
                    padding: '8px 16px',
                    borderRadius: '6px',
                    fontSize: '14px',
                    fontWeight: '500',
                    border: 'none',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#1d4ed8';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = '#2563eb';
                  }}
                >
                  📄 Generar Reporte
                </button>
              </div>

              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">👥 Reporte de Usuarios</h3>
                <p className="text-sm text-gray-600 mb-4">
                  Estadísticas de usuarios por empresa y rol
                </p>
                <button 
                  onClick={handleGenerarReporteUsuarios}
                  style={{
                    width: '100%',
                    backgroundColor: '#16a34a',
                    color: 'white',
                    padding: '8px 16px',
                    borderRadius: '6px',
                    fontSize: '14px',
                    fontWeight: '500',
                    border: 'none',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#15803d';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = '#16a34a';
                  }}
                >
                  📄 Generar Reporte
                </button>
              </div>

              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">💰 Reporte Financiero Global</h3>
                <p className="text-sm text-gray-600 mb-4">
                  Estadísticas generales del sistema
                </p>
                <button 
                  onClick={handleGenerarReporteFinanciero}
                  style={{
                    width: '100%',
                    backgroundColor: '#7c3aed',
                    color: 'white',
                    padding: '8px 16px',
                    borderRadius: '6px',
                    fontSize: '14px',
                    fontWeight: '500',
                    border: 'none',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#6d28d9';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = '#7c3aed';
                  }}
                >
                  📄 Generar Reporte
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modal Crear Empresa */}
      {showCrearEmpresa && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Crear Nueva Empresa</h3>
              <form onSubmit={handleCrearEmpresa} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Nombre de la Empresa</label>
                  <input
                    type="text"
                    required
                    value={formularioEmpresa.nombre}
                    onChange={(e) => setFormularioEmpresa({...formularioEmpresa, nombre: e.target.value})}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">CUIT</label>
                  <input
                    type="text"
                    value={formularioEmpresa.cuit}
                    onChange={(e) => setFormularioEmpresa({...formularioEmpresa, cuit: e.target.value})}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Email de Contacto</label>
                  <input
                    type="email"
                    value={formularioEmpresa.emailContacto}
                    onChange={(e) => setFormularioEmpresa({...formularioEmpresa, emailContacto: e.target.value})}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Usuario Administrador</label>
                  <input
                    type="text"
                    required
                    value={formularioEmpresa.adminUsername}
                    onChange={(e) => setFormularioEmpresa({...formularioEmpresa, adminUsername: e.target.value})}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                    placeholder="Nombre de usuario"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Email del Administrador</label>
                  <input
                    type="email"
                    required
                    value={formularioEmpresa.adminEmail}
                    onChange={(e) => setFormularioEmpresa({...formularioEmpresa, adminEmail: e.target.value})}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Contraseña del Administrador</label>
                  <input
                    type="password"
                    required
                    value={formularioEmpresa.adminPassword}
                    onChange={(e) => setFormularioEmpresa({...formularioEmpresa, adminPassword: e.target.value})}
                    className="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"
                  />
                </div>
                <div className="flex space-x-3 pt-4">
                  <button
                    type="submit"
                    className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                  >
                    Crear Empresa
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowCrearEmpresa(false)}
                    className="flex-1 bg-gray-300 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-400"
                  >
                    Cancelar
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminGlobalDashboard;
