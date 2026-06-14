import React, { useState, useEffect, useCallback } from 'react';
import api from '../../services/api';
import { API_ENDPOINTS } from '../../services/apiEndpoints';
import { Icon } from '../../core/components/Icon';
import BarraSubnavegacionAlimentacionPorcinos from '../../modules/porcinos/components/BarraSubnavegacionAlimentacionPorcinos';
import {
  configuracionService,
  CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL,
} from '../../modules/porcinos/services/configuracionService';

interface DiaAlimentacionDTO {
  id: number;
  fecha: string;
  estado: string;
  totalLotesAtendidos: number;
  totalAnimalesAtendidos: number;
  totalRecetasUsadas: number;
  totalInsumosConsumidos: number;
  tieneAlertasStockInsuficiente: boolean;
  cantidadAlertas: number;
  confirmadoPorNombre?: string;
  fechaConfirmacion?: string;
  /** Día pendiente: cantidad de consumos (líneas) del día */
  consumosDelDia?: number;
  /** Pendiente: cuántos siguen solo estimados (sin kg real) */
  consumosSoloEstimados?: number;
}

interface ConsumoDetalleDTO {
  id: number;
  tipoComponente: string;
  nombreComponente: string;
  cantidadRequerida: number;
  cantidadDisponible: number;
  cantidadDescontada: number;
  stockResultante: number;
  deficit?: number;
  tieneDeficit: boolean;
  porcentajeCobertura: number;
  unidadMedida: string;
}

interface ConsumoDiarioDTO {
  id: number;
  etapaAlimentacion: string;
  cantidadAnimales: number;
  recriaId?: number;
  madreId?: number;
  recriaNombre?: string;
  madreNombre?: string;
  recetaNombre: string;
  /** kg proyectados (plan) */
  cantidadRecetaTotal: number;
  /** kg informados por el operario */
  cantidadRecetaReal?: number | null;
  /** kg usados para insumos y stock */
  cantidadRecetaEfectiva?: number | null;
  tipoRegistroConsumo?: string;
  detalles: ConsumoDetalleDTO[];
}

interface AlertaStockDTO {
  fecha: string;
  insumoNombre: string;
  deficit: number;
  cantidadRequerida: number;
  cantidadDisponible: number;
  porcentajeCobertura: number;
  unidadMedida: string;
}

interface DiaAlimentacionDetalleDTO {
  dia: DiaAlimentacionDTO;
  consumos: ConsumoDiarioDTO[];
  alertas: AlertaStockDTO[];
}

const CalendarioAlimentacionPorcinos: React.FC = () => {
  const [fechaActual, setFechaActual] = useState(new Date());
  const [calendario, setCalendario] = useState<Record<string, DiaAlimentacionDTO>>({});
  const [loading, setLoading] = useState(false);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [fechaSeleccionada, setFechaSeleccionada] = useState<string | null>(null);
  const [detalleDia, setDetalleDia] = useState<DiaAlimentacionDetalleDTO | null>(null);
  const [loadingDetalle, setLoadingDetalle] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [observaciones, setObservaciones] = useState('');
  const [inputsKgReal, setInputsKgReal] = useState<Record<number, string>>({});
  const [guardandoRealId, setGuardandoRealId] = useState<number | null>(null);
  /** Configuración por empresa: clave CONFIRMAR_CALENDARIO_SOLO_CON_REAL en porcinos. */
  const [exigirKgRealParaConfirmar, setExigirKgRealParaConfirmar] = useState(false);

  const año = fechaActual.getFullYear();
  const mes = fechaActual.getMonth() + 1;
  const primerDiaMes = new Date(año, mes - 1, 1);
  const ultimoDiaMes = new Date(año, mes, 0);
  const primerDiaSemana = primerDiaMes.getDay();
  const diasEnMes = ultimoDiaMes.getDate();
  const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
  const nombresMeses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  const cargarCalendario = async () => {
    setLoading(true);
    try {
      const response = await api.get(
        API_ENDPOINTS.PORCINOS_CALENDARIO_ALIMENTACION.MENSUAL(año, mes)
      );
      const datos = response.data || {};
      const mapa: Record<string, DiaAlimentacionDTO> = {};
      Object.entries(datos).forEach(([fechaStr, dto]) => {
        const fechaFormato = (dto as DiaAlimentacionDTO).fecha;
        if (fechaFormato) {
          mapa[fechaFormato] = dto as DiaAlimentacionDTO;
        }
      });
      setCalendario(mapa);
    } catch (error) {
      console.error('Error cargando calendario de alimentación:', error);
      setCalendario({});
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarCalendario();
  }, [año, mes]);

  useEffect(() => {
    let cancelado = false;
    const cargarPoliticaConfirmacion = async () => {
      const activo = await configuracionService.obtenerPoliticaConfirmacionCalendarioSoloConKgReal();
      if (!cancelado) setExigirKgRealParaConfirmar(activo);
    };
    void cargarPoliticaConfirmacion();
    return () => {
      cancelado = true;
    };
  }, []);

  const cargarDetalleDiaPorFecha = useCallback(async (fechaStr: string) => {
    setLoadingDetalle(true);
    setDetalleDia(null);
    try {
      const response = await api.get(
        API_ENDPOINTS.PORCINOS_CALENDARIO_ALIMENTACION.DETALLE_DIA(fechaStr)
      );
      setDetalleDia(response.data);
    } catch (error) {
      console.error('Error cargando detalle del día:', error);
      setDetalleDia(null);
    } finally {
      setLoadingDetalle(false);
    }
  }, []);

  useEffect(() => {
    if (!detalleDia?.consumos) return;
    const next: Record<number, string> = {};
    detalleDia.consumos.forEach((c) => {
      if (c.cantidadRecetaReal != null && c.cantidadRecetaReal !== undefined) {
        next[c.id] = String(c.cantidadRecetaReal);
      }
    });
    setInputsKgReal(next);
  }, [detalleDia]);

  const abrirModalDia = async (dia: number) => {
    const fechaStr = `${año}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
    setFechaSeleccionada(fechaStr);
    setObservaciones('');
    setInputsKgReal({});
    setModalAbierto(true);
    await cargarDetalleDiaPorFecha(fechaStr);
  };

  const formatearKg = (valor: number | null | undefined) => {
    if (valor == null || Number.isNaN(Number(valor))) return '—';
    return Number(valor).toFixed(2);
  };

  const registrarCantidadReal = async (consumoId: number) => {
    const texto = (inputsKgReal[consumoId] ?? '').trim().replace(',', '.');
    const kg = parseFloat(texto);
    if (!(kg > 0)) {
      alert('Ingrese una cantidad de ración en kg mayor que cero.');
      return;
    }
    setGuardandoRealId(consumoId);
    try {
      await api.post(
        API_ENDPOINTS.PORCINOS_CALENDARIO_ALIMENTACION.REGISTRAR_CANTIDAD_REAL(consumoId),
        {
          cantidadRecetaKg: kg
        }
      );
      if (fechaSeleccionada) {
        await cargarDetalleDiaPorFecha(fechaSeleccionada);
      }
      await cargarCalendario();
    } catch (error: any) {
      console.error('Error registrando cantidad real:', error);
      alert(error?.response?.data?.message || 'No se pudo registrar la cantidad real.');
    } finally {
      setGuardandoRealId(null);
    }
  };

  const confirmarDia = async () => {
    if (!fechaSeleccionada) return;
    const quedanSoloEstimados = detalleDia?.consumos?.some(
      (c) => !c.tipoRegistroConsumo || c.tipoRegistroConsumo === 'ESTIMADO'
    );
    if (exigirKgRealParaConfirmar && quedanSoloEstimados) {
      alert(
        'Su empresa exige registrar el kg real de ración en todos los consumos antes de confirmar el día. ' +
          `Complete los kg reales o desactive la opción en Porcinos → Configuración → Generales (clave ${CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL}).`
      );
      return;
    }
    if (quedanSoloEstimados) {
      const continuar = window.confirm(
        'Quedan consumos solo estimados (sin kg real cargado). Al confirmar, el descuento de stock usará la proyección para esos ítems. ¿Desea continuar?'
      );
      if (!continuar) return;
    }
    setConfirmando(true);
    try {
      await api.post(
        API_ENDPOINTS.PORCINOS_CALENDARIO_ALIMENTACION.CONFIRMAR_DIA(fechaSeleccionada),
        {
          observaciones: observaciones.trim() || null
        }
      );
      setModalAbierto(false);
      cargarCalendario();
    } catch (error: any) {
      console.error('Error confirmando día:', error);
      alert(error?.response?.data?.message || 'Error al confirmar el día');
    } finally {
      setConfirmando(false);
    }
  };

  const obtenerEstadoDia = (dia: number): DiaAlimentacionDTO | null => {
    const fechaStr = `${año}-${String(mes).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
    return calendario[fechaStr] || null;
  };

  const obtenerColorEstado = (estado: string): string => {
    switch (estado) {
      case 'CONFIRMADO':
      case 'CON_CORRECCIONES':
        return '#10b981';
      case 'PENDIENTE':
        return '#f59e0b';
      default:
        return '#e5e7eb';
    }
  };

  const esHoy = (dia: number): boolean => {
    const hoy = new Date();
    return dia === hoy.getDate() && mes === hoy.getMonth() + 1 && año === hoy.getFullYear();
  };

  const mesAnterior = () => setFechaActual(new Date(año, mes - 2, 1));
  const mesSiguiente = () => setFechaActual(new Date(año, mes, 1));
  const irAHoy = () => setFechaActual(new Date());

  const hayConsumosSoloEstimados =
    detalleDia?.consumos?.some(
      (c) => !c.tipoRegistroConsumo || c.tipoRegistroConsumo === 'ESTIMADO'
    ) ?? false;
  const confirmarBloqueadoPorPolitica =
    exigirKgRealParaConfirmar && detalleDia?.dia?.estado === 'PENDIENTE' && hayConsumosSoloEstimados;

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <BarraSubnavegacionAlimentacionPorcinos />
      <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#1f2937', marginBottom: '1rem' }}>
        Calendario de alimentación diaria
      </h1>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1rem'
      }}>
        <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
          {nombresMeses[mes - 1]} {año}
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button onClick={mesAnterior} style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontSize: '0.875rem'
          }}>
            ← Anterior
          </button>
          <button onClick={irAHoy} style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#6b7280',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontSize: '0.875rem'
          }}>
            Hoy
          </button>
          <button onClick={mesSiguiente} style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontSize: '0.875rem'
          }}>
            Siguiente →
          </button>
        </div>
      </div>

      <div style={{
        display: 'flex',
        gap: '1rem',
        marginBottom: '1rem',
        flexWrap: 'wrap'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#f59e0b', borderRadius: '4px' }} />
          <span style={{ fontSize: '0.875rem' }}>Pendiente (Confirmar)</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#10b981', borderRadius: '4px' }} />
          <span style={{ fontSize: '0.875rem' }}>Confirmado</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span
            style={{
              display: 'inline-block',
              minWidth: '18px',
              height: '18px',
              lineHeight: '18px',
              textAlign: 'center',
              fontSize: '0.65rem',
              fontWeight: 'bold',
              backgroundColor: '#7c3aed',
              color: 'white',
              borderRadius: '50%'
            }}
          >
            !
          </span>
          <span style={{ fontSize: '0.875rem' }}>Pendiente: falta kg real en algún consumo</span>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <Icon name="Loader" size={32} />
          <p>Cargando calendario...</p>
        </div>
      ) : (
        <div style={{
          backgroundColor: 'white',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          overflow: 'hidden'
        }}>
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            backgroundColor: '#f3f4f6',
            borderBottom: '2px solid #e5e7eb'
          }}>
            {diasSemana.map(d => (
              <div key={d} style={{
                padding: '0.75rem',
                textAlign: 'center',
                fontWeight: 'bold',
                color: '#374151',
                fontSize: '0.875rem'
              }}>
                {d}
              </div>
            ))}
          </div>
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            gap: '1px',
            backgroundColor: '#e5e7eb'
          }}>
            {Array.from({ length: primerDiaSemana }).map((_, i) => (
              <div key={`e-${i}`} style={{ backgroundColor: 'white', minHeight: '100px' }} />
            ))}
            {Array.from({ length: diasEnMes }).map((_, i) => {
              const dia = i + 1;
              const estadoDia = obtenerEstadoDia(dia);
              const esDiaHoy = esHoy(dia);
              const tieneDatos = !!estadoDia;
              const esPendiente = estadoDia?.estado === 'PENDIENTE';
              const faltaKgReal =
                esPendiente &&
                estadoDia.consumosSoloEstimados != null &&
                estadoDia.consumosSoloEstimados > 0;

              return (
                <div
                  key={dia}
                  style={{
                    backgroundColor: tieneDatos ? obtenerColorEstado(estadoDia!.estado) : 'white',
                    minHeight: '100px',
                    padding: '0.5rem',
                    border: esDiaHoy ? '2px solid #3b82f6' : '1px solid #e5e7eb',
                    cursor: tieneDatos ? 'pointer' : 'default',
                    color: tieneDatos ? 'white' : '#1f2937',
                    opacity: tieneDatos ? 0.9 : 1
                  }}
                  onClick={() => tieneDatos && abrirModalDia(dia)}
                >
                  <div style={{ fontWeight: esDiaHoy ? 'bold' : 'normal', marginBottom: '0.25rem' }}>
                    {dia}
                  </div>
                  {tieneDatos && (
                    <div style={{ fontSize: '0.7rem', display: 'flex', flexDirection: 'column', gap: '2px' }}>
                      {esPendiente ? (
                        <span>[Confirmar]</span>
                      ) : (
                        <span>[Confirmado]</span>
                      )}
                      {faltaKgReal && (
                        <span
                          title="Hay consumos sin kg real cargado"
                          style={{
                            alignSelf: 'flex-start',
                            backgroundColor: '#6d28d9',
                            color: 'white',
                            borderRadius: '999px',
                            padding: '0 5px',
                            fontSize: '0.6rem',
                            fontWeight: 700
                          }}
                        >
                          Real
                        </span>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {modalAbierto && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.5rem',
            width: '90%',
            maxWidth: '700px',
            maxHeight: '90vh',
            overflow: 'auto'
          }}>
            <h2 style={{ marginTop: 0, marginBottom: '1rem' }}>
              Resumen de consumo - {fechaSeleccionada}
            </h2>

            {exigirKgRealParaConfirmar && detalleDia?.dia?.estado === 'PENDIENTE' && (
              <div
                style={{
                  marginBottom: '1rem',
                  padding: '0.65rem 0.75rem',
                  borderRadius: '0.375rem',
                  backgroundColor: '#fef3c7',
                  border: '1px solid #fcd34d',
                  color: '#92400e',
                  fontSize: '0.85rem'
                }}
              >
                Política de empresa: <strong>no se puede confirmar</strong> el día si queda algún consumo en modo
                estimado. Debe cargar el kg real en cada línea antes de confirmar.
              </div>
            )}

            {!exigirKgRealParaConfirmar &&
              detalleDia?.dia?.estado === 'PENDIENTE' &&
              detalleDia.dia.consumosSoloEstimados != null &&
              detalleDia.dia.consumosSoloEstimados > 0 && (
              <div
                style={{
                  marginBottom: '1rem',
                  padding: '0.65rem 0.75rem',
                  borderRadius: '0.375rem',
                  backgroundColor: '#f5f3ff',
                  border: '1px solid #c4b5fd',
                  color: '#5b21b6',
                  fontSize: '0.85rem'
                }}
              >
                Quedan <strong>{detalleDia.dia.consumosSoloEstimados}</strong> consumo(s) solo estimados (sin kg
                real). Podés cargarlos abajo o confirmar el día: el stock se descontará con la{' '}
                <strong>proyección</strong> en los que no hayas cargado real.
              </div>
            )}

            {loadingDetalle ? (
              <p>Cargando detalle...</p>
            ) : detalleDia ? (
              <>
                <div style={{ marginBottom: '1rem' }}>
                  <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Consumos por Recría/Madre</h3>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f3f4f6' }}>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #e5e7eb' }}>Recría/Madre</th>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #e5e7eb' }}>Etapa</th>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #e5e7eb' }}>Receta</th>
                        <th style={{ padding: '0.5rem', textAlign: 'right', border: '1px solid #e5e7eb' }}>Proyectado (kg)</th>
                        <th style={{ padding: '0.5rem', textAlign: 'right', border: '1px solid #e5e7eb' }}>Efectivo (kg)</th>
                        <th style={{ padding: '0.5rem', textAlign: 'center', border: '1px solid #e5e7eb' }}>Tipo</th>
                        {detalleDia.dia?.estado === 'PENDIENTE' && (
                          <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #e5e7eb' }}>Kg real</th>
                        )}
                        {detalleDia.dia?.estado === 'PENDIENTE' && (
                          <th style={{ padding: '0.5rem', textAlign: 'center', border: '1px solid #e5e7eb' }}>Acción</th>
                        )}
                      </tr>
                    </thead>
                    <tbody>
                      {detalleDia.consumos?.map((c) => (
                        <tr key={c.id}>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb' }}>
                            {c.recriaNombre || c.madreNombre || 'N/A'}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb' }}>{c.etapaAlimentacion}</td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb' }}>{c.recetaNombre}</td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'right' }}>
                            {formatearKg(c.cantidadRecetaTotal)}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'right' }}>
                            {formatearKg(
                              c.cantidadRecetaEfectiva != null
                                ? c.cantidadRecetaEfectiva
                                : c.cantidadRecetaTotal
                            )}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'center' }}>
                            {c.tipoRegistroConsumo === 'REAL' ? (
                              <span style={{ color: '#059669', fontWeight: 600 }}>REAL</span>
                            ) : (
                              <span style={{ color: '#92400e' }}>ESTIMADO</span>
                            )}
                          </td>
                          {detalleDia.dia?.estado === 'PENDIENTE' && (
                            <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb' }}>
                              <input
                                type="number"
                                min={0}
                                step="0.01"
                                value={inputsKgReal[c.id] ?? ''}
                                onChange={(e) =>
                                  setInputsKgReal((prev) => ({ ...prev, [c.id]: e.target.value }))
                                }
                                placeholder={formatearKg(c.cantidadRecetaTotal)}
                                style={{
                                  width: '100%',
                                  maxWidth: '7rem',
                                  padding: '0.35rem',
                                  border: '1px solid #d1d5db',
                                  borderRadius: '0.25rem'
                                }}
                              />
                            </td>
                          )}
                          {detalleDia.dia?.estado === 'PENDIENTE' && (
                            <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'center' }}>
                              <button
                                type="button"
                                onClick={() => registrarCantidadReal(c.id)}
                                disabled={guardandoRealId === c.id}
                                style={{
                                  padding: '0.35rem 0.6rem',
                                  fontSize: '0.8rem',
                                  backgroundColor: '#2563eb',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '0.25rem',
                                  cursor: guardandoRealId === c.id ? 'not-allowed' : 'pointer',
                                  opacity: guardandoRealId === c.id ? 0.7 : 1
                                }}
                              >
                                {guardandoRealId === c.id ? 'Guardando…' : 'Guardar real'}
                              </button>
                            </td>
                          )}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {detalleDia.dia?.estado === 'PENDIENTE' && (
                    <p style={{ fontSize: '0.8rem', color: '#6b7280', marginTop: '0.5rem' }}>
                      La cantidad efectiva (insumos y descuento al confirmar) usa el kg real si lo cargás; si no, la
                      proyección.
                    </p>
                  )}
                </div>

                <div style={{ marginBottom: '1rem' }}>
                  <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem' }}>Insumos - Requerido / Disponible / Déficit</h3>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f3f4f6' }}>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #e5e7eb' }}>Insumo</th>
                        <th style={{ padding: '0.5rem', textAlign: 'right', border: '1px solid #e5e7eb' }}>Requerido</th>
                        <th style={{ padding: '0.5rem', textAlign: 'right', border: '1px solid #e5e7eb' }}>Disponible</th>
                        <th style={{ padding: '0.5rem', textAlign: 'right', border: '1px solid #e5e7eb' }}>Déficit</th>
                      </tr>
                    </thead>
                    <tbody>
                      {detalleDia.consumos?.flatMap(c => c.detalles || []).map((d, i) => (
                        <tr key={i} style={d.tieneDeficit ? { backgroundColor: '#fef2f2' } : {}}>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb' }}>{d.nombreComponente}</td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'right' }}>
                            {d.cantidadRequerida?.toFixed(2)} {d.unidadMedida}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'right' }}>
                            {d.cantidadDisponible?.toFixed(2)}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #e5e7eb', textAlign: 'right', color: d.tieneDeficit ? '#dc2626' : undefined }}>
                            {d.tieneDeficit ? (d.deficit?.toFixed(2) || '-') : '-'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {detalleDia.alertas && detalleDia.alertas.length > 0 && (
                  <div style={{
                    backgroundColor: '#fef2f2',
                    padding: '0.75rem',
                    borderRadius: '0.375rem',
                    marginBottom: '1rem',
                    fontSize: '0.875rem',
                    color: '#991b1b'
                  }}>
                    <strong>Alertas de stock insuficiente:</strong>
                    <ul style={{ margin: '0.5rem 0 0 1rem', padding: 0 }}>
                      {detalleDia.alertas.map((a, i) => (
                        <li key={i}>
                          {a.insumoNombre}: déficit {a.deficit?.toFixed(2)} {a.unidadMedida}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {detalleDia.dia?.estado === 'PENDIENTE' && (
                  <>
                    <div style={{ marginBottom: '1rem' }}>
                      <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                        Observaciones (opcional)
                      </label>
                      <textarea
                        value={observaciones}
                        onChange={(e) => setObservaciones(e.target.value)}
                        style={{
                          width: '100%',
                          padding: '0.5rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.375rem',
                          fontSize: '0.875rem',
                          minHeight: '60px'
                        }}
                        placeholder="Ej: Ajustes realizados..."
                      />
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                      <button
                        onClick={() => setModalAbierto(false)}
                        style={{
                          padding: '0.5rem 1rem',
                          backgroundColor: '#6b7280',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer'
                        }}
                      >
                        Cancelar
                      </button>
                      <button
                        type="button"
                        onClick={confirmarDia}
                        disabled={confirmando || confirmarBloqueadoPorPolitica}
                        title={
                          confirmarBloqueadoPorPolitica
                            ? 'Faltan kg reales en algún consumo según la política de la empresa'
                            : undefined
                        }
                        style={{
                          padding: '0.5rem 1rem',
                          backgroundColor: confirmarBloqueadoPorPolitica ? '#9ca3af' : '#10b981',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: confirmando || confirmarBloqueadoPorPolitica ? 'not-allowed' : 'pointer'
                        }}
                      >
                        {confirmando ? 'Confirmando...' : 'Confirmar y descontar'}
                      </button>
                    </div>
                  </>
                )}

                {detalleDia.dia?.estado !== 'PENDIENTE' && (
                  <div style={{
                    padding: '0.75rem',
                    backgroundColor: '#d1fae5',
                    borderRadius: '0.375rem',
                    color: '#065f46',
                    marginBottom: '1rem'
                  }}>
                    Día ya confirmado{fechaSeleccionada && detalleDia.dia?.confirmadoPorNombre ? ` por ${detalleDia.dia.confirmadoPorNombre}` : ''}.
                  </div>
                )}
              </>
            ) : (
              <p>No hay datos de alimentación para esta fecha.</p>
            )}

            {detalleDia && detalleDia.dia?.estado !== 'PENDIENTE' && (
              <button
                onClick={() => setModalAbierto(false)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#6b7280',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: 'pointer'
                }}
              >
                Cerrar
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CalendarioAlimentacionPorcinos;
