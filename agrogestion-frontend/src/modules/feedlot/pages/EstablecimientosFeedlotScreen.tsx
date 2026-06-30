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
import type { FeedlotCorral, FeedlotEstablecimiento, FeedlotEstadoCorral } from '../types';
import {
  actualizarCorral,
  actualizarEstablecimiento,
  crearCorral,
  crearEstablecimiento,
  listarCorrales,
  listarEstablecimientos,
  mensajeError,
} from '../services/feedlotApi';

const ESTADOS_CORRAL: { valor: FeedlotEstadoCorral; etiqueta: string }[] = [
  { valor: 'DISPONIBLE', etiqueta: 'Disponible' },
  { valor: 'OCUPADO', etiqueta: 'Ocupado' },
  { valor: 'INACTIVO', etiqueta: 'Inactivo' },
];

const EstablecimientosFeedlotScreen: React.FC = () => {
  const navigate = useNavigate();
  const [establecimientos, setEstablecimientos] = useState<FeedlotEstablecimiento[]>([]);
  const [corralesPorEst, setCorralesPorEst] = useState<Record<number, FeedlotCorral[]>>({});
  const [expandidos, setExpandidos] = useState<Record<number, boolean>>({});
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogoEst, setDialogoEst] = useState(false);
  const [estEditando, setEstEditando] = useState<FeedlotEstablecimiento | null>(null);
  const [estNombre, setEstNombre] = useState('');
  const [estUbicacion, setEstUbicacion] = useState('');
  const [estCapacidad, setEstCapacidad] = useState('');
  const [estActivo, setEstActivo] = useState(true);

  const [dialogoCorral, setDialogoCorral] = useState(false);
  const [estCorralId, setEstCorralId] = useState<number | null>(null);
  const [corralEditando, setCorralEditando] = useState<FeedlotCorral | null>(null);
  const [corralNombre, setCorralNombre] = useState('');
  const [corralCapacidad, setCorralCapacidad] = useState('');
  const [corralEstado, setCorralEstado] = useState<FeedlotEstadoCorral>('DISPONIBLE');
  const [corralActivo, setCorralActivo] = useState(true);

  const cargarCorrales = useCallback(async (estId: number) => {
    const lista = await listarCorrales(estId);
    setCorralesPorEst((prev) => ({ ...prev, [estId]: lista }));
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
    if (nuevo && !corralesPorEst[estId]) {
      try {
        await cargarCorrales(estId);
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

  const abrirEditarEst = (e: FeedlotEstablecimiento) => {
    setEstEditando(e);
    setEstNombre(e.nombre);
    setEstUbicacion(e.ubicacion ?? '');
    setEstCapacidad(e.capacidadTotalCabezas != null ? String(e.capacidadTotalCabezas) : '');
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
        capacidadTotalCabezas:
          estCapacidad.trim() === '' ? null : parseInt(estCapacidad, 10),
        activo: estActivo,
      };
      if (estEditando) {
        await actualizarEstablecimiento(estEditando.id, cuerpo);
      } else {
        await crearEstablecimiento(cuerpo);
      }
      setDialogoEst(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const abrirNuevoCorral = (estId: number) => {
    setEstCorralId(estId);
    setCorralEditando(null);
    setCorralNombre('');
    setCorralCapacidad('');
    setCorralEstado('DISPONIBLE');
    setCorralActivo(true);
    setDialogoCorral(true);
  };

  const abrirEditarCorral = (estId: number, c: FeedlotCorral) => {
    setEstCorralId(estId);
    setCorralEditando(c);
    setCorralNombre(c.nombre);
    setCorralCapacidad(c.capacidadCabezas != null ? String(c.capacidadCabezas) : '');
    setCorralEstado((c.estado as FeedlotEstadoCorral) ?? 'DISPONIBLE');
    setCorralActivo(c.activo !== false);
    setDialogoCorral(true);
  };

  const guardarCorral = async () => {
    if (estCorralId == null) return;
    try {
      if (!corralNombre.trim()) {
        setError('El nombre del corral es obligatorio.');
        return;
      }
      const cuerpo = {
        nombre: corralNombre.trim(),
        capacidadCabezas: corralCapacidad.trim() === '' ? null : parseInt(corralCapacidad, 10),
        estado: corralEstado,
        activo: corralActivo,
      };
      if (corralEditando) {
        await actualizarCorral(estCorralId, corralEditando.id, cuerpo);
      } else {
        await crearCorral(estCorralId, cuerpo);
      }
      setDialogoCorral(false);
      await cargarCorrales(estCorralId);
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Typography variant="h4">Establecimientos y corrales</Typography>
        <Button variant="outlined" onClick={() => navigate('/feedlot/panel')}>
          Panel
        </Button>
      </Stack>

      <Typography variant="body2" color="text.secondary" paragraph>
        Cada establecimiento feedlot agrupa corrales (módulos de confinamiento). Un corral aloja un lote activo a la vez.
      </Typography>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={1} sx={{ my: 2 }}>
          <CircularProgress size={22} />
          <Typography variant="body2">Cargando…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <>
          <Button variant="contained" sx={{ mb: 2 }} onClick={abrirNuevoEst}>
            Nuevo establecimiento
          </Button>

          {establecimientos.length === 0 ? (
            <Paper sx={{ p: 2 }} variant="outlined">
              <Typography color="text.secondary">Sin establecimientos. Creá el primero para habilitar corrales.</Typography>
            </Paper>
          ) : (
            establecimientos.map((e) => (
              <Paper key={e.id} sx={{ mb: 2, overflow: 'hidden' }} variant="outlined">
                <Stack
                  direction="row"
                  alignItems="center"
                  spacing={1}
                  sx={{ p: 2, bgcolor: 'action.hover', cursor: 'pointer' }}
                  onClick={() => void toggleExpandir(e.id)}
                >
                  <IconButton size="small" tabIndex={-1}>
                    {expandidos[e.id] ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                  </IconButton>
                  <Box flex={1}>
                    <Typography variant="subtitle1" fontWeight={600}>
                      {e.nombre}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {e.ubicacion ?? 'Sin ubicación'} · Cap. {e.capacidadTotalCabezas ?? '—'} cab.
                    </Typography>
                  </Box>
                  <Chip size="small" label={e.activo !== false ? 'Activo' : 'Inactivo'} />
                  <IconButton
                    size="small"
                    aria-label="Ubicación en mapa"
                    title="Ubicación en mapa"
                    onClick={(ev) => {
                      ev.stopPropagation();
                      navigate(`/feedlot/establecimientos-mapa?id=${e.id}`);
                    }}
                  >
                    <MapPin size={16} />
                  </IconButton>
                  <IconButton
                    size="small"
                    aria-label="Editar establecimiento"
                    onClick={(ev) => {
                      ev.stopPropagation();
                      abrirEditarEst(e);
                    }}
                  >
                    <Pencil size={16} />
                  </IconButton>
                </Stack>

                <Collapse in={expandidos[e.id]}>
                  <Box sx={{ p: 2, pt: 0 }}>
                    <Button size="small" sx={{ mb: 1 }} onClick={() => abrirNuevoCorral(e.id)}>
                      Nuevo corral
                    </Button>
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Nombre</TableCell>
                            <TableCell align="right">Capacidad</TableCell>
                            <TableCell>Estado</TableCell>
                            <TableCell>Activo</TableCell>
                            <TableCell align="right">Acciones</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {(corralesPorEst[e.id] ?? []).length === 0 ? (
                            <TableRow>
                              <TableCell colSpan={5}>
                                <Typography variant="body2" color="text.secondary">
                                  Sin corrales. Agregá uno para recibir lotes.
                                </Typography>
                              </TableCell>
                            </TableRow>
                          ) : (
                            (corralesPorEst[e.id] ?? []).map((c) => (
                              <TableRow key={c.id}>
                                <TableCell>{c.nombre}</TableCell>
                                <TableCell align="right">{c.capacidadCabezas ?? '—'}</TableCell>
                                <TableCell>{c.estado ?? '—'}</TableCell>
                                <TableCell>{c.activo !== false ? 'Sí' : 'No'}</TableCell>
                                <TableCell align="right">
                                  <IconButton size="small" onClick={() => abrirEditarCorral(e.id, c)}>
                                    <Pencil size={16} />
                                  </IconButton>
                                </TableCell>
                              </TableRow>
                            ))
                          )}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </Box>
                </Collapse>
              </Paper>
            ))
          )}
        </>
      )}

      <Dialog open={dialogoEst} onClose={() => setDialogoEst(false)} fullWidth maxWidth="sm">
        <DialogTitle>{estEditando ? 'Editar establecimiento' : 'Nuevo establecimiento'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={estNombre} onChange={(ev) => setEstNombre(ev.target.value)} />
            <TextField label="Ubicación" fullWidth value={estUbicacion} onChange={(ev) => setEstUbicacion(ev.target.value)} />
            <TextField label="Capacidad total (cabezas)" type="number" fullWidth value={estCapacidad} onChange={(ev) => setEstCapacidad(ev.target.value)} />
            <FormControlLabel control={<Switch checked={estActivo} onChange={(ev) => setEstActivo(ev.target.checked)} />} label="Activo" />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoEst(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarEst()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoCorral} onClose={() => setDialogoCorral(false)} fullWidth maxWidth="sm">
        <DialogTitle>{corralEditando ? 'Editar corral' : 'Nuevo corral'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={corralNombre} onChange={(ev) => setCorralNombre(ev.target.value)} />
            <TextField label="Capacidad (cabezas)" type="number" fullWidth value={corralCapacidad} onChange={(ev) => setCorralCapacidad(ev.target.value)} />
            <TextField select label="Estado operativo" fullWidth value={corralEstado} onChange={(ev) => setCorralEstado(ev.target.value as FeedlotEstadoCorral)}>
              {ESTADOS_CORRAL.map((s) => (
                <MenuItem key={s.valor} value={s.valor}>{s.etiqueta}</MenuItem>
              ))}
            </TextField>
            <FormControlLabel control={<Switch checked={corralActivo} onChange={(ev) => setCorralActivo(ev.target.checked)} />} label="Activo" />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoCorral(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarCorral()}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default EstablecimientosFeedlotScreen;
