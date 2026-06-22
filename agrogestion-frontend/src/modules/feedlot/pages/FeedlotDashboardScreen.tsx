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
import { useCampana } from '../../../contexts/CampanaContext';
import type { FeedlotLote } from '../types';
import { listarLotes, mensajeError, obtenerPanelResumen } from '../services/feedlotApi';

function formatearNumero(valor: number, fracciones = 2): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: fracciones,
  }).format(valor);
}

const FeedlotDashboardScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [snackbarExito, setSnackbarExito] = useState(false);
  const [cabezasEnFeed, setCabezasEnFeed] = useState(0);
  const [lotesActivos, setLotesActivos] = useState(0);
  const [gmdPromedio, setGmdPromedio] = useState<number | null>(null);
  const [mortalidadPromedio, setMortalidadPromedio] = useState<number | null>(null);
  const [consumoTotalKg, setConsumoTotalKg] = useState<number | null>(null);
  const [lotesVista, setLotesVista] = useState<FeedlotLote[]>([]);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const [panel, activos] = await Promise.all([
        obtenerPanelResumen(),
        listarLotes({ estado: 'ACTIVO' }),
      ]);
      setCabezasEnFeed(Number(panel.cabezasEnFeed ?? 0));
      setLotesActivos(Number(panel.lotesActivos ?? activos.length));
      setGmdPromedio(panel.gmdPromedio != null ? Number(panel.gmdPromedio) : null);
      setMortalidadPromedio(
        panel.mortalidadPctPromedio != null ? Number(panel.mortalidadPctPromedio) : null
      );
      setConsumoTotalKg(panel.consumoTotalKg != null ? Number(panel.consumoTotalKg) : null);
      setLotesVista(activos.slice(0, 10));
      if (mostrarExito) {
        setSnackbarExito(true);
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar(false);
  }, [cargar, campanaActiva?.id]);

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="BarChart" size={32} />
        <Typography variant="h4" component="h1">
          Feedlot — Panel
        </Typography>
      </Stack>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
        Resumen del período de gestión activo: plantel en feed, lotes activos e indicadores zootécnicos.
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="outlined" size="small" onClick={() => navigate('/feedlot/lotes')}>
          Ver lotes
        </Button>
        <Button variant="contained" size="small" onClick={() => navigate('/feedlot/lotes/nuevo')}>
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
            <Paper elevation={1} sx={{ p: 2, borderLeft: (t) => `4px solid ${t.palette.primary.main}` }}>
              <Typography variant="caption" color="text.secondary">
                Lotes activos
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {lotesActivos}
              </Typography>
            </Paper>
            <Paper elevation={1} sx={{ p: 2, borderLeft: (t) => `4px solid ${t.palette.success.main}` }}>
              <Typography variant="caption" color="text.secondary">
                Cabezas en feed
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {formatearNumero(cabezasEnFeed, 0)}
              </Typography>
            </Paper>
            <Paper elevation={1} sx={{ p: 2, borderLeft: (t) => `4px solid ${t.palette.warning.main}` }}>
              <Typography variant="caption" color="text.secondary">
                GMD promedio (kg/día)
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {gmdPromedio != null ? formatearNumero(gmdPromedio, 3) : '—'}
              </Typography>
            </Paper>
            <Paper elevation={1} sx={{ p: 2, borderLeft: (t) => `4px solid ${t.palette.error.light}` }}>
              <Typography variant="caption" color="text.secondary">
                Mortalidad % promedio
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {mortalidadPromedio != null ? `${formatearNumero(mortalidadPromedio, 2)} %` : '—'}
              </Typography>
            </Paper>
            <Paper elevation={1} sx={{ p: 2, borderLeft: (t) => `4px solid ${t.palette.info.main}` }}>
              <Typography variant="caption" color="text.secondary">
                Consumo total alimento (kg)
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {consumoTotalKg != null ? formatearNumero(consumoTotalKg, 0) : '—'}
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
                  <TableCell>Corral</TableCell>
                  <TableCell>Categoría</TableCell>
                  <TableCell align="right">Cabezas</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lotesVista.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5}>
                      <Typography color="text.secondary" variant="body2">
                        No hay lotes activos en el período.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  lotesVista.map((l) => (
                    <TableRow key={l.id} hover>
                      <TableCell>{l.nombre}</TableCell>
                      <TableCell>{l.corralNombre ?? '—'}</TableCell>
                      <TableCell>{l.categoriaNombre ?? '—'}</TableCell>
                      <TableCell align="right">{l.cabezasActuales ?? '—'}</TableCell>
                      <TableCell align="right">
                        <Button
                          size="small"
                          variant="text"
                          onClick={() => navigate(`/feedlot/lotes/${l.id}`)}
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

export default FeedlotDashboardScreen;
