import React from 'react';
import { useModule } from '../core/hooks/useModule';
import { codigoModuloEconomia } from '../core/utils/codigoModuloEconomia';
import FinanzasManagement from './FinanzasManagement';

/** Misma pantalla de finanzas, filtrada por el módulo activo. */
const FinanzasDelModulo: React.FC = () => {
  const { currentModule } = useModule();
  return <FinanzasManagement modulo={codigoModuloEconomia(currentModule)} />;
};

export default FinanzasDelModulo;
