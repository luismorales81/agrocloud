import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
import type { AvicolaPonedorasGalpon } from '../types';
import { listarGalpones, mensajeErrorPonedoras } from '../services/avicolaPonedorasService';
import { etiquetaEstadoGalpon, formatearFechaCorta } from '../util/formateo';
import FiltroDelPeriodoActivo from '../../../components/FiltroDelPeriodoActivo';
import { useCampana } from '../../../contexts/CampanaContext';

const AvicolaPonedorasListado: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [galpones, setGalpones] = useState<AvicolaPonedorasGalpon[]>([]);
  const [filtroEstado, setFiltroEstado] = useState<'ACTIVO' | 'CERRADO' | ''>('ACTIVO');
  const [filtroDelPeriodo, setFiltroDelPeriodo] = useState(false);

  useEffect(() => {
    if (filtroEstado !== 'ACTIVO') {
      setFiltroDelPeriodo(true);
    }
  }, [filtroEstado]);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const lista = await listarGalpones({
        estado: filtroEstado || undefined,
        delPeriodoActivo: filtroDelPeriodo,
      });
      setGalpones(lista);
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    } finally {
      setCargando(false);
    }
  }, [filtroEstado, filtroDelPeriodo]);

  useEffect(() => {
    void cargar();
  }, [cargar, campanaActiva?.id]);

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="List" size={28} />
        <Typography variant="h4" component="h1">
          Galpones
        </Typography>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Listado de galpones de la empresa (entidad central del módulo ponedoras, distinta del lote en carne).
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="outlined" size="small" onClick={() => navigate('/avicola-ponedoras/panel')}>
          Volver al resumen
        </Button>
        <Button variant="text" size="small" onClick={() => void cargar()} disabled={cargando}>
          Actualizar
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando galpones…</Typography>
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
                <TableCell>Raza</TableCell>
                <TableCell>Fecha ingreso</TableCell>
                <TableCell align="right">Aves</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acción</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {galpones.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6}>
                    <Typography color="text.secondary" variant="body2">
                      No hay galpones registrados.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                galpones.map((g) => (
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
      )}
    </Box>
  );
};

export default AvicolaPonedorasListado;
