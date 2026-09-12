import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const MODULOS_CON_CONFIGURACION = [
  'cultivos',
  'porcinos',
  'avicola-crianza',
  'avicola-huevos',
  'avicola-ponedoras',
  'feedlot',
  'lecheria',
] as const;

/**
 * Redirige rutas de períodos al destino correcto según el módulo activo.
 */
const RedireccionConfiguracionPeriodos: React.FC = () => {
  const location = useLocation();
  const segmentoModulo = location.pathname.split('/').filter(Boolean)[0];

  const modulo = MODULOS_CON_CONFIGURACION.includes(segmentoModulo as (typeof MODULOS_CON_CONFIGURACION)[number])
    ? segmentoModulo
    : 'cultivos';

  if (modulo === 'cultivos') {
    return <Navigate to="/cultivos/configuracion?tab=periodos" replace />;
  }

  if (modulo === 'porcinos') {
    return <Navigate to="/porcinos/configuracion/periodos" replace />;
  }

  return <Navigate to={`/${modulo}/configuracion/periodos`} replace />;
};

export default RedireccionConfiguracionPeriodos;
