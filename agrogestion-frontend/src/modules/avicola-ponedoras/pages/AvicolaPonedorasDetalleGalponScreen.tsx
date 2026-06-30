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
  AvicolaPonedorasGalpon,
  Consumo,
  DescarteAves,
  DescarteMotivo,
  EventoSanitario,
  HuevoCategoria,
  Muerte,
  Postura,
  Resumen,
  VentaHuevos,
} from '../types';
import {
  obtenerGalpon,
  obtenerResumenGalpon,
  listarPosturas,
  registrarPostura,
  listarMuertes,
  registrarMuerte,
  listarConsumos,
  registrarConsumo,
  listarEventosSanitarios,
  registrarEventoSanitario,
  listarVentasHuevos,
  registrarVentaHuevos,
  listarDescarteAves,
  registrarDescarteAves,
  guardarAmbienteDiario,
  mensajeErrorPonedoras,
} from '../services/avicolaPonedorasService';
import {
  exportarOperacionesGalponPonedoras,
  type FormatoExportacionAvicola,
} from '../../../utilidades/exportacionAvicola';
import PanelClimaEstablecimiento from '../../../components/PanelClimaEstablecimiento';
import BotonRellenarClima from '../../../components/BotonRellenarClima';

interface InsumoOpcion {
  id: number;
  nombre: string;
}

const CATEGORIAS_HUEVO: { valor: HuevoCategoria; etiqueta: string }[] = [
  { valor: 'A', etiqueta: 'Categoría A' },
  { valor: 'B', etiqueta: 'Categoría B' },
  { valor: 'ROTO', etiqueta: 'Roto' },
  { valor: 'SUCIO', etiqueta: 'Sucio' },
];

const MOTIVOS_DESCARTE: { valor: DescarteMotivo; etiqueta: string }[] = [
  { valor: 'LIQUIDACION', etiqueta: 'Liquidación' },
  { valor: 'BAJA_PRODUCTIVA', etiqueta: 'Baja productiva' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

const AvicolaPonedorasDetalleGalponScreen: React.FC = () => {
  const { galponId: galponIdParam } = useParams<{ galponId: string }>();
  const navigate = useNavigate();
  const galponId = galponIdParam ? parseInt(galponIdParam, 10) : NaN;

  const [pestana, setPestana] = useState(0);
  const [galpon, setGalpon] = useState<AvicolaPonedorasGalpon | null>(null);
  const [resumen, setResumen] = useState<Resumen | null>(null);
  const [posturas, setPosturas] = useState<Postura[]>([]);
  const [muertes, setMuertes] = useState<Muerte[]>([]);
  const [consumos, setConsumos] = useState<Consumo[]>([]);
  const [eventos, setEventos] = useState<EventoSanitario[]>([]);
  const [ventas, setVentas] = useState<VentaHuevos[]>([]);
  const [descartes, setDescartes] = useState<DescarteAves[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogo, setDialogo] = useState<
    'postura' | 'muerte' | 'consumo' | 'sanidad' | 'venta' | 'descarte' | null
  >(null);
  const hoy = () => new Date().toISOString().slice(0, 10);

  const [fPost, setFPost] = useState(hoy());
  const [catPost, setCatPost] = useState<HuevoCategoria>('A');
  const [cantPost, setCantPost] = useState('');
  const [obsPost, setObsPost] = useState('');
  const [tempPost, setTempPost] = useState('');
  const [humPost, setHumPost] = useState('');

  const [fMue, setFMue] = useState(hoy());
  const [cantMue, setCantMue] = useState('');
  const [causaMue, setCausaMue] = useState('');
  const [obsMue, setObsMue] = useState('');

  const [fCons, setFCons] = useState(hoy());
  const [insCons, setInsCons] = useState<number | ''>('');
  const [cantCons, setCantCons] = useState('');
  const [obsCons, setObsCons] = useState('');

  const [fSan, setFSan] = useState(hoy());
  const [tipoSan, setTipoSan] = useState('');
  const [descSan, setDescSan] = useState('');
  const [insSan, setInsSan] = useState<number | ''>('');
  const [dosisSan, setDosisSan] = useState('');
  const [obsSan, setObsSan] = useState('');

  const [fVen, setFVen] = useState(hoy());
  const [catVen, setCatVen] = useState<HuevoCategoria>('A');
  const [cantVen, setCantVen] = useState('');
  const [puVen, setPuVen] = useState('');
  const [totVen, setTotVen] = useState('');
  const [compVen, setCompVen] = useState('');
  const [obsVen, setObsVen] = useState('');

  const [fDes, setFDes] = useState(hoy());
  const [cantDes, setCantDes] = useState('');
  const [motDes, setMotDes] = useState<DescarteMotivo>('OTRO');
  const [obsDes, setObsDes] = useState('');

  const galponCerrado = galpon?.estado === 'CERRADO';

  const recargarListas = useCallback(async () => {
    if (Number.isNaN(galponId)) return;
    const [po, mu, co, ev, ve, de, r, g] = await Promise.all([
      listarPosturas(galponId),
      listarMuertes(galponId),
      listarConsumos(galponId),
      listarEventosSanitarios(galponId),
      listarVentasHuevos(galponId),
      listarDescarteAves(galponId),
      obtenerResumenGalpon(galponId),
      obtenerGalpon(galponId),
    ]);
    setPosturas(po);
    setMuertes(mu);
    setConsumos(co);
    setEventos(ev);
    setVentas(ve);
    setDescartes(de);
    setResumen(r);
    setGalpon(g);
  }, [galponId]);

  const cargarInicial = useCallback(async () => {
    if (Number.isNaN(galponId)) {
      setError('Identificador de galpón inválido');
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
      setError(mensajeErrorPonedoras(err));
    } finally {
      setCargando(false);
    }
  }, [galponId, recargarListas]);

  useEffect(() => {
    cargarInicial();
  }, [cargarInicial]);

  const cerrarDialogos = () => setDialogo(null);

  const exportarGalpon = async (formato: FormatoExportacionAvicola) => {
    if (!galpon) return;
    try {
      setError(null);
      const nomInsumo = (id: number) => insumos.find((i) => i.id === id)?.nombre ?? `Insumo ${id}`;
      await exportarOperacionesGalponPonedoras(
        {
          nombreGalpon: galpon.nombre ?? `Galpón ${galpon.id}`,
          posturas,
          muertes,
          consumos,
          eventos,
          ventas,
          descartes,
          nombreInsumo: nomInsumo,
        },
        formato
      );
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    }
  };

  const guardarPostura = async () => {
    try {
      const n = parseInt(cantPost, 10);
      if (Number.isNaN(n) || n < 0) {
        setError('Cantidad inválida');
        return;
      }
      const parseOptDec = (s: string) => {
        if (!s.trim()) return null;
        const v = parseFloat(s.replace(',', '.'));
        return Number.isNaN(v) ? null : v;
      };
      const temp = parseOptDec(tempPost);
      const hum = parseOptDec(humPost);
      await registrarPostura(galponId, {
        fecha: fPost,
        categoriaHuevo: catPost,
        cantidad: n,
        observaciones: obsPost.trim() || null,
      });
      if (temp != null || hum != null) {
        await guardarAmbienteDiario(galponId, {
          fecha: fPost,
          temperaturaDia: temp,
          humedadDia: hum,
        });
      }
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    }
  };

  const guardarMuerte = async () => {
    try {
      const c = parseInt(cantMue, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad inválida');
        return;
      }
      await registrarMuerte(galponId, {
        fecha: fMue,
        cantidad: c,
        causa: causaMue.trim() || null,
        observaciones: obsMue.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
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
      await registrarConsumo(galponId, {
        insumoId: Number(insCons),
        fecha: fCons,
        cantidad: cant,
        tipo: 'MANUAL',
        observaciones: obsCons.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    }
  };

  const guardarSanidad = async () => {
    try {
      if (!tipoSan.trim()) {
        setError('El tipo de evento es obligatorio');
        return;
      }
      await registrarEventoSanitario(galponId, {
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
      setError(mensajeErrorPonedoras(err));
    }
  };

  const guardarVenta = async () => {
    try {
      const c = parseInt(cantVen, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad inválida');
        return;
      }
      await registrarVentaHuevos(galponId, {
        fecha: fVen,
        categoriaHuevo: catVen,
        cantidad: c,
        precioUnitario: puVen.trim() === '' ? undefined : parseFloat(puVen.replace(',', '.')),
        total: totVen.trim() === '' ? undefined : parseFloat(totVen.replace(',', '.')),
        comprador: compVen.trim() || null,
        observaciones: obsVen.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    }
  };

  const guardarDescarte = async () => {
    try {
      const c = parseInt(cantDes, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad inválida');
        return;
      }
      await registrarDescarteAves(galponId, {
        fecha: fDes,
        cantidad: c,
        motivo: motDes,
        observaciones: obsDes.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    }
  };

  if (Number.isNaN(galponId)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Galpón no válido</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/avicola-ponedoras/galpones')}>
          Volver a galpones
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" flexWrap="wrap" alignItems="center" gap={2} mb={2}>
        <Button variant="outlined" onClick={() => navigate('/avicola-ponedoras/galpones')}>
          ← Galpones
        </Button>
        {galpon && (
          <Typography variant="h5" component="h1">
            {galpon.nombre}
            {galponCerrado && <Chip sx={{ ml: 1 }} size="small" label="Cerrado" />}
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

      {!cargando && galpon && (
        <PanelClimaEstablecimiento
          climaLatitud={galpon.climaLatitud}
          climaLongitud={galpon.climaLongitud}
          nombreEstablecimiento={galpon.establecimientoNombre ?? undefined}
          rutaMapa={
            galpon.establecimientoId != null
              ? `/avicola-crianza/establecimientos-mapa?id=${galpon.establecimientoId}`
              : null
          }
        />
      )}

      {!cargando && galpon && (
        <>
          <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
            <Typography variant="body2" color="text.secondary" sx={{ alignSelf: 'center', mr: 1 }}>
              Exportar operaciones del galpón:
            </Typography>
            <Button size="small" variant="outlined" onClick={() => void exportarGalpon('excel')}>
              Excel
            </Button>
            <Button size="small" variant="outlined" onClick={() => void exportarGalpon('pdf')}>
              PDF
            </Button>
          </Stack>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }} variant="scrollable">
            <Tab label="Resumen" />
            <Tab label="Postura" />
            <Tab label="Muertes" />
            <Tab label="Consumos" />
            <Tab label="Sanidad" />
            <Tab label="Ventas huevos" />
            <Tab label="Descarte aves" />
          </Tabs>

          {pestana === 0 && resumen && (
            <Paper sx={{ p: 2 }}>
              <Stack spacing={1}>
                <Typography>
                  <strong>Aves disponibles:</strong> {resumen.cantidadDisponible ?? '—'}
                </Typography>
                <Typography>
                  <strong>Mortalidad (%):</strong>{' '}
                  {resumen.mortalidadPct != null ? String(resumen.mortalidadPct) : '—'}
                </Typography>
                <Typography>
                  <strong>Total huevos:</strong> {resumen.totalHuevos ?? '—'}
                </Typography>
                <Typography>
                  <strong>Días en producción:</strong> {resumen.diasEnProduccion ?? '—'}
                </Typography>
                <Typography>
                  <strong>% postura:</strong>{' '}
                  {resumen.porcentajePostura != null ? String(resumen.porcentajePostura) : '—'}
                </Typography>
                <Typography>
                  <strong>Huevos / ave / día:</strong>{' '}
                  {resumen.huevosPorAvePorDia != null ? String(resumen.huevosPorAvePorDia) : '—'}
                </Typography>
              </Stack>
            </Paper>
          )}

          {pestana === 1 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={galponCerrado}
                onClick={() => {
                  setFPost(hoy());
                  setCatPost('A');
                  setCantPost('');
                  setTempPost('');
                  setHumPost('');
                  setObsPost('');
                  setDialogo('postura');
                }}
              >
                Registrar postura
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Categoría</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell>Obs.</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {posturas.map((p) => (
                      <TableRow key={p.id}>
                        <TableCell>{p.fecha}</TableCell>
                        <TableCell>{p.categoriaHuevo}</TableCell>
                        <TableCell align="right">{p.cantidad}</TableCell>
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
                disabled={galponCerrado}
                onClick={() => {
                  setFMue(hoy());
                  setCantMue('');
                  setCausaMue('');
                  setObsMue('');
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
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {muertes.map((m) => (
                      <TableRow key={m.id}>
                        <TableCell>{m.fecha}</TableCell>
                        <TableCell align="right">{m.cantidad}</TableCell>
                        <TableCell>{m.causa ?? '—'}</TableCell>
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
                disabled={galponCerrado}
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
                      <TableCell align="right">Insumo</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {consumos.map((c) => (
                      <TableRow key={c.id}>
                        <TableCell>{c.fecha}</TableCell>
                        <TableCell align="right">{c.insumoId}</TableCell>
                        <TableCell align="right">{String(c.cantidad)}</TableCell>
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
                disabled={galponCerrado}
                onClick={() => {
                  setFSan(hoy());
                  setTipoSan('');
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
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {eventos.map((e) => (
                      <TableRow key={e.id}>
                        <TableCell>{e.fecha}</TableCell>
                        <TableCell>{e.tipo ?? '—'}</TableCell>
                        <TableCell>{e.descripcion ?? '—'}</TableCell>
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
                disabled={galponCerrado}
                onClick={() => {
                  setFVen(hoy());
                  setCatVen('A');
                  setCantVen('');
                  setPuVen('');
                  setTotVen('');
                  setCompVen('');
                  setObsVen('');
                  setDialogo('venta');
                }}
              >
                Registrar venta de huevos
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Categoría</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell align="right">Total</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {ventas.map((v) => (
                      <TableRow key={v.id}>
                        <TableCell>{v.fecha}</TableCell>
                        <TableCell>{v.categoriaHuevo}</TableCell>
                        <TableCell align="right">{v.cantidad}</TableCell>
                        <TableCell align="right">{v.total != null ? String(v.total) : '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 6 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={galponCerrado}
                onClick={() => {
                  setFDes(hoy());
                  setCantDes('');
                  setMotDes('OTRO');
                  setObsDes('');
                  setDialogo('descarte');
                }}
              >
                Registrar descarte de aves
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell align="right">Cantidad</TableCell>
                      <TableCell>Motivo</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {descartes.map((d) => (
                      <TableRow key={d.id}>
                        <TableCell>{d.fecha}</TableCell>
                        <TableCell align="right">{d.cantidad}</TableCell>
                        <TableCell>{d.motivo}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </>
      )}

      <Dialog open={dialogo === 'postura'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar postura</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fPost} onChange={(ev) => setFPost(ev.target.value)} />
            <TextField select label="Categoría" fullWidth value={catPost} onChange={(ev) => setCatPost(ev.target.value as HuevoCategoria)}>
              {CATEGORIAS_HUEVO.map((c) => (
                <MenuItem key={c.valor} value={c.valor}>
                  {c.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Cantidad (huevos)" type="number" fullWidth value={cantPost} onChange={(ev) => setCantPost(ev.target.value)} inputProps={{ min: 0 }} />
            <Typography variant="caption" color="text.secondary">
              Ambiente del día (opcional; se guarda una vez por fecha de postura)
            </Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              <TextField label="Temperatura (°C)" fullWidth value={tempPost} onChange={(ev) => setTempPost(ev.target.value)} placeholder="Opcional" />
              <TextField label="Humedad (%)" fullWidth value={humPost} onChange={(ev) => setHumPost(ev.target.value)} placeholder="Opcional" />
            </Stack>
            <BotonRellenarClima
              climaLatitud={galpon?.climaLatitud}
              climaLongitud={galpon?.climaLongitud}
              deshabilitado={galponCerrado}
              mensajeSinCoordenadas="Configurá la ubicación del establecimiento en Avícola crianza (mapa) para autocompletar."
              onValores={(v) => {
                if (v.temperatura != null) setTempPost(String(v.temperatura));
                if (v.humedad != null) setHumPost(String(v.humedad));
              }}
            />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsPost} onChange={(ev) => setObsPost(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarPostura}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'muerte'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar muertes</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fMue} onChange={(ev) => setFMue(ev.target.value)} />
            <TextField label="Cantidad" type="number" fullWidth value={cantMue} onChange={(ev) => setCantMue(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField label="Causa (opcional)" fullWidth value={causaMue} onChange={(ev) => setCausaMue(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsMue} onChange={(ev) => setObsMue(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarMuerte}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'consumo'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Consumo de insumo</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fCons} onChange={(ev) => setFCons(ev.target.value)} />
            <TextField select label="Insumo" fullWidth value={insCons} onChange={(ev) => setInsCons(ev.target.value === '' ? '' : Number(ev.target.value))}>
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
            <TextField label="Tipo" fullWidth value={tipoSan} onChange={(ev) => setTipoSan(ev.target.value)} />
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

      <Dialog open={dialogo === 'venta'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Venta de huevos</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fVen} onChange={(ev) => setFVen(ev.target.value)} />
            <TextField select label="Categoría" fullWidth value={catVen} onChange={(ev) => setCatVen(ev.target.value as HuevoCategoria)}>
              {CATEGORIAS_HUEVO.map((c) => (
                <MenuItem key={c.valor} value={c.valor}>
                  {c.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Cantidad" type="number" fullWidth value={cantVen} onChange={(ev) => setCantVen(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField label="Precio unitario (opcional)" fullWidth value={puVen} onChange={(ev) => setPuVen(ev.target.value)} />
            <TextField label="Total (opcional)" fullWidth value={totVen} onChange={(ev) => setTotVen(ev.target.value)} />
            <TextField label="Comprador" fullWidth value={compVen} onChange={(ev) => setCompVen(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsVen} onChange={(ev) => setObsVen(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarVenta}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'descarte'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Descarte de aves</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fDes} onChange={(ev) => setFDes(ev.target.value)} />
            <TextField label="Cantidad" type="number" fullWidth value={cantDes} onChange={(ev) => setCantDes(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField select label="Motivo" fullWidth value={motDes} onChange={(ev) => setMotDes(ev.target.value as DescarteMotivo)}>
              {MOTIVOS_DESCARTE.map((m) => (
                <MenuItem key={m.valor} value={m.valor}>
                  {m.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsDes} onChange={(ev) => setObsDes(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={guardarDescarte}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AvicolaPonedorasDetalleGalponScreen;
