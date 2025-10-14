import React, { useState, useEffect, useMemo } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import FieldsManagement from './components/FieldsManagement';
import LotesManagement from './components/LotesManagement';
import CultivosManagement from './components/CultivosManagement';
import CosechasManagement from './components/CosechasManagement';
import InsumosManagement from './components/InsumosManagement';
import MaquinariaManagement from './components/MaquinariaManagement';
import LaboresManagement from './components/LaboresManagement';
import ReportsManagement from './components/ReportsManagement';
import BalanceReport from './components/BalanceReport';
import FinanzasManagement from './components/FinanzasManagement';
import InventarioGranosManagement from './components/InventarioGranosManagement';
import AdminUsuarios from './components/AdminUsuarios';
import AdminEmpresas from './components/AdminEmpresas';
import AdminDashboard from './components/AdminDashboard';
import AdminGlobalDashboard from './components/AdminGlobalDashboard';
import EmpresaSelector from './components/EmpresaSelector';
import OfflineIndicator from './components/OfflineIndicator';
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import ChangePasswordModal from './components/ChangePasswordModal';
import CurrencySelector from './components/CurrencySelector';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { EmpresaProvider, useEmpresa } from './contexts/EmpresaContext';
import { CurrencyProvider, useCurrencyContext } from './contexts/CurrencyContext';
import { useCurrencyUpdate } from './hooks/useCurrencyUpdate';
import { usePermissions } from './hooks/usePermissions';
import ProtectedRouteComponent from './components/ProtectedRoute';
import api, { authService } from './services/api';

// Dashboard con menú lateral
const Dashboard: React.FC = () => {
  const { user } = useAuth();
  
  // Verificar que el contexto de empresa esté disponible
  const empresaContext = useEmpresa();
  const { rolUsuario } = empresaContext || {}; // Obtener rol del contexto de empresa
  
  if (!empresaContext) {
    return <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600">Cargando contexto de empresa...</p>
      </div>
    </div>;
  }
  
  const { empresaActiva, esAdministrador, esAsesor, esOperario, esContador, esTecnico, esSoloLectura, tienePermisoFinanciero } = empresaContext;
  const { formatCurrency, selectedCurrency, exchangeType, rateInfo, realRates, changeCurrency, changeExchangeType } = useCurrencyContext();
  useCurrencyUpdate(); // Forzar actualización cuando cambie la moneda

  const [activePage, setActivePage] = useState('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [dashboardStats, setDashboardStats] = useState({
    campos: 0,
    lotes: 0,
    cultivos: 0,
    cosechas: 0,
    insumos: 0,
    maquinaria: 0,
    labores: 0,
    balance: 0,
    balanceOperativo: 0,
    balancePatrimonial: 0,
    totalIngresos: 0,
    totalEgresos: 0,
    valorActivos: 0
  });

  // Detectar cambios de tamaño de pantalla
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth <= 768);
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Detectar cambios de conectividad
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Actualizar tasas de cotización cada 30 segundos - DESHABILITADO TEMPORALMENTE
  // useEffect(() => {
  //   const updateRatesInterval = setInterval(async () => {
  //     try {
  //       await changeCurrency(selectedCurrency); // Esto disparará la actualización
  //     } catch (error) {
  //       console.error('Error actualizando tasas:', error);
  //     }
  //   }, 30000); // 30 segundos

  //   return () => clearInterval(updateRatesInterval);
  // }, [selectedCurrency, changeCurrency]);



  // Cargar estadísticas del dashboard
  useEffect(() => {
    const cargarEstadisticas = async () => {
      try {
        // El backend detectará automáticamente el rol del usuario basándose en el JWT token
        // Usar el endpoint que funciona correctamente
        
        // Usar el servicio de API que tiene configurado el interceptor de Axios
        const response = await api.get(`/api/v1/dashboard/estadisticas-auth`);
        const data = response.data;
        
        setDashboardStats({
          campos: data.campos || 0,
          lotes: data.lotes || 0,
          cultivos: data.cultivos || 0,
          cosechas: data.cosechas || 0,
          insumos: data.insumos || 0,
          maquinaria: data.maquinaria || 0,
          labores: data.labores || 0,
          balance: data.balance || 0,
          // Mapear campos del backend al frontend
          balanceOperativo: data.balanceOperativo || 0,
          balancePatrimonial: data.balancePatrimonial || 0,
          totalIngresos: data.totalIngresos || 0,
          totalEgresos: data.totalEgresos || 0,
          valorActivos: data.valorActivos || 0
        });
        
        console.log('✅ [Dashboard] Estadísticas cargadas exitosamente:', data);
      } catch (error) {
        console.error('❌ [Dashboard] Error cargando estadísticas:', error);
        // Usar valores por defecto si hay error
        setDashboardStats({
          campos: 0,
          lotes: 0,
          cultivos: 0,
          cosechas: 0,
          insumos: 0,
          maquinaria: 0,
          labores: 0,
          balance: 0,
          balanceOperativo: 0,
          balancePatrimonial: 0,
          totalIngresos: 0,
          totalEgresos: 0,
          valorActivos: 0
        });
      }
    };

    // Solo cargar estadísticas si el usuario está autenticado
    if (user && authService.isAuthenticated()) {
      cargarEstadisticas();
    }
  }, [user]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  };

  const renderPage = () => {
    console.log('🔍 [App] Renderizando página. Usuario:', user);
    console.log('🔍 [App] Rol del usuario:', user?.roleName);
    console.log('🔍 [App] Página activa:', activePage);
    
    // Solo SUPERADMIN puede ver AdminGlobalDashboard como dashboard principal
    if (user?.roleName === 'SUPERADMIN') {
      console.log('✅ [App] Usuario es SUPERADMIN, renderizando AdminGlobalDashboard');
      if (activePage === 'dashboard' || activePage === 'admin-global') {
        return <AdminGlobalDashboard />;
      }
    } else {
      console.log('❌ [App] Usuario NO es SUPERADMIN, rol:', user?.roleName);
    }
    
    // Los usuarios ADMINISTRADOR deben ver el dashboard normal, no el AdminDashboard
    // Solo SUPERADMIN tiene acceso al dashboard administrativo global

    switch (activePage) {
      case 'fields':
        return (
          <ProtectedRouteComponent permission="canViewFields">
            <FieldsManagement />
          </ProtectedRouteComponent>
        );
      case 'plots':
        return (
          <ProtectedRouteComponent permission="canViewLotes">
            <LotesManagement />
          </ProtectedRouteComponent>
        );
      case 'crops':
        return (
          <ProtectedRouteComponent permission="canViewCultivos">
            <CultivosManagement />
          </ProtectedRouteComponent>
        );
      case 'cosechas':
        return (
          <ProtectedRouteComponent permission="canViewCosechas">
            <CosechasManagement />
          </ProtectedRouteComponent>
        );
      case 'inputs':
        return (
          <ProtectedRouteComponent permission="canViewInsumos">
            <InsumosManagement />
          </ProtectedRouteComponent>
        );
      case 'machinery':
        return (
          <ProtectedRouteComponent permission="canViewMaquinaria">
            <MaquinariaManagement />
          </ProtectedRouteComponent>
        );
      case 'labors':
        return (
          <ProtectedRouteComponent permission="canViewLabores">
            <LaboresManagement />
          </ProtectedRouteComponent>
        );
      case 'balance':
        return (
          <ProtectedRouteComponent permission="canViewFinancialReports">
            <BalanceReport />
          </ProtectedRouteComponent>
        );
      case 'finanzas':
        return (
          <ProtectedRouteComponent permission="canViewFinances">
            <FinanzasManagement />
          </ProtectedRouteComponent>
        );
      case 'inventario':
        return (
          <ProtectedRouteComponent permission="canViewFinances">
            <InventarioGranosManagement />
          </ProtectedRouteComponent>
        );
      case 'reports':
        return (
          <ProtectedRouteComponent permission="canViewReports">
            <ReportsManagement />
          </ProtectedRouteComponent>
        );
      case 'users':
        return (
          <ProtectedRouteComponent permission="canManageUsers">
            <UserManagement key="admin-usuarios-stable" />
          </ProtectedRouteComponent>
        );
      case 'admin-empresas':
        return (
          <ProtectedRouteComponent permission="canManageCompanies">
            <AdminEmpresas />
          </ProtectedRouteComponent>
        );
      case 'dashboard':
      default:
  return (
          <div style={{ padding: '2rem' }}>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: '2rem'
        }}>
          <h1 style={{ 
            fontSize: '2rem', 
            fontWeight: 'bold', 
            color: '#1f2937'
          }}>
                📊 Dashboard
          </h1>
          
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '1rem'
          }}>
            {/* Selector de Empresa */}
            {empresaActiva && (
              <div style={{
                backgroundColor: 'white',
                borderRadius: '0.375rem',
                boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
                border: '1px solid #e5e7eb',
                padding: '0.5rem'
              }}>
                <EmpresaSelector />
              </div>
            )}
            
            {/* Selector de Moneda */}
            <div style={{
              backgroundColor: 'white',
              borderRadius: '0.375rem',
              boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
              border: '1px solid #e5e7eb',
              padding: '0.5rem'
            }}>
              <select
                value={selectedCurrency === 'ARS' ? 'ARS' : exchangeType}
                onChange={(event) => {
                  const value = event.target.value;
                  if (value === 'ARS') {
                    changeCurrency('ARS');
                  } else if (value === 'oficial' || value === 'blue') {
                    changeCurrency('USD');
                    changeExchangeType(value);
                  }
                  // Forzar actualización inmediata
                  setTimeout(() => {
                    window.dispatchEvent(new Event('currencyUpdate'));
                  }, 100);
                }}
                style={{
                  border: 'none',
                  outline: 'none',
                  fontSize: '0.875rem',
                  fontWeight: '500',
                  color: '#374151',
                  backgroundColor: 'transparent',
                  cursor: 'pointer',
                  minWidth: '180px'
                }}
              >
                <option value="ARS">💰 ARS (Pesos Argentinos)</option>
                <option value="oficial">
                  💵 USD Oficial {realRates?.oficial ? `($${realRates.oficial.toFixed(2)})` : ''}
                </option>
                <option value="blue">
                  💙 USD Blue {realRates?.blue ? `($${realRates.blue.toFixed(2)})` : ''}
                </option>
              </select>
            </div>
            
            {/* Botón Cerrar Sesión */}
            <button
              onClick={handleLogout}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#ef4444',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer'
              }}
            >
              Cerrar sesión
            </button>
          </div>
        </div>
        
        <div style={{ 
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
          color: 'white', 
          padding: '1.5rem', 
          borderRadius: '0.5rem', 
          marginBottom: '2rem' 
        }}>
          <h2 style={{ marginBottom: '0.5rem' }}>
            Bienvenido, {user?.name || 'Usuario'}!
          </h2>
          <p style={{ opacity: '0.9' }}>
            Rol: {rolUsuario ? rolUsuario.replace(/_/g, ' ').split(' ').map(word => word.charAt(0) + word.slice(1).toLowerCase()).join(' ') : 'Usuario'}
          </p>
        </div>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', 
          gap: '1.5rem' 
        }}>
          {/* Tarjetas Financieras - Solo para usuarios con permiso financiero */}
          {tienePermisoFinanciero() && (
            <>
              {/* Balance Operativo (Corto plazo) */}
              <div style={{ 
                background: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
                color: 'white',
                padding: '1.5rem',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                cursor: 'pointer',
                transition: 'transform 0.2s ease, box-shadow 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
              }}
              onClick={() => setActivePage('balance')}
              >
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>📊</span>
                  <div>
                    <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Balance Operativo</h3>
                    <p style={{ margin: 0, opacity: '0.9', fontSize: '1.2rem', fontWeight: 'bold' }}>{formatCurrency(dashboardStats.balanceOperativo)}</p>
                  </div>
                </div>
                <p style={{ opacity: '0.9', fontSize: '0.85rem', margin: 0 }}>Ingresos - Egresos (Flujo de caja)</p>
              </div>

              {/* Balance Patrimonial (Largo plazo) */}
              <div style={{ 
                background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                color: 'white',
                padding: '1.5rem',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                cursor: 'pointer',
                transition: 'transform 0.2s ease, box-shadow 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
              }}
              onClick={() => setActivePage('balance')}
              >
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>💰</span>
                  <div>
                    <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Balance Patrimonial</h3>
                    <p style={{ margin: 0, opacity: '0.9', fontSize: '1.2rem', fontWeight: 'bold' }}>{formatCurrency(dashboardStats.balancePatrimonial)}</p>
                  </div>
                </div>
                <p style={{ opacity: '0.9', fontSize: '0.85rem', margin: 0 }}>Incluye valor de activos</p>
              </div>

              {/* Desglose Financiero */}
              <div style={{ 
                background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
                color: 'white',
                padding: '1.5rem',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                cursor: 'pointer',
                transition: 'transform 0.2s ease, box-shadow 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
              }}
              onClick={() => setActivePage('finanzas')}
              >
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>📈</span>
                  <div>
                    <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Desglose Financiero</h3>
                    <div style={{ fontSize: '0.9rem', opacity: '0.9' }}>
                      <div>Ingresos: {formatCurrency(dashboardStats.totalIngresos)}</div>
                      <div>Egresos: {formatCurrency(dashboardStats.totalEgresos)}</div>
                      <div>Activos: {formatCurrency(dashboardStats.valorActivos)}</div>
                    </div>
                  </div>
                </div>
                <p style={{ opacity: '0.9', fontSize: '0.85rem', margin: 0 }}>Detalle de ingresos, egresos y activos</p>
              </div>
            </>
          )}

          {/* Campos */}
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('fields')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>🌾</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Campos</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.campos} registrados</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Administra tus terrenos y lotes</p>
          </div>

          {/* Lotes */}
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('plots')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>🔲</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Lotes</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.lotes} activos</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Gestiona las parcelas de cultivo</p>
          </div>

          {/* Cultivos */}
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('crops')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>🌱</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Cultivos</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.cultivos} en curso</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Control de plantaciones y variedades</p>
          </div>

          {/* Cosechas - DESHABILITADO: Ahora se gestiona desde Lotes
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('cosechas')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>🌾</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Cosechas</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.cosechas} registradas</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Registro de cosechas y rendimientos</p>
          </div> */}

          {/* Insumos */}
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('inputs')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>🧪</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Insumos</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.insumos} en inventario</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Gestión de fertilizantes y productos</p>
          </div>

          {/* Maquinaria */}
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('machinery')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>🚜</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Maquinaria</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.maquinaria} equipos</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Control de equipos y mantenimiento</p>
          </div>

          {/* Labores */}
          <div style={{ 
            backgroundColor: 'white', 
            padding: '1.5rem',
            borderRadius: '0.5rem',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
            cursor: 'pointer',
            transition: 'transform 0.2s ease, box-shadow 0.2s ease'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
          }}
          onClick={() => setActivePage('labors')}
          >
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
              <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>⚒️</span>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Labores</h3>
                <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>{dashboardStats.labores} tareas</p>
              </div>
            </div>
            <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Seguimiento de tareas agrícolas</p>
          </div>

          {/* Finanzas - Solo para usuarios con permiso financiero */}
          {tienePermisoFinanciero() && (
            <div style={{ 
              backgroundColor: 'white', 
              padding: '1.5rem',
              borderRadius: '0.5rem',
              boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
              cursor: 'pointer',
              transition: 'transform 0.2s ease, box-shadow 0.2s ease'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-2px)';
              e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
            }}
            onClick={() => setActivePage('finanzas')}
            >
              <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                <span style={{ fontSize: '2rem', marginRight: '0.75rem' }}>💳</span>
                <div>
                  <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.1rem' }}>Finanzas</h3>
                  <p style={{ margin: 0, color: '#6b7280', fontSize: '0.9rem' }}>Control completo</p>
                </div>
              </div>
              <p style={{ color: '#6b7280', fontSize: '0.85rem', margin: 0 }}>Control de ingresos y egresos</p>
            </div>
          )}
        </div>
          </div>
        );
    }
  };

  // Componente Sidebar
  const Sidebar: React.FC<{
    activePage: string;
    onPageChange: (page: string) => void;
    user: any;
    isMobile: boolean;
    sidebarOpen: boolean;
    onToggleSidebar: () => void;
  }> = ({ activePage, onPageChange, user, isMobile, sidebarOpen, onToggleSidebar }) => {
    const permissions = usePermissions();
    // Persistir estado del menú en localStorage para evitar reinicios
  const [expandedGroups, setExpandedGroups] = useState<string[]>(() => {
    const saved = localStorage.getItem('sidebar-expanded-groups');
    return saved ? JSON.parse(saved) : ['produccion', 'recursos', 'finanzas', 'admin'];
  });
    
    // Solo SUPERADMIN puede ver el Dashboard Administrador
    const isSuperAdmin = user?.roleName === 'SUPERADMIN';
    const isAdministrador = user?.roleName === 'ADMINISTRADOR';
    
    const toggleGroup = (groupId: string) => {
      setExpandedGroups(prev => {
        const newGroups = prev.includes(groupId) 
          ? prev.filter(id => id !== groupId)
          : [...prev, groupId];
        
        // Guardar en localStorage para persistir el estado
        localStorage.setItem('sidebar-expanded-groups', JSON.stringify(newGroups));
        return newGroups;
      });
    };
    
    // Configurar menú agrupado según el rol del usuario (memoizado para evitar re-renders)
    const menuStructure = useMemo(() => {
      if (isSuperAdmin) {
      // Dashboard Administrador Global - Solo para SUPERADMIN
      return {
        singleItems: [
          { id: 'dashboard', label: 'Dashboard Admin Global', icon: '📊', permission: null }
        ],
        groups: [
          {
            id: 'admin',
            label: 'Administración',
            icon: '⚙️',
            items: [
              { id: 'users', label: 'Usuarios', icon: '👥', permission: 'canManageUsers' },
              { id: 'admin-empresas', label: 'Empresas', icon: '🏢', permission: 'canManageCompanies' }
            ]
          }
        ]
      };
    } else {
      // Dashboard normal para todos los demás usuarios
      const adminItems = [];
      if (isAdministrador) {
        adminItems.push({ id: 'users', label: 'Usuarios', icon: '👥', permission: 'canManageUsers' });
      }
      
      const menuStructure = {
        singleItems: [
          { id: 'dashboard', label: 'Dashboard', icon: '📊', permission: null }
        ],
        groups: [
          {
            id: 'produccion',
            label: 'Producción',
            icon: '🌱',
            items: [
              { id: 'fields', label: 'Campos', icon: '🌾', permission: 'canViewFields' },
              { id: 'plots', label: 'Lotes', icon: '🔲', permission: 'canViewLotes' },
              { id: 'crops', label: 'Cultivos', icon: '🌱', permission: 'canViewCultivos' },
              { id: 'labors', label: 'Labores', icon: '⚒️', permission: 'canViewLabores' }
              // { id: 'cosechas', label: 'Cosechas', icon: '🌾', permission: 'canViewCosechas' } // Ahora se gestiona desde Lotes
            ]
          },
          {
            id: 'recursos',
            label: 'Recursos & Stock',
            icon: '📦',
            items: [
              { id: 'inputs', label: 'Insumos', icon: '🧪', permission: 'canViewInsumos' },
              { id: 'machinery', label: 'Maquinaria', icon: '🚜', permission: 'canViewMaquinaria' },
              { id: 'inventario', label: 'Inventario Granos', icon: '📦', permission: 'canViewFinances' }
            ]
          },
          {
            id: 'reportes',
            label: 'Reportes y Análisis',
            icon: '📊',
            items: [
              { id: 'reports', label: 'Reportes Operativos', icon: '📋', permission: 'canViewReports' }
            ]
          },
          {
            id: 'finanzas',
            label: 'Gestión Financiera',
            icon: '💰',
            items: [
              { id: 'finanzas', label: 'Finanzas', icon: '💳', permission: 'canViewFinances' },
              { id: 'balance', label: 'Balance', icon: '💰', permission: 'canViewFinancialReports' }
            ]
          }
        ]
      };
      
      // Agregar grupo de Administración solo si hay items
      if (adminItems.length > 0) {
        menuStructure.groups.push({
          id: 'admin',
          label: 'Administración',
          icon: '⚙️',
          items: adminItems
        });
      }
      
      return menuStructure;
      }
    }, [isSuperAdmin, isAdministrador]);
    
    // Filtrar items según permisos
    const filterItemsByPermissions = (items: any[]) => {
      return items.filter(item => !item.permission || permissions[item.permission as keyof typeof permissions]);
    };
    
    const filteredSingleItems = filterItemsByPermissions(menuStructure.singleItems);
    const filteredGroups = menuStructure.groups
      .map(group => ({
        ...group,
        items: filterItemsByPermissions(group.items)
      }))
      .filter(group => group.items.length > 0);

    return (
      <div style={{
        position: 'fixed',
        left: 0,
        top: 0,
        height: '100vh',
        width: '250px',
        backgroundColor: '#1f2937',
        color: 'white',
        transform: isMobile && !sidebarOpen ? 'translateX(-100%)' : 'translateX(0)',
        transition: 'transform 0.3s ease',
        zIndex: 1000,
        overflowY: 'auto'
      }}>
        <div style={{ padding: '1.5rem' }}>
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            marginBottom: '2rem',
            fontSize: '1.25rem',
            fontWeight: 'bold'
          }}>
            🌾 AgroCloud
          </div>
          
          <div style={{ marginBottom: '2rem' }}>
            <div style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '0.5rem' }}>
              Perfil
            </div>
            <div style={{ fontSize: '1rem', fontWeight: '500' }}>
              {user?.name || 'Usuario'}
            </div>
            <div style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '1rem' }}>
              {rolUsuario ? rolUsuario.replace(/_/g, ' ').split(' ').map(word => word.charAt(0) + word.slice(1).toLowerCase()).join(' ') : 'Usuario'}
            </div>
            <button
              onClick={() => setShowChangePassword(true)}
              style={{
                width: '100%',
                padding: '0.5rem',
                backgroundColor: '#374151',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontSize: '0.75rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.25rem'
              }}
            >
              🔐 Cambiar Contraseña
            </button>
          </div>
          
          <nav>
            {/* Items individuales (Dashboard) */}
            {filteredSingleItems.map((item) => (
              <button
                key={item.id}
                onClick={() => {
                  onPageChange(item.id);
                  if (isMobile) onToggleSidebar();
                }}
                data-testid={`nav-${item.id}`}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  marginBottom: '0.5rem',
                  backgroundColor: activePage === item.id ? '#374151' : 'transparent',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                  textAlign: 'left',
                  fontSize: '0.875rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  fontWeight: activePage === item.id ? '600' : '400'
                }}
              >
                <span>{item.icon}</span>
                {item.label}
              </button>
            ))}
            
            {/* Grupos colapsables */}
            {filteredGroups.map((group) => (
              <div key={group.id} style={{ marginBottom: '0.5rem' }}>
                {/* Header del grupo */}
                <button
                  onClick={() => toggleGroup(group.id)}
                  style={{
                    width: '100%',
                    padding: '0.75rem 1rem',
                    backgroundColor: '#374151',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    textAlign: 'left',
                    fontSize: '0.875rem',
                    fontWeight: '600',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span>{group.icon}</span>
                    {group.label}
                  </div>
                  <span style={{ 
                    transform: expandedGroups.includes(group.id) ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.2s ease',
                    fontSize: '0.75rem'
                  }}>
                    ▼
                  </span>
                </button>
                
                {/* Items del grupo */}
                {expandedGroups.includes(group.id) && (
                  <div style={{ 
                    paddingLeft: '0.5rem',
                    marginTop: '0.25rem'
                  }}>
                    {group.items.map((item) => (
                      <button
                        key={item.id}
                        onClick={() => {
                          onPageChange(item.id);
                          if (isMobile) onToggleSidebar();
                        }}
                        data-testid={`nav-${item.id}`}
                        style={{
                          width: '100%',
                          padding: '0.625rem 1rem',
                          marginBottom: '0.25rem',
                          backgroundColor: activePage === item.id ? '#4b5563' : 'transparent',
                          color: activePage === item.id ? 'white' : '#d1d5db',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          textAlign: 'left',
                          fontSize: '0.8125rem',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.5rem',
                          transition: 'all 0.15s ease'
                        }}
                        onMouseEnter={(e) => {
                          if (activePage !== item.id) {
                            e.currentTarget.style.backgroundColor = '#374151';
                            e.currentTarget.style.color = 'white';
                          }
                        }}
                        onMouseLeave={(e) => {
                          if (activePage !== item.id) {
                            e.currentTarget.style.backgroundColor = 'transparent';
                            e.currentTarget.style.color = '#d1d5db';
                          }
                        }}
                      >
                        <span>{item.icon}</span>
                        {item.label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </nav>
        </div>
      </div>
    );
  };

  // Componente UserManagement
  const UserManagement: React.FC = () => {
    return <AdminUsuarios />;
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Overlay para móvil */}
      {isMobile && sidebarOpen && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            zIndex: 999
          }}
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <Sidebar 
        activePage={activePage} 
        onPageChange={setActivePage} 
        user={user}
        isMobile={isMobile}
        sidebarOpen={sidebarOpen}
        onToggleSidebar={() => setSidebarOpen(!sidebarOpen)}
      />
      
      <div style={{ 
        marginLeft: isMobile ? 0 : '250px', 
        flex: 1,
        transition: 'margin-left 0.3s ease'
      }}>
        {/* Botón de menú para móvil */}
        {isMobile && (
          <button
            onClick={() => setSidebarOpen(true)}
            style={{
              position: 'fixed',
              top: '1rem',
              left: '1rem',
              zIndex: 1001,
              padding: '0.5rem',
              backgroundColor: '#1f2937',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer'
            }}
          >
            ☰
          </button>
        )}

        {/* Contenido principal */}
        <div style={{ paddingTop: isMobile ? '4rem' : '0' }}>
          {renderPage()}
        </div>

        {/* Modal de cambio de contraseña */}
        <ChangePasswordModal 
          isOpen={showChangePassword} 
          onClose={() => setShowChangePassword(false)} 
        />

        {/* Selector de moneda integrado en la cabecera */}
      </div>
      
      {/* Indicador de estado offline */}
      <OfflineIndicator />
        
        {/* Banner de estado offline */}
        {!isOnline && (
          <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            backgroundColor: '#fef3c7',
            color: '#92400e',
            padding: '0.5rem',
            textAlign: 'center',
            fontSize: '0.875rem',
            zIndex: 1000,
            borderBottom: '1px solid #f59e0b'
          }}>
            📡 Modo Offline - Trabajando con datos locales
          </div>
        )}
    </div>
  );
};

// Componente para rutas protegidas
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = !!localStorage.getItem('token');
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

// Componente para rutas públicas
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = !!localStorage.getItem('token');
  
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  
  return <>{children}</>;
};

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route path="/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
      <Route path="/reset-password" element={<PublicRoute><ResetPassword /></PublicRoute>} />
      <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <AuthProvider>
      <EmpresaProvider>
        <CurrencyProvider>
          <Router>
            <AppRoutes />
          </Router>
        </CurrencyProvider>
      </EmpresaProvider>
    </AuthProvider>
  );
};

export default App;
