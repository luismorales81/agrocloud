import React, { useCallback, useEffect, useMemo, useState } from 'react';
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
import type { LecheriaAnimal, LecheriaCurvaLactancia } from '../types';
import {
  listarAnimales,
  mensajeError,
  obtenerClimaProduccion,
  obtenerCurvasLactancia,
  obtenerRankingProduccion,
} from '../services/lecheriaApi';

function fmt(n: number | null | undefined, dec = 1): string {
  if (n == null || Number.isNaN(Number(n))) return '—';
  return Number(n).toFixed(dec);
}

const ReportesLecheriaScreen: React.FC = () => {
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [animales, setAnimales] = useState<LecheriaAnimal[]>([]);

  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [animalCurvaId, setAnimalCurvaId] = useState<number | ''>('');
  const [curvas, setCurvas] = useState<LecheriaCurvaLactancia[]>([]);
  const [ranking, setRanking] = useState<{ identificacion: string; litros: number }[]>([]);
  const [climaProd, setClimaProd] = useState<
    { fecha: string; temperatura?: number; humedad?: number; litros?: number }[]
  >([]);
  const [cargandoReportes, setCargandoReportes] = useState(false);

  const cargarInicial = useCallback(async () => {
    try {
      setCargando(true);
      const lista = await listarAnimales();
      setAnimales(lista);
      if (lista.length === 1) {
        setAnimalCurvaId(lista[0].id);
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargarInicial();
  }, [cargarInicial]);

  const cargarReportes = useCallback(async () => {
    try {
      setCargandoReportes(true);
      setError(null);
      const [c, r, cl] = await Promise.all([
        obtenerCurvasLactancia(animalCurvaId === '' ? undefined : Number(animalCurvaId)),
        obtenerRankingProduccion(desde || undefined, hasta || undefined),
        obtenerClimaProduccion(desde || undefined, hasta || undefined),
      ]);
      setCurvas(c);
      setRanking(
        (r.items ?? []).map((it) => ({
          identificacion: it.identificacion ?? `Animal ${it.animalId}`,
          litros: Number(it.litrosTotales ?? 0),
        }))
      );
      setClimaProd(
        (cl.puntos ?? []).map((p) => ({
          fecha: String(p.fecha ?? '').slice(5),
          temperatura: p.temperatura != null ? Number(p.temperatura) : undefined,
          humedad: p.humedad != null ? Number(p.humedad) : undefined,
          litros: p.litros != null ? Number(p.litros) : undefined,
        }))
      );
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargandoReportes(false);
    }
  }, [animalCurvaId, desde, hasta]);

  useEffect(() => {
    if (!cargando) {
      void cargarReportes();
    }
  }, [cargarReportes, cargando]);

  const puntosCurva = useMemo(() => {
    const curva = curvas.find((c) => animalCurvaId !== '' && c.animalId === animalCurvaId) ?? curvas[0];
    return (curva?.puntos ?? []).map((p) => ({
      dim: p.dim ?? 0,
      litros: Number(p.litros ?? 0),
      fecha: String(p.fecha ?? '').slice(5),
    }));
  }, [curvas, animalCurvaId]);

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="BarChart" size={28} />
        <Typography variant="h4" component="h1">
          Reportes — Lechería
        </Typography>
      </Stack>

      <Stack direction="row" spacing={2} flexWrap="wrap" sx={{ mb: 3 }}>
        <TextField
          label="Desde"
          type="date"
          size="small"
          InputLabelProps={{ shrink: true }}
          value={desde}
          onChange={(e) => setDesde(e.target.value)}
        />
        <TextField
          label="Hasta"
          type="date"
          size="small"
          InputLabelProps={{ shrink: true }}
          value={hasta}
          onChange={(e) => setHasta(e.target.value)}
        />
        <Button variant="outlined" onClick={() => void cargarReportes()} disabled={cargandoReportes}>
          Actualizar
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <Stack spacing={3}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Curvas de lactancia
            </Typography>
            <FormControl size="small" sx={{ minWidth: 220, mb: 2 }}>
              <InputLabel>Animal</InputLabel>
              <Select
                label="Animal"
                value={animalCurvaId}
                onChange={(e) => setAnimalCurvaId(e.target.value === '' ? '' : Number(e.target.value))}
              >
                <MenuItem value="">
                  <em>Todos</em>
                </MenuItem>
                {animales.map((a) => (
                  <MenuItem key={a.id} value={a.id}>
                    {a.identificacion}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            {puntosCurva.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={puntosCurva}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="dim" name="DIM" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line type="monotone" dataKey="litros" name="Litros" stroke="#0284c7" dot />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <Typography color="text.secondary">Sin datos de curva para el animal seleccionado.</Typography>
            )}
          </Paper>

          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Ranking de producción
            </Typography>
            {ranking.length > 0 ? (
              <>
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={ranking.slice(0, 15)} layout="vertical" margin={{ left: 80 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" />
                    <YAxis type="category" dataKey="identificacion" width={80} />
                    <Tooltip formatter={(v: number) => `${fmt(v)} L`} />
                    <Bar dataKey="litros" name="Litros" fill="#0284c7" />
                  </BarChart>
                </ResponsiveContainer>
                <TableContainer sx={{ mt: 2 }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>#</TableCell>
                        <TableCell>Animal</TableCell>
                        <TableCell align="right">Litros totales</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {ranking.map((it, idx) => (
                        <TableRow key={it.identificacion}>
                          <TableCell>{idx + 1}</TableCell>
                          <TableCell>{it.identificacion}</TableCell>
                          <TableCell align="right">{fmt(it.litros)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </>
            ) : (
              <Typography color="text.secondary">Sin datos de ranking en el período.</Typography>
            )}
          </Paper>

          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Clima vs producción
            </Typography>
            {climaProd.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={climaProd}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="fecha" />
                  <YAxis yAxisId="left" />
                  <YAxis yAxisId="right" orientation="right" />
                  <Tooltip />
                  <Legend />
                  <Line yAxisId="left" type="monotone" dataKey="litros" name="Litros" stroke="#0284c7" />
                  <Line yAxisId="right" type="monotone" dataKey="temperatura" name="Temp. °C" stroke="#f59e0b" />
                  <Line yAxisId="right" type="monotone" dataKey="humedad" name="Humedad %" stroke="#10b981" />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <Typography color="text.secondary">
                Sin correlación clima-producción en el período (requiere ordeñes con datos de clima).
              </Typography>
            )}
          </Paper>
        </Stack>
      )}
    </Box>
  );
};

export default ReportesLecheriaScreen;
