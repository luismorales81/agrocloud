import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type {
  PorcinosCatalogo,
  PorcinosEstablecimiento,
  PorcinosGalpon,
  PorcinosMadre,
  PorcinosPadrillo,
} from '../typesApiV2';
import {
  listarEstablecimientos,
  listarGalpones,
  listarPadrillos,
  listarRazas,
  listarTiposServicio,
  mensajeError,
  obtenerGestacionActivaMadre,
  obtenerMadre,
  obtenerPartoPendienteDestete,
  registrarDestete,
  registrarParto,
  registrarServicioMadre,
} from '../services/porcinosApi';

interface GalponOpcion extends PorcinosGalpon {
  establecimientoNombre: string;
}

type TipoDialogo = 'servicio' | 'parto' | 'destete' | null;

const DetalleMadrePorcinosScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const madreId = id != null ? parseInt(id, 10) : NaN;

  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [madre, setMadre] = useState<PorcinosMadre | null>(null);
  const [padrillos, setPadrillos] = useState<PorcinosPadrillo[]>([]);
  const [tiposServicio, setTiposServicio] = useState<PorcinosCatalogo[]>([]);
  const [galpones, setGalpones] = useState<GalponOpcion[]>([]);

  const [dialogo, setDialogo] = useState<TipoDialogo>(null);
  const [gestacionId, setGestacionId] = useState<number | null>(null);
  const [partoId, setPartoId] = useState<number | null>(null);
  const [fechaProbableParto, setFechaProbableParto] = useState<string | null>(null);

  const hoy = () => new Date().toISOString().slice(0, 10);

  const [fServicio, setFServicio] = useState(hoy());
  const [padrilloId, setPadrilloId] = useState<number | ''>('');
  const [tipoServicioId, setTipoServicioId] = useState<number | ''>('');
  const [obsServicio, setObsServicio] = useState('');

  const [fParto, setFParto] = useState(hoy());
  const [nacidosVivos, setNacidosVivos] = useState('');
  const [nacidosMuertos, setNacidosMuertos] = useState('0');
  const [momificados, setMomificados] = useState('0');
  const [obsParto, setObsParto] = useState('');

  const [fDestete, setFDestete] = useState(hoy());
  const [cantDestetados, setCantDestetados] = useState('');
  const [pesoDestete, setPesoDestete] = useState('');
  const [galponDesteteId, setGalponDesteteId] = useState<number | ''>('');
  const [nombreLote, setNombreLote] = useState('');

  const cargarGalpones = useCallback(async () => {
    const ests = await listarEstablecimientos();
    const opciones: GalponOpcion[] = [];
    for (const est of ests.filter((e: PorcinosEstablecimiento) => e.activo !== false)) {
      const lista = await listarGalpones(est.id);
      for (const g of lista.filter((gal) => gal.activo !== false)) {
        opciones.push({ ...g, establecimientoNombre: est.nombre });
      }
    }
    setGalpones(opciones);
  }, []);

  const cargar = useCallback(async () => {
    if (Number.isNaN(madreId)) return;
    setCargando(true);
    setError(null);
    try {
      const [m, p, t] = await Promise.all([
        obtenerMadre(madreId),
        listarPadrillos(),
        listarTiposServicio(),
        listarRazas(),
        cargarGalpones(),
      ]);
      setMadre(m);
      setPadrillos(p.filter((x) => x.activo !== false));
      setTiposServicio(t.filter((x) => x.activo !== false));
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [madreId, cargarGalpones]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const abrirServicio = () => {
    setFServicio(hoy());
    setPadrilloId('');
    setTipoServicioId('');
    setObsServicio('');
    setDialogo('servicio');
  };

  const abrirParto = async () => {
    try {
      const gestacion = await obtenerGestacionActivaMadre(madreId);
      setGestacionId(gestacion.id);
      setFechaProbableParto(gestacion.fechaProbableParto ?? null);
      setFParto(hoy());
      setNacidosVivos('');
      setNacidosMuertos('0');
      setMomificados('0');
      setObsParto('');
      setDialogo('parto');
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const abrirDestete = async () => {
    try {
      const parto = await obtenerPartoPendienteDestete(madreId);
      setPartoId(parto.id);
      setFDestete(hoy());
      setCantDestetados(String(parto.nacidosVivos ?? ''));
      setPesoDestete('');
      setGalponDesteteId('');
      setNombreLote(
        madre?.caravana ? `Destete-${madre.caravana}-${hoy()}` : ''
      );
      setDialogo('destete');
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarServicio = async () => {
    try {
      await registrarServicioMadre(madreId, {
        fecha: fServicio,
        padrilloId: padrilloId === '' ? null : Number(padrilloId),
        tipoServicioId: tipoServicioId === '' ? null : Number(tipoServicioId),
        observaciones: obsServicio.trim() || null,
      });
      setDialogo(null);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarParto = async () => {
    if (gestacionId == null) return;
    const vivos = parseInt(nacidosVivos, 10);
    if (Number.isNaN(vivos) || vivos < 0) {
      setError('Indicá nacidos vivos válidos.');
      return;
    }
    try {
      await registrarParto(gestacionId, {
        fecha: fParto,
        nacidosVivos: vivos,
        nacidosMuertos: parseInt(nacidosMuertos, 10) || 0,
        momificados: parseInt(momificados, 10) || 0,
        observaciones: obsParto.trim() || null,
      });
      setDialogo(null);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarDestete = async () => {
    if (partoId == null) return;
    const cant = parseInt(cantDestetados, 10);
    const peso = parseFloat(pesoDestete.replace(',', '.'));
    if (galponDesteteId === '' || Number.isNaN(cant) || cant <= 0 || Number.isNaN(peso) || peso <= 0) {
      setError('Completá galpón destino, cantidad y peso promedio.');
      return;
    }
    try {
      const destete = await registrarDestete(partoId, {
        fecha: fDestete,
        cantidadDestetados: cant,
        pesoPromedioKg: peso,
        galponId: Number(galponDesteteId),
        loteNombre: nombreLote.trim() || null,
      });
      setDialogo(null);
      if (destete.loteId != null) {
        navigate(`/porcinos/lotes/${destete.loteId}`);
      } else {
        await cargar();
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  if (Number.isNaN(madreId)) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Identificador de madre inválido.</Alert>
      </Box>
    );
  }

  const puedeServicio =
    madre?.activo !== false &&
    (madre?.estado === 'ADULTA' || madre?.estado === 'CACHORRA' || madre?.estado === 'DESCARTE');
  const puedeParto = madre?.estado === 'GESTACION';
  const puedeDestete = madre?.estado === 'LACTANCIA';

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 800 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Button size="small" onClick={() => navigate('/porcinos/reproduccion')}>
          ← Volver
        </Button>
        <Typography variant="h5" component="h1">
          Madre {madre?.caravana ?? `#${madreId}`}
        </Typography>
        {madre?.estado && (
          <Chip size="small" label={madre.estado} color="primary" variant="outlined" />
        )}
      </Stack>

      {cargando && <CircularProgress size={24} />}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && madre && (
        <>
          <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
            <Stack spacing={1}>
              <Typography variant="body2">
                <strong>Raza:</strong> {madre.razaNombre ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Galpón:</strong> {madre.galponNombre ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Ingreso:</strong>{' '}
                {madre.fechaIngreso ? new Date(madre.fechaIngreso).toLocaleDateString('es-AR') : '—'}
              </Typography>
            </Stack>
          </Paper>

          <Typography variant="subtitle1" gutterBottom>
            Acciones del ciclo reproductivo
          </Typography>
          <Stack direction="row" flexWrap="wrap" gap={1} sx={{ mb: 2 }}>
            <Button variant="contained" disabled={!puedeServicio} onClick={abrirServicio}>
              Registrar servicio
            </Button>
            <Button variant="contained" color="secondary" disabled={!puedeParto} onClick={() => void abrirParto()}>
              Registrar parto
            </Button>
            <Button variant="contained" color="success" disabled={!puedeDestete} onClick={() => void abrirDestete()}>
              Registrar destete
            </Button>
          </Stack>

          {!puedeServicio && !puedeParto && !puedeDestete && (
            <Alert severity="info">
              No hay acciones disponibles para el estado actual ({madre.estado ?? '—'}).
            </Alert>
          )}
        </>
      )}

      <Dialog open={dialogo === 'servicio'} onClose={() => setDialogo(null)} fullWidth maxWidth="sm">
        <DialogTitle>Registrar servicio</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              type="date"
              label="Fecha"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={fServicio}
              onChange={(e) => setFServicio(e.target.value)}
            />
            <TextField
              select
              label="Padrillo"
              fullWidth
              value={padrilloId}
              onChange={(e) => setPadrilloId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">Sin padrillo / IA</MenuItem>
              {padrillos.map((p) => (
                <MenuItem key={p.id} value={p.id}>
                  {p.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Tipo de servicio"
              fullWidth
              value={tipoServicioId}
              onChange={(e) => setTipoServicioId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {tiposServicio.map((t) => (
                <MenuItem key={t.id} value={t.id}>
                  {t.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={obsServicio}
              onChange={(e) => setObsServicio(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarServicio()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'parto'} onClose={() => setDialogo(null)} fullWidth maxWidth="sm">
        <DialogTitle>Registrar parto</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {fechaProbableParto && (
              <Typography variant="body2" color="text.secondary">
                Fecha probable de parto: {new Date(fechaProbableParto).toLocaleDateString('es-AR')}
              </Typography>
            )}
            <TextField
              type="date"
              label="Fecha del parto"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={fParto}
              onChange={(e) => setFParto(e.target.value)}
            />
            <TextField
              label="Nacidos vivos"
              fullWidth
              required
              type="number"
              value={nacidosVivos}
              onChange={(e) => setNacidosVivos(e.target.value)}
            />
            <Stack direction="row" spacing={2}>
              <TextField
                label="Nacidos muertos"
                fullWidth
                type="number"
                value={nacidosMuertos}
                onChange={(e) => setNacidosMuertos(e.target.value)}
              />
              <TextField
                label="Momificados"
                fullWidth
                type="number"
                value={momificados}
                onChange={(e) => setMomificados(e.target.value)}
              />
            </Stack>
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={obsParto}
              onChange={(e) => setObsParto(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarParto()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogo === 'destete'} onClose={() => setDialogo(null)} fullWidth maxWidth="sm">
        <DialogTitle>Registrar destete</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Al confirmar se creará automáticamente un lote de engorde.
            </Typography>
            <TextField
              type="date"
              label="Fecha del destete"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={fDestete}
              onChange={(e) => setFDestete(e.target.value)}
            />
            <TextField
              label="Cantidad destetados"
              fullWidth
              required
              type="number"
              value={cantDestetados}
              onChange={(e) => setCantDestetados(e.target.value)}
            />
            <TextField
              label="Peso promedio (kg)"
              fullWidth
              required
              type="number"
              value={pesoDestete}
              onChange={(e) => setPesoDestete(e.target.value)}
            />
            <TextField
              select
              label="Galpón destino"
              fullWidth
              required
              value={galponDesteteId}
              onChange={(e) => setGalponDesteteId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <MenuItem value="">Seleccionar…</MenuItem>
              {galpones
                .filter((g) => g.estado === 'DISPONIBLE' || g.estado == null)
                .map((g) => (
                  <MenuItem key={g.id} value={g.id}>
                    {g.establecimientoNombre} — {g.nombre}
                  </MenuItem>
                ))}
            </TextField>
            <TextField
              label="Nombre del lote (opcional)"
              fullWidth
              value={nombreLote}
              onChange={(e) => setNombreLote(e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(null)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarDestete()}>
            Guardar y crear lote
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DetalleMadrePorcinosScreen;
