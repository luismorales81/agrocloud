import React from 'react';
import InsumosUnificados from '../../../components/InsumosUnificados';

/** Insumos del módulo crianza: misma vista simplificada que huevos (sin agroquímicos). */
const InsumosCrianzaScreen: React.FC = () => {
  return <InsumosUnificados vistaSimplificadaAvicola />;
};

export default InsumosCrianzaScreen;
