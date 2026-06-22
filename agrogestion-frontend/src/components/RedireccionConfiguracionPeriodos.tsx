import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

/**
 * Redirige rutas legacy de períodos al tab correspondiente en Configuración unificada.
 */
const RedireccionConfiguracionPeriodos: React.FC = () => {
  const location = useLocation();
  const base = location.pathname.startsWith('/porcinos')
    ? '/porcinos/configuracion'
    : '/cultivos/configuracion';
  return <Navigate to={`${base}?tab=periodos`} replace />;
};

export default RedireccionConfiguracionPeriodos;
