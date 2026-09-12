import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
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
import { Pencil } from 'lucide-react';
import { Icon } from '../../../components/icons';
import type { AvicolaPonedorasGalpon, GalponEstado } from '../types';
import {
  actualizarGalpon,
  crearGalpon,
  listarGalpones,
  mensajeErrorPonedoras,
} from '../services/avicolaPonedorasService';
import {
  listarEstablecimientosCrianza,
  type AvicolaEstablecimientoRespuesta,
} from '../../avicola-crianza/services/avicolaCrianzaApi';
import { etiquetaEstadoGalpon, formatearFechaCorta } from '../util/formateo';
import FiltroDelPeriodoActivo from '../../../components/FiltroDelPeriodoActivo';
import { useCampana } from '../../../contexts/CampanaContext';

const AvicolaPonedorasListado: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [errorDialogo, setErrorDialogo] = useState<string | null>(null);
  const [galpones, setGalpones] = useState<AvicolaPonedorasGalpon[]>([]);
  const [establecimientos, setEstablecimientos] = useState<AvicolaEstablecimientoRespuesta[]>([]);
  const [filtroEstado, setFiltroEstado] = useState<'ACTIVO' | 'CERRADO' | ''>('ACTIVO');
  const [filtroDelPeriodo, setFiltroDelPeriodo] = useState(false);

  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [galponEditando, setGalponEditando] = useState<AvicolaPonedorasGalpon | null>(null);
  const [establecimientoId, setEstablecimientoId] = useState<number | ''>('');
  const [nombre, setNombre] = useState('');
  const [raza, setRaza] = useState('');
  const [fechaIngreso, setFechaIngreso] = useState('');
  const [cantidadInicial, setCantidadInicial] = useState('');
  const [cantidadAves, setCantidadAves] = useState('');
  const [estadoGalpon, setEstadoGalpon] = useState<GalponEstado>('ACTIVO');
  const [fechaCierre, setFechaCierre] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [guardando, setGuardando] = useState(false);

  const establecimientosActivos = establecimientos.filter((e) => e.activo !== false);

  useEffect(() => {
    if (filtroEstado !== 'ACTIVO') {
      setFiltroDelPeriodo(true);
    }
  }, [filtroEstado]);

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [lista, est] = await Promise.all([
        listarGalpones({
          estado: filtroEstado || undefined,
          delPeriodoActivo: filtroDelPeriodo,
        }),
        listarEstablecimientosCrianza(),
      ]);
      setGalpones(lista);
      setEstablecimientos(est);
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    } finally {
      setCargando(false);
    }
  }, [filtroEstado, filtroDelPeriodo]);

  useEffect(() => {
    void cargar();
  }, [cargar, campanaActiva?.id]);

  const abrirNuevo = () => {
    setGalponEditando(null);
    setEstablecimientoId('');
    setNombre('');
    setRaza('');
    setFechaIngreso(new Date().toISOString().slice(0, 10));
    setCantidadInicial('');
    setCantidadAves('');
    setEstadoGalpon('ACTIVO');
    setFechaCierre('');
    setObservaciones('');
    setErrorDialogo(null);
    setDialogoAbierto(true);
  };

  const abrirEditar = (g: AvicolaPonedorasGalpon) => {
    setGalponEditando(g);
    setEstablecimientoId(g.establecimientoId ?? '');
    setNombre(g.nombre);
    setRaza(g.raza ?? '');
    setFechaIngreso(g.fechaIngreso?.slice(0, 10) || '');
    setCantidadInicial(g.cantidadInicial != null ? String(g.cantidadInicial) : '');
    setCantidadAves(g.cantidadAves != null ? String(g.cantidadAves) : '');
    setEstadoGalpon((g.estado as GalponEstado) || 'ACTIVO');
    setFechaCierre(g.fechaCierre?.slice(0, 10) || '');
    setObservaciones(g.observaciones ?? '');
    setErrorDialogo(null);
    setDialogoAbierto(true);
  };

  const cerrarDialogo = () => {
    setDialogoAbierto(false);
    setGalponEditando(null);
  };

  const guardarGalpon = async () => {
    if (!nombre.trim()) {
      setErrorDialogo('El nombre del galpón es obligatorio.');
      return;
    }
    if (establecimientoId === '') {
      setErrorDialogo('Seleccioná un establecimiento.');
      return;
    }
    if (!fechaIngreso) {
      setErrorDialogo('La fecha de ingreso es obligatoria.');
      return;
    }

    setGuardando(true);
    setErrorDialogo(null);
    try {
      if (galponEditando) {
        const aves = cantidadAves.trim() === '' ? undefined : parseInt(cantidadAves, 10);
        if (aves !== undefined && (Number.isNaN(aves) || aves < 0)) {
          setErrorDialogo('Cantidad de aves inválida.');
          setGuardando(false);
          return;
        }
        await actualizarGalpon(galponEditando.id, {
          establecimientoId: Number(establecimientoId),
          nombre: nombre.trim(),
          raza: raza.trim() || null,
          fechaIngreso,
          cantidadAves: aves ?? null,
          estado: estadoGalpon,
          fechaCierre: fechaCierre || null,
          observaciones: observaciones.trim() || null,
        });
      } else {
        const inicial = parseInt(cantidadInicial, 10);
        if (Number.isNaN(inicial) || inicial <= 0) {
          setErrorDialogo('La cantidad inicial debe ser mayor a cero.');
          setGuardando(false);
          return;
        }
        const aves = cantidadAves.trim() === '' ? inicial : parseInt(cantidadAves, 10);
        if (Number.isNaN(aves) || aves < 0) {
          setErrorDialogo('Cantidad de aves inválida.');
          setGuardando(false);
          return;
        }
        await crearGalpon({
          establecimientoId: Number(establecimientoId),
          nombre: nombre.trim(),
          raza: raza.trim() || null,
          fechaIngreso,
          cantidadInicial: inicial,
          cantidadAves: aves,
          observaciones: observaciones.trim() || null,
        });
      }
      cerrarDialogo();
      await cargar();
    } catch (err: unknown) {
      setErrorDialogo(mensajeErrorPonedoras(err));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="List" size={28} />
        <Typography variant="h4" component="h1">
          Galpones
        </Typography>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Listado de galpones de la empresa (entidad central del módulo ponedoras, distinta del lote en carne).
      </Typography>

      <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }} alignItems="center">
        <Button variant="contained" size="small" onClick={abrirNuevo}>
          Nuevo galpón
        </Button>
        <Button variant="outlined" size="small" onClick={() => navigate('/avicola-ponedoras/panel')}>
          Volver al resumen
        </Button>
        <Button variant="text" size="small" onClick={() => void cargar()} disabled={cargando}>
          Actualizar
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando galpones…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ my: 2 }} onClose={() => setError(null)}>
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

      {!cargando && !error && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="medium">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell>Establecimiento</TableCell>
                <TableCell>Raza</TableCell>
                <TableCell>Fecha ingreso</TableCell>
                <TableCell align="right">Aves</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {galpones.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7}>
                    <Typography color="text.secondary" variant="body2">
                      No hay galpones registrados. Usá «Nuevo galpón» para crear uno.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                galpones.map((g) => (
                  <TableRow key={g.id} hover>
                    <TableCell>{g.nombre}</TableCell>
                    <TableCell>{g.establecimientoNombre?.trim() ? g.establecimientoNombre : '—'}</TableCell>
                    <TableCell>{g.raza?.trim() ? g.raza : '—'}</TableCell>
                    <TableCell>{formatearFechaCorta(g.fechaIngreso)}</TableCell>
                    <TableCell align="right">{g.cantidadAves ?? '—'}</TableCell>
                    <TableCell>{etiquetaEstadoGalpon(g.estado ?? undefined)}</TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end" alignItems="center">
                        <IconButton
                          size="small"
                          aria-label="Editar galpón"
                          onClick={() => abrirEditar(g)}
                          disabled={g.estado === 'CERRADO'}
                        >
                          <Pencil size={16} />
                        </IconButton>
                        <Button size="small" variant="text" onClick={() => navigate(`/avicola-ponedoras/galpones/${g.id}`)}>
                          Abrir
                        </Button>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogoAbierto} onClose={cerrarDialogo} maxWidth="sm" fullWidth>
        <DialogTitle>{galponEditando ? 'Editar galpón' : 'Nuevo galpón'}</DialogTitle>
        <DialogContent>
          {errorDialogo && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErrorDialogo(null)}>
              {errorDialogo}
            </Alert>
          )}
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              select
              label="Establecimiento"
              value={establecimientoId}
              onChange={(e) => setEstablecimientoId(e.target.value === '' ? '' : Number(e.target.value))}
              required
              fullWidth
              size="small"
              helperText={
                establecimientosActivos.length === 0
                  ? 'Creá establecimientos en Avícola crianza primero.'
                  : undefined
              }
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {establecimientosActivos.map((e) => (
                <MenuItem key={e.id} value={e.id}>
                  {e.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Nombre"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
              fullWidth
              size="small"
            />
            <TextField
              label="Raza"
              value={raza}
              onChange={(e) => setRaza(e.target.value)}
              fullWidth
              size="small"
            />
            <TextField
              label="Fecha de ingreso"
              type="date"
              value={fechaIngreso}
              onChange={(e) => setFechaIngreso(e.target.value)}
              required
              fullWidth
              size="small"
              InputLabelProps={{ shrink: true }}
            />
            {!galponEditando && (
              <TextField
                label="Cantidad inicial"
                type="number"
                value={cantidadInicial}
                onChange={(e) => setCantidadInicial(e.target.value)}
                required
                fullWidth
                size="small"
                inputProps={{ min: 1 }}
              />
            )}
            <TextField
              label="Cantidad de aves actual"
              type="number"
              value={cantidadAves}
              onChange={(e) => setCantidadAves(e.target.value)}
              fullWidth
              size="small"
              inputProps={{ min: 0 }}
              helperText={galponEditando ? undefined : 'Si se deja vacío, se usa la cantidad inicial.'}
            />
            {galponEditando && (
              <>
                <TextField
                  select
                  label="Estado"
                  value={estadoGalpon}
                  onChange={(e) => setEstadoGalpon(e.target.value as GalponEstado)}
                  fullWidth
                  size="small"
                >
                  <MenuItem value="ACTIVO">Activo</MenuItem>
                  <MenuItem value="CERRADO">Cerrado</MenuItem>
                </TextField>
                {estadoGalpon === 'CERRADO' && (
                  <TextField
                    label="Fecha de cierre"
                    type="date"
                    value={fechaCierre}
                    onChange={(e) => setFechaCierre(e.target.value)}
                    fullWidth
                    size="small"
                    InputLabelProps={{ shrink: true }}
                  />
                )}
              </>
            )}
            <TextField
              label="Observaciones"
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              fullWidth
              size="small"
              multiline
              minRows={2}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarDialogo} disabled={guardando}>
            Cancelar
          </Button>
          <Button
            variant="contained"
            onClick={() => void guardarGalpon()}
            disabled={guardando || !nombre.trim() || establecimientoId === ''}
          >
            {guardando ? 'Guardando…' : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AvicolaPonedorasListado;
