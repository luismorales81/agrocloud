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
import {
  listarLotesCrianza,
  type AvicolaLoteRespuesta,
  type EspecieCrianza,
  mensajeError,
} from '../services/avicolaCrianzaApi';

const ETIQUETA_ESPECIE: Record<EspecieCrianza, string> = {
  POLLO_PARRILLERO: 'Pollo parrillero',
  GALLINA_PONEDORA: 'Gallina ponedora',
  PAVO: 'Pavo',
  OTRO: 'Otro',
};

/**
 * Resumen operativo del módulo (SPEC crianza): accesos claros a lotes, datos maestros y detalle por lote.
 */
const DashboardCrianzaScreen: React.FC = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lotes, setLotes] = useState<AvicolaLoteRespuesta[]>([]);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const lista = await listarLotesCrianza();
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
        const n = l.cantidadAnimales ?? l.cantidadInicial;
        return acc + (typeof n === 'number' && !Number.isNaN(n) ? n : 0);
      }, 0),
    [lotesActivos]
  );

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Avícola — Crianza
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Según la SPEC del módulo: <strong>lotes</strong> de aves con pesadas, mortalidad, ventas/faena, consumo de
        insumos (inventario CORE) y sanidad. Antes de dar de alta un lote, cargá establecimientos y razas (menú lateral:
        entradas separadas, como en cultivos).
      </Typography>

      <Stack direction="row" spacing={1} sx={{ mb: 2 }} flexWrap="wrap" useFlexGap>
        <Button size="small" variant="contained" color="primary" onClick={() => navigate('/avicola-crianza/dashboard')}>
          Calendario
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-crianza/establecimientos')}>
          Establecimientos
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-crianza/razas')}>
          Razas
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-crianza/insumos')}>
          Insumos
        </Button>
        <Button size="small" variant="outlined" onClick={() => navigate('/avicola-crianza/lotes')}>
          Todos los lotes
        </Button>
        <Button size="small" variant="contained" onClick={() => navigate('/avicola-crianza/lotes')}>
          Nuevo lote (desde listado)
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
                Lotes activos
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
                Total lotes (incl. cerrados)
              </Typography>
              <Typography variant="h4">{lotes.length}</Typography>
            </Paper>
          </Stack>

          <Typography variant="h6" gutterBottom>
            Lotes activos — acceso rápido
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            Abrí un lote para registrar pesadas, muertes, consumos, sanidad y ventas.
          </Typography>

          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Establecimiento</TableCell>
                  <TableCell>Raza</TableCell>
                  <TableCell>Especie</TableCell>
                  <TableCell align="right">Aves</TableCell>
                  <TableCell>Ingreso</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lotesActivos.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <Typography color="text.secondary" variant="body2">
                        No hay lotes activos. Cargá establecimientos y razas, luego creá un lote desde «Todos los
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
                      <TableCell>
                        {l.especie ? ETIQUETA_ESPECIE[l.especie as EspecieCrianza] ?? l.especie : '—'}
                      </TableCell>
                      <TableCell align="right">{l.cantidadAnimales ?? l.cantidadInicial ?? '—'}</TableCell>
                      <TableCell>{l.fechaIngreso?.slice(0, 10) ?? '—'}</TableCell>
                      <TableCell align="right">
                        <Button size="small" variant="text" onClick={() => navigate(`/avicola-crianza/lotes/${l.id}`)}>
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

export default DashboardCrianzaScreen;
