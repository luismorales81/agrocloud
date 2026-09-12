import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import { insumosService } from '../../../services/domain/insumosLaboresServices';
import { Autocomplete } from '../../../components/ui/Autocomplete';
import { Icon } from '../../../components/icons';
import PanelClimaEstablecimiento from '../../../components/PanelClimaEstablecimiento';
import BotonRellenarClima from '../../../components/BotonRellenarClima';
import {
  filtrarInsumosAlimento,
  mapearInsumosDesdeApi,
  type InsumoOpcionFeedlot,
} from '../../feedlot/utils/mapearInsumos';
import type {
  PorcinosCatalogo,
  PorcinosConsumo,
  PorcinosEventoSanitario,
  PorcinosLote,
  PorcinosMuerte,
  PorcinosPesada,
  PorcinosResumenEconomico,
  PorcinosTipoEventoSanitario,
  PorcinosTipoVenta,
  PorcinosVenta,
} from '../typesApiV2';
import {
  cerrarLote,
  listarCausasMortalidad,
  listarConsumos,
  listarEventosSanitarios,
  listarMuertes,
  listarPesadas,
  listarVentasLote,
  mensajeError,
  obtenerLote,
  obtenerResumenEconomicoLote,
  registrarConsumo,
  registrarEventoSanitario,
  registrarMuerte,
  registrarPesada,
  registrarVentaLote,
} from '../services/porcinosApi';

const TIPOS_VENTA: { valor: PorcinosTipoVenta; etiqueta: string }[] = [
  { valor: 'FAENA', etiqueta: 'Faena' },
  { valor: 'ENGORDE', etiqueta: 'Venta engorde' },
  { valor: 'REPRODUCTOR', etiqueta: 'Venta reproductor' },
];

const TIPOS_SANIDAD: { valor: PorcinosTipoEventoSanitario; etiqueta: string }[] = [
  { valor: 'VACUNA', etiqueta: 'Vacuna' },
  { valor: 'TRATAMIENTO', etiqueta: 'Tratamiento' },
  { valor: 'DIAGNOSTICO', etiqueta: 'Diagnóstico' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

type TipoDialogo = 'pesada' | 'consumo' | 'muerte' | 'sanidad' | 'venta' | 'cierre' | null;

function formatearFecha(fechaIso: string | undefined): string {
  if (!fechaIso) return '—';
  try {
    return new Date(fechaIso).toLocaleDateString('es-AR');
  } catch {
    return fechaIso.slice(0, 10);
  }
}

function formatearMoneda(valor: number | null | undefined): string {
  if (valor == null) return '—';
  return `$ ${valor.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

const DetalleLotePorcinosScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const loteId = id != null ? parseInt(id, 10) : NaN;
  const hoy = () => new Date().toISOString().slice(0, 10);

  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lote, setLote] = useState<PorcinosLote | null>(null);
  const [pestana, setPestana] = useState(0);
  const [pesadas, setPesadas] = useState<PorcinosPesada[]>([]);
  const [consumos, setConsumos] = useState<PorcinosConsumo[]>([]);
  const [muertes, setMuertes] = useState<PorcinosMuerte[]>([]);
  const [sanidad, setSanidad] = useState<PorcinosEventoSanitario[]>([]);
  const [ventas, setVentas] = useState<PorcinosVenta[]>([]);
  const [resumenEconomico, setResumenEconomico] = useState<PorcinosResumenEconomico | null>(null);
  const [causasMortalidad, setCausasMortalidad] = useState<PorcinosCatalogo[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcionFeedlot[]>([]);
  const [temperaturaDia, setTemperaturaDia] = useState<number | ''>('');
  const [humedadDia, setHumedadDia] = useState<number | ''>('');

  const [dialogo, setDialogo] = useState<TipoDialogo>(null);
  const [fPesada, setFPesada] = useState(hoy());
  const [pesoProm, setPesoProm] = useState('');
  const [cabMuestreadas, setCabMuestreadas] = useState('');
  const [obsPesada, setObsPesada] = useState('');

  const [fConsumo, setFConsumo] = useState(hoy());
  const [insumoConsumo, setInsumoConsumo] = useState<number | ''>('');
  const [cantConsumo, setCantConsumo] = useState('');
  const [obsConsumo, setObsConsumo] = useState('');

  const [fMuerte, setFMuerte] = useState(hoy());
  const [cabMuerte, setCabMuerte] = useState('');
  const [causaMuerte, setCausaMuerte] = useState<number | ''>('');
  const [obsMuerte, setObsMuerte] = useState('');

  const [fSan, setFSan] = useState(hoy());
  const [tipoSan, setTipoSan] = useState<PorcinosTipoEventoSanitario>('VACUNA');
  const [descSan, setDescSan] = useState('');
  const [insumoSan, setInsumoSan] = useState<number | ''>('');
  const [cantSan, setCantSan] = useState('');
  const [diasRetiro, setDiasRetiro] = useState('');
  const [obsSan, setObsSan] = useState('');

  const [fVenta, setFVenta] = useState(hoy());
  const [tipoVenta, setTipoVenta] = useState<PorcinosTipoVenta>('FAENA');
  const [cabVenta, setCabVenta] = useState('');
  const [pesoVenta, setPesoVenta] = useState('');
  const [precioKg, setPrecioKg] = useState('');
  const [totalVenta, setTotalVenta] = useState('');
  const [comprador, setComprador] = useState('');
  const [obsVenta, setObsVenta] = useState('');

  const loteCerrado = lote?.estado === 'CERRADO';
  const insumosAlimento = useMemo(() => filtrarInsumosAlimento(insumos), [insumos]);

  const recargarListas = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    const [detalle, p, c, m, s, v, eco] = await Promise.all([
      obtenerLote(loteId),
      listarPesadas(loteId),
      listarConsumos(loteId),
      listarMuertes(loteId),
      listarEventosSanitarios(loteId),
      listarVentasLote(loteId),
      obtenerResumenEconomicoLote(loteId).catch(() => null),
    ]);
    setLote(detalle);
    setPesadas(p);
    setConsumos(c);
    setMuertes(m);
    setSanidad(s);
    setVentas(v);
    setResumenEconomico(eco);
  }, [loteId]);

  const cargarInicial = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    setCargando(true);
    setError(null);
    try {
      const [rawInsumos, causas] = await Promise.all([
        insumosService.listar().catch(() => []),
        listarCausasMortalidad(),
      ]);
      setInsumos(mapearInsumosDesdeApi(rawInsumos));
      setCausasMortalidad(causas.filter((c) => c.activo !== false));
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [loteId, recargarListas]);

  useEffect(() => {
    void cargarInicial();
  }, [cargarInicial]);

  const cerrarDialogos = () => setDialogo(null);

  const guardarPesada = async () => {
    const pp = parseFloat(pesoProm.replace(',', '.'));
    if (Number.isNaN(pp) || pp <= 0) {
      setError('Peso promedio inválido');
      return;
    }
    try {
      await registrarPesada(loteId, {
        fecha: fPesada,
        pesoPromedioKg: pp,
        cabezasMuestreadas: cabMuestreadas.trim() === '' ? null : parseInt(cabMuestreadas, 10),
        observaciones: obsPesada.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarConsumo = async () => {
    if (insumoConsumo === '') {
      setError('Seleccioná un insumo');
      return;
    }
    const cant = parseFloat(cantConsumo.replace(',', '.'));
    if (Number.isNaN(cant) || cant <= 0) {
      setError('Cantidad inválida');
      return;
    }
    try {
      await registrarConsumo(loteId, {
        insumoId: Number(insumoConsumo),
        fecha: fConsumo,
        cantidadKg: cant,
        observaciones: obsConsumo.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarMuerte = async () => {
    const cab = parseInt(cabMuerte, 10);
    if (Number.isNaN(cab) || cab <= 0) {
      setError('Cabezas inválidas');
      return;
    }
    try {
      await registrarMuerte(loteId, {
        fecha: fMuerte,
        cabezas: cab,
        causaMortalidadId: causaMuerte === '' ? null : Number(causaMuerte),
        observaciones: obsMuerte.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarSanidad = async () => {
    try {
      await registrarEventoSanitario(loteId, {
        fecha: fSan,
        tipo: tipoSan,
        descripcion: descSan.trim() || null,
        insumoId: insumoSan === '' ? null : Number(insumoSan),
        cantidadInsumo: cantSan.trim() === '' ? null : parseFloat(cantSan.replace(',', '.')),
        diasRetiro: diasRetiro.trim() === '' ? null : parseInt(diasRetiro, 10),
        observaciones: obsSan.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarVenta = async () => {
    const cab = parseInt(cabVenta, 10);
    if (Number.isNaN(cab) || cab <= 0) {
      setError('Cabezas inválidas');
      return;
    }
    try {
      await registrarVentaLote(loteId, {
        fecha: fVenta,
        tipo: tipoVenta,
        cabezas: cab,
        pesoPromedioKg: pesoVenta.trim() === '' ? null : parseFloat(pesoVenta.replace(',', '.')),
        precioKg: precioKg.trim() === '' ? null : parseFloat(precioKg.replace(',', '.')),
        total: totalVenta.trim() === '' ? null : parseFloat(totalVenta.replace(',', '.')),
        comprador: comprador.trim() || null,
        observaciones: obsVenta.trim() || null,
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const confirmarCierre = async () => {
    try {
      await cerrarLote(loteId);
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const nomInsumo = (insumoId: number) =>
    insumos.find((i) => i.id === insumoId)?.nombre ?? `Insumo ${insumoId}`;

  if (Number.isNaN(loteId)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Identificador de lote inválido.</Alert>
      </Box>
    );
  }

  const botonesAccion = !loteCerrado && (
    <Stack direction="row" flexWrap="wrap" gap={1} sx={{ mb: 2 }}>
      <Button size="small" variant="outlined" onClick={() => { setFPesada(hoy()); setDialogo('pesada'); }}>
        + Pesada
      </Button>
      <Button size="small" variant="outlined" onClick={() => { setFConsumo(hoy()); setDialogo('consumo'); }}>
        + Consumo
      </Button>
      <Button size="small" variant="outlined" onClick={() => { setFMuerte(hoy()); setDialogo('muerte'); }}>
        + Muerte
      </Button>
      <Button size="small" variant="outlined" onClick={() => { setFSan(hoy()); setDialogo('sanidad'); }}>
        + Sanidad
      </Button>
      <Button size="small" variant="outlined" onClick={() => { setFVenta(hoy()); setDialogo('venta'); }}>
        + Venta
      </Button>
      <Button size="small" color="warning" variant="outlined" onClick={() => setDialogo('cierre')}>
        Cerrar lote
      </Button>
      <Button size="small" onClick={() => navigate(`/porcinos/lotes/${loteId}/editar`)}>
        Editar
      </Button>
    </Stack>
  );

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Button size="small" onClick={() => navigate('/porcinos/lotes')}>
          ← Volver
        </Button>
        <Icon name="List" size={24} />
        <Typography variant="h5" component="h1">
          {lote?.nombre ?? `Lote #${loteId}`}
        </Typography>
        {lote?.estado && (
          <Chip
            size="small"
            label={lote.estado}
            color={lote.estado === 'ACTIVO' ? 'success' : 'default'}
          />
        )}
      </Stack>

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
          <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
            <Stack direction="row" flexWrap="wrap" gap={2}>
              <Typography variant="body2">
                <strong>Etapa:</strong> {lote.etapa ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Cabezas:</strong> {lote.cabezasActuales ?? '—'} / {lote.cabezasInicial ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Ingreso:</strong> {formatearFecha(lote.fechaIngreso)}
              </Typography>
              <Typography variant="body2">
                <strong>Galpón:</strong> {lote.galponNombre ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Establecimiento:</strong> {lote.establecimientoNombre ?? '—'}
              </Typography>
            </Stack>
          </Paper>

          {resumenEconomico && (
            <Paper variant="outlined" sx={{ p: 2, mb: 2, bgcolor: 'grey.50' }}>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Resumen económico (inventario general)
              </Typography>
              <Stack direction="row" flexWrap="wrap" gap={2}>
                <Typography variant="body2">
                  <strong>Alimento consumido:</strong> {resumenEconomico.totalAlimentoKg ?? 0} kg
                </Typography>
                <Typography variant="body2">
                  <strong>Costo alimento:</strong> {formatearMoneda(resumenEconomico.costoAlimento)}
                </Typography>
                <Typography variant="body2">
                  <strong>Costo sanidad:</strong> {formatearMoneda(resumenEconomico.costoSanidad)}
                </Typography>
                <Typography variant="body2">
                  <strong>Costo acumulado:</strong> {formatearMoneda(resumenEconomico.costoAcumulado)}
                </Typography>
                <Typography variant="body2">
                  <strong>Ingresos ventas:</strong> {formatearMoneda(resumenEconomico.ingresosVentas)}
                </Typography>
                <Typography
                  variant="body2"
                  color={
                    resumenEconomico.margen != null && resumenEconomico.margen < 0
                      ? 'error.main'
                      : 'success.main'
                  }
                >
                  <strong>Margen:</strong> {formatearMoneda(resumenEconomico.margen)}
                </Typography>
              </Stack>
            </Paper>
          )}

          {botonesAccion}

          <PanelClimaEstablecimiento
            climaLatitud={lote.climaLatitud}
            climaLongitud={lote.climaLongitud}
            nombreEstablecimiento={lote.establecimientoNombre}
            rutaMapa={
              lote.establecimientoId != null
                ? `/porcinos/establecimientos-mapa?id=${lote.establecimientoId}`
                : null
            }
          />

          <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 1 }}>
            <BotonRellenarClima
              climaLatitud={lote.climaLatitud}
              climaLongitud={lote.climaLongitud}
              onValores={(v) => {
                if (v.temperatura != null) setTemperaturaDia(v.temperatura);
                if (v.humedad != null) setHumedadDia(v.humedad);
              }}
            />
            {(temperaturaDia !== '' || humedadDia !== '') && (
              <Typography variant="caption" color="text.secondary">
                Clima del día: {temperaturaDia !== '' ? `${temperaturaDia} °C` : '—'}
                {humedadDia !== '' ? ` · ${humedadDia} % HR` : ''}
              </Typography>
            )}
          </Stack>

          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }} variant="scrollable">
            <Tab label={`Pesadas (${pesadas.length})`} />
            <Tab label={`Consumos (${consumos.length})`} />
            <Tab label={`Muertes (${muertes.length})`} />
            <Tab label={`Sanidad (${sanidad.length})`} />
            <Tab label={`Ventas (${ventas.length})`} />
          </Tabs>

          {pestana === 0 && (
            <TablaSimple
              vacio="Sin pesadas registradas."
              columnas={['Fecha', 'Peso prom. (kg)', 'Cabezas muestreadas', 'Observaciones']}
              filas={pesadas.map((p) => [
                formatearFecha(p.fecha),
                String(p.pesoPromedioKg),
                p.cabezasMuestreadas != null ? String(p.cabezasMuestreadas) : '—',
                p.observaciones ?? '—',
              ])}
            />
          )}
          {pestana === 1 && (
            <TablaSimple
              vacio="Sin consumos registrados."
              columnas={['Fecha', 'Insumo', 'Cantidad (kg)', 'Observaciones']}
              filas={consumos.map((c) => [
                formatearFecha(c.fecha),
                c.insumoNombre ?? nomInsumo(c.insumoId),
                String(c.cantidadKg),
                c.observaciones ?? '—',
              ])}
            />
          )}
          {pestana === 2 && (
            <TablaSimple
              vacio="Sin muertes registradas."
              columnas={['Fecha', 'Cabezas', 'Causa', 'Observaciones']}
              filas={muertes.map((m) => [
                formatearFecha(m.fecha),
                String(m.cabezas),
                m.causaMortalidadNombre ?? m.causaNombre ?? '—',
                m.observaciones ?? '—',
              ])}
            />
          )}
          {pestana === 3 && (
            <TablaSimple
              vacio="Sin eventos sanitarios."
              columnas={['Fecha', 'Tipo', 'Descripción', 'Días retiro']}
              filas={sanidad.map((e) => [
                formatearFecha(e.fecha),
                e.tipo,
                e.descripcion ?? '—',
                e.diasRetiro != null ? String(e.diasRetiro) : '—',
              ])}
            />
          )}
          {pestana === 4 && (
            <TablaSimple
              vacio="Sin ventas registradas."
              columnas={['Fecha', 'Tipo', 'Cabezas', 'Peso prom.', 'Total']}
              filas={ventas.map((v) => [
                formatearFecha(v.fecha),
                v.tipo,
                String(v.cabezas),
                v.pesoPromedioKg != null ? String(v.pesoPromedioKg) : '—',
                v.total != null ? String(v.total) : '—',
              ])}
            />
          )}
        </>
      )}

      <Dialog open={dialogo === 'pesada'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar pesada</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" fullWidth InputLabelProps={{ shrink: true }} value={fPesada} onChange={(e) => setFPesada(e.target.value)} />
            <TextField label="Peso promedio (kg)" fullWidth required value={pesoProm} onChange={(e) => setPesoProm(e.target.value)} />
            <TextField label="Cabezas muestreadas" fullWidth value={cabMuestreadas} onChange={(e) => setCabMuestreadas(e.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsPesada} onChange={(e) => setObsPesada(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarPesada()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'consumo'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar consumo de alimento</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" fullWidth InputLabelProps={{ shrink: true }} value={fConsumo} onChange={(e) => setFConsumo(e.target.value)} />
            <Autocomplete<InsumoOpcionFeedlot>
              label="Insumo"
              options={insumosAlimento.map((i) => ({
                value: i.id,
                label: i.nombre,
                data: i,
              }))}
              value={insumoConsumo === '' ? undefined : insumoConsumo}
              onChange={(value) => setInsumoConsumo(value != null && value !== '' ? Number(value) : '')}
              placeholder="Buscar insumo…"
            />
            <TextField label="Cantidad (kg)" fullWidth required value={cantConsumo} onChange={(e) => setCantConsumo(e.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsConsumo} onChange={(e) => setObsConsumo(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarConsumo()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'muerte'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar muerte</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" fullWidth InputLabelProps={{ shrink: true }} value={fMuerte} onChange={(e) => setFMuerte(e.target.value)} />
            <TextField label="Cabezas" fullWidth required type="number" value={cabMuerte} onChange={(e) => setCabMuerte(e.target.value)} />
            <TextField select label="Causa" fullWidth value={causaMuerte} onChange={(e) => setCausaMuerte(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">Sin especificar</MenuItem>
              {causasMortalidad.map((c) => (
                <MenuItem key={c.id} value={c.id}>{c.nombre}</MenuItem>
              ))}
            </TextField>
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsMuerte} onChange={(e) => setObsMuerte(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarMuerte()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'sanidad'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Evento sanitario</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" fullWidth InputLabelProps={{ shrink: true }} value={fSan} onChange={(e) => setFSan(e.target.value)} />
            <TextField select label="Tipo" fullWidth value={tipoSan} onChange={(e) => setTipoSan(e.target.value as PorcinosTipoEventoSanitario)}>
              {TIPOS_SANIDAD.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Descripción" fullWidth value={descSan} onChange={(e) => setDescSan(e.target.value)} />
            <Autocomplete<InsumoOpcionFeedlot>
              label="Insumo / medicamento (opcional)"
              options={insumos.map((i) => ({
                value: i.id,
                label: i.nombre,
                data: i,
              }))}
              value={insumoSan === '' ? undefined : insumoSan}
              onChange={(value) => setInsumoSan(value != null && value !== '' ? Number(value) : '')}
              placeholder="Buscar insumo…"
            />
            <TextField label="Cantidad insumo" fullWidth value={cantSan} onChange={(e) => setCantSan(e.target.value)} />
            <TextField label="Días de retiro" fullWidth type="number" value={diasRetiro} onChange={(e) => setDiasRetiro(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarSanidad()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'venta'} onClose={cerrarDialogos} fullWidth maxWidth="sm">
        <DialogTitle>Registrar venta / faena</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" fullWidth InputLabelProps={{ shrink: true }} value={fVenta} onChange={(e) => setFVenta(e.target.value)} />
            <TextField select label="Tipo" fullWidth value={tipoVenta} onChange={(e) => setTipoVenta(e.target.value as PorcinosTipoVenta)}>
              {TIPOS_VENTA.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Cabezas" fullWidth required type="number" value={cabVenta} onChange={(e) => setCabVenta(e.target.value)} />
            <TextField label="Peso promedio (kg)" fullWidth value={pesoVenta} onChange={(e) => setPesoVenta(e.target.value)} />
            <TextField label="Precio por kg" fullWidth value={precioKg} onChange={(e) => setPrecioKg(e.target.value)} />
            <TextField label="Total" fullWidth value={totalVenta} onChange={(e) => setTotalVenta(e.target.value)} />
            <TextField label="Comprador" fullWidth value={comprador} onChange={(e) => setComprador(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarVenta()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'cierre'} onClose={cerrarDialogos}>
        <DialogTitle>Cerrar lote</DialogTitle>
        <DialogContent>
          <Typography>¿Confirmás el cierre del lote? No se podrán registrar más operaciones.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button color="warning" variant="contained" onClick={() => void confirmarCierre()}>Cerrar lote</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

function TablaSimple({
  columnas,
  filas,
  vacio,
}: {
  columnas: string[];
  filas: string[][];
  vacio: string;
}) {
  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            {columnas.map((c) => (
              <TableCell key={c}>{c}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {filas.length === 0 ? (
            <TableRow>
              <TableCell colSpan={columnas.length} align="center">
                <Typography color="text.secondary" sx={{ py: 2 }}>
                  {vacio}
                </Typography>
              </TableCell>
            </TableRow>
          ) : (
            filas.map((fila, idx) => (
              <TableRow key={idx}>
                {fila.map((celda, j) => (
                  <TableCell key={j}>{celda}</TableCell>
                ))}
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default DetalleLotePorcinosScreen;
