/**
 * Servicios centralizados de API.
 * Reexporta servicios desde domain/ para mantener un único punto de importación.
 * Los componentes siguen importando desde aquí: import { laboresService } from '../services/apiServices';
 */

export { authService, eulaService, usuariosService } from './domain/authServices';
export { cultivosService, lotesService } from './domain/cultivosLotesServices';
export { insumosService, dosisAgroquimicosService, laboresService } from './domain/insumosLaboresServices';
export { camposService, maquinariaService } from './domain/camposMaquinariaServices';
export { ingresosService, egresosService, balanceService } from './domain/finanzasServices';
export {
  rolesService,
  aplicacionesAgroquimicasService,
  agroquimicosIntegradosService,
  cosechasService,
  reportesService,
  inventarioGranosService,
  configuracionEstadosService,
} from './domain/rolesReportesAgroquimicosServices';

import { authService, eulaService, usuariosService } from './domain/authServices';
import { cultivosService, lotesService } from './domain/cultivosLotesServices';
import { insumosService, dosisAgroquimicosService, laboresService } from './domain/insumosLaboresServices';
import { camposService, maquinariaService } from './domain/camposMaquinariaServices';
import { ingresosService, egresosService, balanceService } from './domain/finanzasServices';
import {
  rolesService,
  aplicacionesAgroquimicasService,
  agroquimicosIntegradosService,
  cosechasService,
  reportesService,
  inventarioGranosService,
  configuracionEstadosService,
} from './domain/rolesReportesAgroquimicosServices';

export default {
  auth: authService,
  usuarios: usuariosService,
  cultivos: cultivosService,
  lotes: lotesService,
  insumos: insumosService,
  dosisAgroquimicos: dosisAgroquimicosService,
  labores: laboresService,
  campos: camposService,
  maquinaria: maquinariaService,
  ingresos: ingresosService,
  egresos: egresosService,
  roles: rolesService,
  cosechas: cosechasService,
  reportes: reportesService,
  inventarioGranos: inventarioGranosService,
  aplicacionesAgroquimicas: aplicacionesAgroquimicasService,
  agroquimicosIntegrados: agroquimicosIntegradosService,
  balance: balanceService,
  configuracionEstados: configuracionEstadosService,
};
