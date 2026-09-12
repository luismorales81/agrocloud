import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  MenuItem,
  Paper,
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
import { Icon } from '../../../components/icons';
import type { PorcinosReporteResumen } from '../typesApiV2';
import { mensajeError, obtenerReporte } from '../services/porcinosApi';

const TIPOS_REPORTE = [
  { valor: 'productivo', etiqueta: 'Productivo' },
  { valor: 'reproductivo', etiqueta: 'Reproductivo' },
  { valor: 'alimentacion', etiqueta: 'Alimentación' },
  { valor: 'economico', etiqueta: 'Económico' },
];

const ReportesPorcinosScreen: React.FC = () => {
  const [tipo, setTipo] = useState('productivo');
  const [reporte, setReporte] = useState<PorcinosReporteResumen | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const data = await obtenerReporte(tipo);
      setReporte(data);
    } catch (err: unknown) {
      setError(mensajeError(err));
      setReporte(null);
    } finally {
      setCargando(false);
    }
  }, [tipo]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const metricas = reporte?.metricas ?? {};
  const filas = reporte?.filas ?? [];
  const columnas =
    filas.length > 0 ? Object.keys(filas[0]) : [];

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1000 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="BarChart" size={28} />
        <Typography variant="h4" component="h1">
          Reportes
        </Typography>
      </Stack>

      <Stack direction="row" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <TextField
          select
          size="small"
          label="Tipo de reporte"
          value={tipo}
          onChange={(e) => setTipo(e.target.value)}
          sx={{ minWidth: 200 }}
        >
          {TIPOS_REPORTE.map((t) => (
            <MenuItem key={t.valor} value={t.valor}>
              {t.etiqueta}
            </MenuItem>
          ))}
        </TextField>
        <Button variant="outlined" onClick={() => void cargar()} disabled={cargando}>
          Actualizar
        </Button>
      </Stack>

      {cargando && <CircularProgress size={24} />}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && reporte && (
        <>
          {reporte.titulo && (
            <Typography variant="h6" gutterBottom>
              {reporte.titulo}
            </Typography>
          )}

          {Object.keys(metricas).length > 0 && (
            <Stack direction="row" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
              {Object.entries(metricas).map(([clave, valor]) => (
                <Paper key={clave} variant="outlined" sx={{ p: 2, minWidth: 140 }}>
                  <Typography variant="caption" color="text.secondary">
                    {clave}
                  </Typography>
                  <Typography variant="h6">{valor ?? '—'}</Typography>
                </Paper>
              ))}
            </Stack>
          )}

          {columnas.length > 0 && (
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
                  {filas.map((fila, idx) => (
                    <TableRow key={idx}>
                      {columnas.map((c) => (
                        <TableCell key={c}>{fila[c] ?? '—'}</TableCell>
                      ))}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {Object.keys(metricas).length === 0 && filas.length === 0 && (
            <Paper variant="outlined" sx={{ p: 3 }}>
              <Typography color="text.secondary">
                No hay datos para el reporte seleccionado.
              </Typography>
            </Paper>
          )}
        </>
      )}
    </Box>
  );
};

export default ReportesPorcinosScreen;
