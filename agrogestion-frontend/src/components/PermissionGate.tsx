import React from 'react';
import { usePermissions } from '../hooks/usePermissions';

interface PermissionGateProps {
  children: React.ReactNode;
  permission: keyof ReturnType<typeof usePermissions>;
  fallback?: React.ReactNode;
}

const PermissionGate: React.FC<PermissionGateProps> = ({ 
  children, 
  permission, 
  fallback = null 
}) => {
  const permissions = usePermissions();
  
  // Verificar si el usuario tiene el permiso requerido
  const hasPermission = permissions[permission];
  
  if (!hasPermission) {
    return <>{fallback}</>;
  }
  
  return <>{children}</>;
};

export default PermissionGate;
