import React, { useState, useEffect, useMemo } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import FieldsManagement from './components/FieldsManagement';
import LotesManagement from './components/LotesManagement';
import CultivosManagement from './components/CultivosManagement';
import CosechasManagement from './components/CosechasManagement';
import InsumosManagement from './components/InsumosManagement';
import MaquinariaManagement from './components/MaquinariaManagement';
import LaboresManagement from './components/LaboresManagement';
import AplicacionesAgroquimicas from './components/AplicacionesAgroquimicas';
import AgroquimicosIntegrados from './components/AgroquimicosIntegrados';
import InsumosUnificados from './components/InsumosUnificados';
import ReportsManagement from './components/ReportsManagement';
import BalanceReport from './components/BalanceReport';
import FinanzasManagement from './components/FinanzasManagement';
import InventarioGranosManagement from './components/InventarioGranosManagement';
import AdminUsuarios from './components/AdminUsuarios';
import AdminEmpresas from './components/AdminEmpresas';
import AdminDashboard from './components/AdminDashboard';
import AdminGlobalDashboard from './components/AdminGlobalDashboard';
import AyudaSistema from './components/AyudaSistema';
import EmpresaSelector from './components/EmpresaSelector';
import OfflineIndicator from './components/OfflineIndicator';
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import ChangePasswordModal from './components/ChangePasswordModal';
import CurrencySelector from './components/CurrencySelector';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { EmpresaProvider, useEmpresa } from './contexts/EmpresaContext';
import { CampanaProvider } from './contexts/CampanaContext';
import { CurrencyProvider, useCurrencyContext } from './contexts/CurrencyContext';
import { ModuleProvider } from './core/context/ModuleContext';
import { useCurrencyUpdate } from './hooks/useCurrencyUpdate';
import { usePermissions } from './hooks/usePermissions';
import ProtectedRouteComponent from './components/ProtectedRoute';
import CalendarioDashboard from './components/CalendarioDashboard';
import ModularDashboard from './components/ModularDashboard';
import ModuleSelectorScreen from './screens/ModuleSelectorScreen';
import api, { authService } from './services/api';
import muiTheme from './theme/muiTheme';
import { Icon } from './components/icons';

// Dashboard con menú lateral
const Dashboard: React.FC = () => {
  const { user } = useAuth();
  
  const empresaContext = useEmpresa();
  const { rolUsuario, empresaActiva, esAdministrador, esAsesor, esOperario, esContador, esTecnico, esSoloLectura, tienePermisoFinanciero } = empresaContext;
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
        const response = await api.get(`/v1/dashboard/estadisticas-auth`);
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
      } catch (error) {
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
    // Solo SUPERADMIN puede ver AdminGlobalDashboard como dashboard principal
    if (user?.roleName === 'SUPERADMIN') {
      if (activePage === 'dashboard' || activePage === 'admin-global') {
        return <AdminGlobalDashboard />;
      }
    } else {
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
      case 'insumos-unificados':
        return (
          <ProtectedRouteComponent permission="canViewInsumos">
            <InsumosUnificados />
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
      case 'ayuda':
        return (
          <ProtectedRouteComponent permission="canManageUsers">
            <AyudaSistema />
          </ProtectedRouteComponent>
        );
      case 'dashboard':
      default:
        return <CalendarioDashboard />;
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
              { id: 'admin-empresas', label: 'Empresas', icon: '🏢', permission: 'canManageCompanies' },
              { id: 'ayuda', label: 'Ayuda', icon: '📚', permission: 'canManageUsers' }
            ]
          }
        ]
      };
    } else {
      // Dashboard normal para todos los demás usuarios
      const adminItems = [];
      if (isAdministrador) {
        adminItems.push({ id: 'users', label: 'Usuarios', icon: '👥', permission: 'canManageUsers' });
        adminItems.push({ id: 'ayuda', label: 'Ayuda', icon: '📚', permission: 'canManageUsers' });
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
            { id: 'insumos-unificados', label: 'Insumos & Agroquímicos', icon: '?', permission: 'canViewInsumos' },
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
            {/* Indicador de estado de conexión */}
            <OfflineIndicator variant="inline" />
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
            <Icon name="Menu" size={24} color="white" />
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
            <Icon name="WifiOff" size={16} style={{ marginRight: '0.5rem', display: 'inline' }} /> Modo Offline - Trabajando con datos locales
          </div>
        )}
    </div>
  );
};

// Componente para rutas protegidas
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = !!localStorage.getItem('user');
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

// Componente para rutas públicas
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = !!localStorage.getItem('user');
  
  if (isAuthenticated) {
    return <Navigate to="/select-module" replace />;
  }
  
  return <>{children}</>;
};

const RedirigirAvicolaCarneACrianza: React.FC = () => {
  const location = useLocation();
  const destino =
    location.pathname.replace(/^\/avicola-carne/, '/avicola-crianza') +
    location.search +
    location.hash;
  return <Navigate to={destino} replace />;
};

const AppRoutes: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route path="/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
      <Route path="/reset-password" element={<PublicRoute><ResetPassword /></PublicRoute>} />
      <Route path="/select-module" element={<ProtectedRoute><ModuleSelectorScreen /></ProtectedRoute>} />
      <Route path="/dashboard/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/cultivos/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/porcinos/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/avicola-crianza/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/avicola-huevos/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/avicola-carne/*" element={<ProtectedRoute><RedirigirAvicolaCarneACrianza /></ProtectedRoute>} />
      <Route path="/avicola-ponedoras/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/feedlot/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      <Route path="/lecheria/*" element={<ProtectedRoute><ModularDashboard /></ProtectedRoute>} />
      {/* Mantener ruta legacy para compatibilidad */}
      <Route path="/dashboard-legacy" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <ThemeProvider theme={muiTheme}>
      <CssBaseline />
      <AuthProvider>
        <EmpresaProvider>
          <CampanaProvider>
          <CurrencyProvider>
            <ModuleProvider>
              <Router>
                <AppRoutes />
              </Router>
            </ModuleProvider>
          </CurrencyProvider>
          </CampanaProvider>
        </EmpresaProvider>
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
