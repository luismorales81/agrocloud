import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Divider,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Icon } from '../../../components/icons';
import BotonRellenarClima from '../../../components/BotonRellenarClima';
import type { LecheriaAnimal, LecheriaTurnoOrdene } from '../types';
import { listarAnimales, mensajeError, obtenerAnimal, registrarOrdene } from '../services/lecheriaApi';

const TURNOS: { valor: LecheriaTurnoOrdene; etiqueta: string }[] = [
  { valor: 'AM', etiqueta: 'Mañana (AM)' },
  { valor: 'PM', etiqueta: 'Tarde (PM)' },
  { valor: 'TOTAL', etiqueta: 'Total del día' },
];

const RegistroOrdeneScreen: React.FC = () => {
  const navigate = useNavigate();
  const [animales, setAnimales] = useState<LecheriaAnimal[]>([]);
  const [animalId, setAnimalId] = useState<number | ''>('');
  const [animalSel, setAnimalSel] = useState<LecheriaAnimal | null>(null);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState<string | null>(null);

  const [fecha, setFecha] = useState(() => new Date().toISOString().slice(0, 10));
  const [turno, setTurno] = useState<LecheriaTurnoOrdene>('AM');
  const [litros, setLitros] = useState('');
  const [grasa, setGrasa] = useState('');
  const [proteina, setProteina] = useState('');
  const [rcs, setRcs] = useState('');
  const [temperatura, setTemperatura] = useState('');
  const [humedad, setHumedad] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const cargarAnimales = useCallback(async () => {
    try {
      setCargando(true);
      const lista = await listarAnimales();
      const lactantes = lista.filter((a) => a.estado === 'LACTANDO' && a.activo !== false);
      setAnimales(lactantes.length > 0 ? lactantes : lista);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargarAnimales();
  }, [cargarAnimales]);

  useEffect(() => {
    if (animalId === '') {
      setAnimalSel(null);
      return;
    }
    void obtenerAnimal(Number(animalId))
      .then(setAnimalSel)
      .catch((err: unknown) => setError(mensajeError(err)));
  }, [animalId]);

  const limpiarFormulario = () => {
    setLitros('');
    setGrasa('');
    setProteina('');
    setRcs('');
    setObservaciones('');
  };

  const guardar = async () => {
    if (animalId === '' || !litros.trim()) {
      setError('Seleccioná un animal e ingresá los litros.');
      return;
    }
    try {
      setGuardando(true);
      setError(null);
      setExito(null);
      await registrarOrdene(Number(animalId), {
        fecha,
        turno,
        litros: parseFloat(litros.replace(',', '.')),
        grasaPct: grasa.trim() ? parseFloat(grasa.replace(',', '.')) : null,
        proteinaPct: proteina.trim() ? parseFloat(proteina.replace(',', '.')) : null,
        rcs: rcs.trim() ? parseInt(rcs, 10) : null,
        temperaturaAmbiente: temperatura.trim() ? parseFloat(temperatura.replace(',', '.')) : null,
        humedadAmbiente: humedad.trim() ? parseFloat(humedad.replace(',', '.')) : null,
        observaciones: observaciones.trim() || null,
      });
      setExito('Ordeñe registrado correctamente.');
      limpiarFormulario();
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Box
      sx={{
        p: { xs: 2, md: 3 },
        maxWidth: 1200,
        width: '100%',
        mx: 'auto',
      }}
    >
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        alignItems={{ xs: 'flex-start', sm: 'center' }}
        justifyContent="space-between"
        spacing={2}
        sx={{ mb: 2 }}
      >
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="Milk" size={32} />
          <Box>
            <Typography variant="h4" component="h1">
              Registro de ordeñe
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Carga rápida de producción con calidad láctea y clima ambiente.
            </Typography>
          </Box>
        </Stack>
        <Button variant="outlined" size="small" onClick={() => navigate('/lecheria/panel')}>
          Volver al panel
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando animales…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      {exito && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setExito(null)}>
          {exito}
        </Alert>
      )}

      {!cargando && (
        <Paper variant="outlined" sx={{ p: { xs: 2, md: 3 } }}>
          <Box
            component="form"
            onSubmit={(e) => {
              e.preventDefault();
              void guardar();
            }}
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: 3,
            }}
          >
            {/* Identificación */}
            <Box>
              <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
                Animal y ordeñe
              </Typography>
              <Box
                sx={{
                  display: 'grid',
                  gridTemplateColumns: { xs: '1fr', md: '2fr 1fr 1fr 1fr' },
                  gap: 2,
                  alignItems: 'start',
                }}
              >
                <TextField
                  select
                  label="Animal"
                  fullWidth
                  value={animalId}
                  onChange={(e) => setAnimalId(e.target.value === '' ? '' : Number(e.target.value))}
                  sx={{ gridColumn: { xs: '1', md: '1 / -1' } }}
                >
                  <MenuItem value="">
                    <em>Seleccionar…</em>
                  </MenuItem>
                  {animales.map((a) => (
                    <MenuItem key={a.id} value={a.id}>
                      {a.identificacion} — {a.rodeoNombre ?? 'sin rodeo'}
                    </MenuItem>
                  ))}
                </TextField>

                {animalSel && (
                  <Stack
                    direction="row"
                    flexWrap="wrap"
                    gap={1}
                    sx={{ gridColumn: { xs: '1', md: '1 / -1' }, minHeight: 32 }}
                  >
                    {animalSel.especie && <Chip size="small" label={animalSel.especie} />}
                    {animalSel.estado && <Chip size="small" variant="outlined" label={animalSel.estado} />}
                    {animalSel.razaNombre && <Chip size="small" variant="outlined" label={animalSel.razaNombre} />}
                    {animalSel.dim != null && <Chip size="small" color="primary" label={`DIM ${animalSel.dim}`} />}
                  </Stack>
                )}

                <TextField
                  label="Fecha"
                  type="date"
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                  value={fecha}
                  onChange={(e) => setFecha(e.target.value)}
                />
                <TextField
                  select
                  label="Turno"
                  fullWidth
                  value={turno}
                  onChange={(e) => setTurno(e.target.value as LecheriaTurnoOrdene)}
                >
                  {TURNOS.map((t) => (
                    <MenuItem key={t.valor} value={t.valor}>
                      {t.etiqueta}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  label="Litros"
                  fullWidth
                  required
                  value={litros}
                  onChange={(e) => setLitros(e.target.value)}
                  inputProps={{ inputMode: 'decimal' }}
                />
              </Box>
            </Box>

            <Divider />

            {/* Calidad y clima en la misma fila de secciones */}
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
                gap: 3,
                alignItems: 'start',
              }}
            >
              <Box>
                <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
                  Calidad láctea
                </Typography>
                <Box
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: '1fr 1fr',
                    gap: 2,
                  }}
                >
                  <TextField label="% Grasa" fullWidth value={grasa} onChange={(e) => setGrasa(e.target.value)} />
                  <TextField label="% Proteína" fullWidth value={proteina} onChange={(e) => setProteina(e.target.value)} />
                  <TextField
                    label="RCS (células)"
                    fullWidth
                    value={rcs}
                    onChange={(e) => setRcs(e.target.value)}
                    sx={{ gridColumn: '1 / -1' }}
                  />
                </Box>
              </Box>

              <Box>
                <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
                  Clima ambiente
                </Typography>
                <Stack spacing={2}>
                  <BotonRellenarClima
                    climaLatitud={animalSel?.climaLatitud}
                    climaLongitud={animalSel?.climaLongitud}
                    onValores={(v) => {
                      if (v.temperatura != null) setTemperatura(String(v.temperatura));
                      if (v.humedad != null) setHumedad(String(v.humedad));
                    }}
                    onError={setError}
                  />
                  <Box
                    sx={{
                      display: 'grid',
                      gridTemplateColumns: '1fr 1fr',
                      gap: 2,
                    }}
                  >
                    <TextField
                      label="Temperatura °C"
                      fullWidth
                      value={temperatura}
                      onChange={(e) => setTemperatura(e.target.value)}
                    />
                    <TextField
                      label="Humedad %"
                      fullWidth
                      value={humedad}
                      onChange={(e) => setHumedad(e.target.value)}
                    />
                  </Box>
                </Stack>
              </Box>
            </Box>

            <Divider />

            <Box>
              <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>
                Observaciones
              </Typography>
              <TextField
                label="Notas del ordeñe"
                fullWidth
                multiline
                minRows={2}
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
              />
            </Box>

            <Divider />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} justifyContent="flex-end">
              <Button type="button" variant="outlined" onClick={limpiarFormulario} disabled={guardando}>
                Limpiar campos
              </Button>
              <Button type="submit" variant="contained" disabled={guardando}>
                {guardando ? 'Guardando…' : 'Registrar ordeñe'}
              </Button>
            </Stack>
          </Box>
        </Paper>
      )}
    </Box>
  );
};

export default RegistroOrdeneScreen;
