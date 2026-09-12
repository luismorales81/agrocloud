import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type {
  PorcinosDieta,
  PorcinosEstablecimiento,
  PorcinosGalpon,
  PorcinosLoteEtapa,
  PorcinosLoteOrigen,
} from '../typesApiV2';
import {
  actualizarLote,
  crearLote,
  listarDietas,
  listarEstablecimientos,
  listarGalpones,
  mensajeError,
  obtenerLote,
} from '../services/porcinosApi';

interface GalponOpcion extends PorcinosGalpon {
  establecimientoNombre: string;
}

const ORIGENES: { valor: PorcinosLoteOrigen; etiqueta: string }[] = [
  { valor: 'EXTERNO', etiqueta: 'Ingreso externo' },
  { valor: 'DESTETE', etiqueta: 'Desde destete' },
];

const ETAPAS: { valor: PorcinosLoteEtapa; etiqueta: string }[] = [
  { valor: 'RECRIA', etiqueta: 'Recría' },
  { valor: 'ENGORDE', etiqueta: 'Engorde' },
];

const PorcinosLoteFormScreen: React.FC = () => {
  const navigate = useNavigate();
  const { id: idParam } = useParams<{ id: string }>();
  const esEdicion = Boolean(idParam && window.location.pathname.includes('/editar'));
  const loteId = idParam ? parseInt(idParam, 10) : NaN;

  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [galpones, setGalpones] = useState<GalponOpcion[]>([]);
  const [dietas, setDietas] = useState<PorcinosDieta[]>([]);

  const [galponId, setGalponId] = useState<number | ''>('');
  const [nombre, setNombre] = useState('');
  const [origen, setOrigen] = useState<PorcinosLoteOrigen>('EXTERNO');
  const [etapa, setEtapa] = useState<PorcinosLoteEtapa>('ENGORDE');
  const [fechaIngreso, setFechaIngreso] = useState(() => new Date().toISOString().slice(0, 10));
  const [cabezasInicial, setCabezasInicial] = useState('');
  const [pesoIngreso, setPesoIngreso] = useState('');
  const [dietaId, setDietaId] = useState<number | ''>('');
  const [observaciones, setObservaciones] = useState('');

  const galponesDisponibles = useMemo(() => {
    if (esEdicion) return galpones;
    return galpones.filter((g) => g.estado === 'DISPONIBLE' || g.estado == null);
  }, [galpones, esEdicion]);

  const cargarCatalogos = useCallback(async () => {
    const [ests, dts] = await Promise.all([listarEstablecimientos(), listarDietas()]);
    setDietas(dts.filter((d) => d.activo !== false));
    const opciones: GalponOpcion[] = [];
    for (const est of ests.filter((e: PorcinosEstablecimiento) => e.activo !== false)) {
      const lista = await listarGalpones(est.id);
      for (const galpon of lista.filter((g) => g.activo !== false)) {
        opciones.push({ ...galpon, establecimientoNombre: est.nombre });
      }
    }
    setGalpones(opciones);
  }, []);

  useEffect(() => {
    const init = async () => {
      try {
        setCargando(true);
        setError(null);
        await cargarCatalogos();
        if (esEdicion && !Number.isNaN(loteId)) {
          const lote = await obtenerLote(loteId);
          setGalponId(lote.galponId ?? '');
          setNombre(lote.nombre ?? '');
          setOrigen((lote.origen as PorcinosLoteOrigen) ?? 'EXTERNO');
          setEtapa((lote.etapa as PorcinosLoteEtapa) ?? 'ENGORDE');
          setFechaIngreso(lote.fechaIngreso?.slice(0, 10) ?? fechaIngreso);
          setCabezasInicial(String(lote.cabezasInicial ?? ''));
          setPesoIngreso(
            lote.pesoPromedioIngresoKg != null ? String(lote.pesoPromedioIngresoKg) : ''
          );
          setDietaId(lote.dietaId ?? '');
          setObservaciones(lote.observaciones ?? '');
        }
      } catch (err: unknown) {
        setError(mensajeError(err));
      } finally {
        setCargando(false);
      }
    };
    void init();
  }, [cargarCatalogos, esEdicion, loteId, fechaIngreso]);

  const guardar = async () => {
    try {
      if (galponId === '' || !nombre.trim()) {
        setError('Completá galpón y nombre del lote.');
        return;
      }
      const cab = parseInt(cabezasInicial, 10);
      if (Number.isNaN(cab) || cab <= 0) {
        setError('Cabezas iniciales inválidas.');
        return;
      }
      const cuerpo = {
        galponId: Number(galponId),
        nombre: nombre.trim(),
        origen,
        etapa,
        fechaIngreso,
        cabezasInicial: cab,
        pesoPromedioIngresoKg:
          pesoIngreso.trim() === '' ? null : parseFloat(pesoIngreso.replace(',', '.')),
        dietaId: dietaId === '' ? null : Number(dietaId),
        observaciones: observaciones.trim() || null,
      };
      setGuardando(true);
      setError(null);
      if (esEdicion && !Number.isNaN(loteId)) {
        await actualizarLote(loteId, cuerpo);
        navigate(`/porcinos/lotes/${loteId}`);
      } else {
        const creado = await crearLote(cuerpo);
        navigate(`/porcinos/lotes/${creado.id}`);
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  if (Number.isNaN(loteId) && esEdicion) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Identificador de lote inválido</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/porcinos/lotes')}>
          Volver
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 720 }}>
      <Typography variant="h4" gutterBottom>
        {esEdicion ? 'Editar lote de engorde' : 'Nuevo lote de engorde'}
      </Typography>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={1} sx={{ my: 2 }}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando formulario…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <Paper sx={{ p: 3 }} variant="outlined">
          <Stack spacing={2}>
            <TextField
              select
              label="Galpón"
              fullWidth
              required
              value={galponId}
              onChange={(e) => setGalponId(e.target.value === '' ? '' : Number(e.target.value))}
              helperText={
                galponesDisponibles.length === 0
                  ? 'No hay galpones disponibles. Creá establecimientos y galpones primero.'
                  : 'Un galpón aloja un lote activo a la vez.'
              }
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {galponesDisponibles.map((g) => (
                <MenuItem key={g.id} value={g.id}>
                  {g.establecimientoNombre} — {g.nombre}
                  {g.estado ? ` (${g.estado})` : ''}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              label="Nombre del lote"
              fullWidth
              required
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
            />

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                select
                label="Origen"
                fullWidth
                value={origen}
                onChange={(e) => setOrigen(e.target.value as PorcinosLoteOrigen)}
              >
                {ORIGENES.map((o) => (
                  <MenuItem key={o.valor} value={o.valor}>
                    {o.etiqueta}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Etapa"
                fullWidth
                value={etapa}
                onChange={(e) => setEtapa(e.target.value as PorcinosLoteEtapa)}
              >
                {ETAPAS.map((e) => (
                  <MenuItem key={e.valor} value={e.valor}>
                    {e.etiqueta}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                type="date"
                label="Fecha de ingreso"
                fullWidth
                InputLabelProps={{ shrink: true }}
                value={fechaIngreso}
                onChange={(e) => setFechaIngreso(e.target.value)}
              />
              <TextField
                label="Cabezas iniciales"
                fullWidth
                required
                type="number"
                value={cabezasInicial}
                onChange={(e) => setCabezasInicial(e.target.value)}
              />
            </Stack>

            <TextField
              label="Peso promedio ingreso (kg)"
              fullWidth
              type="number"
              value={pesoIngreso}
              onChange={(e) => setPesoIngreso(e.target.value)}
            />

            <TextField
              select
              label="Dieta (opcional)"
              fullWidth
              value={dietaId}
              onChange={(e) => setDietaId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">Sin dieta asignada</MenuItem>
              {dietas.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  {d.nombre}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
            />

            <Stack direction="row" spacing={1} justifyContent="flex-end">
              <Button onClick={() => navigate('/porcinos/lotes')}>Cancelar</Button>
              <Button variant="contained" onClick={() => void guardar()} disabled={guardando}>
                {guardando ? 'Guardando…' : 'Guardar'}
              </Button>
            </Stack>
          </Stack>
        </Paper>
      )}
    </Box>
  );
};

export default PorcinosLoteFormScreen;
