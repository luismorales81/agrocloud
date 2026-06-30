import React, { useCallback, useEffect, useState } from 'react';
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
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { Icon } from '../../../components/icons';
import {
  Line,
  LineChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { AmbienteDiario, AvicolaPonedorasGalpon } from '../types';
import {
  listarAmbienteDiario,
  listarGalpones,
  mensajeErrorPonedoras,
  obtenerResumenGalpon,
} from '../services/avicolaPonedorasService';
import { etiquetaEstadoGalpon, formatearFechaCorta, formatearNumero } from '../util/formateo';

const MAX_GALPONES_TABLA = 12;
const MAX_RESUMENES_PARALELOS = 40;

interface KpisPanel {
  galponesActivos: number;
  posturaPromedioPct: number | null;
  huevosPorAveDiaPromedio: number | null;
  mortalidadPromedioPct: number | null;
}

const AvicolaPonedorasDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [kpis, setKpis] = useState<KpisPanel>({
    galponesActivos: 0,
    posturaPromedioPct: null,
    huevosPorAveDiaPromedio: null,
    mortalidadPromedioPct: null,
  });
  const [galponesActivos, setGalponesActivos] = useState<AvicolaPonedorasGalpon[]>([]);
  const [galponClimaId, setGalponClimaId] = useState<number | ''>('');
  const [serieAmbiente, setSerieAmbiente] = useState<AmbienteDiario[]>([]);
  const [cargandoAmbiente, setCargandoAmbiente] = useState(false);
  const [snackbarExito, setSnackbarExito] = useState(false);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const activos = await listarGalpones({ estado: 'ACTIVO' });

      const muestra = activos.slice(0, MAX_RESUMENES_PARALELOS);
      const resumenes = await Promise.all(
        muestra.map(async (g) => {
          try {
            return await obtenerResumenGalpon(g.id);
          } catch {
            return null;
          }
        })
      );

      const posturas = resumenes
        .filter((r): r is NonNullable<(typeof resumenes)[number]> => r !== null && r.porcentajePostura != null)
        .map((r) => r.porcentajePostura as number);
      const huevosAve = resumenes
        .filter((r): r is NonNullable<(typeof resumenes)[number]> => r !== null && r.huevosPorAvePorDia != null)
        .map((r) => r.huevosPorAvePorDia as number);
      const mortalidades = resumenes
        .filter((r): r is NonNullable<(typeof resumenes)[number]> => r !== null && r.mortalidadPct != null)
        .map((r) => r.mortalidadPct as number);

      const posturaPromedioPct =
        posturas.length > 0 ? posturas.reduce((a, b) => a + b, 0) / posturas.length : null;
      const huevosPorAveDiaPromedio =
        huevosAve.length > 0 ? huevosAve.reduce((a, b) => a + b, 0) / huevosAve.length : null;
      const mortalidadPromedioPct =
        mortalidades.length > 0 ? mortalidades.reduce((a, b) => a + b, 0) / mortalidades.length : null;

      setKpis({
        galponesActivos: activos.length,
        posturaPromedioPct,
        huevosPorAveDiaPromedio,
        mortalidadPromedioPct,
      });
      setGalponesActivos(activos.slice(0, MAX_GALPONES_TABLA));
      if (activos.length === 1) {
        setGalponClimaId(activos[0].id);
      }
      if (mostrarExito) {
        setSnackbarExito(true);
      }
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar(false);
  }, [cargar]);

  const cargarSerieAmbiente = useCallback(async () => {
    if (galponClimaId === '') {
      setSerieAmbiente([]);
      return;
    }
    try {
      setCargandoAmbiente(true);
      const datos = await listarAmbienteDiario(Number(galponClimaId));
      setSerieAmbiente(datos);
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
      setSerieAmbiente([]);
    } finally {
      setCargandoAmbiente(false);
    }
  }, [galponClimaId]);

  const puntosAmbiente = serieAmbiente
    .filter((a) => a.temperaturaDia != null || a.humedadDia != null)
    .map((a) => ({
      fecha: String(a.fecha).slice(5),
      temperatura: a.temperaturaDia != null ? Number(a.temperaturaDia) : undefined,
      humedad: a.humedadDia != null ? Number(a.humedadDia) : undefined,
    }));

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="BarChart" size={32} />
        <Typography variant="h4" component="h1">
          Avícola ponedoras — Panel
        </Typography>
      </Stack>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
        Indicadores sobre galpones activos; promedios calculados con el resumen por galpón (hasta{' '}
        {MAX_RESUMENES_PARALELOS} registros).
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="contained" size="small" color="primary" onClick={() => navigate('/avicola-ponedoras/dashboard')}>
          Calendario
        </Button>
        <Button variant="outlined" size="small" onClick={() => navigate('/avicola-ponedoras/galpones')}>
          Ver todos los galpones
        </Button>
        <Button variant="text" size="small" onClick={() => void cargar(true)} disabled={cargando}>
          Actualizar datos
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando indicadores…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ my: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <>
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: 2,
              mb: 3,
            }}
          >
            <Paper
              elevation={1}
              sx={{
                p: 2,
                borderLeft: (t) => `4px solid ${t.palette.primary.main}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                Galpones activos
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.galponesActivos}
              </Typography>
            </Paper>
            <Paper
              elevation={1}
              sx={{
                p: 2,
                borderLeft: (t) => `4px solid ${t.palette.success.main}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                % postura promedio
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.posturaPromedioPct != null
                  ? `${formatearNumero(kpis.posturaPromedioPct, 2)} %`
                  : '—'}
              </Typography>
            </Paper>
            <Paper
              elevation={1}
              sx={{
                p: 2,
                borderLeft: (t) => `4px solid ${t.palette.info.main}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                Huevos / ave / día (promedio)
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.huevosPorAveDiaPromedio != null
                  ? formatearNumero(kpis.huevosPorAveDiaPromedio, 3)
                  : '—'}
              </Typography>
            </Paper>
            <Paper
              elevation={1}
              sx={{
                p: 2,
                borderLeft: (t) => `4px solid ${t.palette.error.light}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                Mortalidad % promedio
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.mortalidadPromedioPct != null
                  ? `${formatearNumero(kpis.mortalidadPromedioPct, 2)} %`
                  : '—'}
              </Typography>
            </Paper>
          </Box>

          <Typography variant="h6" sx={{ mb: 1 }}>
            Galpones activos (vista rápida)
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Raza</TableCell>
                  <TableCell>Fecha ingreso</TableCell>
                  <TableCell align="right">Aves</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {galponesActivos.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <Typography color="text.secondary" variant="body2">
                        No hay galpones activos.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  galponesActivos.map((g) => (
                    <TableRow key={g.id} hover>
                      <TableCell>{g.nombre}</TableCell>
                      <TableCell>{g.raza?.trim() ? g.raza : '—'}</TableCell>
                      <TableCell>{formatearFechaCorta(g.fechaIngreso)}</TableCell>
                      <TableCell align="right">{g.cantidadAves ?? '—'}</TableCell>
                      <TableCell>{etiquetaEstadoGalpon(g.estado ?? undefined)}</TableCell>
                      <TableCell align="right">
                        <Button size="small" variant="text" onClick={() => navigate(`/avicola-ponedoras/galpones/${g.id}`)}>
                          Abrir
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          <Paper variant="outlined" sx={{ p: 2, mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Ambiente diario (temperatura y humedad)
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
              <FormControl size="small" sx={{ minWidth: 220 }}>
                <InputLabel>Galpón</InputLabel>
                <Select
                  label="Galpón"
                  value={galponClimaId}
                  onChange={(ev) => setGalponClimaId(ev.target.value === '' ? '' : Number(ev.target.value))}
                >
                  <MenuItem value="">Seleccionar…</MenuItem>
                  {galponesActivos.map((g) => (
                    <MenuItem key={g.id} value={g.id}>
                      {g.nombre}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Button variant="outlined" onClick={() => void cargarSerieAmbiente()} disabled={cargandoAmbiente || galponClimaId === ''}>
                Cargar serie
              </Button>
            </Stack>
            {cargandoAmbiente && (
              <Stack direction="row" alignItems="center" spacing={1}>
                <CircularProgress size={20} />
                <Typography variant="body2">Cargando…</Typography>
              </Stack>
            )}
            {!cargandoAmbiente && puntosAmbiente.length > 0 && (
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={puntosAmbiente}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="fecha" tick={{ fontSize: 11 }} />
                  <YAxis yAxisId="temp" tick={{ fontSize: 11 }} unit="°C" />
                  <YAxis yAxisId="hum" orientation="right" tick={{ fontSize: 11 }} unit="%" />
                  <Tooltip />
                  <Legend />
                  <Line yAxisId="temp" type="monotone" dataKey="temperatura" stroke="#dc2626" name="Temp. °C" dot={{ r: 2 }} />
                  <Line yAxisId="hum" type="monotone" dataKey="humedad" stroke="#2563eb" name="Humedad %" dot={{ r: 2 }} />
                </LineChart>
              </ResponsiveContainer>
            )}
            {!cargandoAmbiente && galponClimaId !== '' && puntosAmbiente.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                Sin registros de ambiente. Guardalos al registrar postura en el detalle del galpón.
              </Typography>
            )}
          </Paper>
        </>
      )}

      <Snackbar
        open={snackbarExito}
        autoHideDuration={3500}
        onClose={() => setSnackbarExito(false)}
        message="Datos del panel actualizados"
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
};

export default AvicolaPonedorasDashboard;
