import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
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
import { listarLotesCarne, mensajeErrorCarne } from '../services/avicolaCarneService';
import { etiquetaEspecieCarne } from '../util/etiquetasEspecieCarne';

function formatearFecha(fechaIso: string | undefined): string {
  if (!fechaIso) return '—';
  try {
    const d = new Date(fechaIso);
    if (Number.isNaN(d.getTime())) return fechaIso.slice(0, 10);
    return d.toLocaleDateString('es-AR');
  } catch {
    return fechaIso.slice(0, 10);
  }
}

function chipEstado(estado: string | undefined): React.ReactElement {
  const valor = estado ?? '—';
  const esActivo = valor === 'ACTIVO';
  return (
    <Chip
      size="small"
      label={valor}
      color={esActivo ? 'success' : 'default'}
      variant={esActivo ? 'filled' : 'outlined'}
    />
  );
}

const AvicolaCarneListadoScreen: React.FC = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lotes, setLotes] = useState<AvicolaCarneLote[]>([]);
  const [snackbarExito, setSnackbarExito] = useState(false);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const lista = await listarLotesCarne();
      setLotes(lista);
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
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="List" size={28} />
          <Typography variant="h4" component="h1">
            Lotes — Avícola carne
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1} flexWrap="wrap">
          <Button variant="outlined" onClick={() => navigate('/avicola-carne/panel')}>
            Resumen
          </Button>
          <Button variant="text" onClick={() => void cargar(true)} disabled={cargando}>
            Actualizar
          </Button>
          <Button variant="contained" onClick={() => navigate('/avicola-carne/lotes/nuevo')}>
            Nuevo lote
          </Button>
        </Stack>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando lotes…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ my: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="medium">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell>Especie</TableCell>
                <TableCell>Fecha ingreso</TableCell>
                <TableCell align="right">Cantidad</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lotes.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6}>
                    <Typography color="text.secondary">No hay lotes registrados.</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                lotes.map((l) => (
                  <TableRow key={l.id} hover>
                    <TableCell>{l.nombre}</TableCell>
                    <TableCell>{etiquetaEspecieCarne(l.especie)}</TableCell>
                    <TableCell>{formatearFecha(l.fechaIngreso)}</TableCell>
                    <TableCell align="right">{l.cantidadAnimales ?? '—'}</TableCell>
                    <TableCell>{chipEstado(l.estado)}</TableCell>
                    <TableCell align="right">
                      <Button size="small" variant="text" onClick={() => navigate(`/avicola-carne/lotes/${l.id}`)}>
                        Ver
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Snackbar
        open={snackbarExito}
        autoHideDuration={3000}
        onClose={() => setSnackbarExito(false)}
        message="Listado actualizado"
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  );
};

export default AvicolaCarneListadoScreen;
