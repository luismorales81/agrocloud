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
  FeedlotCatalogo,
  FeedlotCorral,
  FeedlotDieta,
  FeedlotEstablecimiento,
  FeedlotProveedorOrigen,
  FeedlotTipoTenencia,
} from '../types';
import {
  actualizarLote,
  crearLote,
  listarCategorias,
  listarCorrales,
  listarDietas,
  listarEstablecimientos,
  listarProveedores,
  listarRazas,
  mensajeError,
  obtenerLote,
} from '../services/feedlotApi';

interface CorralOpcion extends FeedlotCorral {
  establecimientoNombre: string;
}

const TIPOS_TENENCIA: { valor: FeedlotTipoTenencia; etiqueta: string }[] = [
  { valor: 'PROPIO', etiqueta: 'Propio' },
  { valor: 'CONSIGNACION', etiqueta: 'Consignación (hotelería)' },
];

const FeedlotLoteFormScreen: React.FC = () => {
  const navigate = useNavigate();
  const { id: idParam } = useParams<{ id: string }>();
  const esEdicion = Boolean(idParam && window.location.pathname.includes('/editar'));
  const loteId = idParam ? parseInt(idParam, 10) : NaN;

  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [corrales, setCorrales] = useState<CorralOpcion[]>([]);
  const [categorias, setCategorias] = useState<FeedlotCatalogo[]>([]);
  const [razas, setRazas] = useState<FeedlotCatalogo[]>([]);
  const [proveedores, setProveedores] = useState<FeedlotProveedorOrigen[]>([]);
  const [dietas, setDietas] = useState<FeedlotDieta[]>([]);

  const [corralId, setCorralId] = useState<number | ''>('');
  const [nombre, setNombre] = useState('');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [razaId, setRazaId] = useState<number | ''>('');
  const [proveedorId, setProveedorId] = useState<number | ''>('');
  const [dietaId, setDietaId] = useState<number | ''>('');
  const [tipoTenencia, setTipoTenencia] = useState<FeedlotTipoTenencia>('PROPIO');
  const [fechaIngreso, setFechaIngreso] = useState(() => new Date().toISOString().slice(0, 10));
  const [cabezasInicial, setCabezasInicial] = useState('');
  const [pesoIngreso, setPesoIngreso] = useState('');
  const [precioCompraKg, setPrecioCompraKg] = useState('');
  const [costoHoteleriaDia, setCostoHoteleriaDia] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const corralesDisponibles = useMemo(() => {
    if (esEdicion) {
      return corrales;
    }
    return corrales.filter((c) => c.estado === 'DISPONIBLE' || c.estado == null);
  }, [corrales, esEdicion]);

  const cargarCatalogos = useCallback(async () => {
    const [ests, cats, razs, provs, dts] = await Promise.all([
      listarEstablecimientos(),
      listarCategorias(),
      listarRazas(),
      listarProveedores(),
      listarDietas(),
    ]);
    setCategorias(cats.filter((c) => c.activo !== false));
    setRazas(razs.filter((r) => r.activo !== false));
    setProveedores(provs.filter((p) => p.activo !== false));
    setDietas(dts.filter((d) => d.activo !== false));

    const opciones: CorralOpcion[] = [];
    for (const est of ests.filter((e: FeedlotEstablecimiento) => e.activo !== false)) {
      const lista = await listarCorrales(est.id);
      for (const corral of lista.filter((c) => c.activo !== false)) {
        opciones.push({ ...corral, establecimientoNombre: est.nombre });
      }
    }
    setCorrales(opciones);
  }, []);

  useEffect(() => {
    const init = async () => {
      try {
        setCargando(true);
        setError(null);
        await cargarCatalogos();
        if (esEdicion && !Number.isNaN(loteId)) {
          const lote = await obtenerLote(loteId);
          setCorralId(lote.corralId ?? '');
          setNombre(lote.nombre ?? '');
          setCategoriaId(lote.categoriaId ?? '');
          setRazaId(lote.razaId ?? '');
          setProveedorId(lote.proveedorId ?? '');
          setDietaId(lote.dietaId ?? '');
          setTipoTenencia((lote.tipoTenencia as FeedlotTipoTenencia) ?? 'PROPIO');
          setFechaIngreso(lote.fechaIngreso?.slice(0, 10) ?? fechaIngreso);
          setCabezasInicial(String(lote.cabezasInicial ?? ''));
          setPesoIngreso(
            lote.pesoPromedioIngresoKg != null ? String(lote.pesoPromedioIngresoKg) : ''
          );
          setPrecioCompraKg(lote.precioCompraKg != null ? String(lote.precioCompraKg) : '');
          setCostoHoteleriaDia(
            lote.costoHoteleriaDia != null ? String(lote.costoHoteleriaDia) : ''
          );
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
      if (corralId === '' || categoriaId === '' || razaId === '' || !nombre.trim()) {
        setError('Completá corral, nombre, categoría y raza.');
        return;
      }
      const cab = parseInt(cabezasInicial, 10);
      if (Number.isNaN(cab) || cab <= 0) {
        setError('Cabezas iniciales inválidas.');
        return;
      }
      const cuerpo = {
        corralId: Number(corralId),
        nombre: nombre.trim(),
        categoriaId: Number(categoriaId),
        razaId: Number(razaId),
        proveedorId: proveedorId === '' ? null : Number(proveedorId),
        dietaId: dietaId === '' ? null : Number(dietaId),
        tipoTenencia,
        fechaIngreso,
        cabezasInicial: cab,
        pesoPromedioIngresoKg:
          pesoIngreso.trim() === '' ? null : parseFloat(pesoIngreso.replace(',', '.')),
        precioCompraKg:
          tipoTenencia === 'PROPIO' && precioCompraKg.trim() !== ''
            ? parseFloat(precioCompraKg.replace(',', '.'))
            : null,
        costoHoteleriaDia:
          tipoTenencia === 'CONSIGNACION' && costoHoteleriaDia.trim() !== ''
            ? parseFloat(costoHoteleriaDia.replace(',', '.'))
            : null,
        observaciones: observaciones.trim() || null,
      };
      setGuardando(true);
      setError(null);
      if (esEdicion && !Number.isNaN(loteId)) {
        await actualizarLote(loteId, cuerpo);
        navigate(`/feedlot/lotes/${loteId}`);
      } else {
        const creado = await crearLote(cuerpo);
        navigate(`/feedlot/lotes/${creado.id}`);
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
        <Button sx={{ mt: 2 }} onClick={() => navigate('/feedlot/lotes')}>
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
              label="Corral"
              fullWidth
              required
              value={corralId}
              onChange={(e) => setCorralId(e.target.value === '' ? '' : Number(e.target.value))}
              helperText={
                corralesDisponibles.length === 0
                  ? 'No hay corrales disponibles. Creá establecimientos y corrales primero.'
                  : 'Un corral aloja un lote activo a la vez.'
              }
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {corralesDisponibles.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.establecimientoNombre} — {c.nombre}
                  {c.estado ? ` (${c.estado})` : ''}
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
                label="Categoría"
                fullWidth
                required
                value={categoriaId}
                onChange={(e) => setCategoriaId(e.target.value === '' ? '' : Number(e.target.value))}
              >
                <MenuItem value="">Seleccionar…</MenuItem>
                {categorias.map((c) => (
                  <MenuItem key={c.id} value={c.id}>
                    {c.nombre}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Raza"
                fullWidth
                required
                value={razaId}
                onChange={(e) => setRazaId(e.target.value === '' ? '' : Number(e.target.value))}
              >
                <MenuItem value="">Seleccionar…</MenuItem>
                {razas.map((r) => (
                  <MenuItem key={r.id} value={r.id}>
                    {r.nombre}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>

            <TextField
              select
              label="Tipo de tenencia"
              fullWidth
              value={tipoTenencia}
              onChange={(e) => setTipoTenencia(e.target.value as FeedlotTipoTenencia)}
            >
              {TIPOS_TENENCIA.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>
                  {t.etiqueta}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              select
              label="Proveedor / origen (opcional)"
              fullWidth
              value={proveedorId}
              onChange={(e) => setProveedorId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">Ninguno</MenuItem>
              {proveedores.map((p) => (
                <MenuItem key={p.id} value={p.id}>
                  {p.nombre} {p.tipo ? `(${p.tipo})` : ''}
                </MenuItem>
              ))}
            </TextField>

            <TextField
              select
              label="Dieta (opcional)"
              fullWidth
              value={dietaId}
              onChange={(e) => setDietaId(e.target.value === '' ? '' : Number(e.target.value))}
              helperText="Para consumo teórico vs real en el detalle del lote"
            >
              <MenuItem value="">Sin dieta asignada</MenuItem>
              {dietas.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  {d.nombre}
                </MenuItem>
              ))}
            </TextField>

            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <TextField
                type="date"
                label="Fecha de ingreso"
                InputLabelProps={{ shrink: true }}
                fullWidth
                value={fechaIngreso}
                onChange={(e) => setFechaIngreso(e.target.value)}
              />
              <TextField
                label="Cabezas iniciales"
                type="number"
                fullWidth
                required
                value={cabezasInicial}
                onChange={(e) => setCabezasInicial(e.target.value)}
                inputProps={{ min: 1 }}
                disabled={esEdicion}
                helperText={esEdicion ? 'No editable en lote existente' : undefined}
              />
            </Stack>

            <TextField
              label="Peso promedio ingreso (kg)"
              fullWidth
              value={pesoIngreso}
              onChange={(e) => setPesoIngreso(e.target.value)}
            />

            {tipoTenencia === 'PROPIO' && (
              <TextField
                label="Precio compra ($/kg live, opcional)"
                fullWidth
                value={precioCompraKg}
                onChange={(e) => setPrecioCompraKg(e.target.value)}
              />
            )}

            {tipoTenencia === 'CONSIGNACION' && (
              <TextField
                label="Costo hotelería ($/cab/día, opcional)"
                fullWidth
                value={costoHoteleriaDia}
                onChange={(e) => setCostoHoteleriaDia(e.target.value)}
                helperText="Tarifa diaria de engorde para animales de terceros"
              />
            )}

            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
            />

            <Stack direction="row" spacing={1} justifyContent="flex-end">
              <Button onClick={() => navigate('/feedlot/lotes')}>Cancelar</Button>
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

export default FeedlotLoteFormScreen;
