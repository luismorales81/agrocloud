/**
 * Utilidades para labores: mapeo de estados y tipos entre frontend y backend.
 * Extraído de LaboresManagement para reducir tamaño del componente.
 */

import type { EstadoLabor } from '../types/labores.types';

export const mapEstadoToBackend = (estado: string): string => {
  const estadoMap: Record<string, string> = {
    planificada: 'PLANIFICADA',
    en_progreso: 'EN_PROGRESO',
    completada: 'COMPLETADA',
    cancelada: 'CANCELADA',
    anulada: 'ANULADA',
  };
  return estadoMap[estado] || 'PLANIFICADA';
};

export const mapEstadoFromBackend = (estado: string): EstadoLabor => {
  const estadoMap: Record<string, EstadoLabor> = {
    PLANIFICADA: 'planificada',
    EN_PROGRESO: 'en_progreso',
    COMPLETADA: 'completada',
    INTERRUMPIDA: 'interrumpida',
    CANCELADA: 'cancelada',
    ANULADA: 'anulada',
  };
  return estadoMap[estado] || 'planificada';
};

export const mapTipoLaborToBackend = (tipo: string): string => {
  const tipoMap: Record<string, string> = {
    siembra: 'SIEMBRA',
    fertilizacion: 'FERTILIZACION',
    riego: 'RIEGO',
    cosecha: 'COSECHA',
    mantenimiento: 'MANTENIMIENTO',
    poda: 'PODA',
    control_plagas: 'CONTROL_PLAGAS',
    control_malezas: 'CONTROL_MALEZAS',
    desmalezado: 'CONTROL_MALEZAS',
    aplicacion_herbicida: 'CONTROL_MALEZAS',
    aplicacion_insecticida: 'CONTROL_PLAGAS',
    pulverizacion: 'MANTENIMIENTO',
    arado: 'MANTENIMIENTO',
    rastra: 'MANTENIMIENTO',
    monitoreo: 'ANALISIS_SUELO',
    otro: 'OTROS',
    analisis_suelo: 'ANALISIS_SUELO',
    otros: 'OTROS',
  };
  return tipoMap[tipo.toLowerCase()] || 'OTROS';
};

export const mapTipoLaborFromBackend = (tipo: string): string => {
  const tipoMap: Record<string, string> = {
    SIEMBRA: 'siembra',
    FERTILIZACION: 'fertilizacion',
    RIEGO: 'riego',
    COSECHA: 'cosecha',
    MANTENIMIENTO: 'mantenimiento',
    PODA: 'poda',
    CONTROL_PLAGAS: 'control_plagas',
    CONTROL_MALEZAS: 'control_malezas',
    ANALISIS_SUELO: 'analisis_suelo',
    OTROS: 'otros',
  };
  return tipoMap[tipo] || 'otros';
};

export const ESTADOS_LABOR = [
  { value: 'planificada', label: 'Planificada', color: '#6b7280' },
  { value: 'en_progreso', label: 'En Progreso', color: '#3b82f6' },
  { value: 'completada', label: 'Completada', color: '#10b981' },
  { value: 'interrumpida', label: 'Interrumpida', color: '#f59e0b' },
  { value: 'cancelada', label: 'Cancelada', color: '#ef4444' },
  { value: 'anulada', label: 'Anulada', color: '#9ca3af' },
] as const;

export const TODOS_LOS_TIPOS_LABOR = [
  'siembra', 'fertilizacion', 'cosecha', 'riego', 'pulverizacion',
  'arado', 'rastra', 'desmalezado', 'aplicacion_herbicida',
  'aplicacion_insecticida', 'monitoreo', 'otro',
] as const;
