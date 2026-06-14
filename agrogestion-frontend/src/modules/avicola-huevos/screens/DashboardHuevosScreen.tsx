import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { listarLotesHuevos, type AvicolaHuevoLoteRespuesta, mensajeError } from '../services/avicolaHuevosApi';

/**
 * Resumen operativo del módulo (SPEC huevos): postura, producción diaria, consumos y sanidad por lote.
 */
const DashboardHuevosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lotes, setLotes] = useState<AvicolaHuevoLoteRespuesta[]>([]);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const lista = await listarLotesHuevos();
      setLotes(lista);
      setError(null);
    } catch (e: unknown) {
      setError(mensajeError(e));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const lotesActivos = useMemo(
    () => lotes.filter((l) => (l.estado ?? 'ACTIVO') === 'ACTIVO'),
    [lotes]
  );

  const totalAvesActivas = useMemo(
    () =>
      lotesActivos.reduce((acc, l) => {
        const n = l.cantidadAvesActual ?? l.cantidadAvesInicial;
        return acc + (typeof n === 'number' && !Number.isNaN(n) ? n : 0);
      }, 0),
    [lotesActivos]
  );

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Avícola — Huevos
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Lotes de <strong>postura</strong>, <strong>producción diaria</strong>, <strong>consumos</strong> con inventario de
        insumos y <strong>sanidad</strong>. El calendario y los insumos están en este mismo módulo.
      </Typography>

      <Paper variant="outlined" sx={{ p: 2, mb: 2, bgcolor: 'action.hover' }}>
        <Typography variant="subtitle2" gutterBottom>
          Guía rápida
        </Typography>
        <Typography component="ul" variant="body2" sx={{ pl: 2, m: 0 }}>
          <li>«Establecimientos» y «Razas» tienen cada uno su entrada en el menú; el mapa se abre desde el listado de establecimientos.</li>
          <li>«Insumos» para altas y stock; luego «Lotes de postura» y el detalle del lote para cargar el día.</li>
          <li>En el detalle: una fila de producción por fecha; consumos y sanidad desde los botones de cada pestaña.</li>
        </Typography>
      </Paper>

      <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
        <Button size="small" variant="outlined" onClick={() => void cargar()} disabled={cargando}>
          Actualizar panel
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-huevos/establecimientos')}>
          Establecimientos
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-huevos/razas')}>
          Razas
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-huevos/insumos')}>
          Insumos
        </Button>
        <Button size="small" variant="contained" onClick={() => navigate('/avicola-huevos/lotes')}>
          Lotes de postura
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando datos…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="warning" sx={{ my: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 3 }}>
            <Paper sx={{ p: 2, flex: 1 }}>
              <Typography variant="subtitle2" color="text.secondary">
                Lotes de postura activos
              </Typography>
              <Typography variant="h4">{lotesActivos.length}</Typography>
            </Paper>
            <Paper sx={{ p: 2, flex: 1 }}>
              <Typography variant="subtitle2" color="text.secondary">
                Aves en lotes activos (aprox.)
              </Typography>
              <Typography variant="h4">{totalAvesActivas}</Typography>
            </Paper>
            <Paper sx={{ p: 2, flex: 1 }}>
              <Typography variant="subtitle2" color="text.secondary">
                Total lotes
              </Typography>
              <Typography variant="h4">{lotes.length}</Typography>
            </Paper>
          </Stack>

          <Typography variant="h6" gutterBottom>
            Lotes activos — acceso rápido
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            En el detalle del lote cargás producción diaria de huevos, consumos y eventos sanitarios.
          </Typography>

          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Establecimiento</TableCell>
                  <TableCell>Raza</TableCell>
                  <TableCell align="right">Aves actuales</TableCell>
                  <TableCell>Inicio</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lotesActivos.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <Typography color="text.secondary" variant="body2">
                        No hay lotes activos. Configurá establecimientos y razas, luego creá un lote desde «Todos los
                        lotes».
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  lotesActivos.map((l) => (
                    <TableRow key={l.id} hover>
                      <TableCell>{l.nombre}</TableCell>
                      <TableCell>{l.establecimientoNombre ?? '—'}</TableCell>
                      <TableCell>{l.razaNombre ?? '—'}</TableCell>
                      <TableCell align="right">{l.cantidadAvesActual ?? l.cantidadAvesInicial ?? '—'}</TableCell>
                      <TableCell>{l.fechaInicio?.slice(0, 10) ?? '—'}</TableCell>
                      <TableCell align="right">
                        <Button size="small" variant="text" onClick={() => navigate(`/avicola-huevos/lotes/${l.id}`)}>
                          Abrir detalle
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
    </Box>
  );
};

export default DashboardHuevosScreen;
