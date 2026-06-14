import React from 'react';
import InsumosUnificados from '../../../components/InsumosUnificados';

/**
 * Insumos del módulo huevos: vista reducida (sin agroquímicos).
 */
const InsumosHuevosScreen: React.FC = () => {
  return <InsumosUnificados vistaSimplificadaAvicola />;
};

export default InsumosHuevosScreen;
