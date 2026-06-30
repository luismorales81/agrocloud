import React, { useCallback, useEffect, useState } from 'react';
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
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Stack,
  Switch,
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
import { Pencil } from 'lucide-react';
import type { LecheriaEspecie, LecheriaMotivoBaja, LecheriaRaza } from '../types';
import {
  actualizarMotivoBaja,
  actualizarRaza,
  crearMotivoBaja,
  crearRaza,
  listarMotivosBaja,
  listarRazas,
  mensajeError,
} from '../services/lecheriaApi';

const ESPECIES: { valor: LecheriaEspecie; etiqueta: string }[] = [
  { valor: 'BOVINO', etiqueta: 'Bovino' },
  { valor: 'BUFALO', etiqueta: 'Búfalo' },
  { valor: 'CAPRINO', etiqueta: 'Caprino' },
  { valor: 'OVINO', etiqueta: 'Ovino' },
  { valor: 'CAMELIDO', etiqueta: 'Camélido' },
];

const CatalogosLecheriaScreen: React.FC = () => {
  const [pestana, setPestana] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [razas, setRazas] = useState<LecheriaRaza[]>([]);
  const [motivos, setMotivos] = useState<LecheriaMotivoBaja[]>([]);
  const [filtroEspecie, setFiltroEspecie] = useState<LecheriaEspecie | ''>('');

  const [dialogo, setDialogo] = useState(false);
  const [tipoCatalogo, setTipoCatalogo] = useState<'raza' | 'motivo'>('raza');
  const [editando, setEditando] = useState<LecheriaRaza | LecheriaMotivoBaja | null>(null);
  const [nombre, setNombre] = useState('');
  const [especie, setEspecie] = useState<LecheriaEspecie>('BOVINO');
  const [activo, setActivo] = useState(true);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const [r, m] = await Promise.all([
        listarRazas(filtroEspecie || undefined),
        listarMotivosBaja(),
      ]);
      setRazas(r);
      setMotivos(m);
      setError(null);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, [filtroEspecie]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const abrirNuevo = (tipo: typeof tipoCatalogo) => {
    setTipoCatalogo(tipo);
    setEditando(null);
    setNombre('');
    setEspecie('BOVINO');
    setActivo(true);
    setDialogo(true);
  };

  const abrirEditar = (tipo: typeof tipoCatalogo, item: LecheriaRaza | LecheriaMotivoBaja) => {
    setTipoCatalogo(tipo);
    setEditando(item);
    setNombre(item.nombre);
    setActivo(item.activo !== false);
    if (tipo === 'raza' && 'especie' in item) {
      setEspecie((item.especie as LecheriaEspecie) ?? 'BOVINO');
    }
    setDialogo(true);
  };

  const guardar = async () => {
    if (!nombre.trim()) return;
    try {
      if (tipoCatalogo === 'raza') {
        const cuerpo = { nombre: nombre.trim(), especie, activo };
        if (editando) {
          await actualizarRaza(editando.id, cuerpo);
        } else {
          await crearRaza(cuerpo);
        }
      } else {
        const cuerpo = { nombre: nombre.trim(), activo };
        if (editando) {
          await actualizarMotivoBaja(editando.id, cuerpo);
        } else {
          await crearMotivoBaja(cuerpo);
        }
      }
      setDialogo(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 900 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Catálogos — Lechería
      </Typography>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando catálogos…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }}>
        <Tab label="Razas por especie" />
        <Tab label="Motivos de baja" />
      </Tabs>

      {pestana === 0 && (
        <>
          <Stack direction="row" spacing={2} sx={{ mb: 2 }} alignItems="center">
            <TextField
              select
              size="small"
              label="Filtrar especie"
              value={filtroEspecie}
              onChange={(e) => setFiltroEspecie(e.target.value as LecheriaEspecie | '')}
              sx={{ minWidth: 160 }}
            >
              <MenuItem value="">Todas</MenuItem>
              {ESPECIES.map((op) => (
                <MenuItem key={op.valor} value={op.valor}>{op.etiqueta}</MenuItem>
              ))}
            </TextField>
            <Button variant="contained" onClick={() => abrirNuevo('raza')}>
              Nueva raza
            </Button>
          </Stack>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Especie</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell align="right">Editar</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {razas.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>{r.nombre}</TableCell>
                    <TableCell>{r.especie}</TableCell>
                    <TableCell>
                      <Chip size="small" label={r.activo !== false ? 'Activo' : 'Inactivo'} />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => abrirEditar('raza', r)}>
                        <Pencil size={16} />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      {pestana === 1 && (
        <>
          <Button variant="contained" sx={{ mb: 2 }} onClick={() => abrirNuevo('motivo')}>
            Nuevo motivo de baja
          </Button>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Estado</TableCell>
                  <TableCell align="right">Editar</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {motivos.map((m) => (
                  <TableRow key={m.id}>
                    <TableCell>{m.nombre}</TableCell>
                    <TableCell>
                      <Chip size="small" label={m.activo !== false ? 'Activo' : 'Inactivo'} />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => abrirEditar('motivo', m)}>
                        <Pencil size={16} />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} maxWidth="xs" fullWidth>
        <DialogTitle>
          {editando ? 'Editar' : 'Nuevo'}{' '}
          {tipoCatalogo === 'raza' ? 'raza' : 'motivo de baja'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={nombre} onChange={(e) => setNombre(e.target.value)} />
            {tipoCatalogo === 'raza' && (
              <TextField select label="Especie" fullWidth value={especie} onChange={(e) => setEspecie(e.target.value as LecheriaEspecie)}>
                {ESPECIES.map((op) => (
                  <MenuItem key={op.valor} value={op.valor}>{op.etiqueta}</MenuItem>
                ))}
              </TextField>
            )}
            <FormControlLabel control={<Switch checked={activo} onChange={(e) => setActivo(e.target.checked)} />} label="Activo" />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardar()}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CatalogosLecheriaScreen;
