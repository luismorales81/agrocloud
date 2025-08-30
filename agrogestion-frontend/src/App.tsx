import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import FieldsManagement from './components/FieldsManagement';
import LotesManagement from './components/LotesManagement';
import CultivosManagement from './components/CultivosManagement';
import InsumosManagement from './components/InsumosManagement';
import MaquinariaManagement from './components/MaquinariaManagement';
import LaboresManagement from './components/LaboresManagement';
import ReportsManagement from './components/ReportsManagement';
import BalanceReport from './components/BalanceReport';
import FinanzasManagement from './components/FinanzasManagement';
import OfflineIndicator from './components/OfflineIndicator';
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import ChangePasswordModal from './components/ChangePasswordModal';
import { AuthProvider, useAuth } from './contexts/AuthContext';

// Dashboard con menú lateral
const Dashboard: React.FC = () => {
  const { user } = useAuth();

  const [activePage, setActivePage] = useState('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [dashboardStats, setDashboardStats] = useState({
    campos: 0,
    lotes: 0,
    cultivos: 0,
    insumos: 0,
    maquinaria: 0,
    labores: 0,
    balance: 0
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



  // Cargar estadísticas del dashboard
  useEffect(() => {
    // Simular carga de datos reales
    setDashboardStats({
      campos: 3,
      lotes: 12,
      cultivos: 5,
      insumos: 8,
      maquinaria: 4,
      labores: 15,
      balance: 125000
    });
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  };

  const renderPage = () => {
    switch (activePage) {
      case 'fields':
        return <FieldsManagement />;
      case 'plots':
        return <LotesManagement />;
      case 'crops':
        return <CultivosManagement />;
      case 'inputs':
        return <InsumosManagement />;
      case 'machinery':
        return <MaquinariaManagement />;
      case 'labors':
        return <LaboresManagement />;
      case 'balance':
        return <BalanceReport />;
      case 'finanzas':
        return <FinanzasManagement />;
      case 'reports':
        return <ReportsManagement />;
      case 'users':
        return <UserManagement />;
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
            Rol: {user?.roleName ? user.roleName.charAt(0).toUpperCase() + user.roleName.slice(1) : 'Usuario'}
          </p>
        </div>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', 
          gap: '1.5rem' 
        }}>
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

          {/* Balance Total */}
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
                <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Balance Total</h3>
                <p style={{ margin: 0, opacity: '0.9', fontSize: '1.2rem', fontWeight: 'bold' }}>${dashboardStats.balance.toLocaleString()}</p>
              </div>
            </div>
            <p style={{ opacity: '0.9', fontSize: '0.85rem', margin: 0 }}>Resumen financiero del sistema</p>
          </div>

          {/* Finanzas */}
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
    const menuItems = [
      { id: 'dashboard', label: 'Dashboard', icon: '📊' },
      { id: 'fields', label: 'Campos', icon: '🌾' },
      { id: 'plots', label: 'Lotes', icon: '🔲' },
      { id: 'crops', label: 'Cultivos', icon: '🌱' },
      { id: 'inputs', label: 'Insumos', icon: '🧪' },
      { id: 'machinery', label: 'Maquinaria', icon: '🚜' },
      { id: 'labors', label: 'Labores', icon: '⚒️' },
      { id: 'balance', label: 'Balance', icon: '💰' },
      { id: 'finanzas', label: 'Finanzas', icon: '💳' },
      { id: 'reports', label: 'Reportes', icon: '📋' },
      { id: 'users', label: 'Usuarios', icon: '👥' }
    ];

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
            🌾 AgroGestion
          </div>
          
          <div style={{ marginBottom: '2rem' }}>
            <div style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '0.5rem' }}>
              Perfil
            </div>
            <div style={{ fontSize: '1rem', fontWeight: '500' }}>
              {user?.name || 'Usuario'}
            </div>
            <div style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '1rem' }}>
              {user?.roleName ? user.roleName.charAt(0).toUpperCase() + user.roleName.slice(1) : 'Usuario'}
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
            {menuItems.map((item) => (
              <button
                key={item.id}
                onClick={() => {
                  onPageChange(item.id);
                  if (isMobile) onToggleSidebar();
                }}
            style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  marginBottom: '0.25rem',
                  backgroundColor: activePage === item.id ? '#374151' : 'transparent',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
              cursor: 'pointer',
                  textAlign: 'left',
                  fontSize: '0.875rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem'
                }}
              >
                <span>{item.icon}</span>
                {item.label}
              </button>
            ))}
          </nav>
        </div>
      </div>
    );
  };

  // Componente UserManagement
  const UserManagement: React.FC = () => {
    return (
      <div style={{ padding: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '2rem' }}>
          👥 Gestión de Usuarios
        </h1>
        <div style={{ 
          backgroundColor: 'white', 
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)' 
        }}>
          <p style={{ color: '#6b7280' }}>Funcionalidad de gestión de usuarios en desarrollo...</p>
        </div>
          </div>
        );
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
    <Router>
      <AppRoutes />
    </Router>
    </AuthProvider>
  );
};

export default App;
