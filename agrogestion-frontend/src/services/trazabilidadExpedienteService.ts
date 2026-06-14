import api from './api';
import { API_ENDPOINTS } from './apiEndpoints';

export type TipoEntidadExpediente =
  | 'LOTE'
  | 'COSECHA'
  | 'RECRIA'
  | 'VENTA_PORCINO'
  | 'AVICOLA_HUEVOS'
  | 'AVICOLA_CRIANZA'
  | 'AVICOLA_CARNE'
  | 'AVICOLA_PONEDORAS';

export interface RespuestaGeneracionExpediente {
  id: number;
  resultado: string;
  certificacion: string;
  entidadTipo: string;
  entidadId: number;
  hashSnapshot: string;
  hashPdf: string;
  versionMotor: string;
}

export interface ReporteTrazabilidadResumen {
  id: number;
  entidadTipo: string;
  entidadId: number;
  certificacionCodigo: string;
  resultado: string;
  generadoEn: string;
}

export const trazabilidadExpedienteService = {
  async generarExpediente(entidadTipo: TipoEntidadExpediente, entidadId: number) {
    const { data } = await api.post<RespuestaGeneracionExpediente>(
      API_ENDPOINTS.TRAZABILIDAD.EXPEDIENTES,
      { entidadTipo, entidadId }
    );
    return data;
  },

  async listarReportes() {
    const { data } = await api.get<ReporteTrazabilidadResumen[]>(API_ENDPOINTS.TRAZABILIDAD.REPORTES);
    return data;
  },

  async descargarPdf(reporteId: number): Promise<Blob> {
    const { data } = await api.get<Blob>(API_ENDPOINTS.TRAZABILIDAD.PDF(reporteId), {
      responseType: 'blob',
    });
    return data;
  },
};
