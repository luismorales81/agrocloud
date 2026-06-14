import React, { useCallback, useEffect, useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControlLabel,
  Switch,
  Chip,
  IconButton,
} from '@mui/material';
import { Pencil, MapPinned } from 'lucide-react';
import {
  listarEstablecimientosCrianza,
  crearEstablecimientoCrianza,
  actualizarEstablecimientoCrianza,
  AvicolaEstablecimientoRespuesta,
  mensajeError,
} from '../services/avicolaCrianzaApi';

const EstablecimientosCrianzaScreen: React.FC = () => {
  const navigate = useNavigate();
  const [establecimientos, setEstablecimientos] = useState<AvicolaEstablecimientoRespuesta[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogoEst, setDialogoEst] = useState(false);
  const [estEditando, setEstEditando] = useState<AvicolaEstablecimientoRespuesta | null>(null);
  const [estNombre, setEstNombre] = useState('');
  const [estObs, setEstObs] = useState('');
  const [estActivo, setEstActivo] = useState(true);
  const [estUbicacion, setEstUbicacion] = useState('');

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const e = await listarEstablecimientosCrianza();
      setEstablecimientos(e);
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

  const guardarEst = async () => {
    try {
      if (!estNombre.trim()) {
        setError('El nombre del establecimiento es obligatorio.');
        return;
      }
      const ubicacionCuerpo = estUbicacion.trim() || null;
      if (estEditando) {
        await actualizarEstablecimientoCrianza(estEditando.id, {
          nombre: estNombre.trim(),
          observaciones: estObs.trim() || null,
          activo: estActivo,
          ubicacion: ubicacionCuerpo,
        });
      } else {
        await crearEstablecimientoCrianza({
          nombre: estNombre.trim(),
          observaciones: estObs.trim() || null,
          activo: estActivo,
          ubicacion: ubicacionCuerpo,
          coordenadas: null,
        });
      }
      setDialogoEst(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const resumenUbicacionTabla = (e: AvicolaEstablecimientoRespuesta) => {
    const u = e.ubicacion?.trim();
    const c = e.coordenadas?.trim();
    if (u && c) return `${u.length > 28 ? `${u.slice(0, 28)}…` : u} · mapa`;
    if (u) return u.length > 40 ? `${u.slice(0, 40)}…` : u;
    if (c) return 'Coordenadas en mapa';
    return '—';
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Establecimientos — Crianza
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        Datos generales del predio o galpón. La <strong>ubicación en mapa</strong> (polígono o punto GPS) se define con el
        icono de mapa en cada fila o al editar el establecimiento (enlace «Abrir ubicación en mapa»).
      </Typography>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={22} />
          <Typography variant="body2">Cargando…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <Box>
          <Button
            variant="contained"
            sx={{ mb: 2 }}
            onClick={() => {
              setEstEditando(null);
              setEstNombre('');
              setEstObs('');
              setEstActivo(true);
              setEstUbicacion('');
              setDialogoEst(true);
            }}
          >
            Nuevo establecimiento
          </Button>
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Ubicación / mapa</TableCell>
                  <TableCell>Observaciones</TableCell>
                  <TableCell>Activo</TableCell>
                  <TableCell align="right">Acciones</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {establecimientos.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5}>
                      <Typography color="text.secondary">Sin registros.</Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  establecimientos.map((e) => (
                    <TableRow key={e.id}>
                      <TableCell>{e.nombre}</TableCell>
                      <TableCell>{resumenUbicacionTabla(e)}</TableCell>
                      <TableCell>{e.observaciones ?? '—'}</TableCell>
                      <TableCell>
                        <Chip size="small" label={e.activo !== false ? 'Sí' : 'No'} />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          component={RouterLink}
                          to={`/avicola-crianza/establecimientos-mapa?id=${e.id}`}
                          aria-label="Ubicación en mapa"
                          title="Ubicación en mapa"
                          sx={{ mr: 0.5 }}
                        >
                          <MapPinned size={18} />
                        </IconButton>
                        <IconButton
                          size="small"
                          onClick={() => {
                            setEstEditando(e);
                            setEstNombre(e.nombre);
                            setEstObs(e.observaciones ?? '');
                            setEstActivo(e.activo !== false);
                            setEstUbicacion(e.ubicacion ?? '');
                            setDialogoEst(true);
                          }}
                          aria-label="Editar"
                        >
                          <Pencil size={18} />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}

      <Dialog open={dialogoEst} onClose={() => setDialogoEst(false)} fullWidth maxWidth="sm">
        <DialogTitle>{estEditando ? 'Editar establecimiento' : 'Nuevo establecimiento'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Nombre"
            fullWidth
            value={estNombre}
            onChange={(ev) => setEstNombre(ev.target.value)}
          />
          <TextField
            margin="dense"
            label="Ubicación o referencia (texto)"
            fullWidth
            value={estUbicacion}
            onChange={(ev) => setEstUbicacion(ev.target.value)}
            placeholder="Ej.: dirección, nombre del paraje"
            helperText="Opcional. Para polígono o punto GPS usá el icono de mapa en la tabla o el enlace de abajo."
          />
          {estEditando && (
            <Button
              variant="text"
              size="small"
              sx={{ mt: 1, display: 'block' }}
              onClick={() => {
                setDialogoEst(false);
                navigate(`/avicola-crianza/establecimientos-mapa?id=${estEditando.id}`);
              }}
            >
              Abrir ubicación en mapa para este establecimiento
            </Button>
          )}
          <TextField
            margin="dense"
            label="Observaciones"
            fullWidth
            multiline
            minRows={2}
            value={estObs}
            onChange={(ev) => setEstObs(ev.target.value)}
          />
          <FormControlLabel
            control={<Switch checked={estActivo} onChange={(_, c) => setEstActivo(c)} />}
            label="Activo"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoEst(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarEst()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default EstablecimientosCrianzaScreen;
