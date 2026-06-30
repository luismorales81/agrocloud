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
import {
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { insumosService } from '../../../services/domain/insumosLaboresServices';
import { Autocomplete } from '../../../components/ui/Autocomplete';
import type {
  FeedlotAjustePlantel,
  FeedlotBunkScore,
  FeedlotConsumo,
  FeedlotConsumoTeorico,
  FeedlotEventoSanitario,
  FeedlotEventoSanitarioSolicitud,
  FeedlotLecturaComedero,
  FeedlotLote,
  FeedlotMuerte,
  FeedlotPesada,
  FeedlotResumen,
  FeedlotTipoEventoSanitario,
  FeedlotTipoVenta,
  FeedlotVenta,
} from '../types';
import {
  actualizarConsumo,
  actualizarEventoSanitario,
  actualizarLecturaComedero,
  actualizarPesada,
  cerrarLote,
  eliminarConsumo,
  eliminarEventoSanitario,
  eliminarLecturaComedero,
  eliminarPesada,
  listarAjustesPlantel,
  listarConsumos,
  listarEventosSanitarios,
  listarLecturasComedero,
  listarMotivosMuerte,
  listarMuertes,
  listarPesadas,
  listarVentas,
  mensajeError,
  obtenerConsumoTeorico,
  obtenerCurvaPeso,
  obtenerLote,
  obtenerResumenLote,
  registrarAjustePlantel,
  registrarConsumo,
  registrarEventoSanitario,
  registrarLecturaComedero,
  registrarMuerte,
  registrarPesada,
  registrarVenta,
} from '../services/feedlotApi';
import type { FeedlotCatalogo } from '../types';
import PanelClimaEstablecimiento from '../../../components/PanelClimaEstablecimiento';
import BotonRellenarClima from '../../../components/BotonRellenarClima';

const TIPOS_VENTA: { valor: FeedlotTipoVenta; etiqueta: string }[] = [
  { valor: 'FAENA', etiqueta: 'Faena' },
  { valor: 'VENTA_EN_PIE', etiqueta: 'Venta en pie' },
  { valor: 'DESCARTE', etiqueta: 'Descarte' },
];

const TIPOS_SANIDAD: { valor: FeedlotTipoEventoSanitario; etiqueta: string }[] = [
  { valor: 'VACUNA', etiqueta: 'Vacuna' },
  { valor: 'TRATAMIENTO', etiqueta: 'Tratamiento' },
  { valor: 'DIAGNOSTICO', etiqueta: 'Diagnóstico' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

const BUNK_SCORES: { valor: FeedlotBunkScore; etiqueta: string }[] = [
  { valor: 'CERO', etiqueta: '0 — Vacío' },
  { valor: 'MEDIO', etiqueta: '½' },
  { valor: 'UNO', etiqueta: '1' },
  { valor: 'DOS', etiqueta: '2' },
  { valor: 'TRES', etiqueta: '3' },
  { valor: 'CUATRO', etiqueta: '4' },
];

function etiquetaBunkScore(valor: string): string {
  return BUNK_SCORES.find((b) => b.valor === valor)?.etiqueta ?? valor;
}

const TIPOS_AGROQUIMICO = new Set(['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE']);

function esAgroquimico(tipo?: string): boolean {
  return tipo != null && TIPOS_AGROQUIMICO.has(tipo.toUpperCase());
}

function mapearInsumosDesdeApi(raw: unknown): InsumoOpcion[] {
  const arr = Array.isArray(raw) ? raw : [];
  return arr
    .map((x: Record<string, unknown>) => ({
      id: Number(x.id),
      nombre: String(x.nombre ?? x.descripcion ?? `Insumo ${x.id}`),
      tipo: x.tipo != null ? String(x.tipo) : undefined,
      unidadMedida:
        x.unidadMedida != null
          ? String(x.unidadMedida)
          : x.unidad_medida != null
            ? String(x.unidad_medida)
            : 'kg',
      stockActual:
        x.stockActual != null
          ? Number(x.stockActual)
          : x.stock_actual != null
            ? Number(x.stock_actual)
            : undefined,
    }))
    .filter((i) => !Number.isNaN(i.id) && i.id > 0);
}

interface InsumoOpcion {
  id: number;
  nombre: string;
  tipo?: string;
  unidadMedida?: string;
  stockActual?: number;
}

type TipoDialogo =
  | 'pesada'
  | 'consumo'
  | 'muerte'
  | 'sanidad'
  | 'venta'
  | 'cierre'
  | 'ajuste'
  | 'comedero';

const PROPS_DIALOGO = { disableRestoreFocus: true, fullWidth: true, maxWidth: 'sm' as const };

const DetalleLoteFeedlotScreen: React.FC = () => {
  const { id: idParam } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const loteId = idParam ? parseInt(idParam, 10) : NaN;

  const [pestana, setPestana] = useState(0);
  const [lote, setLote] = useState<FeedlotLote | null>(null);
  const [resumen, setResumen] = useState<FeedlotResumen | null>(null);
  const [pesadas, setPesadas] = useState<FeedlotPesada[]>([]);
  const [consumos, setConsumos] = useState<FeedlotConsumo[]>([]);
  const [muertes, setMuertes] = useState<FeedlotMuerte[]>([]);
  const [eventos, setEventos] = useState<FeedlotEventoSanitario[]>([]);
  const [ventas, setVentas] = useState<FeedlotVenta[]>([]);
  const [motivosMuerte, setMotivosMuerte] = useState<FeedlotCatalogo[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const hoy = () => new Date().toISOString().slice(0, 10);

  const [dialogo, setDialogo] = useState<TipoDialogo | null>(null);
  const [ajustes, setAjustes] = useState<FeedlotAjustePlantel[]>([]);
  const [lecturas, setLecturas] = useState<FeedlotLecturaComedero[]>([]);
  const [consumoTeorico, setConsumoTeorico] = useState<FeedlotConsumoTeorico | null>(null);
  const [puntosCurvaMini, setPuntosCurvaMini] = useState<{ fecha: string; peso: number }[]>([]);
  const [editPesadaId, setEditPesadaId] = useState<number | null>(null);
  const [editConsumoId, setEditConsumoId] = useState<number | null>(null);
  const [editEventoId, setEditEventoId] = useState<number | null>(null);
  const [editLecturaId, setEditLecturaId] = useState<number | null>(null);
  const [fAjuste, setFAjuste] = useState('');
  const [cabAjuste, setCabAjuste] = useState('');
  const [motivoAjuste, setMotivoAjuste] = useState('');

  const [fPesada, setFPesada] = useState('');
  const [pesoProm, setPesoProm] = useState('');
  const [cabMuestreadas, setCabMuestreadas] = useState('');
  const [obsPesada, setObsPesada] = useState('');

  const [fConsumo, setFConsumo] = useState(hoy());
  const [insumoConsumo, setInsumoConsumo] = useState<number | ''>('');
  const [cantConsumo, setCantConsumo] = useState('');
  const [msConsumo, setMsConsumo] = useState('');
  const [obsConsumo, setObsConsumo] = useState('');

  const [fMuerte, setFMuerte] = useState(hoy());
  const [cabMuerte, setCabMuerte] = useState('');
  const [motivoMuerte, setMotivoMuerte] = useState<number | ''>('');
  const [obsMuerte, setObsMuerte] = useState('');

  const [fSan, setFSan] = useState(hoy());
  const [tipoSan, setTipoSan] = useState<FeedlotTipoEventoSanitario>('VACUNA');
  const [descSan, setDescSan] = useState('');
  const [insumoSan, setInsumoSan] = useState<number | ''>('');
  const [diasRetiro, setDiasRetiro] = useState('');
  const [obsSan, setObsSan] = useState('');

  const [fVenta, setFVenta] = useState(hoy());
  const [tipoVenta, setTipoVenta] = useState<FeedlotTipoVenta>('VENTA_EN_PIE');
  const [cabVenta, setCabVenta] = useState('');
  const [pesoVenta, setPesoVenta] = useState('');
  const [precioKg, setPrecioKg] = useState('');
  const [totalVenta, setTotalVenta] = useState('');
  const [comprador, setComprador] = useState('');
  const [obsVenta, setObsVenta] = useState('');

  const [fLectura, setFLectura] = useState('');
  const [bunkScore, setBunkScore] = useState<FeedlotBunkScore>('UNO');
  const [kgEntregados, setKgEntregados] = useState('');
  const [tempLectura, setTempLectura] = useState('');
  const [humLectura, setHumLectura] = useState('');
  const [obsLectura, setObsLectura] = useState('');

  const loteCerrado = lote?.estado === 'CERRADO';

  const insumosAlimento = useMemo(
    () =>
      insumos
        .filter((i) => !esAgroquimico(i.tipo))
        .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es')),
    [insumos]
  );

  const insumosSanidad = useMemo(
    () => [...insumos].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es')),
    [insumos]
  );

  const opcionesInsumoAlimento = useMemo(() => {
    const lista = [...insumosAlimento];
    if (insumoConsumo !== '' && !lista.some((i) => i.id === insumoConsumo)) {
      const actual = insumos.find((i) => i.id === insumoConsumo);
      if (actual) lista.push(actual);
    }
    return lista.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
  }, [insumosAlimento, insumos, insumoConsumo]);

  const opcionesInsumoSanidad = useMemo(() => {
    const lista = [...insumosSanidad];
    if (insumoSan !== '' && !lista.some((i) => i.id === insumoSan)) {
      const actual = insumos.find((i) => i.id === insumoSan);
      if (actual) lista.push(actual);
    }
    return lista.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
  }, [insumosSanidad, insumos, insumoSan]);

  const abrirDialogo = (tipo: TipoDialogo) => {
    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
    setDialogo(tipo);
  };

  const recargarListas = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    const [p, c, m, e, v, r, lt, aj] = await Promise.all([
      listarPesadas(loteId),
      listarConsumos(loteId),
      listarMuertes(loteId),
      listarEventosSanitarios(loteId),
      listarVentas(loteId),
      obtenerResumenLote(loteId),
      obtenerLote(loteId),
      listarAjustesPlantel(loteId),
    ]);
    setPesadas(p);
    setConsumos(c);
    setMuertes(m);
    setEventos(e);
    setVentas(v);
    setResumen(r);
    setLote(lt);
    setAjustes(aj);
    try {
      const curva = await obtenerCurvaPeso(loteId);
      const pts = [...(curva.serie ?? []), ...(curva.proyeccion ?? [])].map((p) => ({
        fecha: String(p.fecha).slice(5),
        peso: Number(p.pesoKg),
      }));
      setPuntosCurvaMini(pts);
    } catch {
      setPuntosCurvaMini([]);
    }
  }, [loteId]);

  const cargarLecturas = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    const lista = await listarLecturasComedero(loteId);
    setLecturas(lista);
  }, [loteId]);

  const cargarConsumoTeorico = useCallback(async () => {
    if (Number.isNaN(loteId)) return;
    if (!lote?.dietaId) {
      setConsumoTeorico(null);
      return;
    }
    try {
      const datos = await obtenerConsumoTeorico(loteId);
      setConsumoTeorico(datos);
    } catch {
      setConsumoTeorico(null);
    }
  }, [loteId, lote?.dietaId]);

  const cargarInicial = useCallback(async () => {
    if (Number.isNaN(loteId)) {
      setError('Identificador de lote inválido');
      setCargando(false);
      return;
    }
    try {
      setCargando(true);
      setError(null);
      const [rawInsumos, motivos] = await Promise.all([
        insumosService.listar().catch(() => []),
        listarMotivosMuerte(),
      ]);
      setInsumos(mapearInsumosDesdeApi(rawInsumos));
      setMotivosMuerte(motivos.filter((m) => m.activo !== false));
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

  useEffect(() => {
    if (pestana === 3) void cargarLecturas();
    if (pestana === 4) void cargarConsumoTeorico();
  }, [pestana, cargarLecturas, cargarConsumoTeorico]);

  const cerrarDialogos = () => {
    setDialogo(null);
    setEditPesadaId(null);
    setEditConsumoId(null);
    setEditEventoId(null);
    setEditLecturaId(null);
  };

  const confirmarCierreLote = async () => {
    try {
      const cab = lote?.cabezasActuales ?? 0;
      await cerrarLote(loteId, cab > 0 ? { confirmarConCabezas: true } : undefined);
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarAjuste = async () => {
    const cab = parseInt(cabAjuste, 10);
    if (!motivoAjuste.trim() || Number.isNaN(cab)) {
      setError('Indique cabezas destino y motivo del ajuste');
      return;
    }
    try {
      await registrarAjustePlantel(loteId, {
        fecha: fAjuste,
        cabezasDespues: cab,
        motivo: motivoAjuste.trim(),
      });
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const eliminarRegistro = async (tipo: 'pesada' | 'consumo' | 'sanidad' | 'comedero', id: number) => {
    if (!window.confirm('¿Eliminar este registro?')) return;
    try {
      if (tipo === 'pesada') await eliminarPesada(loteId, id);
      else if (tipo === 'consumo') await eliminarConsumo(loteId, id);
      else if (tipo === 'comedero') await eliminarLecturaComedero(loteId, id);
      else await eliminarEventoSanitario(loteId, id);
      if (tipo === 'comedero') await cargarLecturas();
      else await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const nomInsumo = (id: number) => insumos.find((i) => i.id === id)?.nombre ?? `Insumo ${id}`;

  const guardarPesada = async () => {
    try {
      const pp = parseFloat(pesoProm.replace(',', '.'));
      if (Number.isNaN(pp) || pp <= 0) {
        setError('Peso promedio inválido');
        return;
      }
      const cuerpo = {
        fecha: fPesada,
        pesoPromedioKg: pp,
        cabezasMuestreadas: cabMuestreadas.trim() === '' ? null : parseInt(cabMuestreadas, 10),
        observaciones: obsPesada.trim() || null,
      };
      if (editPesadaId != null) {
        await actualizarPesada(loteId, editPesadaId, cuerpo);
      } else {
        await registrarPesada(loteId, cuerpo);
      }
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
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
      const cuerpo = {
        insumoId: Number(insumoConsumo),
        fecha: fConsumo,
        cantidadKg: cant,
        materiaSecaPct: msConsumo.trim() === '' ? null : parseFloat(msConsumo.replace(',', '.')),
        observaciones: obsConsumo.trim() || null,
      };
      if (editConsumoId != null) {
        await actualizarConsumo(loteId, editConsumoId, cuerpo);
      } else {
        await registrarConsumo(loteId, cuerpo);
      }
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarMuerte = async () => {
    try {
      const c = parseInt(cabMuerte, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad de cabezas inválida');
        return;
      }
      await registrarMuerte(loteId, {
        fecha: fMuerte,
        cabezas: c,
        motivoId: motivoMuerte === '' ? null : Number(motivoMuerte),
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
      const cuerpo: FeedlotEventoSanitarioSolicitud = {
        fecha: fSan,
        tipo: tipoSan,
        descripcion: descSan.trim() || null,
        insumoId: insumoSan === '' ? null : Number(insumoSan),
        diasRetiro: diasRetiro.trim() === '' ? null : parseInt(diasRetiro, 10),
        observaciones: obsSan.trim() || null,
      };
      if (editEventoId != null) {
        await actualizarEventoSanitario(loteId, editEventoId, cuerpo);
      } else {
        await registrarEventoSanitario(loteId, cuerpo);
      }
      cerrarDialogos();
      await recargarListas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarVenta = async () => {
    try {
      const c = parseInt(cabVenta, 10);
      if (Number.isNaN(c) || c <= 0) {
        setError('Cantidad de cabezas inválida');
        return;
      }
      await registrarVenta(loteId, {
        fecha: fVenta,
        tipo: tipoVenta,
        cabezas: c,
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

  const guardarLectura = async () => {
    try {
      const kg = kgEntregados.trim() === '' ? null : parseFloat(kgEntregados.replace(',', '.'));
      if (kg != null && (Number.isNaN(kg) || kg < 0)) {
        setError('Kg entregados inválidos');
        return;
      }
      const parseOptDec = (s: string) => {
        if (!s.trim()) return null;
        const v = parseFloat(s.replace(',', '.'));
        return Number.isNaN(v) ? null : v;
      };
      const cuerpo = {
        fecha: fLectura,
        bunkScore,
        kgEntregados: kg,
        temperaturaDia: parseOptDec(tempLectura),
        humedadDia: parseOptDec(humLectura),
        observaciones: obsLectura.trim() || null,
      };
      if (editLecturaId != null) {
        await actualizarLecturaComedero(loteId, editLecturaId, cuerpo);
      } else {
        await registrarLecturaComedero(loteId, cuerpo);
      }
      cerrarDialogos();
      await cargarLecturas();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  if (Number.isNaN(loteId)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Lote no válido</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/feedlot/lotes')}>
          Volver a lotes
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" flexWrap="wrap" alignItems="center" gap={2} mb={2}>
        <Button variant="outlined" onClick={() => navigate('/feedlot/lotes')}>
          ← Lotes
        </Button>
        {lote && (
          <Typography variant="h5" component="h1">
            {lote.nombre}
            {loteCerrado && <Chip sx={{ ml: 1 }} size="small" label="Cerrado" color="default" />}
          </Typography>
        )}
        {lote && !loteCerrado && (
          <Button size="small" onClick={() => navigate(`/feedlot/lotes/${loteId}/editar`)}>
            Editar datos
          </Button>
        )}
      </Box>

      {lote && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {lote.corralNombre ?? '—'} · {lote.categoriaNombre ?? '—'} · {lote.razaNombre ?? '—'} ·{' '}
          {lote.cabezasActuales ?? 0} cab. actuales
        </Typography>
      )}

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
        <PanelClimaEstablecimiento
          climaLatitud={lote.climaLatitud}
          climaLongitud={lote.climaLongitud}
          nombreEstablecimiento={lote.establecimientoNombre}
          rutaMapa={
            lote.establecimientoId != null
              ? `/feedlot/establecimientos-mapa?id=${lote.establecimientoId}`
              : null
          }
        />
      )}

      {!cargando && lote && (
        <>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }} variant="scrollable">
            <Tab label="Resumen" />
            <Tab label="Pesadas" />
            <Tab label="Consumos" />
            <Tab label="Comederos" />
            <Tab label="Dieta" />
            <Tab label="Muertes" />
            <Tab label="Sanidad" />
            <Tab label="Ventas" />
          </Tabs>

          {pestana === 0 && resumen && (
            <Paper sx={{ p: 2 }}>
              <Stack direction="row" flexWrap="wrap" gap={1} mb={2}>
                {!loteCerrado && (
                  <>
                    <Button variant="outlined" color="warning" onClick={() => abrirDialogo('cierre')}>
                      Cerrar lote
                    </Button>
                    <Button variant="outlined" onClick={() => {
                      setFAjuste(hoy());
                      setCabAjuste(String(lote?.cabezasActuales ?? ''));
                      setMotivoAjuste('');
                      abrirDialogo('ajuste');
                    }}>
                      Ajuste plantel
                    </Button>
                  </>
                )}
              </Stack>
              <Stack spacing={1}>
                <Typography>
                  <strong>Cabezas actuales:</strong> {resumen.cabezasActuales ?? '—'} /{' '}
                  {resumen.cabezasInicial ?? '—'} inicial
                </Typography>
                <Typography>
                  <strong>Días en feedlot:</strong> {resumen.diasEnFeedlot ?? '—'}
                </Typography>
                <Typography>
                  <strong>Peso actual (kg):</strong>{' '}
                  {resumen.pesoActualKg != null ? Number(resumen.pesoActualKg).toFixed(2) : '—'}
                </Typography>
                <Typography>
                  <strong>GMD (kg/día):</strong>{' '}
                  {resumen.gmd != null ? Number(resumen.gmd).toFixed(3) : '—'}
                </Typography>
                <Typography>
                  <strong>Mortalidad (%):</strong>{' '}
                  {resumen.mortalidadPct != null ? Number(resumen.mortalidadPct).toFixed(2) : '—'}
                </Typography>
                <Typography>
                  <strong>Total alimento (kg):</strong>{' '}
                  {resumen.totalAlimentoKg != null ? String(resumen.totalAlimentoKg) : '—'}
                </Typography>
                <Typography>
                  <strong>Conversión alimenticia:</strong>{' '}
                  {resumen.conversionAlimenticia != null ? String(resumen.conversionAlimenticia) : '—'}
                </Typography>
                <Typography>
                  <strong>Consumo cab/día (kg):</strong>{' '}
                  {resumen.consumoCabDia != null ? Number(resumen.consumoCabDia).toFixed(2) : '—'}
                </Typography>
                {lote?.dietaNombre && (
                  <Typography>
                    <strong>Dieta asignada:</strong> {lote.dietaNombre}
                  </Typography>
                )}
              </Stack>
              {puntosCurvaMini.length > 0 && (
                <Box mt={2}>
                  <Typography variant="subtitle2" gutterBottom>
                    Curva de peso (vista rápida)
                  </Typography>
                  <ResponsiveContainer width="100%" height={180}>
                    <LineChart data={puntosCurvaMini}>
                      <XAxis dataKey="fecha" tick={{ fontSize: 10 }} />
                      <YAxis tick={{ fontSize: 10 }} unit=" kg" width={45} />
                      <Tooltip formatter={(v: number) => [`${v.toFixed(1)} kg`, 'Peso']} />
                      <Line type="monotone" dataKey="peso" stroke="#78350f" strokeWidth={2} dot={{ r: 2 }} />
                    </LineChart>
                  </ResponsiveContainer>
                </Box>
              )}
              {ajustes.length > 0 && (
                <Box mt={2}>
                  <Typography variant="subtitle2" gutterBottom>Ajustes de plantel</Typography>
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Fecha</TableCell>
                          <TableCell align="right">Antes</TableCell>
                          <TableCell align="right">Después</TableCell>
                          <TableCell>Motivo</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {ajustes.map((a) => (
                          <TableRow key={a.id}>
                            <TableCell>{a.fecha}</TableCell>
                            <TableCell align="right">{a.cabezasAntes ?? '—'}</TableCell>
                            <TableCell align="right">{a.cabezasDespues ?? '—'}</TableCell>
                            <TableCell>{a.motivo ?? '—'}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Box>
              )}
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
                  setCabMuestreadas('');
                  setObsPesada('');
                  abrirDialogo('pesada');
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
                      <TableCell align="right">Cab. muestreadas</TableCell>
                      <TableCell>Obs.</TableCell>
                      {!loteCerrado && <TableCell align="right">Acciones</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {pesadas.map((p) => (
                      <TableRow key={p.id}>
                        <TableCell>{p.fecha}</TableCell>
                        <TableCell align="right">{String(p.pesoPromedioKg)}</TableCell>
                        <TableCell align="right">{p.cabezasMuestreadas ?? '—'}</TableCell>
                        <TableCell>{p.observaciones ?? '—'}</TableCell>
                        {!loteCerrado && (
                          <TableCell align="right">
                            <Button size="small" onClick={() => {
                              setEditPesadaId(p.id);
                              setFPesada(p.fecha);
                              setPesoProm(String(p.pesoPromedioKg));
                              setCabMuestreadas(p.cabezasMuestreadas != null ? String(p.cabezasMuestreadas) : '');
                              setObsPesada(p.observaciones ?? '');
                              abrirDialogo('pesada');
                            }}>Editar</Button>
                            <Button size="small" color="error" onClick={() => void eliminarRegistro('pesada', p.id)}>Eliminar</Button>
                          </TableCell>
                        )}
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
                  setFConsumo(hoy());
                  setInsumoConsumo('');
                  setCantConsumo('');
                  setMsConsumo('');
                  setObsConsumo('');
                  abrirDialogo('consumo');
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
                      <TableCell align="right">Cantidad (kg)</TableCell>
                      <TableCell align="right">MS %</TableCell>
                      {!loteCerrado && <TableCell align="right">Acciones</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {consumos.map((c) => (
                      <TableRow key={c.id}>
                        <TableCell>{c.fecha}</TableCell>
                        <TableCell>{nomInsumo(c.insumoId)}</TableCell>
                        <TableCell align="right">{String(c.cantidadKg)}</TableCell>
                        <TableCell align="right">{c.materiaSecaPct ?? '—'}</TableCell>
                        {!loteCerrado && (
                          <TableCell align="right">
                            <Button size="small" onClick={() => {
                              setEditConsumoId(c.id);
                              setFConsumo(c.fecha);
                              setInsumoConsumo(c.insumoId);
                              setCantConsumo(String(c.cantidadKg));
                              setMsConsumo(c.materiaSecaPct != null ? String(c.materiaSecaPct) : '');
                              setObsConsumo(c.observaciones ?? '');
                              abrirDialogo('consumo');
                            }}>Editar</Button>
                            <Button size="small" color="error" onClick={() => void eliminarRegistro('consumo', c.id)}>Eliminar</Button>
                          </TableCell>
                        )}
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
                  setFLectura(hoy());
                  setBunkScore('UNO');
                  setKgEntregados('');
                  setTempLectura('');
                  setHumLectura('');
                  setObsLectura('');
                  setEditLecturaId(null);
                  abrirDialogo('comedero');
                }}
              >
                Registrar lectura
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Bunk score</TableCell>
                      <TableCell align="right">Kg entregados</TableCell>
                      <TableCell>Obs.</TableCell>
                      {!loteCerrado && <TableCell align="right">Acciones</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {lecturas.map((lec) => (
                      <TableRow key={lec.id}>
                        <TableCell>{lec.fecha}</TableCell>
                        <TableCell>{etiquetaBunkScore(String(lec.bunkScore))}</TableCell>
                        <TableCell align="right">{lec.kgEntregados ?? '—'}</TableCell>
                        <TableCell>{lec.observaciones ?? '—'}</TableCell>
                        {!loteCerrado && (
                          <TableCell align="right">
                            <Button
                              size="small"
                              onClick={() => {
                                setEditLecturaId(lec.id);
                                setFLectura(lec.fecha);
                                setBunkScore(lec.bunkScore as FeedlotBunkScore);
                                setKgEntregados(lec.kgEntregados != null ? String(lec.kgEntregados) : '');
                                setTempLectura(lec.temperaturaDia != null ? String(lec.temperaturaDia) : '');
                                setHumLectura(lec.humedadDia != null ? String(lec.humedadDia) : '');
                                setObsLectura(lec.observaciones ?? '');
                                abrirDialogo('comedero');
                              }}
                            >
                              Editar
                            </Button>
                            <Button
                              size="small"
                              color="error"
                              onClick={() => void eliminarRegistro('comedero', lec.id)}
                            >
                              Eliminar
                            </Button>
                          </TableCell>
                        )}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 4 && (
            <Box>
              {consumoTeorico?.dietaNombre ? (
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Dieta: {consumoTeorico.dietaNombre}
                  {consumoTeorico.fechaDesde && consumoTeorico.fechaHasta
                    ? ` · ${consumoTeorico.fechaDesde} → ${consumoTeorico.fechaHasta}`
                    : ''}
                </Typography>
              ) : (
                <Alert severity="info" sx={{ mb: 2 }}>
                  El lote no tiene dieta asignada o no hay fases configuradas.
                </Alert>
              )}
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Fase</TableCell>
                      <TableCell align="right">Teórico MS (kg)</TableCell>
                      <TableCell align="right">Real (kg)</TableCell>
                      <TableCell align="right">Diferencia (kg)</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {(consumoTeorico?.dias ?? []).map((d) => (
                      <TableRow key={d.fecha}>
                        <TableCell>{d.fecha}</TableCell>
                        <TableCell>{d.faseNombre ?? '—'}</TableCell>
                        <TableCell align="right">
                          {d.kgMsTeorico != null ? Number(d.kgMsTeorico).toFixed(2) : '—'}
                        </TableCell>
                        <TableCell align="right">
                          {d.kgReal != null ? Number(d.kgReal).toFixed(2) : '—'}
                        </TableCell>
                        <TableCell align="right">
                          {d.diferenciaKg != null ? Number(d.diferenciaKg).toFixed(2) : '—'}
                        </TableCell>
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
                  setFMuerte(hoy());
                  setCabMuerte('');
                  setMotivoMuerte('');
                  setObsMuerte('');
                  abrirDialogo('muerte');
                }}
              >
                Registrar muerte
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell align="right">Cabezas</TableCell>
                      <TableCell>Motivo</TableCell>
                      <TableCell>Obs.</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {muertes.map((m) => (
                      <TableRow key={m.id}>
                        <TableCell>{m.fecha}</TableCell>
                        <TableCell align="right">{m.cabezas}</TableCell>
                        <TableCell>{m.motivoNombre ?? '—'}</TableCell>
                        <TableCell>{m.observaciones ?? '—'}</TableCell>
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
                disabled={loteCerrado}
                onClick={() => {
                  setFSan(hoy());
                  setTipoSan('VACUNA');
                  setDescSan('');
                  setInsumoSan('');
                  setDiasRetiro('');
                  setObsSan('');
                  abrirDialogo('sanidad');
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
                      <TableCell align="right">Días retiro</TableCell>
                      {!loteCerrado && <TableCell align="right">Acciones</TableCell>}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {eventos.map((e) => (
                      <TableRow key={e.id}>
                        <TableCell>{e.fecha}</TableCell>
                        <TableCell>{e.tipo}</TableCell>
                        <TableCell>{e.descripcion ?? '—'}</TableCell>
                        <TableCell>{e.insumoId ? nomInsumo(e.insumoId) : '—'}</TableCell>
                        <TableCell align="right">{e.diasRetiro ?? '—'}</TableCell>
                        {!loteCerrado && (
                          <TableCell align="right">
                            <Button size="small" onClick={() => {
                              setEditEventoId(e.id);
                              setFSan(e.fecha);
                              setTipoSan(e.tipo as FeedlotTipoEventoSanitario);
                              setDescSan(e.descripcion ?? '');
                              setInsumoSan(e.insumoId ?? '');
                              setDiasRetiro(e.diasRetiro != null ? String(e.diasRetiro) : '');
                              setObsSan(e.observaciones ?? '');
                              abrirDialogo('sanidad');
                            }}>Editar</Button>
                            <Button size="small" color="error" onClick={() => void eliminarRegistro('sanidad', e.id)}>Eliminar</Button>
                          </TableCell>
                        )}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          {pestana === 7 && (
            <Box>
              <Button
                variant="contained"
                sx={{ mb: 2 }}
                disabled={loteCerrado}
                onClick={() => {
                  setFVenta(hoy());
                  setTipoVenta('VENTA_EN_PIE');
                  setCabVenta('');
                  setPesoVenta('');
                  setPrecioKg('');
                  setTotalVenta('');
                  setComprador('');
                  setObsVenta('');
                  abrirDialogo('venta');
                }}
              >
                Registrar venta / faena
              </Button>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell align="right">Cabezas</TableCell>
                      <TableCell align="right">Total</TableCell>
                      <TableCell>Comprador</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {ventas.map((v) => (
                      <TableRow key={v.id}>
                        <TableCell>{v.fecha}</TableCell>
                        <TableCell>{v.tipo}</TableCell>
                        <TableCell align="right">{v.cabezas}</TableCell>
                        <TableCell align="right">{v.total != null ? String(v.total) : '—'}</TableCell>
                        <TableCell>{v.comprador ?? '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </>
      )}

      <Dialog open={dialogo === 'pesada'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>{editPesadaId != null ? 'Editar pesada' : 'Nueva pesada'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fPesada} onChange={(ev) => setFPesada(ev.target.value)} />
            <TextField label="Peso promedio (kg)" fullWidth value={pesoProm} onChange={(ev) => setPesoProm(ev.target.value)} />
            <TextField label="Cabezas muestreadas (opcional)" type="number" fullWidth value={cabMuestreadas} onChange={(ev) => setCabMuestreadas(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsPesada} onChange={(ev) => setObsPesada(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarPesada()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'consumo'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>{editConsumoId != null ? 'Editar consumo' : 'Consumo de alimento'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fConsumo} onChange={(ev) => setFConsumo(ev.target.value)} />
            <Autocomplete<InsumoOpcion>
              label="Insumo (alimento / balanceado)"
              options={opcionesInsumoAlimento.map((i) => ({
                value: i.id,
                label:
                  i.stockActual != null
                    ? `${i.nombre} · Stock: ${i.stockActual} ${i.unidadMedida ?? 'kg'}`
                    : i.nombre,
                data: i,
              }))}
              value={insumoConsumo === '' ? undefined : insumoConsumo}
              onChange={(value) => setInsumoConsumo(value != null && value !== '' ? Number(value) : '')}
              placeholder="Buscar alimento por nombre…"
              emptyMessage={
                opcionesInsumoAlimento.length === 0
                  ? 'No hay insumos de alimento en inventario. Carguelos en Insumos feedlot.'
                  : 'No se encontraron insumos'
              }
              maxHeight={280}
            />
            <TextField label="Cantidad (kg)" fullWidth value={cantConsumo} onChange={(ev) => setCantConsumo(ev.target.value)} />
            <TextField label="Materia seca % (opcional)" fullWidth value={msConsumo} onChange={(ev) => setMsConsumo(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsConsumo} onChange={(ev) => setObsConsumo(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarConsumo()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'muerte'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>Registrar muerte</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fMuerte} onChange={(ev) => setFMuerte(ev.target.value)} />
            <TextField label="Cabezas" type="number" fullWidth value={cabMuerte} onChange={(ev) => setCabMuerte(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField select label="Motivo (opcional)" fullWidth value={motivoMuerte} onChange={(ev) => setMotivoMuerte(ev.target.value === '' ? '' : Number(ev.target.value))}>
              <MenuItem value="">Sin motivo catalogado</MenuItem>
              {motivosMuerte.map((m) => (
                <MenuItem key={m.id} value={m.id}>{m.nombre}</MenuItem>
              ))}
            </TextField>
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsMuerte} onChange={(ev) => setObsMuerte(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarMuerte()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'sanidad'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>{editEventoId != null ? 'Editar evento sanitario' : 'Evento sanitario'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fSan} onChange={(ev) => setFSan(ev.target.value)} />
            <TextField select label="Tipo" fullWidth value={tipoSan} onChange={(ev) => setTipoSan(ev.target.value as FeedlotTipoEventoSanitario)}>
              {TIPOS_SANIDAD.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Descripción" fullWidth multiline minRows={2} value={descSan} onChange={(ev) => setDescSan(ev.target.value)} />
            <Autocomplete<InsumoOpcion>
              label="Insumo (opcional)"
              options={opcionesInsumoSanidad.map((i) => ({
                value: i.id,
                label: i.nombre,
                data: i,
              }))}
              value={insumoSan === '' ? undefined : insumoSan}
              onChange={(value) => setInsumoSan(value != null && value !== '' ? Number(value) : '')}
              placeholder="Buscar insumo sanitario…"
              emptyMessage={
                opcionesInsumoSanidad.length === 0
                  ? 'No hay insumos en inventario'
                  : 'No se encontraron insumos'
              }
              maxHeight={240}
            />
            <TextField label="Días de retiro (opcional)" type="number" fullWidth value={diasRetiro} onChange={(ev) => setDiasRetiro(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsSan} onChange={(ev) => setObsSan(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarSanidad()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'venta'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>Registrar venta / faena</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fVenta} onChange={(ev) => setFVenta(ev.target.value)} />
            <TextField select label="Tipo" fullWidth value={tipoVenta} onChange={(ev) => setTipoVenta(ev.target.value as FeedlotTipoVenta)}>
              {TIPOS_VENTA.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Cabezas" type="number" fullWidth value={cabVenta} onChange={(ev) => setCabVenta(ev.target.value)} inputProps={{ min: 1 }} />
            <TextField label="Peso promedio (kg, opcional)" fullWidth value={pesoVenta} onChange={(ev) => setPesoVenta(ev.target.value)} />
            <TextField label="Precio ($/kg, opcional)" fullWidth value={precioKg} onChange={(ev) => setPrecioKg(ev.target.value)} />
            <TextField label="Total (opcional)" fullWidth value={totalVenta} onChange={(ev) => setTotalVenta(ev.target.value)} />
            <TextField label="Comprador" fullWidth value={comprador} onChange={(ev) => setComprador(ev.target.value)} />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsVenta} onChange={(ev) => setObsVenta(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarVenta()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'comedero'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>{editLecturaId != null ? 'Editar lectura de comedero' : 'Nueva lectura de comedero'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fLectura} onChange={(ev) => setFLectura(ev.target.value)} />
            <TextField select label="Bunk score" fullWidth value={bunkScore} onChange={(ev) => setBunkScore(ev.target.value as FeedlotBunkScore)}>
              {BUNK_SCORES.map((b) => (
                <MenuItem key={b.valor} value={b.valor}>{b.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Kg entregados (opcional)" fullWidth value={kgEntregados} onChange={(ev) => setKgEntregados(ev.target.value)} />
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              <TextField label="Temperatura (°C)" fullWidth value={tempLectura} onChange={(ev) => setTempLectura(ev.target.value)} placeholder="Opcional" />
              <TextField label="Humedad (%)" fullWidth value={humLectura} onChange={(ev) => setHumLectura(ev.target.value)} placeholder="Opcional" />
            </Stack>
            <BotonRellenarClima
              climaLatitud={lote?.climaLatitud}
              climaLongitud={lote?.climaLongitud}
              deshabilitado={loteCerrado}
              onValores={(v) => {
                if (v.temperatura != null) setTempLectura(String(v.temperatura));
                if (v.humedad != null) setHumLectura(String(v.humedad));
              }}
            />
            <TextField label="Observaciones" fullWidth multiline minRows={2} value={obsLectura} onChange={(ev) => setObsLectura(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarLectura()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'cierre'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>Cerrar lote</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mt: 1 }}>
            {(lote?.cabezasActuales ?? 0) > 0
              ? `Quedan ${lote?.cabezasActuales} cabezas en plantel. El cierre manual requiere confirmación.`
              : 'El lote quedará en estado CERRADO y el corral volverá a DISPONIBLE.'}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" color="warning" onClick={() => void confirmarCierreLote()}>Confirmar cierre</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'ajuste'} onClose={cerrarDialogos} {...PROPS_DIALOGO}>
        <DialogTitle>Ajuste de plantel</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Cabezas actuales: {lote?.cabezasActuales ?? '—'}
            </Typography>
            <TextField type="date" label="Fecha" InputLabelProps={{ shrink: true }} fullWidth value={fAjuste} onChange={(ev) => setFAjuste(ev.target.value)} />
            <TextField label="Cabezas después del ajuste" type="number" fullWidth value={cabAjuste} onChange={(ev) => setCabAjuste(ev.target.value)} />
            <TextField label="Motivo (obligatorio)" fullWidth multiline minRows={2} value={motivoAjuste} onChange={(ev) => setMotivoAjuste(ev.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogos}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarAjuste()}>Registrar ajuste</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DetalleLoteFeedlotScreen;
