import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useModule } from '../core/hooks/useModule';
import { getModuleMenu } from '../modules';
import { usePermissions } from '../hooks/usePermissions';
import OfflineIndicator from './OfflineIndicator';
import { useEmpresa } from '../contexts/EmpresaContext';
import { Icon } from './icons';

interface ModularSidebarProps {
  isMobile: boolean;
  sidebarOpen: boolean;
  onToggleSidebar: () => void;
  onShowChangePassword: () => void;
}

/**
 * Sidebar dinámico que cambia según el módulo activo
 */
const ModularSidebar: React.FC<ModularSidebarProps> = ({
  isMobile,
  sidebarOpen,
  onToggleSidebar,
  onShowChangePassword,
}) => {
  const { currentModule, moduleInfo } = useModule();
  const { rolUsuario } = useEmpresa();
  const navigate = useNavigate();
  const location = useLocation();
  const permissions = usePermissions();

  const [expandedGroups, setExpandedGroups] = useState<string[]>(() => {
    const saved = localStorage.getItem(`sidebar-expanded-${currentModule}`);
    return saved ? JSON.parse(saved) : [];
  });

  if (!currentModule || !moduleInfo) {
    console.log('ModularSidebar: No hay módulo o moduleInfo', { currentModule, moduleInfo });
    return (
      <div style={{
        position: 'fixed',
        left: 0,
        top: 0,
        height: '100vh',
        width: '250px',
        backgroundColor: '#1f2937',
        color: 'white',
        padding: '1.5rem',
        zIndex: 1000,
      }}>
        <p style={{ color: 'white' }}>Cargando módulo...</p>
      </div>
    );
  }

  const menuItems = getModuleMenu(currentModule);
  console.log('ModularSidebar: menuItems', menuItems, 'currentModule', currentModule);

  const toggleGroup = (groupId: string) => {
    setExpandedGroups((prev) => {
      const newExpanded = prev.includes(groupId)
        ? prev.filter((id) => id !== groupId)
        : [...prev, groupId];
      localStorage.setItem(`sidebar-expanded-${currentModule}`, JSON.stringify(newExpanded));
      return newExpanded;
    });
  };

  // Filtrar items según permisos
  const filteredItems = menuItems.filter(
    (item) => !item.permisos || item.permisos.some((perm) => permissions[perm as keyof typeof permissions])
  );

  console.log('ModularSidebar: filteredItems', filteredItems, 'menuItems', menuItems, 'permissions', permissions);

  const isActive = (ruta: string) => {
    return location.pathname === ruta || location.pathname.startsWith(ruta + '/');
  };

  const handleNavigation = (ruta: string) => {
    navigate(ruta);
    if (isMobile) {
      onToggleSidebar();
    }
  };

  // Asegurar que cuando se carga el sidebar, si estamos en /dashboard, redirigir al dashboard del módulo
  useEffect(() => {
    if (currentModule && location.pathname === '/dashboard') {
      navigate(`/${currentModule}/dashboard`, { replace: true });
    }
  }, [currentModule, location.pathname, navigate]);

  // Debug: Log cuando cambia el módulo
  useEffect(() => {
    console.log('ModularSidebar: useEffect - currentModule cambió', { currentModule, moduleInfo, location: location.pathname });
  }, [currentModule, moduleInfo]);

  return (
    <div
      style={{
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
        overflowY: 'auto',
      }}
    >
      <div style={{ 
        padding: '1.5rem',
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
      }}>
        {/* Header con selector de módulo */}
        <div style={{ marginBottom: '2rem', flexShrink: 0 }}>
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '0.25rem',
              marginBottom: '1rem',
            }}
          >
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '1.25rem',
                fontWeight: 'bold',
              }}
            >
              {typeof moduleInfo.icono === 'string' && /^[A-Z]/.test(moduleInfo.icono) ? (
                <Icon name={moduleInfo.icono as any} size={24} color="white" />
              ) : (
                <span>{moduleInfo.icono}</span>
              )}
              <span>Agrocloud</span>
            </div>
            <div
              style={{
                fontSize: '0.875rem',
                color: '#9ca3af',
                paddingLeft: '1.75rem',
              }}
            >
              {moduleInfo.nombre}
            </div>
          </div>
          <button
            onClick={() => {
              navigate('/select-module');
              if (isMobile) onToggleSidebar();
            }}
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
              gap: '0.25rem',
            }}
          >
            <Icon name="RefreshCw" size={16} style={{ marginRight: '0.25rem' }} /> Cambiar Módulo
          </button>
        </div>

        {/* Información del usuario */}
        <div style={{ marginBottom: '2rem', flexShrink: 0 }}>
          <div style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '0.5rem' }}>
            Perfil
          </div>
          <div style={{ fontSize: '1rem', fontWeight: '500' }}>
            {localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user') || '{}').name || 'Usuario' : 'Usuario'}
          </div>
          <div style={{ fontSize: '0.875rem', color: '#9ca3af', marginBottom: '1rem' }}>
            {rolUsuario
              ? rolUsuario
                  .replace(/_/g, ' ')
                  .split(' ')
                  .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
                  .join(' ')
              : 'Usuario'}
          </div>
          <OfflineIndicator variant="inline" />
          <button
            onClick={onShowChangePassword}
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
              gap: '0.25rem',
              marginTop: '0.5rem',
            }}
          >
            <Icon name="Lock" size={16} style={{ marginRight: '0.25rem' }} /> Cambiar Contraseña
          </button>
        </div>

        {/* Menú del módulo */}
        <nav style={{ 
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          overflowY: 'auto',
        }}>
          {filteredItems.length === 0 ? (
            <div style={{ color: '#9ca3af', fontSize: '0.875rem', padding: '1rem', textAlign: 'center' }}>
              <p>No hay opciones disponibles</p>
              <p style={{ fontSize: '0.75rem', marginTop: '0.5rem' }}>
                Items totales: {menuItems.length}, Filtrados: {filteredItems.length}
              </p>
            </div>
          ) : (
            <>
              {filteredItems.map((item, index) => {
                const active = isActive(item.ruta);
                const isLast = index === filteredItems.length - 1;
                return (
                  <button
                    key={item.id}
                    onClick={() => handleNavigation(item.ruta)}
                    style={{
                      width: '100%',
                      padding: '0.75rem 1rem',
                      marginBottom: isLast ? 0 : '0.5rem',
                      backgroundColor: active ? '#374151' : 'transparent',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      textAlign: 'left',
                      fontSize: '0.875rem',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      fontWeight: active ? '600' : '400',
                      transition: 'all 0.15s ease',
                      ...(isLast && { flex: 1, alignSelf: 'stretch' }),
                    }}
                    onMouseEnter={(e) => {
                      if (!active) {
                        e.currentTarget.style.backgroundColor = '#374151';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (!active) {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }
                    }}
                  >
                    {typeof item.icono === 'string' && /^[A-Z]/.test(item.icono) ? (
                      <Icon name={item.icono as any} size={20} />
                    ) : (
                      <span>{item.icono}</span>
                    )}
                    {item.nombre}
                  </button>
                );
              })}
            </>
          )}
        </nav>
      </div>
    </div>
  );
};

export default ModularSidebar;

