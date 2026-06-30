import React, { useCallback, useEffect, useState } from 'react';
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
  Typography,
} from '@mui/material';
import {
  Line,
  LineChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  listarLotesCrianza,
  obtenerReporteAnalisisLotesCrianza,
  obtenerCurvaPesoCrianza,
  AvicolaCrianzaReporteResumen,
  AvicolaCrianzaCurvaPeso,
  AvicolaLoteRespuesta,
  mensajeError,
} from '../services/avicolaCrianzaApi';

const ReportesCrianzaScreen: React.FC = () => {
  const [resumen, setResumen] = useState<AvicolaCrianzaReporteResumen | null>(null);
  const [lotes, setLotes] = useState<AvicolaLoteRespuesta[]>([]);
  const [loteId, setLoteId] = useState<number | ''>('');
  const [curva, setCurva] = useState<AvicolaCrianzaCurvaPeso | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void (async () => {
      try {
        const [r, l] = await Promise.all([obtenerReporteAnalisisLotesCrianza(), listarLotesCrianza()]);
        setResumen(r);
        setLotes(l);
        if (l.length === 1) setLoteId(l[0].id);
      } catch (e: unknown) {
        setError(mensajeError(e));
      }
    })();
  }, []);

  const cargarCurva = useCallback(async () => {
    if (loteId === '') {
      setCurva(null);
      return;
    }
    try {
      setCargando(true);
      setError(null);
      const c = await obtenerCurvaPesoCrianza(loteId);
      setCurva(c);
    } catch (e: unknown) {
      setError(mensajeError(e));
      setCurva(null);
    } finally {
      setCargando(false);
    }
  }, [loteId]);

  useEffect(() => {
    void cargarCurva();
  }, [cargarCurva]);

  const datosCurva = (curva?.serie ?? []).map((p) => ({
    fecha: p.fecha?.slice(0, 10),
    peso: p.pesoPromedio ?? 0,
    dias: p.diasDesdeIngreso ?? 0,
  }));

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Reportes — Avícola crianza
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {resumen && (
        <Stack direction="row" spacing={2} flexWrap="wrap" sx={{ mb: 3 }}>
          <Paper sx={{ p: 2, minWidth: 140 }}>
            <Typography variant="caption" color="text.secondary">
              Lotes activos
            </Typography>
            <Typography variant="h5">{resumen.lotesActivos}</Typography>
          </Paper>
          <Paper sx={{ p: 2, minWidth: 140 }}>
            <Typography variant="caption" color="text.secondary">
              Lotes cerrados
            </Typography>
            <Typography variant="h5">{resumen.lotesCerrados}</Typography>
          </Paper>
          <Paper sx={{ p: 2, minWidth: 160 }}>
            <Typography variant="caption" color="text.secondary">
              Mortalidad prom.
            </Typography>
            <Typography variant="h5">{resumen.mortalidadPromedioPct?.toFixed(2) ?? '—'} %</Typography>
          </Paper>
          <Paper sx={{ p: 2, minWidth: 160 }}>
            <Typography variant="caption" color="text.secondary">
              FCR prom.
            </Typography>
            <Typography variant="h5">{resumen.conversionPromedio?.toFixed(3) ?? '—'}</Typography>
          </Paper>
        </Stack>
      )}

      {resumen && (
        <TableContainer component={Paper} sx={{ mb: 4 }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Lote</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Disp.</TableCell>
                <TableCell align="right">Mortalidad %</TableCell>
                <TableCell align="right">FCR</TableCell>
                <TableCell align="right">Días</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {resumen.porLote.map((f) => (
                <TableRow key={f.loteId}>
                  <TableCell>{f.nombreLote}</TableCell>
                  <TableCell>{f.estado}</TableCell>
                  <TableCell align="right">{f.cantidadDisponible ?? '—'}</TableCell>
                  <TableCell align="right">{f.mortalidadPct?.toFixed(2) ?? '—'}</TableCell>
                  <TableCell align="right">{f.conversionAlimenticia?.toFixed(3) ?? '—'}</TableCell>
                  <TableCell align="right">{f.diasEnProduccion ?? '—'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Typography variant="h6" gutterBottom>
        Curva de peso
      </Typography>
      <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
        <FormControl sx={{ minWidth: 220 }} size="small">
          <InputLabel>Lote</InputLabel>
          <Select
            label="Lote"
            value={loteId === '' ? '' : String(loteId)}
            onChange={(e) => setLoteId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Seleccionar…</MenuItem>
            {lotes.map((l) => (
              <MenuItem key={l.id} value={String(l.id)}>
                {l.nombre}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <Button variant="outlined" onClick={() => void cargarCurva()} disabled={cargando || loteId === ''}>
          Actualizar
        </Button>
      </Stack>

      {cargando && <CircularProgress size={24} />}
      {!cargando && datosCurva.length > 0 && (
        <Paper sx={{ p: 2, height: 320 }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={datosCurva}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis
                dataKey="dias"
                name="Días"
                label={{ value: 'Días desde ingreso', position: 'insideBottom', offset: -5 }}
              />
              <YAxis label={{ value: 'kg', angle: -90, position: 'insideLeft' }} />
              <Tooltip />
              <Line type="monotone" dataKey="peso" stroke="#2563eb" name="Peso prom. (kg)" dot />
            </LineChart>
          </ResponsiveContainer>
        </Paper>
      )}
    </Box>
  );
};

export default ReportesCrianzaScreen;
