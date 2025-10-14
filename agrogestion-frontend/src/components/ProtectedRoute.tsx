import React from 'react';
import { usePermissions } from '../hooks/usePermissions';
import AccessDenied from './AccessDenied';

interface ProtectedRouteProps {
  children: React.ReactNode;
  permission?: keyof ReturnType<typeof usePermissions>;
  showAccessDenied?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  permission,
  showAccessDenied = true
}) => {
  const permissions = usePermissions();

  // Si no se especifica permiso, permitir por defecto
  if (!permission) {
    return <>{children}</>;
  }

  // Evitar desmontes si los permisos aún no cargaron (undefined)
  const valorPermiso = (permissions as any)?.[permission];
  if (valorPermiso === undefined) {
    return <>{children}</>;
  }

  if (!valorPermiso) {
    // Mostrar mensaje de acceso denegado o redirigir
    if (showAccessDenied) {
      return <AccessDenied />;
    }
    return null;
  }
  
  return <>{children}</>;
};

export default ProtectedRoute;
