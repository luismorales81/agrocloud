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
  Paper,
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
import { Pencil } from 'lucide-react';
import {
  actualizarRazaPonedoras,
  crearRazaPonedoras,
  listarRazasPonedoras,
  mensajeErrorPonedoras,
  type AvicolaPonedorasRaza,
} from '../services/avicolaPonedorasService';

const RazasPonedorasScreen: React.FC = () => {
  const [razas, setRazas] = useState<AvicolaPonedorasRaza[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogoRaza, setDialogoRaza] = useState(false);
  const [razaEditando, setRazaEditando] = useState<AvicolaPonedorasRaza | null>(null);
  const [razaNombre, setRazaNombre] = useState('');
  const [razaActivo, setRazaActivo] = useState(true);

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      setRazas(await listarRazasPonedoras());
      setError(null);
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
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
        await actualizarRazaPonedoras(razaEditando.id, { nombre: razaNombre.trim(), activo: razaActivo });
      } else {
        await crearRazaPonedoras({ nombre: razaNombre.trim(), activo: razaActivo });
      }
      setDialogoRaza(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeErrorPonedoras(err));
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Razas — Ponedoras
      </Typography>
      <Typography variant="body2" color="text.secondary" paragraph>
        Cargá las razas antes de armar un galpón. Un galpón puede tener varias.
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
                  razas.map((raza) => (
                    <TableRow key={raza.id}>
                      <TableCell>{raza.nombre}</TableCell>
                      <TableCell>
                        <Chip size="small" label={raza.activo !== false ? 'Sí' : 'No'} />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          size="small"
                          aria-label="Editar"
                          onClick={() => {
                            setRazaEditando(raza);
                            setRazaNombre(raza.nombre);
                            setRazaActivo(raza.activo !== false);
                            setDialogoRaza(true);
                          }}
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
            control={<Switch checked={razaActivo} onChange={(_, valor) => setRazaActivo(valor)} />}
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

export default RazasPonedorasScreen;
