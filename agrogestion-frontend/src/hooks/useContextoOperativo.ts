import { useEmpresa } from '../contexts/EmpresaContext';
import { useCampana } from '../contexts/CampanaContext';

/** Empresa + período/campaña activos para recargas de datos con alcance temporal. */
export function useContextoOperativo() {
  const { empresaId } = useEmpresa();
  const { campanaId, campanaActiva } = useCampana();
  return { empresaId, campanaId, campanaActiva };
}

export default useContextoOperativo;
