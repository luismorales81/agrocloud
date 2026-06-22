import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  MenuItem,
  Paper,
  Snackbar,
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
import FiltroDelPeriodoActivo from '../../../components/FiltroDelPeriodoActivo';
import { useCampana } from '../../../contexts/CampanaContext';
import type { FeedlotLote } from '../types';
import { listarLotes, mensajeError } from '../services/feedlotApi';

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

const LotesFeedlotScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lotes, setLotes] = useState<FeedlotLote[]>([]);
  const [snackbarExito, setSnackbarExito] = useState(false);
  const [filtroEstado, setFiltroEstado] = useState<'ACTIVO' | 'CERRADO' | ''>('ACTIVO');
  const [filtroDelPeriodo, setFiltroDelPeriodo] = useState(false);

  useEffect(() => {
    if (filtroEstado !== 'ACTIVO') {
      setFiltroDelPeriodo(true);
    }
  }, [filtroEstado]);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const lista = await listarLotes({
        estado: filtroEstado || undefined,
        delPeriodoActivo: filtroDelPeriodo,
      });
      setLotes(lista);
      if (mostrarExito) {
        setSnackbarExito(true);
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [filtroEstado, filtroDelPeriodo]);

  useEffect(() => {
    void cargar(false);
  }, [cargar, campanaActiva?.id]);

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="List" size={28} />
          <Typography variant="h4" component="h1">
            Lotes de engorde
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1} flexWrap="wrap">
          <Button variant="outlined" onClick={() => navigate('/feedlot/panel')}>
            Panel
          </Button>
          <Button variant="text" onClick={() => void cargar(true)} disabled={cargando}>
            Actualizar
          </Button>
          <Button variant="contained" onClick={() => navigate('/feedlot/lotes/nuevo')}>
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

      <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap" sx={{ mb: 2 }}>
        <TextField
          select
          size="small"
          label="Estado"
          value={filtroEstado}
          onChange={(e) => setFiltroEstado(e.target.value as 'ACTIVO' | 'CERRADO' | '')}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="ACTIVO">Activos</MenuItem>
          <MenuItem value="CERRADO">Cerrados</MenuItem>
          <MenuItem value="">Todos</MenuItem>
        </TextField>
        <FiltroDelPeriodoActivo
          activo={filtroDelPeriodo}
          onChange={setFiltroDelPeriodo}
          visible={filtroEstado !== 'ACTIVO'}
        />
      </Stack>

      {!cargando && !error && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="medium">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell>Corral</TableCell>
                <TableCell>Categoría</TableCell>
                <TableCell>Fecha ingreso</TableCell>
                <TableCell align="right">Cabezas</TableCell>
                <TableCell>Tenencia</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lotes.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8}>
                    <Typography color="text.secondary">No hay lotes registrados.</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                lotes.map((l) => (
                  <TableRow key={l.id} hover>
                    <TableCell>{l.nombre}</TableCell>
                    <TableCell>{l.corralNombre ?? '—'}</TableCell>
                    <TableCell>{l.categoriaNombre ?? '—'}</TableCell>
                    <TableCell>{formatearFecha(l.fechaIngreso)}</TableCell>
                    <TableCell align="right">{l.cabezasActuales ?? '—'}</TableCell>
                    <TableCell>{l.tipoTenencia ?? '—'}</TableCell>
                    <TableCell>{chipEstado(l.estado)}</TableCell>
                    <TableCell align="right">
                      <Button size="small" variant="text" onClick={() => navigate(`/feedlot/lotes/${l.id}`)}>
                        Ver
                      </Button>
                      {l.estado === 'ACTIVO' && (
                        <Button
                          size="small"
                          variant="text"
                          onClick={() => navigate(`/feedlot/lotes/${l.id}/editar`)}
                        >
                          Editar
                        </Button>
                      )}
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

export default LotesFeedlotScreen;
