import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
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
import { useCampana } from '../../../contexts/CampanaContext';
import type {
  LecheriaAnimal,
  LecheriaEspecie,
  LecheriaEstadoAnimal,
  LecheriaRaza,
  LecheriaRodeo,
  LecheriaSexoAnimal,
} from '../types';
import {
  crearAnimal,
  listarAnimales,
  listarEstablecimientos,
  listarRazas,
  listarRodeos,
  mensajeError,
} from '../services/lecheriaApi';

const ESPECIES: { valor: LecheriaEspecie; etiqueta: string }[] = [
  { valor: 'BOVINO', etiqueta: 'Bovino' },
  { valor: 'BUFALO', etiqueta: 'Búfalo' },
  { valor: 'CAPRINO', etiqueta: 'Caprino' },
  { valor: 'OVINO', etiqueta: 'Ovino' },
  { valor: 'CAMELIDO', etiqueta: 'Camélido' },
];

const ESTADOS: { valor: LecheriaEstadoAnimal; etiqueta: string }[] = [
  { valor: 'LACTANDO', etiqueta: 'Lactando' },
  { valor: 'SECA', etiqueta: 'Seca' },
  { valor: 'PRENADA', etiqueta: 'Preñada' },
  { valor: 'VAQUILLONA', etiqueta: 'Vaquillona' },
];

const SEXOS: { valor: LecheriaSexoAnimal; etiqueta: string }[] = [
  { valor: 'HEMBRA', etiqueta: 'Hembra' },
  { valor: 'MACHO', etiqueta: 'Macho' },
];

function chipEstado(estado: string | undefined): React.ReactElement {
  const valor = estado ?? '—';
  const color =
    valor === 'LACTANDO' ? 'success' : valor === 'PRENADA' ? 'info' : 'default';
  return <Chip size="small" label={valor} color={color} variant="outlined" />;
}

const AnimalesLecheriaScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [animales, setAnimales] = useState<LecheriaAnimal[]>([]);

  const [dialogoAlta, setDialogoAlta] = useState(false);
  const [razas, setRazas] = useState<LecheriaRaza[]>([]);
  const [rodeos, setRodeos] = useState<LecheriaRodeo[]>([]);
  const [identificacion, setIdentificacion] = useState('');
  const [especie, setEspecie] = useState<LecheriaEspecie>('BOVINO');
  const [sexo, setSexo] = useState<LecheriaSexoAnimal>('HEMBRA');
  const [estado, setEstado] = useState<LecheriaEstadoAnimal>('LACTANDO');
  const [razaId, setRazaId] = useState<number | ''>('');
  const [rodeoId, setRodeoId] = useState<number | ''>('');
  const [fechaNacimiento, setFechaNacimiento] = useState('');
  const [fechaIngreso, setFechaIngreso] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const lista = await listarAnimales();
      setAnimales(lista);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar, campanaActiva?.id]);

  const abrirAlta = async () => {
    try {
      const [r, establecimientos] = await Promise.all([listarRazas(), listarEstablecimientos()]);
      setRazas(r);
      const todosRodeos: LecheriaRodeo[] = [];
      for (const est of establecimientos) {
        const rs = await listarRodeos(est.id);
        todosRodeos.push(...rs);
      }
      setRodeos(todosRodeos);
      setIdentificacion('');
      setEspecie('BOVINO');
      setSexo('HEMBRA');
      setEstado('LACTANDO');
      setRazaId(r.length > 0 ? r[0].id : '');
      setRodeoId(todosRodeos.length > 0 ? todosRodeos[0].id : '');
      setFechaNacimiento('');
      setFechaIngreso(new Date().toISOString().slice(0, 10));
      setObservaciones('');
      setDialogoAlta(true);
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarAlta = async () => {
    if (!identificacion.trim() || razaId === '' || rodeoId === '') {
      setError('Completá identificación, raza y rodeo.');
      return;
    }
    try {
      await crearAnimal({
        identificacion: identificacion.trim(),
        especie,
        sexo,
        estado,
        razaId: Number(razaId),
        rodeoId: Number(rodeoId),
        fechaNacimiento: fechaNacimiento || null,
        fechaIngreso: fechaIngreso || null,
        observaciones: observaciones.trim() || null,
      });
      setDialogoAlta(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="List" size={28} />
          <Typography variant="h4" component="h1">
            Animales
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" onClick={() => navigate('/lecheria/panel')}>
            Panel
          </Button>
          <Button variant="text" onClick={() => void cargar()} disabled={cargando}>
            Actualizar
          </Button>
          <Button variant="contained" onClick={() => void abrirAlta()}>
            Alta animal
          </Button>
        </Stack>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando animales…</Typography>
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
                <TableCell>Identificación</TableCell>
                <TableCell>Raza</TableCell>
                <TableCell>Rodeo</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>DIM</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {animales.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center">
                    No hay animales registrados.
                  </TableCell>
                </TableRow>
              ) : (
                animales.map((a) => (
                  <TableRow key={a.id} hover>
                    <TableCell>{a.identificacion}</TableCell>
                    <TableCell>{a.razaNombre ?? '—'}</TableCell>
                    <TableCell>{a.rodeoNombre ?? '—'}</TableCell>
                    <TableCell>{chipEstado(a.estado)}</TableCell>
                    <TableCell>{a.dim ?? a.lactanciaActiva?.dim ?? '—'}</TableCell>
                    <TableCell align="right">
                      <Button size="small" onClick={() => navigate(`/lecheria/animales/${a.id}`)}>
                        Detalle
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogoAlta} onClose={() => setDialogoAlta(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Alta de animal</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Identificación"
              fullWidth
              required
              value={identificacion}
              onChange={(e) => setIdentificacion(e.target.value)}
            />
            <TextField select label="Especie" fullWidth value={especie} onChange={(e) => setEspecie(e.target.value as LecheriaEspecie)}>
              {ESPECIES.map((op) => (
                <MenuItem key={op.valor} value={op.valor}>
                  {op.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField select label="Sexo" fullWidth value={sexo} onChange={(e) => setSexo(e.target.value as LecheriaSexoAnimal)}>
              {SEXOS.map((op) => (
                <MenuItem key={op.valor} value={op.valor}>
                  {op.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField select label="Estado" fullWidth value={estado} onChange={(e) => setEstado(e.target.value as LecheriaEstadoAnimal)}>
              {ESTADOS.map((op) => (
                <MenuItem key={op.valor} value={op.valor}>
                  {op.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField select label="Raza" fullWidth value={razaId} onChange={(e) => setRazaId(Number(e.target.value))}>
              {razas.map((r) => (
                <MenuItem key={r.id} value={r.id}>
                  {r.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField select label="Rodeo" fullWidth value={rodeoId} onChange={(e) => setRodeoId(Number(e.target.value))}>
              {rodeos.map((r) => (
                <MenuItem key={r.id} value={r.id}>
                  {r.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Fecha nacimiento"
              type="date"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={fechaNacimiento}
              onChange={(e) => setFechaNacimiento(e.target.value)}
            />
            <TextField
              label="Fecha ingreso"
              type="date"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={fechaIngreso}
              onChange={(e) => setFechaIngreso(e.target.value)}
            />
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoAlta(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarAlta()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AnimalesLecheriaScreen;
