import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { API_ENDPOINTS } from '../services/apiEndpoints';
import { Icon } from '../core/components/Icon';

interface AdminStats {
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
  fechaConsulta?: string;
}

interface UserInfo {
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

const AdminDashboard: React.FC = () => {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [usuarios, setUsuarios] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('resumen');
  const [usoData, setUsoData] = useState<any>(null);
  const [auditoriaData, setAuditoriaData] = useState<any>(null);
  const [reportesData, setReportesData] = useState<any>(null);

  useEffect(() => {
    cargarDatos();
  }, []);

  useEffect(() => {
    // Cargar datos específicos cuando cambie la pestaña activa
    if (activeTab === 'uso-sistema' && !usoData) {
      cargarDatosUsoSistema();
    } else if (activeTab === 'auditoria' && !auditoriaData) {
      cargarDatosAuditoria();
    } else if (activeTab === 'reportes' && !reportesData) {
      cargarDatosReportes();
    }
  }, [activeTab]);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      
      // Cargar resumen del sistema
      const resumenData = await api.get(API_ENDPOINTS.ADMIN_DASHBOARD.RESUMEN);
      setStats(resumenData.data);
      
      // Cargar lista de usuarios
      const usuariosData = await api.get(API_ENDPOINTS.ADMIN_DASHBOARD.USUARIOS_LISTA);
      setUsuarios(usuariosData.data.usuarios || []);
      
    } catch (error) {
      console.error('Error cargando datos del admin:', error);
    } finally {
      setLoading(false);
    }
  };

  const cargarDatosUsoSistema = async () => {
    try {
      const data = await api.get(API_ENDPOINTS.ADMIN_DASHBOARD.USO_SISTEMA);
      setUsoData(data.data);
      return data.data;
    } catch (error) {
      console.error('Error cargando datos de uso del sistema:', error);
      return null;
    }
  };

  const cargarDatosAuditoria = async () => {
    try {
      const data = await api.get(API_ENDPOINTS.ADMIN_DASHBOARD.AUDITORIA);
      setAuditoriaData(data.data);
      return data.data;
    } catch (error) {
      console.error('Error cargando datos de auditoría:', error);
      return null;
    }
  };

  const cargarDatosReportes = async () => {
    try {
      const data = await api.get(API_ENDPOINTS.ADMIN_DASHBOARD.REPORTES);
      setReportesData(data.data);
      return data.data;
    } catch (error) {
      console.error('Error cargando datos de reportes:', error);
      return null;
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
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
      case 'PENDIENTE': return 'text-yellow-600 bg-yellow-100';
      case 'SUSPENDIDO': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-green-600"></div>
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
              <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
                <Icon name="Building2" size={32} /> Dashboard Administrador
              </h1>
              <p className="mt-1 text-sm text-gray-500">
                Panel de control del sistema AgroCloud
              </p>
            </div>
            <div className="flex items-center space-x-4">
            <div className="text-right">
              <p className="text-sm text-gray-500">Última actualización</p>
              <p className="text-sm font-medium text-gray-900">
                {stats?.fechaConsulta ? formatDate(stats.fechaConsulta) : 'N/A'}
              </p>
              </div>
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200"
              >
                <Icon name="LogOut" size={16} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Cerrar Sesión
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
              { id: 'resumen', name: 'Resumen', icon: 'BarChart' },
              { id: 'usuarios', name: 'Usuarios', icon: 'Users' },
              { id: 'uso-sistema', name: 'Uso del Sistema', icon: 'Monitor' },
              { id: 'tablas-maestras', name: 'Tablas Maestras', icon: 'Settings' },
              { id: 'auditoria', name: 'Auditoría', icon: 'Lock' },
              { id: 'reportes', name: 'Reportes', icon: 'TrendingUp' }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`py-4 px-1 border-b-2 font-medium text-sm ${
                  activeTab === tab.id
                    ? 'border-green-500 text-green-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon name={tab.icon} size={16} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> {tab.name}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'resumen' && (
          <div className="space-y-6">
            {/* Estadísticas Generales */}
            <div>
              <h2 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
                <Icon name="BarChart" size={24} /> Estadísticas Generales del Sistema
              </h2>
              
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {/* Usuarios */}
                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                        <Icon name="Users" size={20} className="text-white" />
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Usuarios</p>
                      <p className="text-2xl font-semibold text-gray-900">{stats?.totalUsuarios || 0}</p>
                    </div>
                  </div>
                </div>

                {/* Campos */}
                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                        <Icon name="Wheat" size={20} className="text-white" />
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Campos</p>
                      <p className="text-2xl font-semibold text-gray-900">{stats?.totalCampos || 0}</p>
                    </div>
                  </div>
                </div>

                {/* Lotes */}
                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                        <Icon name="Square" size={20} className="text-white" />
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Total Lotes</p>
                      <p className="text-2xl font-semibold text-gray-900">{stats?.totalLotes || 0}</p>
                    </div>
                  </div>
                </div>

                {/* Balance */}
                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      <div className="w-8 h-8 bg-purple-500 rounded-md flex items-center justify-center">
                        <Icon name="DollarSign" size={20} className="text-white" />
                      </div>
                    </div>
                    <div className="ml-4">
                      <p className="text-sm font-medium text-gray-500">Balance General</p>
                      <p className={`text-2xl font-semibold ${
                        (stats?.balanceGeneral || 0) >= 0 ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {formatCurrency(stats?.balanceGeneral || 0)}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Estado de Usuarios */}
            <div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <Icon name="Users" size={20} /> Estado de Usuarios
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-500">Activos</p>
                      <p className="text-2xl font-semibold text-green-600">{stats?.usuariosActivos || 0}</p>
                    </div>
                    <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                      <Icon name="CheckCircle" size={24} className="text-green-600" />
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-500">Pendientes</p>
                      <p className="text-2xl font-semibold text-yellow-600">{stats?.usuariosPendientes || 0}</p>
                    </div>
                    <div className="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center">
                      <Icon name="Loader" size={24} className="text-yellow-600" />
                    </div>
                  </div>
                </div>

                <div className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-500">Suspendidos</p>
                      <p className="text-2xl font-semibold text-red-600">{stats?.usuariosSuspendidos || 0}</p>
                    </div>
                    <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                      <Icon name="XCircle" size={24} className="text-red-600" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Activos del Sistema */}
            <div>
              <h3 className="text-xl font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <Icon name="Building" size={20} /> Activos del Sistema
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="bg-white rounded-lg shadow p-6 text-center">
                  <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Icon name="Sprout" size={24} className="text-blue-600" />
                  </div>
                  <p className="text-sm font-medium text-gray-500">Cultivos</p>
                  <p className="text-2xl font-semibold text-gray-900">{stats?.totalCultivos || 0}</p>
                </div>

                <div className="bg-white rounded-lg shadow p-6 text-center">
                  <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Icon name="FlaskConical" size={24} className="text-green-600" />
                  </div>
                  <p className="text-sm font-medium text-gray-500">Insumos</p>
                  <p className="text-2xl font-semibold text-gray-900">{stats?.totalInsumos || 0}</p>
                </div>

                <div className="bg-white rounded-lg shadow p-6 text-center">
                  <div className="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Icon name="Tractor" size={24} className="text-yellow-600" />
                  </div>
                  <p className="text-sm font-medium text-gray-500">Maquinaria</p>
                  <p className="text-2xl font-semibold text-gray-900">{stats?.totalMaquinaria || 0}</p>
                </div>

                <div className="bg-white rounded-lg shadow p-6 text-center">
                  <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Icon name="Wrench" size={24} className="text-purple-600" />
                  </div>
                  <p className="text-sm font-medium text-gray-500">Labores</p>
                  <p className="text-2xl font-semibold text-gray-900">{stats?.totalLabores || 0}</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'usuarios' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                <Icon name="Users" size={24} /> Gestión de Usuarios
              </h2>
              <button className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 flex items-center gap-2">
                <Icon name="Plus" size={16} /> Nuevo Usuario
              </button>
            </div>

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
                          <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getEstadoColor(usuario.estado)}`}>
                            {usuario.estado}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex flex-wrap gap-1">
                            {usuario.roles?.map((rol, index) => (
                              <span key={index} className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                                {rol}
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
                            <span className="flex items-center gap-1">
                              {usuario.emailVerified ? (
                                <>
                                  <Icon name="CheckCircle" size={12} /> Verificado
                                </>
                              ) : (
                                <>
                                  <Icon name="XCircle" size={12} /> Pendiente
                                </>
                              )}
                            </span>
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(usuario.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <div className="flex space-x-2">
                            <button className="text-blue-600 hover:text-blue-900 flex items-center gap-1">
                              <Icon name="Pencil" size={14} /> Editar
                            </button>
                            <button className="text-yellow-600 hover:text-yellow-900 flex items-center gap-1">
                              <Icon name="Lock" size={14} /> {usuario.activo ? 'Suspender' : 'Activar'}
                            </button>
                            <button className="text-red-600 hover:text-red-900 flex items-center gap-1">
                              <Icon name="Trash2" size={14} /> Eliminar
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
              💻 Uso del Sistema
            </h2>
            
            {/* Estadísticas de Uso */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">👥</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Usuarios Activos Hoy</p>
                    <p className="text-2xl font-semibold text-gray-900">{stats?.usuariosActivos || 0}</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">📊</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Sesiones Activas</p>
                    <p className="text-2xl font-semibold text-gray-900">3</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">🔄</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Operaciones Hoy</p>
                    <p className="text-2xl font-semibold text-gray-900">47</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-purple-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">💾</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Espacio Usado</p>
                    <p className="text-2xl font-semibold text-gray-900">2.3 GB</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Actividad Reciente */}
            <div className="bg-white rounded-lg shadow">
              <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900">📈 Actividad Reciente</h3>
              </div>
              <div className="p-6">
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div className="flex items-center">
                      <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                        <span className="text-green-600 text-sm">✅</span>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium text-gray-900">Usuario admin@agrocloud.com inició sesión</p>
                        <p className="text-xs text-gray-500">Hace 5 minutos</p>
                      </div>
                    </div>
                    <span className="text-xs text-gray-500">Login</span>
                  </div>

                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div className="flex items-center">
                      <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-blue-600 text-sm">➕</span>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium text-gray-900">Nuevo insumo agregado: Fertilizante NPK</p>
                        <p className="text-xs text-gray-500">Hace 12 minutos</p>
                      </div>
                    </div>
                    <span className="text-xs text-gray-500">Insumo</span>
                  </div>

                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div className="flex items-center">
                      <div className="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                        <span className="text-yellow-600 text-sm">📝</span>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium text-gray-900">Labor de siembra completada en Lote A</p>
                        <p className="text-xs text-gray-500">Hace 25 minutos</p>
                      </div>
                    </div>
                    <span className="text-xs text-gray-500">Labor</span>
                  </div>

                  <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div className="flex items-center">
                      <div className="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
                        <span className="text-purple-600 text-sm">💰</span>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium text-gray-900">Nuevo ingreso registrado: $15,000</p>
                        <p className="text-xs text-gray-500">Hace 1 hora</p>
                      </div>
                    </div>
                    <span className="text-xs text-gray-500">Finanzas</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Rendimiento del Sistema */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">⚡ Rendimiento del Sistema</h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">CPU</span>
                    <div className="flex items-center">
                      <div className="w-32 bg-gray-200 rounded-full h-2 mr-2">
                        <div className="bg-green-500 h-2 rounded-full" style={{width: '25%'}}></div>
                      </div>
                      <span className="text-sm font-medium text-gray-900">25%</span>
                    </div>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">Memoria</span>
                    <div className="flex items-center">
                      <div className="w-32 bg-gray-200 rounded-full h-2 mr-2">
                        <div className="bg-yellow-500 h-2 rounded-full" style={{width: '60%'}}></div>
                      </div>
                      <span className="text-sm font-medium text-gray-900">60%</span>
                    </div>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">Almacenamiento</span>
                    <div className="flex items-center">
                      <div className="w-32 bg-gray-200 rounded-full h-2 mr-2">
                        <div className="bg-blue-500 h-2 rounded-full" style={{width: '40%'}}></div>
                      </div>
                      <span className="text-sm font-medium text-gray-900">40%</span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">🌐 Conectividad</h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">Estado del Servidor</span>
                    <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                      ✅ Online
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">Base de Datos</span>
                    <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                      ✅ Conectada
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">Tiempo de Respuesta</span>
                    <span className="text-sm font-medium text-gray-900">45ms</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">Última Sincronización</span>
                    <span className="text-sm font-medium text-gray-900">Hace 2 min</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'tablas-maestras' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-gray-900">
              ⚙️ Administración de Tablas Maestras
            </h2>
            <p className="text-gray-600">
              Gestión de catálogos y combos del sistema que se utilizan en los formularios
            </p>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {/* Tipos de Insumo */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">🧪 Tipos de Insumo</h3>
                    <p className="text-sm text-gray-500">Catálogo de tipos de insumos</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• FERTILIZANTE</p>
                      <p>• HERBICIDA</p>
                      <p>• FUNGICIDA</p>
                      <p>• INSECTICIDA</p>
                      <p>• SEMILLA</p>
                      <p>• COMBUSTIBLE</p>
                      <p>• LUBRICANTE</p>
                      <p>• REPUESTO</p>
                      <p>• HERRAMIENTA</p>
                      <p>• OTROS</p>
                    </div>
                  </div>
                  <button className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Unidades de Medida */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">📏 Unidades de Medida</h3>
                    <p className="text-sm text-gray-500">Catálogo de unidades de medida</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• kg - Kilogramos</p>
                      <p>• l - Litros</p>
                      <p>• m² - Metros cuadrados</p>
                      <p>• ha - Hectáreas</p>
                      <p>• un - Unidades</p>
                      <p>• bolsas - Bolsas</p>
                      <p>• bidones - Bidones</p>
                    </div>
                  </div>
                  <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Tipos de Ingreso */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">💰 Tipos de Ingreso</h3>
                    <p className="text-sm text-gray-500">Catálogo de tipos de ingresos</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• VENTA_CULTIVO</p>
                      <p>• VENTA_ANIMAL</p>
                      <p>• SERVICIOS_AGRICOLAS</p>
                      <p>• SUBSIDIOS</p>
                      <p>• OTROS_INGRESOS</p>
                    </div>
                  </div>
                  <button className="bg-yellow-600 hover:bg-yellow-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Tipos de Egreso */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">💸 Tipos de Egreso</h3>
                    <p className="text-sm text-gray-500">Catálogo de tipos de egresos</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• INSUMO</p>
                      <p>• MAQUINARIA_COMPRA</p>
                      <p>• MAQUINARIA_ALQUILER</p>
                      <p>• SERVICIO</p>
                      <p>• OTROS</p>
                    </div>
                  </div>
                  <button className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Tipos de Labor */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">⚒️ Tipos de Labor</h3>
                    <p className="text-sm text-gray-500">Catálogo de tipos de labores</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• SIEMBRA</p>
                      <p>• FERTILIZACION</p>
                      <p>• RIEGO</p>
                      <p>• COSECHA</p>
                      <p>• MANTENIMIENTO</p>
                      <p>• PODA</p>
                      <p>• CONTROL_PLAGAS</p>
                      <p>• CONTROL_MALEZAS</p>
                      <p>• ANALISIS_SUELO</p>
                      <p>• OTROS</p>
                    </div>
                  </div>
                  <button className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Estados de Labor */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-indigo-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">📊 Estados de Labor</h3>
                    <p className="text-sm text-gray-500">Catálogo de estados de labores</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• PLANIFICADA</p>
                      <p>• EN_PROGRESO</p>
                      <p>• COMPLETADA</p>
                      <p>• CANCELADA</p>
                    </div>
                  </div>
                  <button className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Estados de Maquinaria */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-orange-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">🚜 Estados de Maquinaria</h3>
                    <p className="text-sm text-gray-500">Catálogo de estados de maquinaria</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• ACTIVA</p>
                      <p>• FUERA_DE_SERVICIO</p>
                      <p>• EN_MANTENIMIENTO</p>
                      <p>• RETIRADA</p>
                    </div>
                  </div>
                  <button className="bg-orange-600 hover:bg-orange-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Estados de Cultivo */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-teal-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">🌱 Estados de Cultivo</h3>
                    <p className="text-sm text-gray-500">Catálogo de estados de cultivos</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• ACTIVO</p>
                      <p>• INACTIVO</p>
                    </div>
                  </div>
                  <button className="bg-teal-600 hover:bg-teal-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Estados de Usuario */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-pink-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">👥 Estados de Usuario</h3>
                    <p className="text-sm text-gray-500">Catálogo de estados de usuarios</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• PENDIENTE</p>
                      <p>• ACTIVO</p>
                      <p>• SUSPENDIDO</p>
                      <p>• ELIMINADO</p>
                    </div>
                  </div>
                  <button className="bg-pink-600 hover:bg-pink-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>

              {/* Roles de Usuario */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-cyan-500">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">🔐 Roles de Usuario</h3>
                    <p className="text-sm text-gray-500">Catálogo de roles del sistema</p>
                    <div className="text-xs text-gray-600 mt-2">
                      <p>• ADMIN</p>
                      <p>• ADMINISTRADOR</p>
                      <p>• TECNICO</p>
                      <p>• OPERARIO</p>
                      <p>• PRODUCTOR</p>
                    </div>
                  </div>
                  <button className="bg-cyan-600 hover:bg-cyan-700 text-white px-4 py-2 rounded-md text-sm font-medium">
                    Gestionar
                  </button>
                </div>
              </div>
            </div>

            {/* Acciones Rápidas */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">🚀 Acciones Rápidas</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <button className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-md font-medium">
                  ➕ Agregar Nuevo Tipo
                </button>
                <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium">
                  📊 Exportar Catálogos
                </button>
                <button className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-md font-medium">
                  🔄 Sincronizar Catálogos
                </button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'auditoria' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-gray-900">
              🔒 Auditoría y Seguridad
            </h2>
            
            {/* Resumen de Seguridad */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-green-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">🔐</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Sesiones Activas</p>
                    <p className="text-2xl font-semibold text-gray-900">3</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-yellow-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">⚠️</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Intentos Fallidos</p>
                    <p className="text-2xl font-semibold text-gray-900">2</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-blue-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">📊</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Logs Hoy</p>
                    <p className="text-2xl font-semibold text-gray-900">156</p>
                  </div>
                </div>
              </div>

              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <div className="w-8 h-8 bg-red-500 rounded-md flex items-center justify-center">
                      <span className="text-white text-lg">🚨</span>
                    </div>
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-500">Alertas</p>
                    <p className="text-2xl font-semibold text-gray-900">0</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Logs de Auditoría */}
            <div className="bg-white rounded-lg shadow">
              <div className="px-6 py-4 border-b border-gray-200">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-semibold text-gray-900">📋 Logs de Auditoría</h3>
                  <div className="flex space-x-2">
                    <select className="border border-gray-300 rounded-md px-3 py-1 text-sm">
                      <option>Todos los eventos</option>
                      <option>Login/Logout</option>
                      <option>Creación</option>
                      <option>Modificación</option>
                      <option>Eliminación</option>
                    </select>
                    <button className="bg-blue-600 text-white px-3 py-1 rounded-md text-sm hover:bg-blue-700">
                      🔄 Actualizar
                    </button>
                  </div>
                </div>
              </div>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Fecha/Hora
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Usuario
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Acción
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Recurso
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        IP
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Estado
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    <tr className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        2025-09-04 19:35:16
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        admin@agrocloud.com
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                          LOGIN
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        /api/auth/login
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        127.0.0.1
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                          ✅ Éxito
                        </span>
                      </td>
                    </tr>
                    <tr className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        2025-09-04 19:30:45
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        tecnico@agrocloud.com
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                          CREATE
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        Insumo: Fertilizante NPK
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        192.168.1.100
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                          ✅ Éxito
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'reportes' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-gray-900">
              📈 Reportes Globales
            </h2>
            
            {/* Filtros de Reportes */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">🔍 Filtros de Reportes</h3>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Período</label>
                  <select className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option>Últimos 7 días</option>
                    <option>Últimos 30 días</option>
                    <option>Últimos 3 meses</option>
                    <option>Último año</option>
                    <option>Personalizado</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Tipo de Reporte</label>
                  <select className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option>Financiero</option>
                    <option>Productividad</option>
                    <option>Usuarios</option>
                    <option>Inventario</option>
                    <option>Labores</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Formato</label>
                  <select className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm">
                    <option>PDF</option>
                    <option>Excel</option>
                    <option>CSV</option>
                  </select>
                </div>
                <div className="flex items-end">
                  <button className="w-full bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700">
                    📊 Generar Reporte
                  </button>
                </div>
              </div>
            </div>

            {/* Reportes Disponibles */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {/* Reporte Financiero */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">💰 Reporte Financiero</h3>
                  <span className="text-xs text-gray-500">Actualizado hace 2 horas</span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Análisis completo de ingresos, egresos y balance general del sistema
                </p>
                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Total Ingresos:</span>
                    <span className="font-medium text-green-600">{formatCurrency(stats?.totalIngresos || 0)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Total Egresos:</span>
                    <span className="font-medium text-red-600">{formatCurrency(stats?.totalEgresos || 0)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Balance:</span>
                    <span className={`font-medium ${(stats?.balanceGeneral || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                      {formatCurrency(stats?.balanceGeneral || 0)}
                    </span>
                  </div>
                </div>
                <button className="w-full bg-green-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-green-700">
                  📄 Ver Reporte
                </button>
              </div>

              {/* Reporte de Productividad */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">📊 Reporte de Productividad</h3>
                  <span className="text-xs text-gray-500">Actualizado hace 1 hora</span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Estadísticas de uso del sistema y actividad de usuarios
                </p>
                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Usuarios Activos:</span>
                    <span className="font-medium text-blue-600">{stats?.usuariosActivos || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Labores Completadas:</span>
                    <span className="font-medium text-blue-600">{stats?.totalLabores || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Campos Activos:</span>
                    <span className="font-medium text-blue-600">{stats?.totalCampos || 0}</span>
                  </div>
                </div>
                <button className="w-full bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700">
                  📄 Ver Reporte
                </button>
              </div>

              {/* Reporte de Inventario */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-purple-500">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">📦 Reporte de Inventario</h3>
                  <span className="text-xs text-gray-500">Actualizado hace 30 min</span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Estado actual del inventario de insumos y maquinaria
                </p>
                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Total Insumos:</span>
                    <span className="font-medium text-purple-600">{stats?.totalInsumos || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Maquinaria Activa:</span>
                    <span className="font-medium text-purple-600">{stats?.totalMaquinaria || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Stock Bajo:</span>
                    <span className="font-medium text-red-600">3</span>
                  </div>
                </div>
                <button className="w-full bg-purple-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-purple-700">
                  📄 Ver Reporte
                </button>
              </div>

              {/* Reporte de Usuarios */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-yellow-500">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">👥 Reporte de Usuarios</h3>
                  <span className="text-xs text-gray-500">Actualizado hace 15 min</span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Análisis de usuarios, roles y actividad en el sistema
                </p>
                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Total Usuarios:</span>
                    <span className="font-medium text-yellow-600">{stats?.totalUsuarios || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Pendientes:</span>
                    <span className="font-medium text-yellow-600">{stats?.usuariosPendientes || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Suspendidos:</span>
                    <span className="font-medium text-red-600">{stats?.usuariosSuspendidos || 0}</span>
                  </div>
                </div>
                <button className="w-full bg-yellow-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-yellow-700">
                  📄 Ver Reporte
                </button>
              </div>

              {/* Reporte de Labores */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-red-500">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">⚒️ Reporte de Labores</h3>
                  <span className="text-xs text-gray-500">Actualizado hace 45 min</span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Resumen de labores agrícolas y su estado de ejecución
                </p>
                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Total Labores:</span>
                    <span className="font-medium text-red-600">{stats?.totalLabores || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Completadas:</span>
                    <span className="font-medium text-green-600">4</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Pendientes:</span>
                    <span className="font-medium text-yellow-600">2</span>
                  </div>
                </div>
                <button className="w-full bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-red-700">
                  📄 Ver Reporte
                </button>
              </div>

              {/* Reporte de Cultivos */}
              <div className="bg-white rounded-lg shadow p-6 border-l-4 border-teal-500">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">🌱 Reporte de Cultivos</h3>
                  <span className="text-xs text-gray-500">Actualizado hace 1 hora</span>
                </div>
                <p className="text-sm text-gray-600 mb-4">
                  Análisis de cultivos, rendimientos y estado de los lotes
                </p>
                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Total Cultivos:</span>
                    <span className="font-medium text-teal-600">{stats?.totalCultivos || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Lotes Activos:</span>
                    <span className="font-medium text-teal-600">{stats?.totalLotes || 0}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">En Crecimiento:</span>
                    <span className="font-medium text-green-600">3</span>
                  </div>
                </div>
                <button className="w-full bg-teal-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-teal-700">
                  📄 Ver Reporte
                </button>
              </div>
            </div>

            {/* Reportes Programados */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">⏰ Reportes Programados</h3>
              <div className="space-y-4">
                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="text-sm font-medium text-gray-900">Reporte Financiero Semanal</p>
                    <p className="text-xs text-gray-500">Se ejecuta todos los lunes a las 9:00 AM</p>
                  </div>
                  <div className="flex space-x-2">
                    <button className="text-blue-600 hover:text-blue-900 text-sm">✏️ Editar</button>
                    <button className="text-red-600 hover:text-red-900 text-sm">🗑️ Eliminar</button>
                  </div>
                </div>
                <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="text-sm font-medium text-gray-900">Reporte de Inventario Mensual</p>
                    <p className="text-xs text-gray-500">Se ejecuta el primer día de cada mes a las 8:00 AM</p>
                  </div>
                  <div className="flex space-x-2">
                    <button className="text-blue-600 hover:text-blue-900 text-sm">✏️ Editar</button>
                    <button className="text-red-600 hover:text-red-900 text-sm">🗑️ Eliminar</button>
                  </div>
                </div>
              </div>
              <button className="mt-4 bg-green-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-green-700">
                ➕ Programar Nuevo Reporte
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
