import React, { useCallback, useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControlLabel,
  Switch,
  Chip,
  IconButton,
} from '@mui/material';
import { Pencil } from 'lucide-react';
import {
  listarRazasHuevos,
  crearRazaHuevos,
  actualizarRazaHuevos,
  AvicolaHuevoRazaRespuesta,
  mensajeError,
} from '../services/avicolaHuevosApi';

const RazasHuevosScreen: React.FC = () => {
  const [razas, setRazas] = useState<AvicolaHuevoRazaRespuesta[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogoRaza, setDialogoRaza] = useState(false);
  const [razaEditando, setRazaEditando] = useState<AvicolaHuevoRazaRespuesta | null>(null);
  const [razaNombre, setRazaNombre] = useState('');
  const [razaActivo, setRazaActivo] = useState(true);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const r = await listarRazasHuevos();
      setRazas(r);
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

  const guardarRaza = async () => {
    try {
      if (!razaNombre.trim()) {
        setError('El nombre de la raza es obligatorio.');
        return;
      }
      if (razaEditando) {
        await actualizarRazaHuevos(razaEditando.id, { nombre: razaNombre.trim(), activo: razaActivo });
      } else {
        await crearRazaHuevos({ nombre: razaNombre.trim(), activo: razaActivo });
      }
      setDialogoRaza(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Razas / líneas — Huevos
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        Líneas genéticas o comerciales usadas en los lotes de postura. Los establecimientos se administran en su propia
        entrada del menú.
      </Typography>

      {cargando && (
        <Box display="flex" alignItems="center" gap={1} my={2}>
          <CircularProgress size={22} />
          <Typography variant="body2">Cargando…</Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <Box>
          <Button
            variant="contained"
            sx={{ mb: 2 }}
            onClick={() => {
              setRazaEditando(null);
              setRazaNombre('');
              setRazaActivo(true);
              setDialogoRaza(true);
            }}
          >
            Nueva raza
          </Button>
          <TableContainer component={Paper}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Activo</TableCell>
                  <TableCell align="right">Acciones</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {razas.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3}>
                      <Typography color="text.secondary">Sin registros.</Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  razas.map((r) => (
                    <TableRow key={r.id}>
                      <TableCell>{r.nombre}</TableCell>
                      <TableCell>
                        <Chip size="small" label={r.activo !== false ? 'Sí' : 'No'} />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          onClick={() => {
                            setRazaEditando(r);
                            setRazaNombre(r.nombre);
                            setRazaActivo(r.activo !== false);
                            setDialogoRaza(true);
                          }}
                          aria-label="Editar"
                        >
                          <Pencil size={18} />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}

      <Dialog open={dialogoRaza} onClose={() => setDialogoRaza(false)} fullWidth maxWidth="sm">
        <DialogTitle>{razaEditando ? 'Editar raza' : 'Nueva raza'}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Nombre"
            fullWidth
            value={razaNombre}
            onChange={(ev) => setRazaNombre(ev.target.value)}
          />
          <FormControlLabel
            control={<Switch checked={razaActivo} onChange={(_, c) => setRazaActivo(c)} />}
            label="Activo"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoRaza(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardarRaza()}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default RazasHuevosScreen;
