import { useModule } from './useModule';
import { obtenerConceptoTemporal } from '../config/conceptosTemporalesPorModulo';

export function useConceptoTemporalModulo() {
  const { currentModule } = useModule();
  const concepto = obtenerConceptoTemporal(currentModule);
  const rutaGestionAbsoluta = currentModule
    ? `/${currentModule}/${concepto.rutaGestionPeriodos}`
    : null;

  return { concepto, moduloId: currentModule, rutaGestionAbsoluta };
}

export default useConceptoTemporalModulo;
