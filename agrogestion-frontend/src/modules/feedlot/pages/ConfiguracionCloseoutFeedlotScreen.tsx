import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  FormControlLabel,
  Paper,
  Radio,
  RadioGroup,
  Stack,
  Typography,
} from '@mui/material';
import { Icon } from '../../../components/icons';
import type { FeedlotMetodoCloseout } from '../types';
import {
  actualizarConfigCloseout,
  mensajeError,
  obtenerConfigCloseout,
} from '../services/feedlotApi';

const METODOS: { valor: FeedlotMetodoCloseout; etiqueta: string; ayuda: string }[] = [
  {
    valor: 'DEADS_IN',
    etiqueta: 'Deads-in',
    ayuda: 'Incluye muertes en el denominador de costo por kg ganado (plantel neto).',
  },
  {
    valor: 'DEADS_OUT',
    etiqueta: 'Deads-out',
    ayuda: 'Excluye muertes del denominador (cabezas iniciales).',
  },
];

const ConfiguracionCloseoutFeedlotScreen: React.FC = () => {
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState(false);
  const [metodo, setMetodo] = useState<FeedlotMetodoCloseout>('DEADS_IN');

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      setError(null);
      const cfg = await obtenerConfigCloseout();
      setMetodo((cfg.metodoCloseout as FeedlotMetodoCloseout) ?? 'DEADS_IN');
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const guardar = async () => {
    try {
      setGuardando(true);
      setError(null);
      setExito(false);
      await actualizarConfigCloseout({ metodoCloseout: metodo });
      setExito(true);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 720 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="Settings" size={28} />
        <Typography variant="h5" component="h1">
          Configuración closeout
        </Typography>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Define cómo se calculan los costos por kg ganado en el reporte de cierre de lote.
      </Typography>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={1} sx={{ my: 2 }}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando configuración…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {exito && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setExito(false)}>
          Configuración guardada correctamente.
        </Alert>
      )}

      {!cargando && (
        <Paper sx={{ p: 3 }}>
          <FormControl component="fieldset" fullWidth>
            <Typography variant="subtitle1" gutterBottom>
              Método de cálculo
            </Typography>
            <RadioGroup
              value={metodo}
              onChange={(ev) => setMetodo(ev.target.value as FeedlotMetodoCloseout)}
            >
              {METODOS.map((m) => (
                <FormControlLabel
                  key={m.valor}
                  value={m.valor}
                  control={<Radio />}
                  label={
                    <Box>
                      <Typography variant="body1">{m.etiqueta}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {m.ayuda}
                      </Typography>
                    </Box>
                  }
                  sx={{ alignItems: 'flex-start', mb: 1 }}
                />
              ))}
            </RadioGroup>
          </FormControl>

          <Stack direction="row" spacing={1} sx={{ mt: 3 }}>
            <Button variant="contained" onClick={() => void guardar()} disabled={guardando}>
              {guardando ? 'Guardando…' : 'Guardar'}
            </Button>
            <Button variant="outlined" onClick={() => void cargar()} disabled={guardando}>
              Descartar cambios
            </Button>
          </Stack>
        </Paper>
      )}
    </Box>
  );
};

export default ConfiguracionCloseoutFeedlotScreen;
