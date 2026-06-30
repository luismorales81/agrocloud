import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Collapse,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { ChevronDown, ChevronRight, MapPin, Pencil } from 'lucide-react';
import type { LecheriaEspecie, LecheriaEstablecimiento, LecheriaRodeo } from '../types';
import {
  actualizarEstablecimiento,
  crearEstablecimiento,
  crearRodeo,
  listarEstablecimientos,
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

const EstablecimientosLecheriaScreen: React.FC = () => {
  const navigate = useNavigate();
  const [establecimientos, setEstablecimientos] = useState<LecheriaEstablecimiento[]>([]);
  const [rodeosPorEst, setRodeosPorEst] = useState<Record<number, LecheriaRodeo[]>>({});
  const [expandidos, setExpandidos] = useState<Record<number, boolean>>({});
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogoEst, setDialogoEst] = useState(false);
  const [estEditando, setEstEditando] = useState<LecheriaEstablecimiento | null>(null);
  const [estNombre, setEstNombre] = useState('');
  const [estUbicacion, setEstUbicacion] = useState('');
  const [estCapacidad, setEstCapacidad] = useState('');
  const [estActivo, setEstActivo] = useState(true);

  const [dialogoRodeo, setDialogoRodeo] = useState(false);
  const [estRodeoId, setEstRodeoId] = useState<number | null>(null);
  const [rodeoNombre, setRodeoNombre] = useState('');
  const [rodeoEspecie, setRodeoEspecie] = useState<LecheriaEspecie>('BOVINO');
  const [rodeoActivo, setRodeoActivo] = useState(true);

  const cargarRodeos = useCallback(async (estId: number) => {
    const lista = await listarRodeos(estId);
    setRodeosPorEst((prev) => ({ ...prev, [estId]: lista }));
  }, []);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const lista = await listarEstablecimientos();
      setEstablecimientos(lista);
      setError(null);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const toggleExpandir = async (estId: number) => {
    const nuevo = !expandidos[estId];
    setExpandidos((prev) => ({ ...prev, [estId]: nuevo }));
    if (nuevo && !rodeosPorEst[estId]) {
      try {
        await cargarRodeos(estId);
      } catch (err: unknown) {
        setError(mensajeError(err));
      }
    }
  };

  const abrirNuevoEst = () => {
    setEstEditando(null);
    setEstNombre('');
    setEstUbicacion('');
    setEstCapacidad('');
    setEstActivo(true);
    setDialogoEst(true);
  };

  const abrirEditarEst = (e: LecheriaEstablecimiento) => {
    setEstEditando(e);
    setEstNombre(e.nombre);
    setEstUbicacion(e.ubicacion ?? '');
    setEstCapacidad(e.capacidadAnimales != null ? String(e.capacidadAnimales) : '');
    setEstActivo(e.activo !== false);
    setDialogoEst(true);
  };

  const guardarEst = async () => {
    try {
      if (!estNombre.trim()) {
        setError('El nombre es obligatorio.');
        return;
      }
      const cuerpo = {
        nombre: estNombre.trim(),
        ubicacion: estUbicacion.trim() || null,
        capacidadAnimales: estCapacidad.trim() === '' ? null : parseInt(estCapacidad, 10),
        activo: estActivo,
      };
      if (estEditando) {
        await actualizarEstablecimiento(estEditando.id, {
          ...cuerpo,
          coordenadas: estEditando.coordenadas ?? null,
        });
      } else {
        await crearEstablecimiento(cuerpo);
      }
      setDialogoEst(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const abrirNuevoRodeo = (estId: number) => {
    setEstRodeoId(estId);
    setRodeoNombre('');
    setRodeoEspecie('BOVINO');
    setRodeoActivo(true);
    setDialogoRodeo(true);
  };

  const guardarRodeo = async () => {
    if (estRodeoId == null || !rodeoNombre.trim()) return;
    try {
      await crearRodeo(estRodeoId, {
        nombre: rodeoNombre.trim(),
        especie: rodeoEspecie,
        activo: rodeoActivo,
      });
      setDialogoRodeo(false);
      await cargarRodeos(estRodeoId);
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1000 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Typography variant="h4" component="h1">
          Establecimientos y rodeos
        </Typography>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<MapPin size={16} />} onClick={() => navigate('/lecheria/establecimientos-mapa')}>
            Ubicación en mapa
          </Button>
          <Button variant="contained" onClick={abrirNuevoEst}>
            Nuevo establecimiento
          </Button>
        </Stack>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <Paper variant="outlined">
          {establecimientos.length === 0 ? (
            <Typography sx={{ p: 2 }} color="text.secondary">
              No hay establecimientos. Creá uno para comenzar.
            </Typography>
          ) : (
            establecimientos.map((est) => (
              <Box key={est.id} sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
                <Stack direction="row" alignItems="center" sx={{ p: 1.5 }}>
                  <IconButton size="small" onClick={() => void toggleExpandir(est.id)}>
                    {expandidos[est.id] ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                  </IconButton>
                  <Typography sx={{ flex: 1 }} fontWeight={500}>
                    {est.nombre}
                    {est.activo === false && (
                      <Chip size="small" label="Inactivo" sx={{ ml: 1 }} />
                    )}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mr: 2 }}>
                    Cap. {est.capacidadAnimales ?? '—'}
                  </Typography>
                  <IconButton size="small" onClick={() => abrirEditarEst(est)}>
                    <Pencil size={16} />
                  </IconButton>
                  <Button size="small" onClick={() => abrirNuevoRodeo(est.id)}>
                    + Rodeo
                  </Button>
                </Stack>
                <Collapse in={expandidos[est.id]}>
                  <TableContainer>
                    <Table size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell>Rodeo</TableCell>
                          <TableCell>Especie</TableCell>
                          <TableCell>Estado</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {(rodeosPorEst[est.id] ?? []).map((r) => (
                          <TableRow key={r.id}>
                            <TableCell>{r.nombre}</TableCell>
                            <TableCell>{r.especie}</TableCell>
                            <TableCell>{r.activo !== false ? 'Activo' : 'Inactivo'}</TableCell>
                          </TableRow>
                        ))}
                        {(rodeosPorEst[est.id] ?? []).length === 0 && (
                          <TableRow>
                            <TableCell colSpan={3} align="center">
                              Sin rodeos
                            </TableCell>
                          </TableRow>
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Collapse>
              </Box>
            ))
          )}
        </Paper>
      )}

      <Dialog open={dialogoEst} onClose={() => setDialogoEst(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{estEditando ? 'Editar establecimiento' : 'Nuevo establecimiento'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={estNombre} onChange={(e) => setEstNombre(e.target.value)} />
            <TextField label="Ubicación (texto)" fullWidth value={estUbicacion} onChange={(e) => setEstUbicacion(e.target.value)} />
            <TextField label="Capacidad animales" fullWidth value={estCapacidad} onChange={(e) => setEstCapacidad(e.target.value)} />
            <FormControlLabel control={<Switch checked={estActivo} onChange={(e) => setEstActivo(e.target.checked)} />} label="Activo" />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoEst(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarEst()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoRodeo} onClose={() => setDialogoRodeo(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Nuevo rodeo</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={rodeoNombre} onChange={(e) => setRodeoNombre(e.target.value)} />
            <TextField select label="Especie" fullWidth value={rodeoEspecie} onChange={(e) => setRodeoEspecie(e.target.value as LecheriaEspecie)}>
              {ESPECIES.map((op) => (
                <MenuItem key={op.valor} value={op.valor}>{op.etiqueta}</MenuItem>
              ))}
            </TextField>
            <FormControlLabel control={<Switch checked={rodeoActivo} onChange={(e) => setRodeoActivo(e.target.checked)} />} label="Activo" />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoRodeo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarRodeo()}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default EstablecimientosLecheriaScreen;
