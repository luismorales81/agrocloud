import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import { Icon } from '../../../components/icons';
import type {
  PorcinosCatalogo,
  PorcinosEstablecimiento,
  PorcinosGalpon,
  PorcinosMadre,
  PorcinosPadrillo,
  PorcinosReproduccionResumen,
} from '../typesApiV2';
import {
  crearMadre,
  crearPadrillo,
  listarEstablecimientos,
  listarGalpones,
  listarMadres,
  listarPadrillos,
  listarRazas,
  mensajeError,
  obtenerReproduccionResumen,
} from '../services/porcinosApi';

interface GalponOpcion extends PorcinosGalpon {
  establecimientoNombre: string;
}

const ReproduccionHubScreen: React.FC = () => {
  const navigate = useNavigate();
  const hoy = () => new Date().toISOString().slice(0, 10);

  const [pestana, setPestana] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [resumen, setResumen] = useState<PorcinosReproduccionResumen | null>(null);
  const [madres, setMadres] = useState<PorcinosMadre[]>([]);
  const [padrillos, setPadrillos] = useState<PorcinosPadrillo[]>([]);
  const [razas, setRazas] = useState<PorcinosCatalogo[]>([]);
  const [galpones, setGalpones] = useState<GalponOpcion[]>([]);

  const [dialogoMadre, setDialogoMadre] = useState(false);
  const [dialogoPadrillo, setDialogoPadrillo] = useState(false);
  const [caravana, setCaravana] = useState('');
  const [razaId, setRazaId] = useState<number | ''>('');
  const [galponId, setGalponId] = useState<number | ''>('');
  const [fechaIngreso, setFechaIngreso] = useState(hoy());
  const [nombrePadrillo, setNombrePadrillo] = useState('');
  const [razaPadrilloId, setRazaPadrilloId] = useState<number | ''>('');

  const cargarCatalogos = useCallback(async () => {
    const [ests, r] = await Promise.all([listarEstablecimientos(), listarRazas()]);
    setRazas(r.filter((x) => x.activo !== false));
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
    setCargando(true);
    setError(null);
    try {
      const [r, m, p] = await Promise.all([
        obtenerReproduccionResumen(),
        listarMadres(),
        listarPadrillos(),
      ]);
      setResumen(r);
      setMadres(m);
      setPadrillos(p);
      await cargarCatalogos();
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [cargarCatalogos]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const guardarMadre = async () => {
    if (!caravana.trim()) {
      setError('La caravana es obligatoria.');
      return;
    }
    try {
      const creada = await crearMadre({
        caravana: caravana.trim(),
        razaId: razaId === '' ? null : Number(razaId),
        galponId: galponId === '' ? null : Number(galponId),
        fechaIngreso,
      });
      setDialogoMadre(false);
      await cargar();
      navigate(`/porcinos/madres/${creada.id}`);
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarPadrillo = async () => {
    if (!nombrePadrillo.trim()) {
      setError('El nombre es obligatorio.');
      return;
    }
    try {
      await crearPadrillo({
        nombre: nombrePadrillo.trim(),
        razaId: razaPadrilloId === '' ? null : Number(razaPadrilloId),
      });
      setDialogoPadrillo(false);
      setNombrePadrillo('');
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const tarjetas = [
    { etiqueta: 'Madres activas', valor: resumen?.madresActivas ?? madres.filter((m) => m.activo !== false).length },
    { etiqueta: 'En gestación', valor: resumen?.gestacionesEnCurso ?? 0 },
    { etiqueta: 'En lactancia', valor: resumen?.madresEnLactancia ?? 0 },
    { etiqueta: 'Partos registrados', valor: resumen?.partosPeriodo ?? 0 },
    { etiqueta: 'Destetes', valor: resumen?.destetesPeriodo ?? 0 },
  ];

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="Heart" size={28} />
          <Typography variant="h4" component="h1">
            Reproducción
          </Typography>
        </Stack>
        <Stack direction="row" spacing={1}>
          <Button variant="contained" onClick={() => { setCaravana(''); setDialogoMadre(true); }}>
            Nueva madre
          </Button>
          <Button variant="outlined" onClick={() => setDialogoPadrillo(true)}>
            Nuevo padrillo
          </Button>
        </Stack>
      </Stack>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando datos reproductivos…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <>
          <Stack direction="row" flexWrap="wrap" gap={2} sx={{ mb: 3 }}>
            {tarjetas.map((t) => (
              <Paper key={t.etiqueta} variant="outlined" sx={{ p: 2, minWidth: 140, flex: '1 1 140px' }}>
                <Typography variant="caption" color="text.secondary">
                  {t.etiqueta}
                </Typography>
                <Typography variant="h5">{t.valor}</Typography>
              </Paper>
            ))}
          </Stack>

          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }}>
            <Tab label={`Madres (${madres.length})`} />
            <Tab label={`Padrillos (${padrillos.length})`} />
          </Tabs>

          {pestana === 0 && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Caravana</TableCell>
                    <TableCell>Estado</TableCell>
                    <TableCell>Raza</TableCell>
                    <TableCell>Galpón</TableCell>
                    <TableCell>Activa</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {madres.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        <Typography color="text.secondary" sx={{ py: 2 }}>
                          No hay madres registradas. Usá «Nueva madre» para comenzar el ciclo reproductivo.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    madres.map((m) => (
                      <TableRow
                        key={m.id}
                        hover
                        sx={{ cursor: 'pointer' }}
                        onClick={() => navigate(`/porcinos/madres/${m.id}`)}
                      >
                        <TableCell>{m.caravana}</TableCell>
                        <TableCell>{m.estado ?? '—'}</TableCell>
                        <TableCell>{m.razaNombre ?? '—'}</TableCell>
                        <TableCell>{m.galponNombre ?? '—'}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={m.activo !== false ? 'Sí' : 'No'}
                            color={m.activo !== false ? 'success' : 'default'}
                          />
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {pestana === 1 && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Nombre</TableCell>
                    <TableCell>Raza</TableCell>
                    <TableCell>Activo</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {padrillos.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={3} align="center">
                        <Typography color="text.secondary" sx={{ py: 2 }}>
                          No hay padrillos registrados.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    padrillos.map((p) => (
                      <TableRow key={p.id}>
                        <TableCell>{p.nombre}</TableCell>
                        <TableCell>{p.razaNombre ?? '—'}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={p.activo !== false ? 'Sí' : 'No'}
                            color={p.activo !== false ? 'success' : 'default'}
                          />
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </>
      )}

      <Dialog open={dialogoMadre} onClose={() => setDialogoMadre(false)} fullWidth maxWidth="sm">
        <DialogTitle>Nueva madre</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Caravana" fullWidth required value={caravana} onChange={(e) => setCaravana(e.target.value)} />
            <TextField select label="Raza" fullWidth value={razaId} onChange={(e) => setRazaId(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">Sin especificar</MenuItem>
              {razas.map((r) => (
                <MenuItem key={r.id} value={r.id}>{r.nombre}</MenuItem>
              ))}
            </TextField>
            <TextField select label="Galpón" fullWidth value={galponId} onChange={(e) => setGalponId(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">Sin asignar</MenuItem>
              {galpones.map((g) => (
                <MenuItem key={g.id} value={g.id}>{g.establecimientoNombre} — {g.nombre}</MenuItem>
              ))}
            </TextField>
            <TextField type="date" label="Fecha de ingreso" fullWidth InputLabelProps={{ shrink: true }} value={fechaIngreso} onChange={(e) => setFechaIngreso(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoMadre(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarMadre()}>Crear</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoPadrillo} onClose={() => setDialogoPadrillo(false)} fullWidth maxWidth="sm">
        <DialogTitle>Nuevo padrillo</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={nombrePadrillo} onChange={(e) => setNombrePadrillo(e.target.value)} />
            <TextField select label="Raza" fullWidth value={razaPadrilloId} onChange={(e) => setRazaPadrilloId(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">Sin especificar</MenuItem>
              {razas.map((r) => (
                <MenuItem key={r.id} value={r.id}>{r.nombre}</MenuItem>
              ))}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoPadrillo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarPadrillo()}>Crear</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReproduccionHubScreen;
