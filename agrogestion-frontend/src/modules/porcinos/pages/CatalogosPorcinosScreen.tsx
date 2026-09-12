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
import type { PorcinosCatalogo } from '../typesApiV2';
import {
  crearCausaMortalidad,
  crearMotivoBaja,
  crearRaza,
  crearTipoServicio,
  listarCausasMortalidad,
  listarMotivosBaja,
  listarRazas,
  listarTiposServicio,
  mensajeError,
} from '../services/porcinosApi';

const CatalogosPorcinosScreen: React.FC = () => {
  const [pestana, setPestana] = useState(0);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [razas, setRazas] = useState<PorcinosCatalogo[]>([]);
  const [motivosBaja, setMotivosBaja] = useState<PorcinosCatalogo[]>([]);
  const [causasMortalidad, setCausasMortalidad] = useState<PorcinosCatalogo[]>([]);
  const [tiposServicio, setTiposServicio] = useState<PorcinosCatalogo[]>([]);
  const [dialogo, setDialogo] = useState(false);
  const [nombreNuevo, setNombreNuevo] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [r, m, c, t] = await Promise.all([
        listarRazas(),
        listarMotivosBaja(),
        listarCausasMortalidad(),
        listarTiposServicio(),
      ]);
      setRazas(r);
      setMotivosBaja(m);
      setCausasMortalidad(c);
      setTiposServicio(t);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const listas = [razas, motivosBaja, causasMortalidad, tiposServicio];
  const titulos = ['Razas', 'Motivos de baja', 'Causas de mortalidad', 'Tipos de servicio'];
  const activa = listas[pestana] ?? [];

  const guardarCatalogo = async () => {
    if (!nombreNuevo.trim()) {
      setError('El nombre es obligatorio.');
      return;
    }
    const cuerpo = { nombre: nombreNuevo.trim(), activo: true };
    try {
      if (pestana === 0) await crearRaza(cuerpo);
      else if (pestana === 1) await crearMotivoBaja(cuerpo);
      else if (pestana === 2) await crearCausaMortalidad(cuerpo);
      else await crearTipoServicio(cuerpo);
      setDialogo(false);
      setNombreNuevo('');
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 900 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="BookOpen" size={28} />
          <Typography variant="h4" component="h1">
            Catálogos
          </Typography>
        </Stack>
        <Button variant="contained" onClick={() => setDialogo(true)}>
          Agregar ítem
        </Button>
      </Stack>

      {cargando && <CircularProgress size={24} />}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <>
          <Tabs value={pestana} onChange={(_, v) => setPestana(v)} sx={{ mb: 2 }} variant="scrollable">
            {titulos.map((t, i) => (
              <Tab key={t} label={`${t} (${listas[i]?.length ?? 0})`} />
            ))}
          </Tabs>

          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {activa.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={2} align="center">
                      <Typography color="text.secondary" sx={{ py: 2 }}>
                        Sin registros en este catálogo.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  activa.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.nombre}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={item.activo !== false ? 'Activo' : 'Inactivo'}
                          color={item.activo !== false ? 'success' : 'default'}
                        />
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} fullWidth maxWidth="xs">
        <DialogTitle>Agregar a {titulos[pestana]}</DialogTitle>
        <DialogContent>
          <TextField
            label="Nombre"
            fullWidth
            required
            sx={{ mt: 1 }}
            value={nombreNuevo}
            onChange={(e) => setNombreNuevo(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarCatalogo()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CatalogosPorcinosScreen;
