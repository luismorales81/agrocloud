/**
 * Hook con lógica de permisos para labores (quién puede modificar, anular, eliminar).
 * Extraído de LaboresManagement para reducir tamaño del componente.
 */

import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { useMemo } from 'react';

export function useLaboresPermisos() {
  const { user } = useAuth();
  const empresaContext = useEmpresa();
  const esOperario = empresaContext.esOperario();
  const esAdministrador = empresaContext.esAdministrador();
  const esJefeCampo = empresaContext.esJefeCampo();
  const esConsultorExterno = empresaContext.esConsultorExterno();

  return useMemo(() => {
    const puedeModificarLabor = (labor: { responsable?: string; estado?: string }): boolean => {
      if (esConsultorExterno) return false;
      if (esAdministrador || esJefeCampo) return true;
      if (esOperario) {
        const nombreUsuario = user?.name ?? '';
        const responsableLabor = labor.responsable ?? '';
        return nombreUsuario.toLowerCase() === responsableLabor.toLowerCase();
      }
      return false;
    };

    const puedeAnularLabor = (labor: { estado?: string }): boolean => {
      if (esConsultorExterno) return false;
      const estado = (labor?.estado ?? '').toLowerCase();
      const requiereAnulacion = estado === 'completada' || estado === 'en_progreso';
      return (esAdministrador || esJefeCampo) && requiereAnulacion;
    };

    const puedeEliminarLabor = (labor: { estado?: string }): boolean => {
      if (!puedeModificarLabor(labor)) return false;
      const estado = (labor?.estado ?? '').toLowerCase();
      return estado === 'planificada' || estado === 'cancelada' || estado === 'anulada';
    };

    return { puedeModificarLabor, puedeAnularLabor, puedeEliminarLabor };
  }, [user?.name, esOperario, esAdministrador, esJefeCampo, esConsultorExterno]);
}
