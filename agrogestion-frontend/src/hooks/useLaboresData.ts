/**
 * Hook para carga y estado de datos de labores (lotes, insumos, maquinaria, cultivos, labores).
 * Extraído de LaboresManagement para reducir tamaño del componente.
 */

import { useState, useCallback } from 'react';
import { offlineService } from '../services/OfflineService';
import { haySesionActiva } from '../services/api';
import { lotesService, insumosService, maquinariaService, cultivosService } from '../services/apiServices';
import type { LaborDetalladoDTO } from '../types/labor.types';
import type {
  Labor,
  Lote,
  Insumo,
  Maquinaria,
  MaquinariaAsignada,
  LaborMaquinaria,
  LaborManoObra,
} from '../types/labores.types';
import { mapEstadoFromBackend } from '../utils/laboresUtils';

export type CultivoOption = { id: number; nombre: string; tipo: string };

export type FiltrosLabores = {
  loteId?: number;
  estado?: string;
  busqueda?: string;
};

export interface UseLaboresDataResult {
  labores: Labor[];
  setLabores: React.Dispatch<React.SetStateAction<Labor[]>>;
  lotes: Lote[];
  insumos: Insumo[];
  setInsumos: React.Dispatch<React.SetStateAction<Insumo[]>>;
  maquinaria: Maquinaria[];
  cultivos: CultivoOption[];
  loading: boolean;
  setLoading: React.Dispatch<React.SetStateAction<boolean>>;
  totalElementos: number;
  totalPaginas: number;
  loadData: () => Promise<void>;
  cargarLabores: (pagina: number, tamano: number, filtros?: FiltrosLabores) => Promise<void>;
}

function mapearLaboresDesdeDto(laboresData: LaborDetalladoDTO[]): Labor[] {
  const laboresActivas = laboresData.filter((labor) => labor.activo !== false);
  return laboresActivas.map((labor: LaborDetalladoDTO) => ({
    id: labor.id,
    tipo: labor.tipo || '',
    fecha: labor.fechaInicio || '',
    fecha_fin: labor.fechaFin || '',
    observaciones: labor.observaciones || '',
    lote_id: labor.loteId || 0,
    lote_nombre: labor.loteNombre || '',
    estado: mapEstadoFromBackend(labor.estado || 'PLANIFICADA'),
    overdue: labor.overdue === true,
    fecha_realizacion: labor.fechaRealizacion || undefined,
    maquinaria_asignada: (labor.maquinariaAsignada || []).map((m): MaquinariaAsignada => ({
      maquinaria_id: m.maquinaria_id ?? 0,
      maquinaria_nombre: m.maquinaria_nombre ?? '',
      costo_total: m.costo_total ?? 0,
    })),
    responsable: labor.responsable || '',
    horas_trabajo: labor.horasTrabajo || 0,
    costo_total: labor.costoTotal || 0,
    costo_maquinaria: labor.costoMaquinaria || 0,
    costo_mano_obra: labor.costoManoObra || 0,
    costo_insumos: labor.costoInsumos || 0,
    maquinarias: (labor.maquinarias || []) as LaborMaquinaria[],
    mano_obra: (labor.manoObra || []).map((mo: any) => ({
      id_labor_mano_obra: mo.idLaborManoObra || mo.id_labor_mano_obra,
      id_labor: mo.idLabor || mo.id_labor,
      descripcion: mo.descripcion,
      cantidad_personas: mo.cantidadPersonas || mo.cantidad_personas || 1,
      proveedor: mo.proveedor,
      costo_total: mo.costoTotal || mo.costo_total || 0,
      horas_trabajo: mo.horasTrabajo || mo.horas_trabajo,
      observaciones: mo.observaciones,
    })),
    insumos_usados: (labor.insumosUsados || []).map((ins: any) => ({
      insumo_id: ins.idInsumo || ins.insumo_id || ins.id_insumo,
      insumo_nombre: ins.insumoNombre || ins.insumo_nombre || ins.nombre || 'Insumo sin nombre',
      cantidad_usada: ins.cantidadUsada || ins.cantidad_usada || ins.cantidad || 0,
      cantidad_planificada: ins.cantidadPlanificada || ins.cantidad_planificada || ins.cantidad_usada || 0,
      unidad_medida: ins.unidadMedida || ins.unidad_medida || ins.unidad || '',
      costo_unitario: ins.costoUnitario || ins.costo_unitario || ins.precio_unitario || 0,
      costo_total:
        ins.costoTotal ||
        ins.costo_total ||
        (ins.cantidadUsada || ins.cantidad_usada || ins.cantidad || 0) *
          (ins.costoUnitario || ins.costo_unitario || ins.precio_unitario || 0),
    })),
  }));
}

export function useLaboresData(): UseLaboresDataResult {
  const [labores, setLabores] = useState<Labor[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [maquinaria, setMaquinaria] = useState<Maquinaria[]>([]);
  const [cultivos, setCultivos] = useState<CultivoOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalElementos, setTotalElementos] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);

  const cargarLabores = useCallback(async (pagina: number, tamano: number, filtros?: FiltrosLabores) => {
    try {
      const loteId = filtros?.loteId && filtros.loteId > 0 ? filtros.loteId : undefined;
      const paginaResp = await offlineService.getLaboresPaginadas(pagina, tamano, {
        loteId,
        estado: filtros?.estado,
        busqueda: filtros?.busqueda,
      });
      setLabores(mapearLaboresDesdeDto(paginaResp.contenido));
      setTotalElementos(paginaResp.totalElementos);
      setTotalPaginas(Math.max(1, paginaResp.totalPaginas));
    } catch (error) {
      console.error('Error cargando labores:', error);
    }
  }, []);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      if (!haySesionActiva()) {
        console.error('No hay sesión activa');
        alert('La sesión expiró. Por favor, inicia sesión nuevamente.');
        return;
      }

      try {
        const lotesData = await lotesService.listarCultivo();
        const lotesMapeados: Lote[] = (Array.isArray(lotesData) ? lotesData : []).map((lote: any) => ({
          id: lote.id,
          nombre: lote.nombre,
          superficie: lote.areaHectareas || 0,
          cultivo: lote.cultivoActual || '',
          estado: lote.estado,
        }));
        setLotes(lotesMapeados);
      } catch (error) {
        console.error('Error cargando lotes:', error);
      }

      try {
        const insumosData = await insumosService.listar();
        const insumosMapeados: Insumo[] = (Array.isArray(insumosData) ? insumosData : []).map((insumo: any) => ({
          id: insumo.id,
          nombre: insumo.nombre,
          tipo: insumo.tipo,
          stock_actual: insumo.stockActual || 0,
          unidad_medida: insumo.unidadMedida || '',
          precio_unitario: insumo.precioUnitario || 0,
        }));
        setInsumos(insumosMapeados);
      } catch (error) {
        console.error('Error cargando insumos:', error);
      }

      try {
        const cultivosData = await cultivosService.listar();
        const cultivosMapeados: CultivoOption[] = (Array.isArray(cultivosData) ? cultivosData : []).map((c: any) => ({
          id: c.id,
          nombre: c.nombre || c.variedad || '',
          tipo: c.tipo || '',
        }));
        setCultivos(cultivosMapeados);
      } catch (error) {
        console.error('Error cargando cultivos:', error);
      }

      try {
        const maquinariaData = await maquinariaService.listar();
        const maquinariaMapeada: Maquinaria[] = (Array.isArray(maquinariaData) ? maquinariaData : []).map((maq: any) => ({
          id: maq.id,
          nombre: maq.nombre,
          tipo: maq.tipo,
          estado: maq.estado,
          kilometros_uso: maq.kilometrosUso || 0,
          costo_por_hora: maq.costoPorHora || 0,
        }));
        setMaquinaria(maquinariaMapeada);
      } catch (error) {
        console.error('Error cargando maquinaria:', error);
      }
    } catch (error) {
      console.error('Error cargando datos:', error);
      alert('Error al cargar los datos. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    labores,
    setLabores,
    lotes,
    insumos,
    setInsumos,
    maquinaria,
    cultivos,
    loading,
    setLoading,
    totalElementos,
    totalPaginas,
    loadData,
    cargarLabores,
  };
}
