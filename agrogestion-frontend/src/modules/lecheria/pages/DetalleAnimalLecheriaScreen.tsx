import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
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
import PanelClimaEstablecimiento from '../../../components/PanelClimaEstablecimiento';
import type {
  LecheriaAnimal,
  LecheriaEventoReproductivo,
  LecheriaEventoSanitario,
  LecheriaRegistroOrdene,
  LecheriaScoreCorporal,
  LecheriaTipoEventoReproductivo,
  LecheriaTipoEventoSanitario,
} from '../types';
import {
  listarEventosReproductivos,
  listarEventosSanitarios,
  listarOrdenes,
  listarScoresCorporales,
  mensajeError,
  obtenerAnimal,
  registrarEventoReproductivo,
  registrarEventoSanitario,
  registrarScoreCorporal,
} from '../services/lecheriaApi';

const TIPOS_REPRO: { valor: LecheriaTipoEventoReproductivo; etiqueta: string }[] = [
  { valor: 'SERVICIO', etiqueta: 'Servicio' },
  { valor: 'TACTO', etiqueta: 'Tacto' },
  { valor: 'PARTO', etiqueta: 'Parto' },
  { valor: 'SECADO', etiqueta: 'Secado' },
  { valor: 'ABORTO', etiqueta: 'Aborto' },
];

const TIPOS_SANIDAD: { valor: LecheriaTipoEventoSanitario; etiqueta: string }[] = [
  { valor: 'VACUNA', etiqueta: 'Vacuna' },
  { valor: 'DESPARASITACION', etiqueta: 'Desparasitación' },
  { valor: 'TRATAMIENTO', etiqueta: 'Tratamiento' },
  { valor: 'MASTITIS', etiqueta: 'Mastitis' },
  { valor: 'CONTROL', etiqueta: 'Control' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

const DetalleAnimalLecheriaScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const animalId = id ? parseInt(id, 10) : NaN;

  const [pestana, setPestana] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [animal, setAnimal] = useState<LecheriaAnimal | null>(null);
  const [ordenes, setOrdenes] = useState<LecheriaRegistroOrdene[]>([]);
  const [reproduccion, setReproduccion] = useState<LecheriaEventoReproductivo[]>([]);
  const [sanidad, setSanidad] = useState<LecheriaEventoSanitario[]>([]);
  const [ecc, setEcc] = useState<LecheriaScoreCorporal[]>([]);

  const [dialogoRepro, setDialogoRepro] = useState(false);
  const [tipoRepro, setTipoRepro] = useState<LecheriaTipoEventoReproductivo>('SERVICIO');
  const [fechaRepro, setFechaRepro] = useState('');
  const [obsRepro, setObsRepro] = useState('');

  const [dialogoSanidad, setDialogoSanidad] = useState(false);
  const [tipoSanidad, setTipoSanidad] = useState<LecheriaTipoEventoSanitario>('VACUNA');
  const [fechaSanidad, setFechaSanidad] = useState('');
  const [descSanidad, setDescSanidad] = useState('');

  const [dialogoEcc, setDialogoEcc] = useState(false);
  const [fechaEcc, setFechaEcc] = useState('');
  const [valorEcc, setValorEcc] = useState('');

  const cargar = useCallback(async () => {
    if (Number.isNaN(animalId)) return;
    setCargando(true);
    setError(null);
    try {
      const [a, o, r, s, e] = await Promise.all([
        obtenerAnimal(animalId),
        listarOrdenes(animalId),
        listarEventosReproductivos(animalId),
        listarEventosSanitarios(animalId),
        listarScoresCorporales(animalId),
      ]);
      setAnimal(a);
      setOrdenes(o);
      setReproduccion(r);
      setSanidad(s);
      setEcc(e);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [animalId]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const guardarRepro = async () => {
    if (!fechaRepro) return;
    try {
      await registrarEventoReproductivo(animalId, {
        tipo: tipoRepro,
        fecha: fechaRepro,
        observaciones: obsRepro || null,
      });
      setDialogoRepro(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarSanidad = async () => {
    if (!fechaSanidad) return;
    try {
      await registrarEventoSanitario(animalId, {
        tipo: tipoSanidad,
        fecha: fechaSanidad,
        descripcion: descSanidad || null,
      });
      setDialogoSanidad(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const guardarEcc = async () => {
    if (!fechaEcc || !valorEcc) return;
    try {
      await registrarScoreCorporal(animalId, {
        fecha: fechaEcc,
        valor: parseFloat(valorEcc.replace(',', '.')),
      });
      setDialogoEcc(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  if (Number.isNaN(animalId)) {
    return <Alert severity="error">ID de animal inválido.</Alert>;
  }

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Button size="small" onClick={() => navigate('/lecheria/animales')}>
          ← Volver
        </Button>
        <Typography variant="h5" component="h1">
          {animal?.identificacion ?? 'Animal'}
        </Typography>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ my: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && animal && (
        <>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }}>
            <Tab label="Resumen" />
            <Tab label="Ordeñes" />
            <Tab label="Reproducción" />
            <Tab label="Sanidad" />
            <Tab label="ECC" />
          </Tabs>

          {pestana === 0 && (
            <Paper variant="outlined" sx={{ p: 2 }}>
              {animal.climaLatitud != null && (
                <PanelClimaEstablecimiento
                  climaLatitud={animal.climaLatitud}
                  climaLongitud={animal.climaLongitud}
                  nombreEstablecimiento={animal.rodeoNombre ?? undefined}
                  rutaMapa="/lecheria/establecimientos-mapa"
                />
              )}
              <Typography variant="body2">
                <strong>Raza:</strong> {animal.razaNombre ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Rodeo:</strong> {animal.rodeoNombre ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Estado:</strong> {animal.estado ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>DIM:</strong> {animal.dim ?? animal.lactanciaActiva?.dim ?? '—'}
              </Typography>
              <Typography variant="body2">
                <strong>Especie:</strong> {animal.especie ?? '—'} · <strong>Sexo:</strong>{' '}
                {animal.sexo ?? '—'}
              </Typography>
              {animal.observaciones && (
                <Typography variant="body2" sx={{ mt: 1 }}>
                  <strong>Observaciones:</strong> {animal.observaciones}
                </Typography>
              )}
              <Button sx={{ mt: 2 }} variant="contained" onClick={() => navigate('/lecheria/ordene')}>
                Registrar ordeñe
              </Button>
            </Paper>
          )}

          {pestana === 1 && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Fecha</TableCell>
                    <TableCell>Turno</TableCell>
                    <TableCell>Litros</TableCell>
                    <TableCell>RCS</TableCell>
                    <TableCell>Temp.</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {ordenes.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        Sin ordeñes registrados.
                      </TableCell>
                    </TableRow>
                  ) : (
                    ordenes.map((o) => (
                      <TableRow key={o.id}>
                        <TableCell>{o.fecha?.slice(0, 10)}</TableCell>
                        <TableCell>{o.turno}</TableCell>
                        <TableCell>{o.litros}</TableCell>
                        <TableCell>{o.rcs ?? '—'}</TableCell>
                        <TableCell>{o.temperaturaAmbiente ?? '—'}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {pestana === 2 && (
            <>
              <Button sx={{ mb: 2 }} variant="outlined" onClick={() => { setFechaRepro(new Date().toISOString().slice(0, 10)); setDialogoRepro(true); }}>
                Nuevo evento
              </Button>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell>Resultado</TableCell>
                      <TableCell>Observaciones</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {reproduccion.map((ev) => (
                      <TableRow key={ev.id}>
                        <TableCell>{ev.fecha?.slice(0, 10)}</TableCell>
                        <TableCell>{ev.tipo}</TableCell>
                        <TableCell>{ev.resultado ?? '—'}</TableCell>
                        <TableCell>{ev.observaciones ?? '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          )}

          {pestana === 3 && (
            <>
              <Button sx={{ mb: 2 }} variant="outlined" onClick={() => { setFechaSanidad(new Date().toISOString().slice(0, 10)); setDialogoSanidad(true); }}>
                Nuevo evento sanitario
              </Button>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell>Descripción</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {sanidad.map((ev) => (
                      <TableRow key={ev.id}>
                        <TableCell>{ev.fecha?.slice(0, 10)}</TableCell>
                        <TableCell>{ev.tipo}</TableCell>
                        <TableCell>{ev.descripcion ?? '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          )}

          {pestana === 4 && (
            <>
              <Button sx={{ mb: 2 }} variant="outlined" onClick={() => { setFechaEcc(new Date().toISOString().slice(0, 10)); setDialogoEcc(true); }}>
                Nuevo ECC
              </Button>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fecha</TableCell>
                      <TableCell>Valor</TableCell>
                      <TableCell>Observaciones</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {ecc.map((s) => (
                      <TableRow key={s.id}>
                        <TableCell>{s.fecha?.slice(0, 10)}</TableCell>
                        <TableCell>{s.valor}</TableCell>
                        <TableCell>{s.observaciones ?? '—'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          )}
        </>
      )}

      <Dialog open={dialogoRepro} onClose={() => setDialogoRepro(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Evento reproductivo</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField select label="Tipo" fullWidth value={tipoRepro} onChange={(e) => setTipoRepro(e.target.value as LecheriaTipoEventoReproductivo)}>
              {TIPOS_REPRO.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Fecha" type="date" fullWidth InputLabelProps={{ shrink: true }} value={fechaRepro} onChange={(e) => setFechaRepro(e.target.value)} />
            <TextField label="Observaciones" fullWidth multiline value={obsRepro} onChange={(e) => setObsRepro(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoRepro(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarRepro()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoSanidad} onClose={() => setDialogoSanidad(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Evento sanitario</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField select label="Tipo" fullWidth value={tipoSanidad} onChange={(e) => setTipoSanidad(e.target.value as LecheriaTipoEventoSanitario)}>
              {TIPOS_SANIDAD.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>
              ))}
            </TextField>
            <TextField label="Fecha" type="date" fullWidth InputLabelProps={{ shrink: true }} value={fechaSanidad} onChange={(e) => setFechaSanidad(e.target.value)} />
            <TextField label="Descripción" fullWidth value={descSanidad} onChange={(e) => setDescSanidad(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoSanidad(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarSanidad()}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoEcc} onClose={() => setDialogoEcc(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Score corporal (ECC)</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Fecha" type="date" fullWidth InputLabelProps={{ shrink: true }} value={fechaEcc} onChange={(e) => setFechaEcc(e.target.value)} />
            <TextField label="Valor (1-5)" fullWidth value={valorEcc} onChange={(e) => setValorEcc(e.target.value)} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoEcc(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarEcc()}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DetalleAnimalLecheriaScreen;
