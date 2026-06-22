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
import type { FeedlotCatalogo, FeedlotProveedorOrigen, FeedlotTipoProveedor } from '../types';
import {
  actualizarCategoria,
  actualizarMotivoMuerte,
  actualizarProveedor,
  actualizarRaza,
  crearCategoria,
  crearMotivoMuerte,
  crearProveedor,
  crearRaza,
  listarCategorias,
  listarMotivosMuerte,
  listarProveedores,
  listarRazas,
  mensajeError,
} from '../services/feedlotApi';

const TIPOS_PROVEEDOR: { valor: FeedlotTipoProveedor; etiqueta: string }[] = [
  { valor: 'PROPIETARIO', etiqueta: 'Propietario' },
  { valor: 'CONSIGNATARIO', etiqueta: 'Consignatario' },
  { valor: 'CAMPO_PROPIO', etiqueta: 'Campo propio' },
];

const CatalogosFeedlotScreen: React.FC = () => {
  const navigate = useNavigate();
  const [pestana, setPestana] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [categorias, setCategorias] = useState<FeedlotCatalogo[]>([]);
  const [razas, setRazas] = useState<FeedlotCatalogo[]>([]);
  const [motivos, setMotivos] = useState<FeedlotCatalogo[]>([]);
  const [proveedores, setProveedores] = useState<FeedlotProveedorOrigen[]>([]);

  const [dialogo, setDialogo] = useState(false);
  const [tipoCatalogo, setTipoCatalogo] = useState<'categoria' | 'raza' | 'motivo' | 'proveedor'>('categoria');
  const [editando, setEditando] = useState<FeedlotCatalogo | FeedlotProveedorOrigen | null>(null);
  const [nombre, setNombre] = useState('');
  const [activo, setActivo] = useState(true);
  const [tipoProveedor, setTipoProveedor] = useState<FeedlotTipoProveedor>('PROPIETARIO');

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const [c, r, m, p] = await Promise.all([
        listarCategorias(),
        listarRazas(),
        listarMotivosMuerte(),
        listarProveedores(),
      ]);
      setCategorias(c);
      setRazas(r);
      setMotivos(m);
      setProveedores(p);
      setError(null);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const abrirNuevo = (tipo: typeof tipoCatalogo) => {
    setTipoCatalogo(tipo);
    setEditando(null);
    setNombre('');
    setActivo(true);
    setTipoProveedor('PROPIETARIO');
    setDialogo(true);
  };

  const abrirEditar = (tipo: typeof tipoCatalogo, item: FeedlotCatalogo | FeedlotProveedorOrigen) => {
    setTipoCatalogo(tipo);
    setEditando(item);
    setNombre(item.nombre);
    setActivo(item.activo !== false);
    if ('tipo' in item && item.tipo) {
      setTipoProveedor(item.tipo as FeedlotTipoProveedor);
    }
    setDialogo(true);
  };

  const guardar = async () => {
    try {
      if (!nombre.trim()) {
        setError('El nombre es obligatorio.');
        return;
      }
      if (tipoCatalogo === 'categoria') {
        const cuerpo = { nombre: nombre.trim(), activo };
        if (editando) await actualizarCategoria(editando.id, cuerpo);
        else await crearCategoria(cuerpo);
      } else if (tipoCatalogo === 'raza') {
        const cuerpo = { nombre: nombre.trim(), activo };
        if (editando) await actualizarRaza(editando.id, cuerpo);
        else await crearRaza(cuerpo);
      } else if (tipoCatalogo === 'motivo') {
        const cuerpo = { nombre: nombre.trim(), activo };
        if (editando) await actualizarMotivoMuerte(editando.id, cuerpo);
        else await crearMotivoMuerte(cuerpo);
      } else {
        const cuerpo = { nombre: nombre.trim(), tipo: tipoProveedor, activo };
        if (editando) await actualizarProveedor(editando.id, cuerpo);
        else await crearProveedor(cuerpo);
      }
      setDialogo(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  const tituloPestana = ['Categorías', 'Razas', 'Motivos de muerte', 'Proveedores de origen'][pestana];

  const renderTablaCatalogo = (
    items: FeedlotCatalogo[],
    tipo: 'categoria' | 'raza' | 'motivo'
  ) => (
    <>
      <Button variant="contained" size="small" sx={{ mb: 2 }} onClick={() => abrirNuevo(tipo)}>
        Nuevo registro
      </Button>
      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Activo</TableCell>
              <TableCell align="right">Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3}>
                  <Typography color="text.secondary">Sin registros.</Typography>
                </TableCell>
              </TableRow>
            ) : (
              items.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.nombre}</TableCell>
                  <TableCell>
                    <Chip size="small" label={item.activo !== false ? 'Sí' : 'No'} />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton size="small" onClick={() => abrirEditar(tipo, item)}>
                      <Pencil size={16} />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Typography variant="h4">Catálogos feedlot</Typography>
        <Button variant="outlined" onClick={() => navigate('/feedlot/panel')}>
          Panel
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={1} sx={{ my: 2 }}>
          <CircularProgress size={22} />
          <Typography variant="body2">Cargando catálogos…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }}>
            <Tab label="Categorías" />
            <Tab label="Razas" />
            <Tab label="Motivos muerte" />
            <Tab label="Proveedores" />
          </Tabs>

          <Typography variant="h6" sx={{ mb: 1 }}>
            {tituloPestana}
          </Typography>

          {pestana === 0 && renderTablaCatalogo(categorias, 'categoria')}
          {pestana === 1 && renderTablaCatalogo(razas, 'raza')}
          {pestana === 2 && renderTablaCatalogo(motivos, 'motivo')}
          {pestana === 3 && (
            <>
              <Button variant="contained" size="small" sx={{ mb: 2 }} onClick={() => abrirNuevo('proveedor')}>
                Nuevo proveedor
              </Button>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Nombre</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell>Activo</TableCell>
                      <TableCell align="right">Acciones</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {proveedores.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4}>
                          <Typography color="text.secondary">Sin proveedores.</Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      proveedores.map((p) => (
                        <TableRow key={p.id}>
                          <TableCell>{p.nombre}</TableCell>
                          <TableCell>{p.tipo ?? '—'}</TableCell>
                          <TableCell>
                            <Chip size="small" label={p.activo !== false ? 'Sí' : 'No'} />
                          </TableCell>
                          <TableCell align="right">
                            <IconButton size="small" onClick={() => abrirEditar('proveedor', p)}>
                              <Pencil size={16} />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          )}
        </>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} fullWidth maxWidth="sm">
        <DialogTitle>
          {editando ? 'Editar' : 'Nuevo'}{' '}
          {tipoCatalogo === 'categoria'
            ? 'categoría'
            : tipoCatalogo === 'raza'
              ? 'raza'
              : tipoCatalogo === 'motivo'
                ? 'motivo'
                : 'proveedor'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" fullWidth required value={nombre} onChange={(ev) => setNombre(ev.target.value)} />
            {tipoCatalogo === 'proveedor' && (
              <TextField
                select
                label="Tipo"
                fullWidth
                value={tipoProveedor}
                onChange={(ev) => setTipoProveedor(ev.target.value as FeedlotTipoProveedor)}
              >
                {TIPOS_PROVEEDOR.map((t) => (
                  <MenuItem key={t.valor} value={t.valor}>
                    {t.etiqueta}
                  </MenuItem>
                ))}
              </TextField>
            )}
            <FormControlLabel
              control={<Switch checked={activo} onChange={(ev) => setActivo(ev.target.checked)} />}
              label="Activo"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardar()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CatalogosFeedlotScreen;
