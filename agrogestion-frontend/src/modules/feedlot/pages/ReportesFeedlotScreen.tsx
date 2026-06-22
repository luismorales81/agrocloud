import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { Icon } from '../../../components/icons';
import type { FeedlotAnalisisLoteItem } from '../types';
import {
  exportarReportesExcel,
  mensajeError,
  obtenerAnalisisLotes,
  obtenerCurvaPeso,
} from '../services/feedlotApi';

function fmt(n: number | null | undefined, dec = 2): string {
  if (n == null || Number.isNaN(Number(n))) return '—';
  return Number(n).toFixed(dec);
}

const ReportesFeedlotScreen: React.FC = () => {
  const navigate = useNavigate();
  const [lotes, setLotes] = useState<FeedlotAnalisisLoteItem[]>([]);
  const [loteCurvaId, setLoteCurvaId] = useState<number | ''>('');
  const [pesoObjetivo, setPesoObjetivo] = useState('');
  const [cargando, setCargando] = useState(true);
  const [cargandoCurva, setCargandoCurva] = useState(false);
  const [exportando, setExportando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [puntosCurva, setPuntosCurva] = useState<
    { fecha: string; peso: number; tipo: string }[]
  >([]);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      setError(null);
      const analisis = await obtenerAnalisisLotes();
      const items = analisis.lotes ?? [];
      setLotes(items);
      if (items.length === 1) {
        setLoteCurvaId(items[0].loteId);
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const cargarCurva = useCallback(async () => {
    if (loteCurvaId === '') {
      setPuntosCurva([]);
      return;
    }
    try {
      setCargandoCurva(true);
      const po = pesoObjetivo.trim() === '' ? undefined : parseFloat(pesoObjetivo.replace(',', '.'));
      const curva = await obtenerCurvaPeso(
        Number(loteCurvaId),
        po != null && !Number.isNaN(po) ? po : undefined
      );
      const real = (curva.serie ?? []).map((p) => ({
        fecha: String(p.fecha).slice(5),
        peso: Number(p.pesoKg),
        tipo: 'Pesada',
      }));
      const proy = (curva.proyeccion ?? []).map((p) => ({
        fecha: String(p.fecha).slice(5),
        peso: Number(p.pesoKg),
        tipo: 'Proyección',
      }));
      setPuntosCurva([...real, ...proy]);
    } catch (err: unknown) {
      setError(mensajeError(err));
      setPuntosCurva([]);
    } finally {
      setCargandoCurva(false);
    }
  }, [loteCurvaId, pesoObjetivo]);

  useEffect(() => {
    void cargarCurva();
  }, [cargarCurva]);

  const datosGmd = useMemo(
    () =>
      lotes.map((l) => ({
        nombre: l.loteNombre ?? `Lote ${l.loteId}`,
        gmd: l.gmd != null ? Number(l.gmd) : 0,
      })),
    [lotes]
  );

  const datosMortalidad = useMemo(
    () =>
      lotes.map((l) => ({
        nombre: l.loteNombre ?? `Lote ${l.loteId}`,
        mortalidad: l.mortalidadPct != null ? Number(l.mortalidadPct) : 0,
      })),
    [lotes]
  );

  const datosCa = useMemo(
    () =>
      lotes.map((l) => ({
        nombre: l.loteNombre ?? `Lote ${l.loteId}`,
        ca: l.conversionAlimenticia != null ? Number(l.conversionAlimenticia) : 0,
      })),
    [lotes]
  );

  const exportarExcel = async () => {
    try {
      setExportando(true);
      setError(null);
      await exportarReportesExcel();
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setExportando(false);
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="BarChart" size={28} />
          <Typography variant="h5" component="h1">
            Reportes feedlot
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" onClick={() => void cargar()} disabled={cargando}>
            Actualizar
          </Button>
          <Button variant="contained" onClick={() => void exportarExcel()} disabled={exportando}>
            {exportando ? 'Exportando…' : 'Exportar Excel'}
          </Button>
        </Stack>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Comparativa de lotes del período activo: GMD, mortalidad, conversión alimenticia y curva de peso.
      </Typography>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={1} sx={{ my: 2 }}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando análisis…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && lotes.length === 0 && (
        <Alert severity="info">No hay lotes con datos en el período activo.</Alert>
      )}

      {!cargando && lotes.length > 0 && (
        <>
          <TableContainer component={Paper} variant="outlined" sx={{ mb: 3 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Lote</TableCell>
                  <TableCell align="right">GMD (kg/día)</TableCell>
                  <TableCell align="right">Mortalidad (%)</TableCell>
                  <TableCell align="right">Conv. alimenticia</TableCell>
                  <TableCell align="right">Margen</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lotes.map((l) => (
                  <TableRow key={l.loteId} hover>
                    <TableCell>{l.loteNombre ?? `Lote ${l.loteId}`}</TableCell>
                    <TableCell align="right">{fmt(l.gmd, 3)}</TableCell>
                    <TableCell align="right">{fmt(l.mortalidadPct)}</TableCell>
                    <TableCell align="right">{fmt(l.conversionAlimenticia, 3)}</TableCell>
                    <TableCell align="right">{fmt(l.margen)}</TableCell>
                    <TableCell align="right">
                      <Button size="small" onClick={() => navigate(`/feedlot/lotes/${l.loteId}`)}>
                        Ver lote
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
              gap: 2,
              mb: 3,
            }}
          >
            <Paper sx={{ p: 2, height: 280 }}>
              <Typography variant="subtitle2" gutterBottom>
                GMD por lote
              </Typography>
              <ResponsiveContainer width="100%" height="90%">
                <BarChart data={datosGmd}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nombre" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip formatter={(v: number) => [v.toFixed(3), 'GMD']} />
                  <Bar dataKey="gmd" fill="#78350f" name="GMD" />
                </BarChart>
              </ResponsiveContainer>
            </Paper>

            <Paper sx={{ p: 2, height: 280 }}>
              <Typography variant="subtitle2" gutterBottom>
                Mortalidad (%)
              </Typography>
              <ResponsiveContainer width="100%" height="90%">
                <BarChart data={datosMortalidad}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nombre" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip formatter={(v: number) => [`${v.toFixed(2)} %`, 'Mortalidad']} />
                  <Bar dataKey="mortalidad" fill="#dc2626" name="Mortalidad" />
                </BarChart>
              </ResponsiveContainer>
            </Paper>

            <Paper sx={{ p: 2, height: 280 }}>
              <Typography variant="subtitle2" gutterBottom>
                Conversión alimenticia
              </Typography>
              <ResponsiveContainer width="100%" height="90%">
                <BarChart data={datosCa}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="nombre" tick={{ fontSize: 11 }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip formatter={(v: number) => [v.toFixed(3), 'CA']} />
                  <Bar dataKey="ca" fill="#2563eb" name="CA" />
                </BarChart>
              </ResponsiveContainer>
            </Paper>
          </Box>

          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Curva de peso
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
              <FormControl size="small" sx={{ minWidth: 220 }}>
                <InputLabel>Lote</InputLabel>
                <Select
                  label="Lote"
                  value={loteCurvaId}
                  onChange={(ev) => setLoteCurvaId(ev.target.value === '' ? '' : Number(ev.target.value))}
                >
                  <MenuItem value="">Seleccionar…</MenuItem>
                  {lotes.map((l) => (
                    <MenuItem key={l.loteId} value={l.loteId}>
                      {l.loteNombre ?? `Lote ${l.loteId}`}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <TextField
                size="small"
                label="Peso objetivo (kg, opcional)"
                value={pesoObjetivo}
                onChange={(ev) => setPesoObjetivo(ev.target.value)}
                sx={{ maxWidth: 220 }}
              />
              <Button variant="outlined" onClick={() => void cargarCurva()} disabled={cargandoCurva || loteCurvaId === ''}>
                Actualizar curva
              </Button>
            </Stack>

            {cargandoCurva && (
              <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
                <CircularProgress size={20} />
                <Typography variant="body2">Calculando curva…</Typography>
              </Stack>
            )}

            {!cargandoCurva && puntosCurva.length > 0 && (
              <ResponsiveContainer width="100%" height={320}>
                <LineChart data={puntosCurva}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="fecha" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} unit=" kg" />
                  <Tooltip formatter={(v: number) => [`${v.toFixed(1)} kg`, 'Peso']} />
                  <Legend />
                  <Line
                    type="monotone"
                    dataKey="peso"
                    stroke="#78350f"
                    strokeWidth={2}
                    dot={{ r: 3 }}
                    name="Peso (kg)"
                  />
                </LineChart>
              </ResponsiveContainer>
            )}

            {!cargandoCurva && loteCurvaId !== '' && puntosCurva.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                Sin datos de pesadas para este lote.
              </Typography>
            )}
          </Paper>
        </>
      )}
    </Box>
  );
};

export default ReportesFeedlotScreen;
