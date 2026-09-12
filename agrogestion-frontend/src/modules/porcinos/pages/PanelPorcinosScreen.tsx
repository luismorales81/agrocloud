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
import type { PorcinosLote } from '../typesApiV2';
import { listarLotes, mensajeError, obtenerPanelResumen } from '../services/porcinosApi';

function formatearNumero(valor: number, fracciones = 0): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: fracciones,
  }).format(valor);
}

const PanelPorcinosScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [snackbarExito, setSnackbarExito] = useState(false);
  const [cabezasEngorde, setCabezasEngorde] = useState(0);
  const [lotesActivos, setLotesActivos] = useState(0);
  const [madresActivas, setMadresActivas] = useState(0);
  const [gestacionesActivas, setGestacionesActivas] = useState(0);
  const [mortalidadPromedio, setMortalidadPromedio] = useState<number | null>(null);
  const [consumoTotalKg, setConsumoTotalKg] = useState<number | null>(null);
  const [lotesVista, setLotesVista] = useState<PorcinosLote[]>([]);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const [panel, activos] = await Promise.all([
        obtenerPanelResumen(),
        listarLotes({ estado: 'ACTIVO' }),
      ]);
      setCabezasEngorde(Number(panel.cabezasEnLotes ?? 0));
      setLotesActivos(Number(panel.lotesActivos ?? activos.length));
      setMadresActivas(Number(panel.madresActivas ?? 0));
      setGestacionesActivas(Number(panel.gestacionesEnCurso ?? 0));
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

  const tarjetas = [
    { etiqueta: 'Cabezas en engorde', valor: formatearNumero(cabezasEngorde), icono: 'PiggyBank' as const },
    { etiqueta: 'Lotes activos', valor: formatearNumero(lotesActivos), icono: 'List' as const },
    { etiqueta: 'Madres activas', valor: formatearNumero(madresActivas), icono: 'Heart' as const },
    { etiqueta: 'Gestaciones activas', valor: formatearNumero(gestacionesActivas), icono: 'Baby' as const },
    {
      etiqueta: 'Mortalidad prom.',
      valor: mortalidadPromedio != null ? `${mortalidadPromedio.toFixed(1)} %` : '—',
      icono: 'AlertTriangle' as const,
    },
    {
      etiqueta: 'Consumo total (kg)',
      valor: consumoTotalKg != null ? formatearNumero(consumoTotalKg, 0) : '—',
      icono: 'Utensils' as const,
    },
  ];

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="BarChart" size={32} />
          <Typography variant="h4" component="h1">
            Porcinos — Panel
          </Typography>
        </Stack>
        <Button variant="outlined" onClick={() => void cargar(true)} disabled={cargando}>
          Actualizar
        </Button>
      </Stack>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando indicadores…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <Stack direction="row" flexWrap="wrap" gap={2} sx={{ mb: 3 }}>
          {tarjetas.map((t) => (
            <Paper key={t.etiqueta} variant="outlined" sx={{ p: 2, minWidth: 160, flex: '1 1 160px' }}>
              <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 0.5 }}>
                <Icon name={t.icono} size={20} />
                <Typography variant="caption" color="text.secondary">
                  {t.etiqueta}
                </Typography>
              </Stack>
              <Typography variant="h5">{t.valor}</Typography>
            </Paper>
          ))}
        </Stack>
      )}

      {!cargando && lotesVista.length > 0 && (
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
            <Typography variant="h6">Lotes activos recientes</Typography>
            <Button size="small" onClick={() => navigate('/porcinos/lotes')}>
              Ver todos
            </Button>
          </Stack>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Etapa</TableCell>
                  <TableCell align="right">Cabezas</TableCell>
                  <TableCell>Establecimiento</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lotesVista.map((l) => (
                  <TableRow
                    key={l.id}
                    hover
                    sx={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/porcinos/lotes/${l.id}`)}
                  >
                    <TableCell>{l.nombre}</TableCell>
                    <TableCell>{l.etapa ?? '—'}</TableCell>
                    <TableCell align="right">{l.cabezasActuales ?? '—'}</TableCell>
                    <TableCell>{l.establecimientoNombre ?? '—'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      <Snackbar
        open={snackbarExito}
        autoHideDuration={3000}
        onClose={() => setSnackbarExito(false)}
        message="Panel actualizado"
      />
    </Box>
  );
};

export default PanelPorcinosScreen;
