/**
 * Tipos para el sistema modular de AgroCloud
 */

export type ModuleId =
  | 'cultivos'
  | 'porcinos'
  | 'avicola-crianza'
  | 'avicola-huevos'
  | 'avicola-carne'
  | 'avicola-ponedoras';

export interface ModuleInfo {
  id: ModuleId;
  nombre: string;
  descripcion: string;
  icono: string;
  color: string;
  habilitado: boolean;
}

export interface MenuItem {
  id: string;
  nombre: string;
  icono: string;
  ruta: string;
  permisos?: string[];
}

export interface ModuleRoute {
  path: string;
  name: string;
  component: React.ComponentType<any>;
  permisos?: string[];
}

export interface ModuleConfig {
  id: ModuleId;
  nombre: string;
  descripcion: string;
  icono: string;
  color: string;
  menu: MenuItem[];
  routes: ModuleRoute[];
}

