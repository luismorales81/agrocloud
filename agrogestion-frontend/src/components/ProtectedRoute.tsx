import React from 'react';
import { usePermissions } from '../hooks/usePermissions';
import AccessDenied from './AccessDenied';

interface ProtectedRouteProps {
  children: React.ReactNode;
  permission: keyof ReturnType<typeof usePermissions>;
  showAccessDenied?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  permission,
  showAccessDenied = true
}) => {
  const permissions = usePermissions();
  
  // Verificar si el usuario tiene el permiso requerido
  const hasPermission = permissions[permission];
  
  if (!hasPermission) {
    // Mostrar mensaje de acceso denegado o redirigir
    if (showAccessDenied) {
      return <AccessDenied />;
    }
    return null;
  }
  
  return <>{children}</>;
};

export default ProtectedRoute;
