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
import type { LecheriaAccionDia } from '../types';
import { mensajeError, obtenerPanelResumen } from '../services/lecheriaApi';

function formatearNumero(valor: number, fracciones = 1): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: fracciones,
  }).format(valor);
}

const PanelLecheriaScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [snackbarExito, setSnackbarExito] = useState(false);
  const [litrosTotales, setLitrosTotales] = useState<number | null>(null);
  const [litrosPorAnimal, setLitrosPorAnimal] = useState<number | null>(null);
  const [promedioRcs, setPromedioRcs] = useState<number | null>(null);
  const [lactando, setLactando] = useState(0);
  const [secas, setSecas] = useState(0);
  const [prenadas, setPrenadas] = useState(0);
  const [acciones, setAcciones] = useState<LecheriaAccionDia[]>([]);

  const cargar = useCallback(async (mostrarExito: boolean) => {
    setCargando(true);
    setError(null);
    try {
      const panel = await obtenerPanelResumen();
      setLitrosTotales(panel.litrosTotalesPeriodo != null ? Number(panel.litrosTotalesPeriodo) : null);
      setLitrosPorAnimal(
        panel.litrosPorAnimalLactante != null ? Number(panel.litrosPorAnimalLactante) : null
      );
      setPromedioRcs(panel.promedioRcs != null ? Number(panel.promedioRcs) : null);
      setLactando(Number(panel.animalesLactando ?? 0));
      setSecas(Number(panel.animalesSecas ?? 0));
      setPrenadas(Number(panel.animalesPrenadas ?? 0));
      setAcciones(panel.acciones ?? []);
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
        <Icon name="Milk" size={32} />
        <Typography variant="h4" component="h1">
          Lechería — Panel
        </Typography>
      </Stack>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
        Resumen del período activo: producción, estado del rodeo y acciones pendientes del día.
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="outlined" size="small" onClick={() => navigate('/lecheria/animales')}>
          Ver animales
        </Button>
        <Button variant="contained" size="small" onClick={() => navigate('/lecheria/ordene')}>
          Registrar ordeñe
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
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: 2,
              mb: 3,
            }}
          >
            {[
              { etiqueta: 'Litros período', valor: litrosTotales != null ? `${formatearNumero(litrosTotales)} L` : '—' },
              { etiqueta: 'L/vaca lactante', valor: litrosPorAnimal != null ? `${formatearNumero(litrosPorAnimal)} L` : '—' },
              { etiqueta: 'RCS promedio', valor: promedioRcs != null ? formatearNumero(promedioRcs, 0) : '—' },
              { etiqueta: 'Lactando', valor: String(lactando) },
              { etiqueta: 'Secas', valor: String(secas) },
              { etiqueta: 'Preñadas', valor: String(prenadas) },
            ].map((kpi) => (
              <Paper key={kpi.etiqueta} variant="outlined" sx={{ p: 2 }}>
                <Typography variant="caption" color="text.secondary">
                  {kpi.etiqueta}
                </Typography>
                <Typography variant="h5" fontWeight={600}>
                  {kpi.valor}
                </Typography>
              </Paper>
            ))}
          </Box>

          <Typography variant="h6" gutterBottom>
            Acciones del día
          </Typography>
          {acciones.length === 0 ? (
            <Typography color="text.secondary" sx={{ mb: 2 }}>
              No hay acciones pendientes para hoy.
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ mb: 2 }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Animal</TableCell>
                    <TableCell>Mensaje</TableCell>
                    <TableCell align="right">Acción</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {acciones.map((a, idx) => (
                    <TableRow key={`${a.animalId}-${idx}`}>
                      <TableCell>{a.tipo ?? '—'}</TableCell>
                      <TableCell>{a.identificacion ?? '—'}</TableCell>
                      <TableCell>{a.mensaje ?? '—'}</TableCell>
                      <TableCell align="right">
                        {a.animalId != null && (
                          <Button
                            size="small"
                            onClick={() => navigate(`/lecheria/animales/${a.animalId}`)}
                          >
                            Ver
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      )}

      <Snackbar
        open={snackbarExito}
        autoHideDuration={3000}
        onClose={() => setSnackbarExito(false)}
        message="Datos actualizados"
      />
    </Box>
  );
};

export default PanelLecheriaScreen;
