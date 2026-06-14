import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Typography,
  Button,
  Tabs,
  Tab,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TableContainer,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Stack,
  IconButton,
  Chip,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { insumosService } from '../../../services/domain/insumosLaboresServices';
import weatherService from '../../../services/weatherService';
import {
  obtenerLoteHuevos,
  obtenerResumenHuevos,
  listarProduccionHuevos,
  registrarProduccionHuevos,
  listarConsumosHuevos,
  registrarConsumoHuevos,
  actualizarConsumoHuevos,
  registrarAjustePlantelHuevos,
  listarAjustesPlantelHuevos,
  listarEventosSanitariosHuevos,
  registrarEventoSanitarioHuevos,
  AvicolaHuevoLoteRespuesta,
  AvicolaHuevosResumenRespuesta,
  AvicolaHuevoProduccionDiariaRespuesta,
  AvicolaHuevoConsumoRespuesta,
  AvicolaHuevoEventoSanitarioRespuesta,
  AvicolaHuevoAjustePlantelRespuesta,
  mensajeError,
} from '../services/avicolaHuevosApi';
import {
  exportarProduccionDiariaHuevos,
  type FormatoExportacionAvicola,
} from '../../../utilidades/exportacionAvicola';

interface InsumoOpcion {
  id: number;
  nombre: string;
}

const TIPOS_EVENTO_SANITARIO: { valor: string; etiqueta: string }[] = [
  { valor: 'VACUNACION', etiqueta: 'Vacunación' },
  { valor: 'TRATAMIENTO', etiqueta: 'Tratamiento' },
  { valor: 'DIAGNOSTICO', etiqueta: 'Diagnóstico' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

const DetalleLoteHuevosScreen: React.FC = () => {
  const { loteId: loteIdParam } = useParams<{ loteId: string }>();
  const navigate = useNavigate();
  const loteId = loteIdParam ? parseInt(loteIdParam, 10) : NaN;

  const [pestana, setPestana] = useState(0);
  const [lote, setLote] = useState<AvicolaHuevoLoteRespuesta | null>(null);
  const [resumen, setResumen] = useState<AvicolaHuevosResumenRespuesta | null>(null);
  const [produccion, setProduccion] = useState<AvicolaHuevoProduccionDiariaRespuesta[]>([]);
  const [consumos, setConsumos] = useState<AvicolaHuevoConsumoRespuesta[]>([]);
  const [eventos, setEventos] = useState<AvicolaHuevoEventoSanitarioRespuesta[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogo, setDialogo] = useState<'produccion' | 'consumo' | 'sanidad' | 'ajuste' | 'editarConsumo' | null>(null);
  const hoy = () => new Date().toISOString().slice(0, 10);

  const [fProd, setFProd] = useState(hoy());
  const [tam1, setTam1] = useState('0');
  const [tam2, setTam2] = useState('0');
  const [tam3, setTam3] = useState('0');
  const [tam4, setTam4] = useState('0');
  const [rotos, setRotos] = useState('0');
  const [tempProd, setTempProd] = useState('');
  const [humProd, setHumProd] = useState('');
  const [obsProd, setObsProd] = useState('');
  const [cargandoClimaProduccion, setCargandoClimaProduccion] = useState(false);

  const [fCons, setFCons] = useState(hoy());
  const [insCons, setInsCons] = useState<number | ''>('');
  const [cantCons, setCantCons] = useState('');
  const [obsCons, setObsCons] = useState('');
  const [consumoEditId, setConsumoEditId] = useState<number | null>(null);

  const [avesNueva, setAvesNueva] = useState('');
  const [motivoAjuste, setMotivoAjuste] = useState('');
  const [ajustes, setAjustes] = useState<AvicolaHuevoAjustePlantelRespuesta[]>([]);

  const [fSan, setFSan] = useState(hoy());
  const [tipoSan, setTipoSan] = useState('VACUNACION');
  const [descSan, setDescSan] = useState('');
  const [insSan, setInsSan] = useState<number | ''>('');
  const [dosisSan, setDosisSan] = useState('');
  const [obsSan, setObsSan] = useState('');

  const loteCerrado = lote?.estado === 'CERRADO';

  const normalizarFechaSoloDia = useCallback((f: string | undefined | null) => {
    if (f == null || f === '') return '';
    const s = String(f).trim();
    return s.length >= 10 ? s.slice(0, 10) : s;
  }, []);

  /** Cambia solo si cambia la fecha elegida o los datos de servidor para ese día (evita pisar edición local al refrescar la lista). */
  const huellaProduccionServidorParaFecha = useMemo(() => {
    const dia = fProd.slice(0, 10);
    const r = produccion.find((p) => normalizarFechaSoloDia(p.fecha) === dia);
    if (!r) return `${dia}|_`;
    return [
      dia,
      r.id,
      r.huevosTam1 ?? 0,
      r.huevosTam2 ?? 0,
      r.huevosTam3 ?? 0,
      r.huevosTam4 ?? 0,
      r.huevosRotos ?? 0,
      r.temperaturaDia ?? '',
      r.humedadDia ?? '',
      r.observaciones ?? '',
    ].join('|');
  }, [produccion, fProd, normalizarFechaSoloDia]);

  const aplicarProduccionRegistradaAlFormulario = useCallback(
    (fechaSeleccion: string) => {
      const dia = fechaSeleccion.slice(0, 10);
      const reg = produccion.find((p) => normalizarFechaSoloDia(p.fecha) === dia);
      if (reg) {
        setTam1(String(reg.huevosTam1 ?? 0));
        setTam2(String(reg.huevosTam2 ?? 0));
        setTam3(String(reg.huevosTam3 ?? 0));
        setTam4(String(reg.huevosTam4 ?? 0));
        setRotos(String(reg.huevosRotos ?? 0));
        setTempProd(reg.temperaturaDia != null ? String(reg.temperaturaDia) : '');
        setHumProd(reg.humedadDia != null ? String(reg.humedadDia) : '');
        setObsProd(reg.observaciones ?? '');
      } else {
        setTam1('0');
        setTam2('0');
        setTam3('0');
        setTam4('0');
        setRotos('0');
        setTempProd('');
        setHumProd('');
        setObsProd('');
      }
    },
    [produccion, normalizarFechaSoloDia]
  );

  useEffect(() => {
    if (dialogo !== 'produccion') return;
    aplicarProduccionRegistradaAlFormulario(fProd);
  }, [dialogo, fProd, huellaProduccionServidorParaFecha, aplicarProduccionRegistradaAlFormulario]);

  const totalProduccionCalculado = useMemo(() => {
    const n = (s: string) => {
      const x = parseInt(s, 10);
      return Number.isNaN(x) ? 0 : Math.max(0, x);
    };
    return n(tam1) + n(tam2) + n(tam3) + n(tam4) + n(rotos);
  }, [tam1, tam2, tam3, tam4, rotos]);

  const mapaNombreInsumo = useMemo(() => {
    const m = new Map<number, string>();
    insumos.forEach((i) => m.set(i.id, i.nombre));
    return m;
  }, [insumos]);

  const etiquetaInsumoConsumo = useCallback(
    (c: AvicolaHuevoConsumoRespuesta) =>
      c.insumoNombre?.trim() || mapaNombreInsumo.get(c.insumoId) || `Insumo #${c.insumoId}`,
    [mapaNombreInsumo]
  );

  const etiquetaInsumoEvento = useCallback(
    (ev: AvicolaHuevoEventoSanitarioRespuesta) => {
      if (!ev.insumoId) return '—';
      return ev.insumoNombre?.trim() || mapaNombreInsumo.get(ev.insumoId) || `Insumo #${ev.insumoId}`;
    },
    [mapaNombreInsumo]
  );

  const recargarListas = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    const [p, c, e, r, lt, aj] = await Promise.all([
      listarProduccionHuevos(loteId),
      listarConsumosHuevos(loteId),
      listarEventosSanitariosHuevos(loteId),
      obtenerResumenHuevos(loteId),
      obtenerLoteHuevos(loteId),
      listarAjustesPlantelHuevos(loteId),
    ]);
    setProduccion(p);
    setConsumos(c);
    setEventos(e);
    setResumen(r);
    setLote(lt);
    setAjustes(aj);
  }, [loteId]);

  const cargarInicial = useCallback(async () => {
    if (Number.isNaN(loteId)) {
      setError('Identificador de lote inválido');
      setCargando(false);
      return;
    }
    try {
      setCargando(true);
      setError(null);
      await Promise.all([
        insumosService
          .listar()
          .then((rawInsumos) => {
            const arr = Array.isArray(rawInsumos) ? rawInsumos : [];
            setInsumos(
              arr.map((x: Record<string, unknown>) => ({
                id: Number(x.id),
                nombre: String(x.nombre ?? x.descripcion ?? `Insumo ${x.id}`),
              }))
            );
          })
          .catch(() => setInsumos([])),
        recargarListas(),
      ]);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [loteId, recargarListas]);

  useEffect(() => {
    cargarInicial();
  }, [cargarInicial]);

  const cerrarDialogos = () => {
    setDialogo(null);
    setConsumoEditId(null);
  };

  const exportarTablaProduccion = async (formato: FormatoExportacionAvicola) => {
    if (!lote) return;
    if (produccion.length === 0) {
      setError('No hay filas de producción para exportar.');
      return;
    }
    try {
      setError(null);
      await exportarProduccionDiariaHuevos(produccion, lote.nombre ?? `Lote ${lote.id}`, formato);
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const rellenarTemperaturaHumedadDesdeClima = async () => {
    const lat = lote?.climaLatitud;
    const lon = lote?.climaLongitud;
    if (lat == null || lon == null) return;
    try {
      setCargandoClimaProduccion(true);
      const datos = await weatherService.getWeatherByCoordinates(lat, lon);
      const t = datos.current.temperature;
      const h = datos.current.humidity;
      setTempProd(Number.isFinite(t) ? String(Math.round(t * 10) / 10) : '');
      setHumProd(Number.isFinite(h) ? String(Math.round(h * 10) / 10) : '');
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargandoClimaProduccion(false);
    }
  };

  const guardarProduccion = async () => {
    try {
      const parseOptDec = (s: string) => {
        if (!s.trim()) return null;
        const v = parseFloat(s.replace(',', '.'));
        return Number.isNaN(v) ? null : v;
      };
      await registrarProduccionHuevos(loteId, {
        fecha: fProd,
        huevosTam1: parseInt(tam1, 10) || 0,
        huevosTam2: parseInt(tam2, 10) || 0,
        huevosTam3: parseInt(tam3, 10) || 0,
        huevosTam4: parseInt(tam4, 10) || 0,
        huevosRotos: parseInt(rotos, 10) || 0,
        temperaturaDia: parseOptDec(tempProd),
        humedadDia: parseOptDec(humProd),
        observaciones: obsProd.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarConsumo = async () => {
    try {
      if (insCons === '') {
        setError('Seleccioná un insumo');
        return;
      }
      const cant = parseFloat(cantCons.replace(',', '.'));
      if (Number.isNaN(cant) || cant <= 0) {
        setError('Cantidad inválida');
        return;
      }
      await registrarConsumoHuevos(loteId, {
        insumoId: Number(insCons),
        fecha: fCons,
        cantidad: cant,
        tipo: 'MANUAL',
        observaciones: obsCons.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarEdicionConsumo = async () => {
    if (consumoEditId == null) return;
    try {
      const cant = parseFloat(cantCons.replace(',', '.'));
      if (Number.isNaN(cant) || cant <= 0) {
        setError('Cantidad inválida');
        return;
      }
      await actualizarConsumoHuevos(loteId, consumoEditId, {
        fecha: fCons,
        cantidad: cant,
        observaciones: obsCons.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarAjustePlantel = async () => {
    try {
      const n = parseInt(avesNueva, 10);
      if (Number.isNaN(n) || n < 0) {
        setError('Cantidad de aves inválida');
        return;
      }
      if (!motivoAjuste.trim()) {
        setError('El motivo del ajuste es obligatorio');
        return;
      }
      await registrarAjustePlantelHuevos(loteId, { cantidadAvesNueva: n, motivo: motivoAjuste.trim() });
      cerrarDialogos();
      setAvesNueva('');
      setMotivoAjuste('');
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarSanidad = async () => {
    try {
      if (!tipoSan.trim()) {
        setError('El tipo de evento es obligatorio');
        return;
      }
      await registrarEventoSanitarioHuevos(loteId, {
        fecha: fSan,
        tipo: tipoSan.trim(),
        descripcion: descSan.trim() || null,
        insumoId: insSan === '' ? undefined : Number(insSan),
        dosis: dosisSan.trim() === '' ? undefined : parseFloat(dosisSan.replace(',', '.')),
        observaciones: obsSan.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  if (Number.isNaN(loteId)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Lote no válido</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/avicola-huevos/lotes')}>
          Volver a lotes
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" flexWrap="wrap" alignItems="center" gap={2} mb={2}>
        <Button variant="outlined" onClick={() => navigate('/avicola-huevos/lotes')}>
          ← Lotes
        </Button>
        {lote && (
          <Typography variant="h5" component="h1">
            {lote.nombre}
            {loteCerrado && <Chip sx={{ ml: 1 }} size="small" label="Cerrado" />}
          </Typography>
        )}
      </Box>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && lote && (
        <>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }} variant="scrollable">
            <Tab label="Resumen" />
            <Tab label="Producción diaria" />
            <Tab label="Consumos" />
            <Tab label="Sanidad" />
          </Tabs>

          {pestana === 0 && lote && (
            <>
              <Paper sx={{ p: 2, mb: 2 }}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Datos del lote
                </Typography>
                <Stack spacing={0.5}>
                  <Typography>
                    <strong>Establecimiento:</strong> {lote.establecimientoNombre ?? '—'}
                  </Typography>
                  <Typography>
                    <strong>Raza / línea:</strong> {lote.razaNombre ?? '—'}
                  </Typography>
                  <Typography>
                    <strong>Inicio de postura:</strong> {lote.fechaInicio?.slice(0, 10) ?? '—'}
                  </Typography>
                  <Typography>
                    <strong>Aves actuales:</strong> {lote.cantidadAvesActual ?? lote.cantidadAvesInicial ?? '—'}
                  </Typography>
                  {lote.observaciones ? (
                    <Typography variant="body2" color="text.secondary">
                      <strong>Observaciones:</strong> {lote.observaciones}
                    </Typography>
                  ) : null}
                </Stack>
              </Paper>
              {resumen ? (
                <Paper sx={{ p: 2 }}>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Indicadores
                  </Typography>
                  <Stack spacing={1}>
                    <Typography>
                      <strong>Aves actuales (cuadro):</strong> {resumen.cantidadAvesActual ?? '—'}
                    </Typography>
                    <Typography>
                      <strong>Total huevos registrados:</strong> {resumen.totalHuevosProducidos ?? '—'}
                    </Typography>
                    <Typography>
                      <strong>Suma consumos insumo:</strong>{' '}
                      {resumen.sumaConsumosInsumo != null ? String(resumen.sumaConsumosInsumo) : '—'}
                    </Typography>
                    <Typography>
                      <strong>Días en postura:</strong> {resumen.diasEnPostura ?? '—'}
                    </Typography>
                    <Typography>
                      <strong>Huevos / ave / día:</strong>{' '}
                      {resumen.huevosPromedioPorAveYdia != null ? String(resumen.huevosPromedioPorAveYdia) : '—'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
                      Si registrás otra vez la misma fecha en producción diaria, se actualiza el registro (único por
                      día en el servidor).
                    </Typography>
                  </Stack>
                </Paper>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  No se pudo cargar el resumen numérico.
                </Typography>
              )}
              <Button
                variant="outlined"
                sx={{ mt: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setAvesNueva(String(lote.cantidadAvesActual ?? lote.cantidadAvesInicial ?? ''));
                  setMotivoAjuste('');
                  setDialogo('ajuste');
                }}
              >
                Ajustar cantidad de aves (con motivo)
              </Button>
              {ajustes.length > 0 && (
                <Paper sx={{ p: 2, mt: 2 }}>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                    Historial de ajustes de plantel
                  </Typography>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Fecha</TableCell>
                        <TableCell align="right">Anterior</TableCell>
                        <TableCell align="right">Nueva</TableCell>
                        <TableCell>Motivo</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {ajustes.map((a) => (
                        <TableRow key={a.id}>
                          <TableCell>{a.fechaHora?.slice(0, 16)?.replace('T', ' ') ?? '—'}</TableCell>
                          <TableCell align="right">{a.cantidadAvesAnterior}</TableCell>
                          <TableCell align="right">{a.cantidadAvesNueva}</TableCell>
                          <TableCell>{a.motivo}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </Paper>
              )}
            </>
          )}

          {pestana === 1 && (
            <Box>
              <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap alignItems="center">
                <Button
                  variant="contained"
                  disabled={loteCerrado}
                  onClick={() => {
                    setFProd(hoy());
                    setDialogo('produccion');
                  }}
                >
                  Registrar o actualizar día
                </Button>
                <Button variant="outlined" disabled={produccion.length === 0} onClick={() => void exportarTablaProduccion('excel')}>
                  Exportar Excel
                </Button>
                <Button variant="outlined" disabled={produccion.length === 0} onClick={() => void exportarTablaProduccion('pdf')}>
                  Exportar PDF
                </Button>
              </Stack>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell align="right">T1</TableCell>
                      <TableCell align="right">T2</TableCell>
                      <TableCell align="right">T3</TableCell>
                      <TableCell align="right">T4</TableCell>
                      <TableCell align="right">Rotos</TableCell>
                      <TableCell align="right">Total</TableCell>
                      <TableCell align="right">Temp. °C</TableCell>
                      <TableCell align="right">Hum. %</TableCell>
                      <TableCell>Obs.</TableCell>
                      <TableCell align="right">Acciones</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {produccion.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={11}>
                          <Typography color="text.secondary" variant="body2">
                            Todavía no hay producción registrada. Usá «Registrar o actualizar día».
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      produccion.map((p) => (
                        <TableRow key={p.id}>
                          <TableCell>{p.fecha}</TableCell>
                          <TableCell align="right">{p.huevosTam1 ?? 0}</TableCell>
                          <TableCell align="right">{p.huevosTam2 ?? 0}</TableCell>
                          <TableCell align="right">{p.huevosTam3 ?? 0}</TableCell>
                          <TableCell align="right">{p.huevosTam4 ?? 0}</TableCell>
                          <TableCell align="right">{p.huevosRotos ?? 0}</TableCell>
                          <TableCell align="right">{p.totalHuevosDia ?? p.cantidadHuevos}</TableCell>
                          <TableCell align="right">{p.temperaturaDia != null ? String(p.temperaturaDia) : '—'}</TableCell>
                          <TableCell align="right">{p.humedadDia != null ? String(p.humedadDia) : '—'}</TableCell>
                          <TableCell>{p.observaciones ?? '—'}</TableCell>
                          <TableCell align="right">
                            <IconButton
                              size="small"
                              disabled={loteCerrado}
                              aria-label="Editar producción del día"
                              onClick={() => {
                                setFProd(normalizarFechaSoloDia(p.fecha) || hoy());
                                setDialogo('produccion');
                              }}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 2 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setFCons(hoy());
                  setInsCons('');
                  setCantCons('');
                  setObsCons('');
                  setDialogo('consumo');
                }}
              >
                Registrar consumo
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Insumo</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell>Obs.</TableCell>
                      <TableCell align="right">Acciones</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {consumos.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={5}>
                          <Typography color="text.secondary" variant="body2">
                            Sin consumos. Registrá alimento o insumos desde el botón superior.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      consumos.map((c) => (
                        <TableRow key={c.id}>
                          <TableCell>{c.fecha}</TableCell>
                          <TableCell>{etiquetaInsumoConsumo(c)}</TableCell>
                          <TableCell align="right">{String(c.cantidad)}</TableCell>
                          <TableCell>{c.observaciones ?? '—'}</TableCell>
                          <TableCell align="right">
                            <IconButton
                              size="small"
                              disabled={loteCerrado}
                              aria-label="Editar consumo"
                              onClick={() => {
                                setConsumoEditId(c.id);
                                setFCons(c.fecha);
                                setInsCons(c.insumoId);
                                setCantCons(String(c.cantidad));
                                setObsCons(c.observaciones ?? '');
                                setDialogo('editarConsumo');
                              }}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 3 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setFSan(hoy());
                  setTipoSan('VACUNACION');
                  setDescSan('');
                  setInsSan('');
                  setDosisSan('');
                  setObsSan('');
                  setDialogo('sanidad');
                }}
              >
                Registrar evento
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell>Descripción</TableCell>
                      <TableCell>Insumo</TableCell>
                      <TableCell align="right">Dosis</TableCell>
                      <TableCell>Obs.</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {eventos.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6}>
                          <Typography color="text.secondary" variant="body2">
                            Sin eventos sanitarios.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      eventos.map((e) => (
                        <TableRow key={e.id}>
                          <TableCell>{e.fecha}</TableCell>
                          <TableCell>{e.tipo}</TableCell>
                          <TableCell>{e.descripcion ?? '—'}</TableCell>
                          <TableCell>{etiquetaInsumoEvento(e)}</TableCell>
                          <TableCell align="right">{e.dosis != null ? String(e.dosis) : '—'}</TableCell>
                          <TableCell>{e.observaciones ?? '—'}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </>
      )}

      <Dialog open={dialogo === 'produccion'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Producción del día</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              type="date"
              label="Fecha"
              InputLabelProps={{ shrink: true }}
              fullWidth
              value={fProd}
              onChange={(ev) => setFProd(ev.target.value)}
              helperText="Si ya hay registro para esa fecha, se cargan los valores para editarlos."
            />
            <Typography variant="caption" color="text.secondary">
              Total del día (suma automática): <strong>{totalProduccionCalculado}</strong> huevos (tamaños 1–4 más rotos).
            </Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              <TextField label="Tam. 1" type="number" sx={{ width: 100 }} value={tam1} onChange={(ev) => setTam1(ev.target.value)} inputProps={{ min: 0 }} />
              <TextField label="Tam. 2" type="number" sx={{ width: 100 }} value={tam2} onChange={(ev) => setTam2(ev.target.value)} inputProps={{ min: 0 }} />
              <TextField label="Tam. 3" type="number" sx={{ width: 100 }} value={tam3} onChange={(ev) => setTam3(ev.target.value)} inputProps={{ min: 0 }} />
              <TextField label="Tam. 4" type="number" sx={{ width: 100 }} value={tam4} onChange={(ev) => setTam4(ev.target.value)} inputProps={{ min: 0 }} />
              <TextField label="Rotos" type="number" sx={{ width: 100 }} value={rotos} onChange={(ev) => setRotos(ev.target.value)} inputProps={{ min: 0 }} />
            </Stack>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              <TextField label="Temperatura (°C)" fullWidth value={tempProd} onChange={(ev) => setTempProd(ev.target.value)} placeholder="Opcional" />
              <TextField label="Humedad (%)" fullWidth value={humProd} onChange={(ev) => setHumProd(ev.target.value)} placeholder="Opcional" />
            </Stack>
            <Box>
              <Button
                type="button"
                variant="outlined"
                size="small"
                disabled={
                  loteCerrado ||
                  lote?.climaLatitud == null ||
                  lote?.climaLongitud == null ||
                  cargandoClimaProduccion
                }
                onClick={() => void rellenarTemperaturaHumedadDesdeClima()}
              >
                {cargandoClimaProduccion ? 'Consultando clima…' : 'Rellenar con clima del establecimiento'}
              </Button>
              {(lote?.climaLatitud == null || lote?.climaLongitud == null) && (
                <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
                  Definí la ubicación en el mapa del establecimiento (catálogo de huevos) para usar esta acción. Los
                  valores siguen siendo editables a mano.
                </Typography>
              )}
            </Box>
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsProd} onChange={(ev) => setObsProd(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarProduccion}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'consumo' || dialogo === 'editarConsumo'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>{dialogo === 'editarConsumo' ? 'Editar consumo del día' : 'Consumo de insumo'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {insumos.length === 0 && (
              <Alert severity="warning">
                No hay insumos disponibles para esta empresa. Cargá insumos en el módulo de cultivos para poder
                registrar consumos.
              </Alert>
            )}
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fCons} onChange={(ev) => setFCons(ev.target.value)} />
            <TextField
              select
              label="Insumo"
              fullWidth
              disabled={dialogo === 'editarConsumo'}
              value={insCons}
              onChange={(ev) => setInsCons(ev.target.value === '' ? '' : Number(ev.target.value))}
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {insumos.map((i) => (
                <MenuItem key={i.id} value={i.id}>
                  {i.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Cantidad" fullWidth value={cantCons} onChange={(ev) => setCantCons(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsCons} onChange={(ev) => setObsCons(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={dialogo === 'editarConsumo' ? guardarEdicionConsumo : guardarConsumo}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'ajuste'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Ajuste de plantel</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Alert severity="info">
              Queda registrado el motivo y el cambio de aves. El inventario de alimento no se modifica automáticamente.
            </Alert>
            <TextField
              label="Nueva cantidad de aves"
              type="number"
              fullWidth
              value={avesNueva}
              onChange={(ev) => setAvesNueva(ev.target.value)}
              inputProps={{ min: 0 }}
            />
            <TextField label="Motivo del cambio" fullWidth multiline minRows={3} value={motivoAjuste} onChange={(ev) => setMotivoAjuste(ev.target.value)} required />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarAjustePlantel}>
            Registrar ajuste
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'sanidad'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Evento sanitario</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fSan} onChange={(ev) => setFSan(ev.target.value)} />
            <TextField
              select
              label="Tipo de evento"
              fullWidth
              value={tipoSan}
              onChange={(ev) => setTipoSan(ev.target.value)}
            >
              {TIPOS_EVENTO_SANITARIO.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>
                  {t.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Descripción" fullWidth multiline minRows={2} value={descSan} onChange={(ev) => setDescSan(ev.target.value)} />
            <TextField select label="Insumo (opcional)" fullWidth value={insSan} onChange={(ev) => setInsSan(ev.target.value === '' ? '' : Number(ev.target.value))}>
              <MenuItem value="">Ninguno</MenuItem>
              {insumos.map((i) => (
                <MenuItem key={i.id} value={i.id}>
                  {i.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Dosis (opcional)" fullWidth value={dosisSan} onChange={(ev) => setDosisSan(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsSan} onChange={(ev) => setObsSan(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarSanidad}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DetalleLoteHuevosScreen;
