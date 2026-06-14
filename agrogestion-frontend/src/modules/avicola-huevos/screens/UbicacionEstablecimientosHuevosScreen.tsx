import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  type SelectChangeEvent,
  TextField,
  Typography,
} from '@mui/material';
import EditorMapaUbicacionHuevos from '../components/EditorMapaUbicacionHuevos';
import {
  listarEstablecimientosHuevos,
  actualizarEstablecimientoHuevos,
  AvicolaHuevoEstablecimientoRespuesta,
  mensajeError,
} from '../services/avicolaHuevosApi';

/**
 * Pantalla dedicada a mapa y coordenadas (equivalente a «Campos» en cultivos): una entrada de menú propia.
 */
const UbicacionEstablecimientosHuevosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [lista, setLista] = useState<AvicolaHuevoEstablecimientoRespuesta[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);

  const [seleccionId, setSeleccionId] = useState<number | ''>('');
  const [textoUbicacion, setTextoUbicacion] = useState('');
  const [coordenadasJson, setCoordenadasJson] = useState<string | null>(null);

  const onCoordenadasChange = useCallback((json: string | null) => {
    setCoordenadasJson(json);
  }, []);

  const cargarLista = useCallback(async () => {
    try {
      setCargando(true);
      const e = await listarEstablecimientosHuevos();
      setLista(e);
      setError(null);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargarLista();
  }, [cargarLista]);

  const idDesdeUrl = useMemo(() => {
    const raw = searchParams.get('id');
    if (raw == null || raw === '') return null;
    const n = parseInt(raw, 10);
    return Number.isNaN(n) ? null : n;
  }, [searchParams]);

  useEffect(() => {
    if (cargando || lista.length === 0) return;
    if (idDesdeUrl != null && lista.some((x) => x.id === idDesdeUrl)) {
      setSeleccionId(idDesdeUrl);
    }
  }, [cargando, lista, idDesdeUrl]);

  const seleccionado = useMemo(
    () => (seleccionId === '' ? null : lista.find((x) => x.id === seleccionId) ?? null),
    [lista, seleccionId]
  );

  useEffect(() => {
    if (!seleccionado) {
      setTextoUbicacion('');
      setCoordenadasJson(null);
      return;
    }
    setTextoUbicacion(seleccionado.ubicacion ?? '');
    setCoordenadasJson(seleccionado.coordenadas ?? null);
  }, [seleccionado]);

  const alCambiarEstablecimiento = (ev: SelectChangeEvent<number | ''>) => {
    const v = ev.target.value;
    setExito(null);
    if (v === '' || v === undefined) {
      setSeleccionId('');
      setSearchParams({}, { replace: true });
      return;
    }
    const id = typeof v === 'number' ? v : parseInt(String(v), 10);
    setSeleccionId(id);
    setSearchParams({ id: String(id) }, { replace: true });
  };

  const guardar = async () => {
    if (seleccionId === '') return;
    const coordenadasTrim = coordenadasJson?.trim() ?? '';
    try {
      setGuardando(true);
      setError(null);
      setExito(null);
      await actualizarEstablecimientoHuevos(Number(seleccionId), {
        ubicacion: textoUbicacion.trim() || null,
        coordenadas: coordenadasTrim,
      });
      setExito('Ubicación guardada correctamente.');
      await cargarLista();
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  return (
    <Box sx={{ p: 3, maxWidth: 1100, mx: 'auto' }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Ubicación en mapa — Establecimientos huevos
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        Elegí un establecimiento, dibujá el perímetro o un punto GPS y guardá. Misma idea que «Campos» en cultivos:
        pantalla completa para el mapa, sin mezclarlo con el alta de datos generales.
      </Typography>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={22} />
          <Typography variant="body2">Cargando establecimientos…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      {exito && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setExito(null)}>
          {exito}
        </Alert>
      )}

      {!cargando && (
        <Paper variant="outlined" sx={{ p: 2 }}>
          <FormControl fullWidth sx={{ mb: 2 }} size="small">
            <InputLabel id="sel-est-huevos">Establecimiento</InputLabel>
            <Select
              labelId="sel-est-huevos"
              label="Establecimiento"
              value={seleccionId}
              onChange={alCambiarEstablecimiento}
            >
              <MenuItem value="">
                <em>Seleccionar…</em>
              </MenuItem>
              {lista.map((e) => (
                <MenuItem key={e.id} value={e.id}>
                  {e.nombre}
                  {e.activo === false ? ' (inactivo)' : ''}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {seleccionId !== '' && seleccionado && (
            <>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Editando: <strong>{seleccionado.nombre}</strong>
              </Typography>
              <TextField
                margin="dense"
                label="Ubicación o referencia (texto)"
                fullWidth
                sx={{ mb: 2 }}
                value={textoUbicacion}
                onChange={(ev) => setTextoUbicacion(ev.target.value)}
                placeholder="Dirección, paraje, etc."
                helperText="Opcional. Complementa el dibujo en el mapa."
              />

              <EditorMapaUbicacionHuevos
                key={seleccionId}
                abierto
                coordenadasJson={coordenadasJson}
                onCoordenadasJsonChange={onCoordenadasChange}
              />

              <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                <Button variant="contained" onClick={() => void guardar()} disabled={guardando}>
                  {guardando ? 'Guardando…' : 'Guardar ubicación en mapa'}
                </Button>
                <Button variant="outlined" onClick={() => navigate('/avicola-huevos/establecimientos')}>
                  Ir a establecimientos
                </Button>
              </Box>
            </>
          )}

          {!cargando && lista.length === 0 && (
            <Typography color="text.secondary">No hay establecimientos. Creá uno en «Establecimientos».</Typography>
          )}
        </Paper>
      )}
    </Box>
  );
};

export default UbicacionEstablecimientosHuevosScreen;
