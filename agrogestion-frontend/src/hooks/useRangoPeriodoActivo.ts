import { useMemo } from 'react';
import { useCampana } from '../contexts/CampanaContext';

/** Fechas del período/campaña activa para reportes y filtros temporales. */
export function useRangoPeriodoActivo() {
  const { campanaActiva } = useCampana();

  const rango = useMemo(() => {
    if (campanaActiva?.fechaInicio && campanaActiva?.fechaFin) {
      return { inicio: campanaActiva.fechaInicio, fin: campanaActiva.fechaFin };
    }
    const hoy = new Date();
    const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const finMes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    return {
      inicio: inicioMes.toISOString().split('T')[0],
      fin: finMes.toISOString().split('T')[0],
    };
  }, [campanaActiva?.id, campanaActiva?.fechaInicio, campanaActiva?.fechaFin]);

  return { ...rango, campanaActiva };
}

export default useRangoPeriodoActivo;
