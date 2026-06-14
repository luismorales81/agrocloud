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
import type { AvicolaPonedorasGalpon } from '../types';
import {
  listarGalpones,
  mensajeErrorPonedoras,
  obtenerResumenGalpon,
} from '../services/avicolaPonedorasService';
import { etiquetaEstadoGalpon, formatearFechaCorta, formatearNumero } from '../util/formateo';

const MAX_GALPONES_TABLA = 12;
const MAX_RESUMENES_PARALELOS = 40;

interface KpisPanel {
  galponesActivos: number;
  posturaPromedioPct: number | null;
  huevosPorAveDiaPromedio: number | null;
  mortalidadPromedioPct: number | null;
}

const AvicolaPonedorasDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [kpis, setKpis] = useState<KpisPanel>({
    galponesActivos: 0,
    posturaPromedioPct: null,
    huevosPorAveDiaPromedio: null,
    mortalidadPromedioPct: null,
  });
  const [galponesActivos, setGalponesActivos] = useState<AvicolaPonedorasGalpon[]>([]);
  const [snackbarExito, setSnackbarExito] = useState(false);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const activos = await listarGalpones('ACTIVO');

      const muestra = activos.slice(0, MAX_RESUMENES_PARALELOS);
      const resumenes = await Promise.all(
        muestra.map(async (g) => {
          try {
            return await obtenerResumenGalpon(g.id);
          } catch {
            return null;
          }
        })
      );

      const posturas = resumenes
        .filter((r): r is NonNullable<(typeof resumenes)[number]> => r !== null && r.porcentajePostura != null)
        .map((r) => r.porcentajePostura as number);
      const huevosAve = resumenes
        .filter((r): r is NonNullable<(typeof resumenes)[number]> => r !== null && r.huevosPorAvePorDia != null)
        .map((r) => r.huevosPorAvePorDia as number);
      const mortalidades = resumenes
        .filter((r): r is NonNullable<(typeof resumenes)[number]> => r !== null && r.mortalidadPct != null)
        .map((r) => r.mortalidadPct as number);

      const posturaPromedioPct =
        posturas.length > 0 ? posturas.reduce((a, b) => a + b, 0) / posturas.length : null;
      const huevosPorAveDiaPromedio =
        huevosAve.length > 0 ? huevosAve.reduce((a, b) => a + b, 0) / huevosAve.length : null;
      const mortalidadPromedioPct =
        mortalidades.length > 0 ? mortalidades.reduce((a, b) => a + b, 0) / mortalidades.length : null;

      setKpis({
        galponesActivos: activos.length,
        posturaPromedioPct,
        huevosPorAveDiaPromedio,
        mortalidadPromedioPct,
      });
      setGalponesActivos(activos.slice(0, MAX_GALPONES_TABLA));
      if (mostrarExito) {
        setSnackbarExito(true);
      }
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
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
          Avícola ponedoras — Panel
        </Typography>
      </Stack>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
        Indicadores sobre galpones activos; promedios calculados con el resumen por galpón (hasta{' '}
        {MAX_RESUMENES_PARALELOS} registros).
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="contained" size="small" color="primary" onClick={() => navigate('/avicola-ponedoras/dashboard')}>
          Calendario
        </Button>
        <Button variant="outlined" size="small" onClick={() => navigate('/avicola-ponedoras/galpones')}>
          Ver todos los galpones
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
                Galpones activos
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.galponesActivos}
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
                % postura promedio
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.posturaPromedioPct != null
                  ? `${formatearNumero(kpis.posturaPromedioPct, 2)} %`
                  : '—'}
              </Typography>
            </Paper>
            <Paper
              elevation={1}
              sx={{
                p: 2,
                borderLeft: (t) => `4px solid ${t.palette.info.main}`,
              }}
            >
              <Typography variant="caption" color="text.secondary">
                Huevos / ave / día (promedio)
              </Typography>
              <Typography variant="h4" sx={{ fontWeight: 700 }}>
                {kpis.huevosPorAveDiaPromedio != null
                  ? formatearNumero(kpis.huevosPorAveDiaPromedio, 3)
                  : '—'}
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
                {kpis.mortalidadPromedioPct != null
                  ? `${formatearNumero(kpis.mortalidadPromedioPct, 2)} %`
                  : '—'}
              </Typography>
            </Paper>
          </Box>

          <Typography variant="h6" sx={{ mb: 1 }}>
            Galpones activos (vista rápida)
          </Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
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
                {galponesActivos.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <Typography color="text.secondary" variant="body2">
                        No hay galpones activos.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  galponesActivos.map((g) => (
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

export default AvicolaPonedorasDashboard;
