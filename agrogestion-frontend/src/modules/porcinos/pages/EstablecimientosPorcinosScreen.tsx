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
import type { PorcinosEstablecimiento, PorcinosEstadoGalpon, PorcinosGalpon } from '../typesApiV2';
import {
  actualizarEstablecimiento,
  actualizarGalpon,
  crearEstablecimiento,
  crearGalpon,
  listarEstablecimientos,
  listarGalpones,
  mensajeError,
} from '../services/porcinosApi';

const ESTADOS_GALPON: { valor: PorcinosEstadoGalpon; etiqueta: string }[] = [
  { valor: 'DISPONIBLE', etiqueta: 'Disponible' },
  { valor: 'OCUPADO', etiqueta: 'Ocupado' },
  { valor: 'INACTIVO', etiqueta: 'Inactivo' },
];

const EstablecimientosPorcinosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [establecimientos, setEstablecimientos] = useState<PorcinosEstablecimiento[]>([]);
  const [galponesPorEst, setGalponesPorEst] = useState<Record<number, PorcinosGalpon[]>>({});
  const [expandidos, setExpandidos] = useState<Record<number, boolean>>({});
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogoEst, setDialogoEst] = useState(false);
  const [estEditando, setEstEditando] = useState<PorcinosEstablecimiento | null>(null);
  const [estNombre, setEstNombre] = useState('');
  const [estUbicacion, setEstUbicacion] = useState('');
  const [estCapacidad, setEstCapacidad] = useState('');
  const [estActivo, setEstActivo] = useState(true);

  const [dialogoGalpon, setDialogoGalpon] = useState(false);
  const [estGalponId, setEstGalponId] = useState<number | null>(null);
  const [galponEditando, setGalponEditando] = useState<PorcinosGalpon | null>(null);
  const [galponNombre, setGalponNombre] = useState('');
  const [galponCapacidad, setGalponCapacidad] = useState('');
  const [galponEstado, setGalponEstado] = useState<PorcinosEstadoGalpon>('DISPONIBLE');
  const [galponActivo, setGalponActivo] = useState(true);

  const cargarGalpones = useCallback(async (estId: number) => {
    const lista = await listarGalpones(estId);
    setGalponesPorEst((prev) => ({ ...prev, [estId]: lista }));
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
    const abrir = !expandidos[estId];
    setExpandidos((prev) => ({ ...prev, [estId]: abrir }));
    if (abrir && !galponesPorEst[estId]) {
      try {
        await cargarGalpones(estId);
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

  const abrirEditarEst = (e: PorcinosEstablecimiento) => {
    setEstEditando(e);
    setEstNombre(e.nombre);
    setEstUbicacion(e.ubicacion ?? '');
    setEstCapacidad(e.capacidadCabezas != null ? String(e.capacidadCabezas) : '');
    setEstActivo(e.activo !== false);
    setDialogoEst(true);
  };

  const guardarEst = async () => {
    const cuerpo = {
      nombre: estNombre.trim(),
      ubicacion: estUbicacion.trim() || null,
      capacidadCabezas: estCapacidad ? parseInt(estCapacidad, 10) : null,
      activo: estActivo,
    };
    try {
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

  const abrirNuevoGalpon = (estId: number) => {
    setEstGalponId(estId);
    setGalponEditando(null);
    setGalponNombre('');
    setGalponCapacidad('');
    setGalponEstado('DISPONIBLE');
    setGalponActivo(true);
    setDialogoGalpon(true);
  };

  const abrirEditarGalpon = (estId: number, g: PorcinosGalpon) => {
    setEstGalponId(estId);
    setGalponEditando(g);
    setGalponNombre(g.nombre);
    setGalponCapacidad(g.capacidadCabezas != null ? String(g.capacidadCabezas) : '');
    setGalponEstado((g.estado as PorcinosEstadoGalpon) ?? 'DISPONIBLE');
    setGalponActivo(g.activo !== false);
    setDialogoGalpon(true);
  };

  const guardarGalpon = async () => {
    if (estGalponId == null) return;
    const cuerpo = {
      nombre: galponNombre.trim(),
      capacidadCabezas: galponCapacidad ? parseInt(galponCapacidad, 10) : null,
      estado: galponEstado,
      activo: galponActivo,
    };
    try {
      if (galponEditando) {
        await actualizarGalpon(estGalponId, galponEditando.id, cuerpo);
      } else {
        await crearGalpon(estGalponId, cuerpo);
      }
      setDialogoGalpon(false);
      await cargarGalpones(estGalponId);
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Typography variant="h4" component="h1">
          Establecimientos y galpones
        </Typography>
        <Stack direction="row" spacing={1}>
          <Button
            variant="outlined"
            startIcon={<MapPin size={16} />}
            onClick={() => navigate('/porcinos/establecimientos-mapa')}
          >
            Ubicación en mapa
          </Button>
          <Button variant="contained" onClick={abrirNuevoEst}>
            Nuevo establecimiento
          </Button>
        </Stack>
      </Stack>

      {cargando && <CircularProgress size={24} />}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell width={40} />
                <TableCell>Nombre</TableCell>
                <TableCell>Ubicación</TableCell>
                <TableCell align="right">Capacidad</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {establecimientos.map((e) => (
                <React.Fragment key={e.id}>
                  <TableRow>
                    <TableCell>
                      <IconButton size="small" onClick={() => void toggleExpandir(e.id)}>
                        {expandidos[e.id] ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                      </IconButton>
                    </TableCell>
                    <TableCell>{e.nombre}</TableCell>
                    <TableCell>{e.ubicacion ?? '—'}</TableCell>
                    <TableCell align="right">{e.capacidadCabezas ?? '—'}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={e.activo !== false ? 'Activo' : 'Inactivo'}
                        color={e.activo !== false ? 'success' : 'default'}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => abrirEditarEst(e)}>
                        <Pencil size={16} />
                      </IconButton>
                      <Button size="small" onClick={() => navigate(`/porcinos/establecimientos-mapa?id=${e.id}`)}>
                        Mapa
                      </Button>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell colSpan={6} sx={{ py: 0, border: 0 }}>
                      <Collapse in={!!expandidos[e.id]}>
                        <Box sx={{ pl: 4, py: 1 }}>
                          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                            <Typography variant="subtitle2">Galpones</Typography>
                            <Button size="small" onClick={() => abrirNuevoGalpon(e.id)}>
                              + Galpón
                            </Button>
                          </Stack>
                          {(galponesPorEst[e.id] ?? []).map((g) => (
                            <Stack
                              key={g.id}
                              direction="row"
                              justifyContent="space-between"
                              alignItems="center"
                              sx={{ py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}
                            >
                              <Typography variant="body2">
                                {g.nombre} — {g.estado ?? '—'} ({g.capacidadCabezas ?? '?'} cab.)
                              </Typography>
                              <IconButton size="small" onClick={() => abrirEditarGalpon(e.id, g)}>
                                <Pencil size={14} />
                              </IconButton>
                            </Stack>
                          ))}
                          {(galponesPorEst[e.id] ?? []).length === 0 && (
                            <Typography variant="body2" color="text.secondary">
                              Sin galpones.
                            </Typography>
                          )}
                        </Box>
                      </Collapse>
                    </TableCell>
                  </TableRow>
                </React.Fragment>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogoEst} onClose={() => setDialogoEst(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{estEditando ? 'Editar establecimiento' : 'Nuevo establecimiento'}</DialogTitle>
        <DialogContent>
          <TextField
            margin="dense"
            label="Nombre"
            fullWidth
            required
            value={estNombre}
            onChange={(ev) => setEstNombre(ev.target.value)}
          />
          <TextField
            margin="dense"
            label="Ubicación"
            fullWidth
            value={estUbicacion}
            onChange={(ev) => setEstUbicacion(ev.target.value)}
          />
          <TextField
            margin="dense"
            label="Capacidad (cabezas)"
            type="number"
            fullWidth
            value={estCapacidad}
            onChange={(ev) => setEstCapacidad(ev.target.value)}
          />
          <FormControlLabel
            control={<Switch checked={estActivo} onChange={(ev) => setEstActivo(ev.target.checked)} />}
            label="Activo"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoEst(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarEst()} disabled={!estNombre.trim()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoGalpon} onClose={() => setDialogoGalpon(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{galponEditando ? 'Editar galpón' : 'Nuevo galpón'}</DialogTitle>
        <DialogContent>
          <TextField
            margin="dense"
            label="Nombre"
            fullWidth
            required
            value={galponNombre}
            onChange={(ev) => setGalponNombre(ev.target.value)}
          />
          <TextField
            margin="dense"
            label="Capacidad (cabezas)"
            type="number"
            fullWidth
            value={galponCapacidad}
            onChange={(ev) => setGalponCapacidad(ev.target.value)}
          />
          <TextField
            select
            margin="dense"
            label="Estado"
            fullWidth
            value={galponEstado}
            onChange={(ev) => setGalponEstado(ev.target.value as PorcinosEstadoGalpon)}
          >
            {ESTADOS_GALPON.map((o) => (
              <MenuItem key={o.valor} value={o.valor}>
                {o.etiqueta}
              </MenuItem>
            ))}
          </TextField>
          <FormControlLabel
            control={<Switch checked={galponActivo} onChange={(ev) => setGalponActivo(ev.target.checked)} />}
            label="Activo"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoGalpon(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarGalpon()} disabled={!galponNombre.trim()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default EstablecimientosPorcinosScreen;
