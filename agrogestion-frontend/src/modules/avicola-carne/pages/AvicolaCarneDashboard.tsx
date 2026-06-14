import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
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
import type { AvicolaCarneLote } from '../types';
import { listarLotesCarne, mensajeErrorCarne, obtenerResumenCarne } from '../services/avicolaCarneService';
import { etiquetaEspecieCarne } from '../util/etiquetasEspecieCarne';

const MAX_LOTES_TABLA = 10;
const MAX_RESUMENES_PARALELOS = 40;

interface KpisPanel {
  lotesActivos: number;
  totalAves: number;
  conversionPromedio: number | null;
  mortalidadPromedio: number | null;
}

function formatearNumero(valor: number, fracciones = 2): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: fracciones,
  }).format(valor);
}

const AvicolaCarneDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [kpis, setKpis] = useState<KpisPanel>({
    lotesActivos: 0,
    totalAves: 0,
    conversionPromedio: null,
    mortalidadPromedio: null,
  });
  const [lotesActivos, setLotesActivos] = useState<AvicolaCarneLote[]>([]);
  const [snackbarExito, setSnackbarExito] = useState(false);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const activos = await listarLotesCarne('ACTIVO');
      const totalAves = activos.reduce((acc, l) => acc + (l.cantidadAnimales ?? 0), 0);

      const muestra = activos.slice(0, MAX_RESUMENES_PARALELOS);
      const resumenes = await Promise.all(
        muestra.map(async (l) => {
          try {
            return await obtenerResumenCarne(l.id);
          } catch {
            return null;
          }
        })
      );

      const conversiones = resumenes
        .filter((r): r is NonNullable<typeof r> => r !== null && r.conversionAlimenticia != null)
        .map((r) => r.conversionAlimenticia as number);
      const mortalidades = resumenes
        .filter((r): r is NonNullable<typeof r> => r !== null)
        .map((r) => r.mortalidadPct);

      const conversionPromedio =
        conversiones.length > 0
          ? conversiones.reduce((a, b) => a + b, 0) / conversiones.length
          : null;
      const mortalidadPromedio =
        mortalidades.length > 0
          ? mortalidades.reduce((a, b) => a + b, 0) / mortalidades.length
          : null;

      setKpis({
        lotesActivos: activos.length,
        totalAves,
        conversionPromedio,
        mortalidadPromedio,
      });
      setLotesActivos(activos.slice(0, MAX_LOTES_TABLA));
      if (mostrarExito) {
        setSnackbarExito(true);
      }
    } catch (err: unknown) {
      setError(mensajeErrorCarne(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar(false);
  }, [cargar]);

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="BarChart" size={32} />
        <Typography variant="h4" component="h1">
          Avícola carne — Panel
        </Typography>
      </Stack>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
        Resumen de lotes activos e indicadores calculados desde el backend (resumen por lote).
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="contained" size="small" color="primary" onClick={() => navigate('/avicola-carne/dashboard')}>
          Calendario
        </Button>
        <Button variant="outlined" size="small" onClick={() => navigate('/avicola-carne/lotes')}>
          Ver todos los lotes
        </Button>
        <Button variant="contained" size="small" onClick={() => navigate('/avicola-carne/lotes/nuevo')}>
          Nuevo lote
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
                Lotes activos
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.lotesActivos}
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
                Total aves (activos)
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {formatearNumero(kpis.totalAves, 0)}
              </Typography>
            </Paper>
            <Paper
              elevation={1}
              sx={{
                p: 2,
                borderLeft: (t) => `4px solid ${t.palette.warning.main}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                Conversión alimenticia promedio
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.conversionPromedio != null
                  ? formatearNumero(kpis.conversionPromedio, 3)
                  : '—'}
              </Typography>
              <Typography variant="caption" color="text.secondary" display="block">
                Sobre hasta {MAX_RESUMENES_PARALELOS} lotes con dato
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
                {kpis.mortalidadPromedio != null
                  ? `${formatearNumero(kpis.mortalidadPromedio, 2)} %`
                  : '—'}
              </Typography>
            </Paper>
          </Box>

          <Typography variant="h6" sx={{ mb: 1 }}>
            Lotes activos (vista rápida)
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Especie</TableCell>
                  <TableCell align="right">Aves</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lotesActivos.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5}>
                      <Typography color="text.secondary" variant="body2">
                        No hay lotes activos.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  lotesActivos.map((l) => (
                    <TableRow key={l.id} hover>
                      <TableCell>{l.nombre}</TableCell>
                      <TableCell>{etiquetaEspecieCarne(l.especie)}</TableCell>
                      <TableCell align="right">{l.cantidadAnimales ?? '—'}</TableCell>
                      <TableCell>{l.estado ?? '—'}</TableCell>
                      <TableCell align="right">
                        <Button
                          size="small"
                          variant="text"
                          onClick={() => navigate(`/avicola-carne/lotes/${l.id}`)}
                        >
                          Abrir
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
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

export default AvicolaCarneDashboard;
