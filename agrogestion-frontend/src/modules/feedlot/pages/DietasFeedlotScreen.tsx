import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Collapse,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { ChevronDown, ChevronRight, Pencil, Plus } from 'lucide-react';
import { insumosService } from '../../../services/domain/insumosLaboresServices';
import { Icon } from '../../../components/icons';
import type { FeedlotDieta, FeedlotDietaFase, FeedlotDietaFaseSolicitud } from '../types';
import {
  actualizarDieta,
  actualizarFaseDieta,
  crearDieta,
  crearFaseDieta,
  listarDietas,
  mensajeError,
} from '../services/feedlotApi';

interface InsumoOpcion {
  id: number;
  nombre: string;
}

const DietasFeedlotScreen: React.FC = () => {
  const [dietas, setDietas] = useState<FeedlotDieta[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcion[]>([]);
  const [expandidas, setExpandidas] = useState<Set<number>>(new Set());
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogoDieta, setDialogoDieta] = useState(false);
  const [editandoDieta, setEditandoDieta] = useState<FeedlotDieta | null>(null);
  const [nombreDieta, setNombreDieta] = useState('');
  const [activoDieta, setActivoDieta] = useState(true);

  const [dialogoFase, setDialogoFase] = useState(false);
  const [dietaFaseId, setDietaFaseId] = useState<number | null>(null);
  const [editandoFase, setEditandoFase] = useState<FeedlotDietaFase | null>(null);
  const [nombreFase, setNombreFase] = useState('');
  const [diasDesde, setDiasDesde] = useState('');
  const [kgMs, setKgMs] = useState('');
  const [insumoFase, setInsumoFase] = useState<number | ''>('');

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      setError(null);
      const [lista, rawInsumos] = await Promise.all([listarDietas(), insumosService.listar()]);
      setDietas(lista);
      const arr = Array.isArray(rawInsumos) ? rawInsumos : [];
      setInsumos(
        arr.map((x: Record<string, unknown>) => ({
          id: Number(x.id),
          nombre: String(x.nombre ?? x.descripcion ?? `Insumo ${x.id}`),
        }))
      );
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const nomInsumo = (id?: number | null) =>
    id != null ? insumos.find((i) => i.id === id)?.nombre ?? `Insumo ${id}` : '—';

  const toggleExpandir = (id: number) => {
    setExpandidas((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const abrirNuevaDieta = () => {
    setEditandoDieta(null);
    setNombreDieta('');
    setActivoDieta(true);
    setDialogoDieta(true);
  };

  const abrirEditarDieta = (d: FeedlotDieta) => {
    setEditandoDieta(d);
    setNombreDieta(d.nombre);
    setActivoDieta(d.activo !== false);
    setDialogoDieta(true);
  };

  const guardarDieta = async () => {
    if (!nombreDieta.trim()) {
      setError('El nombre de la dieta es obligatorio.');
      return;
    }
    try {
      if (editandoDieta) {
        await actualizarDieta(editandoDieta.id, { nombre: nombreDieta.trim(), activo: activoDieta });
      } else {
        await crearDieta({ nombre: nombreDieta.trim(), activo: activoDieta });
      }
      setDialogoDieta(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const abrirNuevaFase = (dietaId: number) => {
    setDietaFaseId(dietaId);
    setEditandoFase(null);
    setNombreFase('');
    setDiasDesde('0');
    setKgMs('');
    setInsumoFase('');
    setDialogoFase(true);
  };

  const abrirEditarFase = (dietaId: number, fase: FeedlotDietaFase) => {
    setDietaFaseId(dietaId);
    setEditandoFase(fase);
    setNombreFase(fase.nombreFase);
    setDiasDesde(String(fase.diasDesdeIngreso));
    setKgMs(String(fase.kgMsCabezaDia));
    setInsumoFase(fase.insumoId ?? '');
    setDialogoFase(true);
  };

  const guardarFase = async () => {
    if (dietaFaseId == null) return;
    const dias = parseInt(diasDesde, 10);
    const kg = parseFloat(kgMs.replace(',', '.'));
    if (!nombreFase.trim() || Number.isNaN(dias) || Number.isNaN(kg) || kg <= 0) {
      setError('Complete nombre, días desde ingreso y kg MS/cabeza/día válidos.');
      return;
    }
    const cuerpo: FeedlotDietaFaseSolicitud = {
      nombreFase: nombreFase.trim(),
      diasDesdeIngreso: dias,
      kgMsCabezaDia: kg,
      insumoId: insumoFase === '' ? null : Number(insumoFase),
    };
    try {
      if (editandoFase) {
        await actualizarFaseDieta(dietaFaseId, editandoFase.id, cuerpo);
      } else {
        await crearFaseDieta(dietaFaseId, cuerpo);
      }
      setDialogoFase(false);
      setExpandidas((prev) => new Set(prev).add(dietaFaseId));
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="Utensils" size={28} />
          <Typography variant="h5" component="h1">
            Dietas de engorde
          </Typography>
        </Stack>
        <Button variant="contained" startIcon={<Plus size={18} />} onClick={abrirNuevaDieta}>
          Nueva dieta
        </Button>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Definí fases por días desde el ingreso con kg de materia seca por cabeza y día.
      </Typography>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={1} sx={{ my: 2 }}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando dietas…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell width={48} />
                <TableCell>Nombre</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Fases</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {dietas.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5}>
                    <Typography color="text.secondary" variant="body2">
                      No hay dietas registradas.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                dietas.map((d) => {
                  const abierta = expandidas.has(d.id);
                  const fases = d.fases ?? [];
                  return (
                    <React.Fragment key={d.id}>
                      <TableRow hover>
                        <TableCell>
                          <IconButton size="small" onClick={() => toggleExpandir(d.id)}>
                            {abierta ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                          </IconButton>
                        </TableCell>
                        <TableCell>{d.nombre}</TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={d.activo !== false ? 'Activa' : 'Inactiva'}
                            color={d.activo !== false ? 'success' : 'default'}
                          />
                        </TableCell>
                        <TableCell align="right">{fases.length}</TableCell>
                        <TableCell align="right">
                          <IconButton size="small" onClick={() => abrirEditarDieta(d)} aria-label="Editar dieta">
                            <Pencil size={16} />
                          </IconButton>
                          <Button size="small" onClick={() => abrirNuevaFase(d.id)}>
                            + Fase
                          </Button>
                        </TableCell>
                      </TableRow>
                      <TableRow>
                        <TableCell colSpan={5} sx={{ py: 0, borderBottom: abierta ? undefined : 'none' }}>
                          <Collapse in={abierta} timeout="auto" unmountOnExit>
                            <Box sx={{ py: 1, pl: 6 }}>
                              {fases.length === 0 ? (
                                <Typography variant="body2" color="text.secondary">
                                  Sin fases. Agregá al menos una fase para calcular consumo teórico.
                                </Typography>
                              ) : (
                                <Table size="small">
                                  <TableHead>
                                    <TableRow>
                                      <TableCell>Fase</TableCell>
                                      <TableCell align="right">Días desde ingreso</TableCell>
                                      <TableCell align="right">kg MS/cab/día</TableCell>
                                      <TableCell>Insumo</TableCell>
                                      <TableCell align="right">Acciones</TableCell>
                                    </TableRow>
                                  </TableHead>
                                  <TableBody>
                                    {fases.map((f) => (
                                      <TableRow key={f.id}>
                                        <TableCell>{f.nombreFase}</TableCell>
                                        <TableCell align="right">{f.diasDesdeIngreso}</TableCell>
                                        <TableCell align="right">{String(f.kgMsCabezaDia)}</TableCell>
                                        <TableCell>{nomInsumo(f.insumoId)}</TableCell>
                                        <TableCell align="right">
                                          <IconButton
                                            size="small"
                                            onClick={() => abrirEditarFase(d.id, f)}
                                            aria-label="Editar fase"
                                          >
                                            <Pencil size={16} />
                                          </IconButton>
                                        </TableCell>
                                      </TableRow>
                                    ))}
                                  </TableBody>
                                </Table>
                              )}
                            </Box>
                          </Collapse>
                        </TableCell>
                      </TableRow>
                    </React.Fragment>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogoDieta} onClose={() => setDialogoDieta(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editandoDieta ? 'Editar dieta' : 'Nueva dieta'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Nombre"
              fullWidth
              value={nombreDieta}
              onChange={(ev) => setNombreDieta(ev.target.value)}
            />
            <FormControlLabel
              control={
                <Switch checked={activoDieta} onChange={(ev) => setActivoDieta(ev.target.checked)} />
              }
              label="Activa"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoDieta(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarDieta()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dialogoFase} onClose={() => setDialogoFase(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editandoFase ? 'Editar fase' : 'Nueva fase'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Nombre de fase"
              fullWidth
              value={nombreFase}
              onChange={(ev) => setNombreFase(ev.target.value)}
            />
            <TextField
              label="Días desde ingreso"
              type="number"
              fullWidth
              value={diasDesde}
              onChange={(ev) => setDiasDesde(ev.target.value)}
              inputProps={{ min: 0 }}
            />
            <TextField
              label="kg MS por cabeza y día"
              fullWidth
              value={kgMs}
              onChange={(ev) => setKgMs(ev.target.value)}
            />
            <TextField
              select
              label="Insumo (opcional)"
              fullWidth
              value={insumoFase}
              onChange={(ev) => setInsumoFase(ev.target.value === '' ? '' : Number(ev.target.value))}
            >
              <MenuItem value="">Ninguno</MenuItem>
              {insumos.map((i) => (
                <MenuItem key={i.id} value={i.id}>
                  {i.nombre}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoFase(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarFase()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DietasFeedlotScreen;
