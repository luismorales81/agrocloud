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
import type { PorcinosLote } from '../typesApiV2';
import { listarLotes, mensajeError } from '../services/porcinosApi';

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

const LotesPorcinosScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lotes, setLotes] = useState<PorcinosLote[]>([]);
  const [snackbarExito, setSnackbarExito] = useState(false);
  const [filtroEstado, setFiltroEstado] = useState<'ACTIVO' | 'CERRADO' | ''>('ACTIVO');
  const [filtroEtapa, setFiltroEtapa] = useState<'RECRIA' | 'ENGORDE' | ''>('');
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
        etapa: filtroEtapa || undefined,
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
  }, [filtroEstado, filtroEtapa, filtroDelPeriodo]);

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
          <Button variant="contained" onClick={() => navigate('/porcinos/lotes/nuevo')}>
            Nuevo lote
          </Button>
          <Button variant="outlined" onClick={() => void cargar(true)} disabled={cargando}>
            Actualizar
          </Button>
        </Stack>
      </Stack>

      <Stack direction="row" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <TextField
          select
          size="small"
          label="Estado"
          value={filtroEstado}
          onChange={(e) => setFiltroEstado(e.target.value as 'ACTIVO' | 'CERRADO' | '')}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="">Todos</MenuItem>
          <MenuItem value="ACTIVO">Activo</MenuItem>
          <MenuItem value="CERRADO">Cerrado</MenuItem>
        </TextField>
        <TextField
          select
          size="small"
          label="Etapa"
          value={filtroEtapa}
          onChange={(e) => setFiltroEtapa(e.target.value as 'RECRIA' | 'ENGORDE' | '')}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="">Todas</MenuItem>
          <MenuItem value="RECRIA">Recría</MenuItem>
          <MenuItem value="ENGORDE">Engorde</MenuItem>
        </TextField>
        <FiltroDelPeriodoActivo
          activo={filtroDelPeriodo}
          onChange={setFiltroDelPeriodo}
          visible={filtroEstado === 'ACTIVO'}
        />
      </Stack>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando lotes…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell>Etapa</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Ingreso</TableCell>
                <TableCell align="right">Cabezas</TableCell>
                <TableCell>Galpón</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lotes.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    <Typography color="text.secondary" sx={{ py: 2 }}>
                      No hay lotes con los filtros seleccionados.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                lotes.map((l) => (
                  <TableRow
                    key={l.id}
                    hover
                    sx={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/porcinos/lotes/${l.id}`)}
                  >
                    <TableCell>{l.nombre}</TableCell>
                    <TableCell>{l.etapa ?? '—'}</TableCell>
                    <TableCell>{chipEstado(l.estado)}</TableCell>
                    <TableCell>{formatearFecha(l.fechaIngreso)}</TableCell>
                    <TableCell align="right">{l.cabezasActuales ?? '—'}</TableCell>
                    <TableCell>{l.galponNombre ?? '—'}</TableCell>
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
      />
    </Box>
  );
};

export default LotesPorcinosScreen;
