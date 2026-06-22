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
  listarLotesCrianza,
  listarEstablecimientosCrianza,
  listarRazasCrianza,
  crearLoteCrianza,
  actualizarLoteCrianza,
  AvicolaLoteRespuesta,
  AvicolaEstablecimientoRespuesta,
  AvicolaRazaRespuesta,
  EspecieCrianza,
  mensajeError,
} from '../services/avicolaCrianzaApi';
import FiltroDelPeriodoActivo from '../../../components/FiltroDelPeriodoActivo';
import { useCampana } from '../../../contexts/CampanaContext';

const ETIQUETA_ESPECIE: Record<EspecieCrianza, string> = {
  POLLO_PARRILLERO: 'Pollo parrillero',
  GALLINA_PONEDORA: 'Gallina ponedora',
  PAVO: 'Pavo',
  OTRO: 'Otro',
};

const ESPECIES: EspecieCrianza[] = ['POLLO_PARRILLERO', 'GALLINA_PONEDORA', 'PAVO', 'OTRO'];

const LotesCrianzaScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [lotes, setLotes] = useState<AvicolaLoteRespuesta[]>([]);
  const [establecimientos, setEstablecimientos] = useState<AvicolaEstablecimientoRespuesta[]>([]);
  const [razas, setRazas] = useState<AvicolaRazaRespuesta[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogo, setDialogo] = useState(false);
  const [loteEditando, setLoteEditando] = useState<AvicolaLoteRespuesta | null>(null);

  const [establecimientoId, setEstablecimientoId] = useState<number | ''>('');
  const [razaId, setRazaId] = useState<number | ''>('');
  const [nombre, setNombre] = useState('');
  const [especie, setEspecie] = useState<EspecieCrianza>('POLLO_PARRILLERO');
  const [origen, setOrigen] = useState('EXTERNO');
  const [fechaIngreso, setFechaIngreso] = useState('');
  const [cantidadInicial, setCantidadInicial] = useState('');
  const [cantidadAnimales, setCantidadAnimales] = useState('');
  const [pesoPromedio, setPesoPromedio] = useState('');
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
        listarLotesCrianza({
          estado: filtroEstado || undefined,
          delPeriodoActivo: filtroDelPeriodo,
        }),
        listarEstablecimientosCrianza(),
        listarRazasCrianza(),
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
    setEspecie('POLLO_PARRILLERO');
    setOrigen('EXTERNO');
    setFechaIngreso(new Date().toISOString().slice(0, 10));
    setCantidadInicial('');
    setCantidadAnimales('');
    setPesoPromedio('');
    setObservaciones('');
    setDialogo(true);
  };

  const abrirEditar = (l: AvicolaLoteRespuesta) => {
    if (l.estado === 'CERRADO') return;
    setLoteEditando(l);
    setEstablecimientoId(l.establecimientoId ?? '');
    setRazaId(l.razaId ?? '');
    setNombre(l.nombre);
    setEspecie((l.especie as EspecieCrianza) || 'POLLO_PARRILLERO');
    setOrigen(l.origen || 'EXTERNO');
    setFechaIngreso(l.fechaIngreso?.slice(0, 10) || '');
    setCantidadInicial(String(l.cantidadInicial ?? ''));
    setCantidadAnimales(String(l.cantidadAnimales ?? ''));
    setPesoPromedio(l.pesoPromedioIngreso != null ? String(l.pesoPromedioIngreso) : '');
    setObservaciones(l.observaciones || '');
    setDialogo(true);
  };

  const guardar = async () => {
    try {
      if (!nombre.trim()) {
        setError('El nombre del lote es obligatorio.');
        return;
      }
      if (loteEditando) {
        if (establecimientoId === '' || razaId === '') {
          setError('Seleccioná establecimiento y raza.');
          return;
        }
        await actualizarLoteCrianza(loteEditando.id, {
          establecimientoId: Number(establecimientoId),
          razaId: Number(razaId),
          nombre: nombre.trim(),
          especie,
          origen: origen.trim() || undefined,
          observaciones: observaciones.trim() || null,
        });
      } else {
        if (establecimientoId === '' || razaId === '') {
          setError('Seleccioná establecimiento y raza.');
          return;
        }
        const ci = parseInt(cantidadInicial, 10);
        if (Number.isNaN(ci) || ci < 0) {
          setError('Cantidad inicial inválida.');
          return;
        }
        const ca = cantidadAnimales.trim() === '' ? undefined : parseInt(cantidadAnimales, 10);
        if (ca !== undefined && (Number.isNaN(ca) || ca < 0)) {
          setError('Cantidad de animales inválida.');
          return;
        }
        const pp = pesoPromedio.trim() === '' ? undefined : parseFloat(pesoPromedio.replace(',', '.'));
        if (pp !== undefined && Number.isNaN(pp)) {
          setError('Peso promedio inválido.');
          return;
        }
        await crearLoteCrianza({
          establecimientoId: Number(establecimientoId),
          razaId: Number(razaId),
          nombre: nombre.trim(),
          especie,
          origen: origen.trim() || 'EXTERNO',
          fechaIngreso,
          cantidadInicial: ci,
          cantidadAnimales: ca,
          pesoPromedioIngreso: pp,
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

  return (
    <Box sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2} flexWrap="wrap" gap={1}>
        <Typography variant="h4">Lotes de crianza</Typography>
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
                <TableCell>Especie</TableCell>
                <TableCell align="right">Aves</TableCell>
                <TableCell>Ingreso</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {lotes.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8}>
                    <Typography color="text.secondary">No hay lotes cargados.</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                lotes.map((l) => (
                  <TableRow key={l.id} hover>
                    <TableCell>{l.nombre}</TableCell>
                    <TableCell>{l.establecimientoNombre ?? '—'}</TableCell>
                    <TableCell>{l.razaNombre ?? '—'}</TableCell>
                    <TableCell>
                      {l.especie ? ETIQUETA_ESPECIE[l.especie as EspecieCrianza] ?? l.especie : '—'}
                    </TableCell>
                    <TableCell align="right">{l.cantidadAnimales ?? '—'}</TableCell>
                    <TableCell>{l.fechaIngreso}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={l.estado ?? '—'}
                        color={l.estado === 'ACTIVO' ? 'success' : 'default'}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Button size="small" variant="text" onClick={() => navigate(`/avicola-crianza/lotes/${l.id}`)}>
                        Ver
                      </Button>
                      {l.estado !== 'CERRADO' && (
                        <IconButton size="small" onClick={() => abrirEditar(l)} aria-label="Editar lote">
                          <Pencil size={18} />
                        </IconButton>
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
              label="Raza"
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
            <TextField select label="Especie" fullWidth value={especie} onChange={(ev) => setEspecie(ev.target.value as EspecieCrianza)}>
              {ESPECIES.map((esp) => (
                <MenuItem key={esp} value={esp}>
                  {ETIQUETA_ESPECIE[esp]}
                </MenuItem>
              ))}
            </TextField>
            <TextField label="Origen" fullWidth value={origen} onChange={(ev) => setOrigen(ev.target.value)} helperText="Ej. EXTERNO, PROPIO" />
            {!loteEditando && (
              <TextField
                label="Fecha de ingreso"
                type="date"
                fullWidth
                InputLabelProps={{ shrink: true }}
                value={fechaIngreso}
                onChange={(ev) => setFechaIngreso(ev.target.value)}
              />
            )}
            {!loteEditando && (
              <TextField
                label="Cantidad inicial"
                type="number"
                fullWidth
                value={cantidadInicial}
                onChange={(ev) => setCantidadInicial(ev.target.value)}
                inputProps={{ min: 0 }}
              />
            )}
            {!loteEditando && (
              <TextField
                label="Cantidad de animales (opcional)"
                type="number"
                fullWidth
                value={cantidadAnimales}
                onChange={(ev) => setCantidadAnimales(ev.target.value)}
                helperText="Si no informás, se usa la cantidad inicial"
                inputProps={{ min: 0 }}
              />
            )}
            {!loteEditando && (
              <TextField
                label="Peso promedio al ingreso (kg, opcional)"
                type="number"
                fullWidth
                value={pesoPromedio}
                onChange={(ev) => setPesoPromedio(ev.target.value)}
                inputProps={{ step: '0.001', min: 0 }}
              />
            )}
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={observaciones}
              onChange={(ev) => setObservaciones(ev.target.value)}
            />
            {loteEditando && (
              <Typography variant="caption" color="text.secondary">
                La edición no cambia fechas ni cantidades iniciales desde esta pantalla; usá las operaciones del lote en
                la API si necesitás ajustes de stock.
              </Typography>
            )}
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

export default LotesCrianzaScreen;
