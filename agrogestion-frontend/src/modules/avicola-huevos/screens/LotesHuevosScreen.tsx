import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
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
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Stack,
  IconButton,
} from '@mui/material';
import { Pencil } from 'lucide-react';
import {
  listarLotesHuevos,
  listarEstablecimientosHuevos,
  listarRazasHuevos,
  crearLoteHuevos,
  actualizarLoteHuevos,
  cerrarLoteHuevos,
  AvicolaHuevoLoteRespuesta,
  AvicolaHuevoEstablecimientoRespuesta,
  AvicolaHuevoRazaRespuesta,
  mensajeError,
} from '../services/avicolaHuevosApi';
import FiltroDelPeriodoActivo from '../../../components/FiltroDelPeriodoActivo';
import { useCampana } from '../../../contexts/CampanaContext';

const LotesHuevosScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [lotes, setLotes] = useState<AvicolaHuevoLoteRespuesta[]>([]);
  const [establecimientos, setEstablecimientos] = useState<AvicolaHuevoEstablecimientoRespuesta[]>([]);
  const [razas, setRazas] = useState<AvicolaHuevoRazaRespuesta[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogo, setDialogo] = useState(false);
  const [loteEditando, setLoteEditando] = useState<AvicolaHuevoLoteRespuesta | null>(null);
  const [cerrandoId, setCerrandoId] = useState<number | null>(null);

  const [establecimientoId, setEstablecimientoId] = useState<number | ''>('');
  const [razaId, setRazaId] = useState<number | ''>('');
  const [nombre, setNombre] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [cantidadInicial, setCantidadInicial] = useState('');
  const [cantidadActual, setCantidadActual] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [filtroEstado, setFiltroEstado] = useState<'ACTIVO' | 'CERRADO' | ''>('ACTIVO');
  const [filtroDelPeriodo, setFiltroDelPeriodo] = useState(false);

  useEffect(() => {
    if (filtroEstado !== 'ACTIVO') {
      setFiltroDelPeriodo(true);
    }
  }, [filtroEstado]);

  const establecimientosActivos = establecimientos.filter((e) => e.activo !== false);
  const razasActivas = razas.filter((r) => r.activo !== false);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const [l, e, r] = await Promise.all([
        listarLotesHuevos({
          estado: filtroEstado || undefined,
          delPeriodoActivo: filtroDelPeriodo,
        }),
        listarEstablecimientosHuevos(),
        listarRazasHuevos(),
      ]);
      setLotes(l);
      setEstablecimientos(e);
      setRazas(r);
      setError(null);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [filtroEstado, filtroDelPeriodo]);

  useEffect(() => {
    cargar();
  }, [cargar, campanaActiva?.id]);

  const abrirNuevo = () => {
    setLoteEditando(null);
    setEstablecimientoId('');
    setRazaId('');
    setNombre('');
    setFechaInicio(new Date().toISOString().slice(0, 10));
    setCantidadInicial('');
    setCantidadActual('');
    setObservaciones('');
    setDialogo(true);
  };

  const abrirEditar = (l: AvicolaHuevoLoteRespuesta) => {
    if (l.estado === 'CERRADO') return;
    setLoteEditando(l);
    setEstablecimientoId(l.establecimientoId ?? '');
    setRazaId(l.razaId ?? '');
    setNombre(l.nombre);
    setFechaInicio(l.fechaInicio?.slice(0, 10) || '');
    setCantidadInicial(String(l.cantidadAvesInicial ?? ''));
    setCantidadActual(String(l.cantidadAvesActual ?? ''));
    setObservaciones(l.observaciones || '');
    setDialogo(true);
  };

  const guardar = async () => {
    try {
      if (!nombre.trim()) {
        setError('El nombre del lote es obligatorio.');
        return;
      }
      if (establecimientoId === '' || razaId === '') {
        setError('Seleccioná establecimiento y raza.');
        return;
      }
      if (loteEditando) {
        const ca = cantidadActual.trim() === '' ? undefined : parseInt(cantidadActual, 10);
        if (ca !== undefined && (Number.isNaN(ca) || ca < 0)) {
          setError('Cantidad actual de aves inválida.');
          return;
        }
        await actualizarLoteHuevos(loteEditando.id, {
          establecimientoId: Number(establecimientoId),
          razaId: Number(razaId),
          nombre: nombre.trim(),
          cantidadAvesActual: ca,
          observaciones: observaciones.trim() || null,
        });
      } else {
        const ci = parseInt(cantidadInicial, 10);
        if (Number.isNaN(ci) || ci < 0) {
          setError('Cantidad inicial inválida.');
          return;
        }
        const ca = cantidadActual.trim() === '' ? undefined : parseInt(cantidadActual, 10);
        if (ca !== undefined && (Number.isNaN(ca) || ca < 0)) {
          setError('Cantidad actual inválida.');
          return;
        }
        await crearLoteHuevos({
          establecimientoId: Number(establecimientoId),
          razaId: Number(razaId),
          nombre: nombre.trim(),
          fechaInicio,
          cantidadAvesInicial: ci,
          cantidadAvesActual: ca,
          observaciones: observaciones.trim() || null,
        });
      }
      setDialogo(false);
      setError(null);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const cerrar = async (id: number) => {
    if (!window.confirm('¿Cerrar este lote de postura? No podrás registrar producción ni consumos en un lote cerrado.')) {
      return;
    }
    try {
      setCerrandoId(id);
      await cerrarLoteHuevos(id);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCerrandoId(null);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2} flexWrap="wrap" gap={1}>
        <Typography variant="h4">Lotes de postura</Typography>
        <Box display="flex" gap={1}>
          <Button variant="contained" onClick={abrirNuevo} disabled={cargando}>
            Nuevo lote
          </Button>
          <Button variant="outlined" onClick={cargar} disabled={cargando}>
            Actualizar
          </Button>
        </Box>
      </Box>

      {cargando && (
        <Box display="flex" alignItems="center" gap={2} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap" sx={{ mb: 2 }}>
        <TextField
          select
          size="small"
          label="Estado"
          value={filtroEstado}
          onChange={(e) => setFiltroEstado(e.target.value as 'ACTIVO' | 'CERRADO' | '')}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="ACTIVO">Activos</MenuItem>
          <MenuItem value="CERRADO">Cerrados</MenuItem>
          <MenuItem value="">Todos</MenuItem>
        </TextField>
        <FiltroDelPeriodoActivo
          activo={filtroDelPeriodo}
          onChange={setFiltroDelPeriodo}
          visible={filtroEstado !== 'ACTIVO'}
        />
      </Stack>

      {!cargando && (
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell>Establecimiento</TableCell>
                <TableCell>Raza</TableCell>
                <TableCell align="right">Aves</TableCell>
                <TableCell>Inicio</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lotes.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7}>
                    <Typography color="text.secondary">No hay lotes cargados.</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                lotes.map((l) => (
                  <TableRow key={l.id} hover>
                    <TableCell>{l.nombre}</TableCell>
                    <TableCell>{l.establecimientoNombre ?? '—'}</TableCell>
                    <TableCell>{l.razaNombre ?? '—'}</TableCell>
                    <TableCell align="right">{l.cantidadAvesActual ?? '—'}</TableCell>
                    <TableCell>{l.fechaInicio}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={l.estado ?? '—'}
                        color={l.estado === 'ACTIVO' ? 'success' : 'default'}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Button size="small" variant="text" onClick={() => navigate(`/avicola-huevos/lotes/${l.id}`)}>
                        Ver
                      </Button>
                      {l.estado === 'ACTIVO' && (
                        <>
                          <IconButton size="small" onClick={() => abrirEditar(l)} aria-label="Editar lote">
                            <Pencil size={18} />
                          </IconButton>
                          <Button
                            size="small"
                            color="warning"
                            onClick={() => cerrar(l.id)}
                            disabled={cerrandoId === l.id}
                          >
                            Cerrar
                          </Button>
                        </>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} fullWidth maxWidth="sm">
        <DialogTitle>{loteEditando ? 'Editar lote' : 'Nuevo lote'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {(establecimientosActivos.length === 0 || razasActivas.length === 0) && (
              <Alert severity="info">
                {establecimientosActivos.length === 0 && (
                  <Typography variant="body2" display="block">
                    No hay establecimientos activos. Creá uno en «Establecimientos y razas» antes de abrir un lote.
                  </Typography>
                )}
                {razasActivas.length === 0 && (
                  <Typography variant="body2" display="block">
                    No hay razas activas. Cargá al menos una raza en el catálogo.
                  </Typography>
                )}
              </Alert>
            )}
            <TextField
              select
              label="Establecimiento"
              fullWidth
              required
              value={establecimientoId}
              onChange={(ev) => setEstablecimientoId(ev.target.value === '' ? '' : Number(ev.target.value))}
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {establecimientosActivos.map((e) => (
                <MenuItem key={e.id} value={e.id}>
                  {e.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Raza / línea"
              fullWidth
              required
              value={razaId}
              onChange={(ev) => setRazaId(ev.target.value === '' ? '' : Number(ev.target.value))}
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {razasActivas.map((r) => (
                <MenuItem key={r.id} value={r.id}>
                  {r.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Nombre del lote" fullWidth value={nombre} onChange={(ev) => setNombre(ev.target.value)} />
            {!loteEditando && (
              <TextField
                label="Fecha de inicio"
                type="date"
                fullWidth
                InputLabelProps={{ shrink: true }}
                value={fechaInicio}
                onChange={(ev) => setFechaInicio(ev.target.value)}
              />
            )}
            {!loteEditando && (
              <TextField
                label="Cantidad inicial de aves"
                type="number"
                fullWidth
                value={cantidadInicial}
                onChange={(ev) => setCantidadInicial(ev.target.value)}
                inputProps={{ min: 0 }}
              />
            )}
            <TextField
              label={loteEditando ? 'Cantidad actual de aves' : 'Cantidad actual (opcional)'}
              type="number"
              fullWidth
              value={cantidadActual}
              onChange={(ev) => setCantidadActual(ev.target.value)}
              helperText={
                loteEditando
                  ? 'Actualizá el plantel si hubo bajas o ingresos autorizados.'
                  : 'Si no informás, se usa la cantidad inicial'
              }
              inputProps={{ min: 0 }}
            />
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={observaciones}
              onChange={(ev) => setObservaciones(ev.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={guardar}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default LotesHuevosScreen;
