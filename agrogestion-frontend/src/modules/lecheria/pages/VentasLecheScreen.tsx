import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { Icon } from '../../../components/icons';
import { useCampana } from '../../../contexts/CampanaContext';
import type { LecheriaVentaLeche } from '../types';
import { listarVentasLeche, mensajeError, registrarVentaLeche } from '../services/lecheriaApi';

function formatearMoneda(valor: number | null | undefined): string {
  if (valor == null) return '—';
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(valor);
}

const VentasLecheScreen: React.FC = () => {
  const { campanaActiva } = useCampana();
  const [ventas, setVentas] = useState<LecheriaVentaLeche[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogo, setDialogo] = useState(false);

  const [fecha, setFecha] = useState(() => new Date().toISOString().slice(0, 10));
  const [litros, setLitros] = useState('');
  const [precioLitro, setPrecioLitro] = useState('');
  const [comprador, setComprador] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const cargar = useCallback(async () => {
    try {
      setCargando(true);
      const lista = await listarVentasLeche();
      setVentas(lista);
      setError(null);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar, campanaActiva?.id]);

  const abrirNueva = () => {
    setFecha(new Date().toISOString().slice(0, 10));
    setLitros('');
    setPrecioLitro('');
    setComprador('');
    setObservaciones('');
    setDialogo(true);
  };

  const guardar = async () => {
    if (!litros.trim()) {
      setError('Ingresá los litros vendidos.');
      return;
    }
    try {
      await registrarVentaLeche({
        fecha,
        litros: parseFloat(litros.replace(',', '.')),
        precioLitro: precioLitro.trim() ? parseFloat(precioLitro.replace(',', '.')) : null,
        comprador: comprador.trim() || null,
        observaciones: observaciones.trim() || null,
      });
      setDialogo(false);
      await cargar();
    } catch (err: unknown) {
      setError(mensajeError(err));
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 900 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={2} sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="DollarSign" size={28} />
          <Typography variant="h4" component="h1">
            Ventas de leche
          </Typography>
        </Stack>
        <Button variant="contained" onClick={abrirNueva}>
          Registrar venta
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" spacing={2} sx={{ my: 2 }}>
          <CircularProgress size={28} />
          <Typography variant="body2">Cargando ventas…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="medium">
            <TableHead>
              <TableRow>
                <TableCell>Fecha</TableCell>
                <TableCell>Litros</TableCell>
                <TableCell>Precio/L</TableCell>
                <TableCell>Total</TableCell>
                <TableCell>Comprador</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ventas.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No hay ventas registradas.
                  </TableCell>
                </TableRow>
              ) : (
                ventas.map((v) => (
                  <TableRow key={v.id}>
                    <TableCell>{v.fecha?.slice(0, 10)}</TableCell>
                    <TableCell>{v.litros}</TableCell>
                    <TableCell>{v.precioLitro ?? '—'}</TableCell>
                    <TableCell>{formatearMoneda(v.total)}</TableCell>
                    <TableCell>{v.comprador ?? '—'}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Registrar venta de leche</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Fecha" type="date" fullWidth InputLabelProps={{ shrink: true }} value={fecha} onChange={(e) => setFecha(e.target.value)} />
            <TextField label="Litros" fullWidth required value={litros} onChange={(e) => setLitros(e.target.value)} />
            <TextField label="Precio por litro" fullWidth value={precioLitro} onChange={(e) => setPrecioLitro(e.target.value)} />
            <TextField label="Comprador" fullWidth value={comprador} onChange={(e) => setComprador(e.target.value)} />
            <TextField label="Observaciones" fullWidth multiline value={observaciones} onChange={(e) => setObservaciones(e.target.value)} />
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

export default VentasLecheScreen;
