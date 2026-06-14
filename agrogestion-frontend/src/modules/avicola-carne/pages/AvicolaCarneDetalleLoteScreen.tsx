import React, { useCallback, useEffect, useState } from 'react';
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
  Chip,
} from '@mui/material';
import { insumosService } from '../../../services/domain/insumosLaboresServices';
import type {
  AvicolaCarneLote,
  AvicolaCarneTipoVenta,
  Consumo,
  EventoSanitario,
  Muerte,
  Pesada,
  Resumen,
  Venta,
} from '../types';
import {
  obtenerLoteCarne,
  obtenerResumenCarne,
  listarPesadasCarne,
  registrarPesadaCarne,
  listarMuertesCarne,
  registrarMuerteCarne,
  listarVentasCarne,
  registrarVentaCarne,
  listarConsumosCarne,
  registrarConsumoCarne,
  listarEventosSanitariosCarne,
  registrarEventoSanitarioCarne,
  mensajeErrorCarne,
} from '../services/avicolaCarneService';
import {
  exportarOperacionesLoteCarne,
  type FormatoExportacionAvicola,
} from '../../../utilidades/exportacionAvicola';

const TIPOS_VENTA: { valor: AvicolaCarneTipoVenta; etiqueta: string }[] = [
  { valor: 'FAENA', etiqueta: 'Faena' },
  { valor: 'VENTA_EN_PIE', etiqueta: 'Venta en pie' },
  { valor: 'DESCARTE', etiqueta: 'Descarte' },
];

interface InsumoOpcion {
  id: number;
  nombre: string;
}

const AvicolaCarneDetalleLoteScreen: React.FC = () => {
  const { id: idParam } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const loteId = idParam ? parseInt(idParam, 10) : NaN;

  const [pestana, setPestana] = useState(0);
  const [lote, setLote] = useState<AvicolaCarneLote | null>(null);
  const [resumen, setResumen] = useState<Resumen | null>(null);
  const [pesadas, setPesadas] = useState<Pesada[]>([]);
  const [muertes, setMuertes] = useState<Muerte[]>([]);
  const [ventas, setVentas] = useState<Venta[]>([]);
  const [consumos, setConsumos] = useState<Consumo[]>([]);
  const [eventos, setEventos] = useState<EventoSanitario[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogo, setDialogo] = useState<
    'pesada' | 'muerte' | 'venta' | 'consumo' | 'sanidad' | null
  >(null);

  const hoy = () => new Date().toISOString().slice(0, 10);

  const [fPesada, setFPesada] = useState(hoy());
  const [pesoProm, setPesoProm] = useState('');
  const [cantPesada, setCantPesada] = useState('');
  const [obsPesada, setObsPesada] = useState('');

  const [fMuerte, setFMuerte] = useState(hoy());
  const [cantMuerte, setCantMuerte] = useState('');
  const [causaMuerte, setCausaMuerte] = useState('');
  const [obsMuerte, setObsMuerte] = useState('');

  const [fVenta, setFVenta] = useState(hoy());
  const [tipoVenta, setTipoVenta] = useState<AvicolaCarneTipoVenta>('VENTA_EN_PIE');
  const [cantVenta, setCantVenta] = useState('');
  const [pesoVenta, setPesoVenta] = useState('');
  const [puVenta, setPuVenta] = useState('');
  const [totalVenta, setTotalVenta] = useState('');
  const [comprador, setComprador] = useState('');
  const [obsVenta, setObsVenta] = useState('');

  const [fConsumo, setFConsumo] = useState(hoy());
  const [insumoConsumo, setInsumoConsumo] = useState<number | ''>('');
  const [cantConsumo, setCantConsumo] = useState('');
  const [obsConsumo, setObsConsumo] = useState('');

  const [fSan, setFSan] = useState(hoy());
  const [tipoSan, setTipoSan] = useState('');
  const [descSan, setDescSan] = useState('');
  const [insumoSan, setInsumoSan] = useState<number | ''>('');
  const [dosisSan, setDosisSan] = useState('');
  const [obsSan, setObsSan] = useState('');

  const loteCerrado = lote?.estado === 'CERRADO';

  const recargarListas = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    const [p, m, v, c, e] = await Promise.all([
      listarPesadasCarne(loteId),
      listarMuertesCarne(loteId),
      listarVentasCarne(loteId),
      listarConsumosCarne(loteId),
      listarEventosSanitariosCarne(loteId),
    ]);
    setPesadas(p);
    setMuertes(m);
    setVentas(v);
    setConsumos(c);
    setEventos(e);
    const r = await obtenerResumenCarne(loteId);
    setResumen(r);
    const lt = await obtenerLoteCarne(loteId);
    setLote(lt);
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
      const rawInsumos = await insumosService.listar();
      const arr = Array.isArray(rawInsumos) ? rawInsumos : [];
      setInsumos(
        arr.map((x: Record<string, unknown>) => ({
          id: Number(x.id),
          nombre: String(x.nombre ?? x.descripcion ?? `Insumo ${x.id}`),
        }))
      );
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    } finally {
      setCargando(false);
    }
  }, [loteId, recargarListas]);

  useEffect(() => {
    cargarInicial();
  }, [cargarInicial]);

  const cerrarDialogos = () => setDialogo(null);

  const exportarLote = async (formato: FormatoExportacionAvicola) => {
    if (!lote) return;
    try {
      setError(null);
      const nomInsumo = (id: number) => insumos.find((i) => i.id === id)?.nombre ?? `Insumo ${id}`;
      await exportarOperacionesLoteCarne(
        {
          nombreLote: lote.nombre ?? `Lote ${lote.id}`,
          pesadas,
          muertes,
          ventas,
          consumos,
          eventos,
          nombreInsumo: nomInsumo,
        },
        formato
      );
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    }
  };

  const guardarPesada = async () => {
    try {
      const pp = parseFloat(pesoProm.replace(',', '.'));
      if (Number.isNaN(pp) || pp <= 0) {
        setError('Peso promedio inválido');
        return;
      }
      const cp = cantPesada.trim() === '' ? undefined : parseInt(cantPesada, 10);
      await registrarPesadaCarne(loteId, {
        fecha: fPesada,
        pesoPromedio: pp,
        cantidadPesada: cp,
        observaciones: obsPesada.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    }
  };

  const guardarMuerte = async () => {
    try {
      const c = parseInt(cantMuerte, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad de muertes inválida');
        return;
      }
      await registrarMuerteCarne(loteId, {
        fecha: fMuerte,
        cantidad: c,
        causa: causaMuerte.trim() || null,
        observaciones: obsMuerte.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    }
  };

  const guardarVenta = async () => {
    try {
      const c = parseInt(cantVenta, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad de venta inválida');
        return;
      }
      await registrarVentaCarne(loteId, {
        fecha: fVenta,
        tipo: tipoVenta,
        cantidad: c,
        pesoPromedio: pesoVenta.trim() === '' ? undefined : parseFloat(pesoVenta.replace(',', '.')),
        precioUnitario: puVenta.trim() === '' ? undefined : parseFloat(puVenta.replace(',', '.')),
        total: totalVenta.trim() === '' ? undefined : parseFloat(totalVenta.replace(',', '.')),
        comprador: comprador.trim() || null,
        observaciones: obsVenta.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    }
  };

  const guardarConsumo = async () => {
    try {
      if (insumoConsumo === '') {
        setError('Seleccioná un insumo');
        return;
      }
      const cant = parseFloat(cantConsumo.replace(',', '.'));
      if (Number.isNaN(cant) || cant <= 0) {
        setError('Cantidad inválida');
        return;
      }
      await registrarConsumoCarne(loteId, {
        insumoId: Number(insumoConsumo),
        fecha: fConsumo,
        cantidad: cant,
        tipo: 'MANUAL',
        observaciones: obsConsumo.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    }
  };

  const guardarSanidad = async () => {
    try {
      if (!tipoSan.trim()) {
        setError('El tipo de evento es obligatorio');
        return;
      }
      await registrarEventoSanitarioCarne(loteId, {
        fecha: fSan,
        tipo: tipoSan.trim(),
        descripcion: descSan.trim() || null,
        insumoId: insumoSan === '' ? undefined : Number(insumoSan),
        dosis: dosisSan.trim() === '' ? undefined : parseFloat(dosisSan.replace(',', '.')),
        observaciones: obsSan.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    }
  };

  if (Number.isNaN(loteId)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Lote no válido</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/avicola-carne/lotes')}>
          Volver a lotes
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" flexWrap="wrap" alignItems="center" gap={2} mb={2}>
        <Button variant="outlined" onClick={() => navigate('/avicola-carne/lotes')}>
          ← Lotes
        </Button>
        {lote && (
          <Typography variant="h5" component="h1">
            {lote.nombre}
            {loteCerrado && (
              <Chip sx={{ ml: 1 }} size="small" label="Cerrado" color="default" />
            )}
          </Typography>
        )}
      </Box>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando detalle…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && lote && (
        <>
          <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
            <Typography variant="body2" color="text.secondary" sx={{ alignSelf: 'center', mr: 1 }}>
              Exportar operaciones del lote:
            </Typography>
            <Button size="small" variant="outlined" onClick={() => void exportarLote('excel')}>
              Excel
            </Button>
            <Button size="small" variant="outlined" onClick={() => void exportarLote('pdf')}>
              PDF
            </Button>
          </Stack>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }} variant="scrollable">
            <Tab label="Resumen" />
            <Tab label="Pesadas" />
            <Tab label="Muertes" />
            <Tab label="Ventas" />
            <Tab label="Consumos" />
            <Tab label="Sanidad" />
          </Tabs>

          {pestana === 0 && resumen && (
            <Paper sx={{ p: 2 }}>
              <Stack spacing={1}>
                <Typography>
                  <strong>Aves disponibles:</strong> {resumen.cantidadDisponible ?? '—'}
                </Typography>
                <Typography>
                  <strong>Mortalidad (%):</strong>{' '}
                  {resumen.mortalidadPct != null ? Number(resumen.mortalidadPct).toFixed(2) : '—'}
                </Typography>
                <Typography>
                  <strong>Conversión alimenticia:</strong>{' '}
                  {resumen.conversionAlimenticia != null ? String(resumen.conversionAlimenticia) : '—'}
                </Typography>
                <Typography>
                  <strong>Días en producción:</strong> {resumen.diasEnProduccion ?? '—'}
                </Typography>
              </Stack>
            </Paper>
          )}

          {pestana === 1 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setFPesada(hoy());
                  setPesoProm('');
                  setCantPesada('');
                  setObsPesada('');
                  setDialogo('pesada');
                }}
              >
                Registrar pesada
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell align="right">Peso prom. (kg)</TableCell>
                      <TableCell align="right">Cant. pesada</TableCell>
                      <TableCell>Obs.</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pesadas.map((p) => (
                      <TableRow key={p.id}>
                        <TableCell>{p.fecha}</TableCell>
                        <TableCell align="right">{String(p.pesoPromedio)}</TableCell>
                        <TableCell align="right">{p.cantidadPesada ?? '—'}</TableCell>
                        <TableCell>{p.observaciones ?? '—'}</TableCell>
                      </TableRow>
                    ))}
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
                  setFMuerte(hoy());
                  setCantMuerte('');
                  setCausaMuerte('');
                  setObsMuerte('');
                  setDialogo('muerte');
                }}
              >
                Registrar muertes
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell>Causa</TableCell>
                      <TableCell>Obs.</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {muertes.map((m) => (
                      <TableRow key={m.id}>
                        <TableCell>{m.fecha}</TableCell>
                        <TableCell align="right">{m.cantidad}</TableCell>
                        <TableCell>{m.causa ?? '—'}</TableCell>
                        <TableCell>{m.observaciones ?? '—'}</TableCell>
                      </TableRow>
                    ))}
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
                  setFVenta(hoy());
                  setTipoVenta('VENTA_EN_PIE');
                  setCantVenta('');
                  setPesoVenta('');
                  setPuVenta('');
                  setTotalVenta('');
                  setComprador('');
                  setObsVenta('');
                  setDialogo('venta');
                }}
              >
                Registrar venta
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell align="right">Total</TableCell>
                      <TableCell>Comprador</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {ventas.map((v) => (
                      <TableRow key={v.id}>
                        <TableCell>{v.fecha}</TableCell>
                        <TableCell>{v.tipo}</TableCell>
                        <TableCell align="right">{v.cantidad}</TableCell>
                        <TableCell align="right">{v.total != null ? String(v.total) : '—'}</TableCell>
                        <TableCell>{v.comprador ?? '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 4 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setFConsumo(hoy());
                  setInsumoConsumo('');
                  setCantConsumo('');
                  setObsConsumo('');
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
                      <TableCell align="right">Insumo ID</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell>Tipo</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {consumos.map((c) => (
                      <TableRow key={c.id}>
                        <TableCell>{c.fecha}</TableCell>
                        <TableCell align="right">{c.insumoId}</TableCell>
                        <TableCell align="right">{String(c.cantidad)}</TableCell>
                        <TableCell>{c.tipo ?? '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 5 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setFSan(hoy());
                  setTipoSan('');
                  setDescSan('');
                  setInsumoSan('');
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
                      <TableCell align="right">Insumo</TableCell>
                      <TableCell align="right">Dosis</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {eventos.map((e) => (
                      <TableRow key={e.id}>
                        <TableCell>{e.fecha}</TableCell>
                        <TableCell>{e.tipo}</TableCell>
                        <TableCell>{e.descripcion ?? '—'}</TableCell>
                        <TableCell align="right">{e.insumoId ?? '—'}</TableCell>
                        <TableCell align="right">{e.dosis != null ? String(e.dosis) : '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </>
      )}

      <Dialog open={dialogo === 'pesada'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Nueva pesada</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fPesada} onChange={(ev) => setFPesada(ev.target.value)} />
            <TextField label="Peso promedio (kg)" fullWidth value={pesoProm} onChange={(ev) => setPesoProm(ev.target.value)} />
            <TextField label="Cantidad pesada (opcional)" type="number" fullWidth value={cantPesada} onChange={(ev) => setCantPesada(ev.target.value)} inputProps={{ min: 0 }} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsPesada} onChange={(ev) => setObsPesada(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarPesada}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'muerte'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar muertes</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fMuerte} onChange={(ev) => setFMuerte(ev.target.value)} />
            <TextField label="Cantidad" type="number" fullWidth value={cantMuerte} onChange={(ev) => setCantMuerte(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField label="Causa (opcional)" fullWidth value={causaMuerte} onChange={(ev) => setCausaMuerte(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsMuerte} onChange={(ev) => setObsMuerte(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarMuerte}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'venta'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar venta</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fVenta} onChange={(ev) => setFVenta(ev.target.value)} />
            <TextField select label="Tipo" fullWidth value={tipoVenta} onChange={(ev) => setTipoVenta(ev.target.value as AvicolaCarneTipoVenta)}>
              {TIPOS_VENTA.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>
                  {t.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Cantidad de aves" type="number" fullWidth value={cantVenta} onChange={(ev) => setCantVenta(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField label="Peso promedio (opcional)" fullWidth value={pesoVenta} onChange={(ev) => setPesoVenta(ev.target.value)} />
            <TextField label="Precio unitario (opcional)" fullWidth value={puVenta} onChange={(ev) => setPuVenta(ev.target.value)} />
            <TextField label="Total (opcional)" fullWidth value={totalVenta} onChange={(ev) => setTotalVenta(ev.target.value)} />
            <TextField label="Comprador" fullWidth value={comprador} onChange={(ev) => setComprador(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsVenta} onChange={(ev) => setObsVenta(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarVenta}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'consumo'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Consumo de insumo</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fConsumo} onChange={(ev) => setFConsumo(ev.target.value)} />
            <TextField select label="Insumo" fullWidth value={insumoConsumo} onChange={(ev) => setInsumoConsumo(ev.target.value === '' ? '' : Number(ev.target.value))}>
              <MenuItem value="">Seleccionar…</MenuItem>
              {insumos.map((i) => (
                <MenuItem key={i.id} value={i.id}>
                  {i.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Cantidad" fullWidth value={cantConsumo} onChange={(ev) => setCantConsumo(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsConsumo} onChange={(ev) => setObsConsumo(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarConsumo}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'sanidad'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Evento sanitario</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fSan} onChange={(ev) => setFSan(ev.target.value)} />
            <TextField label="Tipo (ej. Vacunación, Tratamiento)" fullWidth value={tipoSan} onChange={(ev) => setTipoSan(ev.target.value)} />
            <TextField label="Descripción" fullWidth multiline minRows={2} value={descSan} onChange={(ev) => setDescSan(ev.target.value)} />
            <TextField select label="Insumo asociado (opcional)" fullWidth value={insumoSan} onChange={(ev) => setInsumoSan(ev.target.value === '' ? '' : Number(ev.target.value))}>
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

export default AvicolaCarneDetalleLoteScreen;
