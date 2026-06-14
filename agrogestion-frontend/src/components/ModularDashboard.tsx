import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useModule } from '../core/hooks/useModule';
import { ModuleId } from '../core/types/module.types';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import ModularSidebar from './ModularSidebar';
import AppNavigator from '../navigation/AppNavigator';
import ChangePasswordModal from './ChangePasswordModal';
import ModuleSelectorScreen from '../screens/ModuleSelectorScreen';

/**
 * Dashboard modular que cambia según el módulo activo
 */
const ModularDashboard: React.FC = () => {
  const { currentModule, loading, setCurrentModule } = useModule();
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  // Detectar el módulo desde la URL si no está establecido en el contexto
  useEffect(() => {
    if (!loading) {
      const pathParts = location.pathname.split('/').filter(Boolean);
      const modulosPorUrl: ModuleId[] = [
        'cultivos',
        'porcinos',
        'avicola-crianza',
        'avicola-huevos',
        'avicola-carne',
        'avicola-ponedoras',
      ];
      const moduleFromUrl =
        pathParts.length > 0 && modulosPorUrl.includes(pathParts[0] as ModuleId)
          ? (pathParts[0] as ModuleId)
          : null;
      
      if (moduleFromUrl && moduleFromUrl !== currentModule) {
        console.log('ModularDashboard: Detectando módulo desde URL', moduleFromUrl);
        setCurrentModule(moduleFromUrl);
      } else if (!currentModule && !moduleFromUrl && !location.pathname.startsWith('/select-module')) {
        // Si no hay módulo en la URL ni en el contexto, redirigir al selector
        console.log('ModularDashboard: Redirigiendo a selector de módulos', { currentModule, pathname: location.pathname });
        navigate('/select-module', { replace: true });
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [loading, currentModule, location.pathname]);

  // Redirigir al dashboard del módulo si estamos en /dashboard y hay un módulo seleccionado
  useEffect(() => {
    if (!loading && currentModule && (location.pathname === '/dashboard' || location.pathname === '/')) {
      navigate(`/${currentModule}/dashboard`, { replace: true });
    }
  }, [loading, currentModule, location.pathname, navigate]);

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

  // Si no hay módulo seleccionado y no está cargando, mostrar selector o esperar
  if (!loading && !currentModule) {
    const pathParts = location.pathname.split('/').filter(Boolean);
    const modulosPorUrl: ModuleId[] = [
      'cultivos',
      'porcinos',
      'avicola-crianza',
      'avicola-huevos',
      'avicola-carne',
      'avicola-ponedoras',
    ];
    const isModuleRoute = pathParts.length > 0 && modulosPorUrl.includes(pathParts[0] as ModuleId);
    
    if (!isModuleRoute && !location.pathname.startsWith('/select-module')) {
      // Redirigir al selector solo si no estamos en una ruta de módulo
      return <ModuleSelectorScreen />;
    }
    
    // Si estamos en una ruta de módulo pero currentModule aún no está establecido, mostrar loading
    if (isModuleRoute) {
      return (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '100vh',
            backgroundColor: '#f3f4f6',
          }}
        >
          <div style={{ textAlign: 'center' }}>
            <div
              style={{
                width: '64px',
                height: '64px',
                border: '4px solid #e5e7eb',
                borderTopColor: '#3b82f6',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite',
                margin: '0 auto 1rem',
              }}
            ></div>
            <p style={{ color: '#6b7280', fontSize: '1rem' }}>Cargando módulo...</p>
          </div>
          <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        </div>
      );
    }
  }

  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          backgroundColor: '#f3f4f6',
        }}
      >
        <div style={{ textAlign: 'center' }}>
          <div
            style={{
              width: '64px',
              height: '64px',
              border: '4px solid #e5e7eb',
              borderTopColor: '#3b82f6',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 1rem',
            }}
          ></div>
          <p style={{ color: '#6b7280', fontSize: '1rem' }}>Cargando módulo...</p>
        </div>
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

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
            zIndex: 999,
          }}
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar modular */}
      <ModularSidebar
        isMobile={isMobile}
        sidebarOpen={sidebarOpen}
        onToggleSidebar={() => setSidebarOpen(!sidebarOpen)}
        onShowChangePassword={() => setShowChangePassword(true)}
      />

      {/* Contenido principal */}
      <div
        style={{
          marginLeft: isMobile ? 0 : '250px',
          flex: 1,
          transition: 'margin-left 0.3s ease',
        }}
      >
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
              cursor: 'pointer',
            }}
          >
            ☰
          </button>
        )}

        {/* Navegador modular */}
        <div style={{ paddingTop: isMobile ? '4rem' : '0' }}>
          <AppNavigator />
        </div>

        {/* Modal de cambio de contraseña */}
        <ChangePasswordModal
          isOpen={showChangePassword}
          onClose={() => setShowChangePassword(false)}
        />
      </div>

      {/* Banner de estado offline */}
      {!isOnline && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: isMobile ? 0 : '250px',
            right: 0,
            backgroundColor: '#fef3c7',
            color: '#92400e',
            padding: '0.5rem',
            textAlign: 'center',
            fontSize: '0.875rem',
            zIndex: 1000,
            borderBottom: '1px solid #f59e0b',
          }}
        >
          📡 Modo Offline - Trabajando con datos locales
        </div>
      )}
    </div>
  );
};

export default ModularDashboard;

