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
  MenuItem,
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
import type { PorcinosLote, PorcinosTipoVenta, PorcinosVenta } from '../typesApiV2';
import { listarLotes, listarVentas, mensajeError, registrarVentaLote } from '../services/porcinosApi';

const TIPOS_VENTA: { valor: PorcinosTipoVenta; etiqueta: string }[] = [
  { valor: 'FAENA', etiqueta: 'Faena' },
  { valor: 'ENGORDE', etiqueta: 'Venta engorde' },
  { valor: 'REPRODUCTOR', etiqueta: 'Venta reproductor' },
];

function formatearFecha(fechaIso: string | undefined): string {
  if (!fechaIso) return '—';
  try {
    return new Date(fechaIso).toLocaleDateString('es-AR');
  } catch {
    return fechaIso.slice(0, 10);
  }
}

function formatearMoneda(valor: number | null | undefined): string {
  if (valor == null) return '—';
  return valor.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

const VentasPorcinosScreen: React.FC = () => {
  const [ventas, setVentas] = useState<PorcinosVenta[]>([]);
  const [lotesActivos, setLotesActivos] = useState<PorcinosLote[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogo, setDialogo] = useState(false);

  const hoy = () => new Date().toISOString().slice(0, 10);
  const [loteId, setLoteId] = useState<number | ''>('');
  const [fecha, setFecha] = useState(hoy());
  const [tipo, setTipo] = useState<PorcinosTipoVenta>('FAENA');
  const [cabezas, setCabezas] = useState('');
  const [pesoProm, setPesoProm] = useState('');
  const [precioKg, setPrecioKg] = useState('');
  const [total, setTotal] = useState('');
  const [comprador, setComprador] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [lista, lotes] = await Promise.all([
        listarVentas(),
        listarLotes({ estado: 'ACTIVO' }),
      ]);
      setVentas(lista);
      setLotesActivos(lotes);
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const abrirDialogo = () => {
    setLoteId(lotesActivos[0]?.id ?? '');
    setFecha(hoy());
    setTipo('FAENA');
    setCabezas('');
    setPesoProm('');
    setPrecioKg('');
    setTotal('');
    setComprador('');
    setObservaciones('');
    setDialogo(true);
  };

  const guardar = async () => {
    if (loteId === '') {
      setError('Seleccioná un lote.');
      return;
    }
    const cab = parseInt(cabezas, 10);
    if (Number.isNaN(cab) || cab <= 0) {
      setError('Las cabezas deben ser mayores a cero.');
      return;
    }
    try {
      await registrarVentaLote(Number(loteId), {
        fecha,
        tipo,
        cabezas: cab,
        pesoPromedioKg: pesoProm.trim() === '' ? null : parseFloat(pesoProm.replace(',', '.')),
        precioKg: precioKg.trim() === '' ? null : parseFloat(precioKg.replace(',', '.')),
        total: total.trim() === '' ? null : parseFloat(total.replace(',', '.')),
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
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1100 }}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
        <Stack direction="row" alignItems="center" spacing={1}>
          <Icon name="DollarSign" size={28} />
          <Typography variant="h4" component="h1">
            Ventas
          </Typography>
        </Stack>
        <Button variant="contained" onClick={abrirDialogo} disabled={lotesActivos.length === 0}>
          Registrar venta
        </Button>
      </Stack>

      {cargando && <CircularProgress size={24} />}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {!cargando && !error && (
        <TableContainer component={Paper} variant="outlined">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Fecha</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Lote</TableCell>
                <TableCell align="right">Cabezas</TableCell>
                <TableCell align="right">Peso prom.</TableCell>
                <TableCell align="right">Total</TableCell>
                <TableCell>Comprador</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ventas.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography color="text.secondary" sx={{ py: 2 }}>
                      No hay ventas registradas.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                ventas.map((v) => (
                  <TableRow key={v.id}>
                    <TableCell>{formatearFecha(v.fecha)}</TableCell>
                    <TableCell>
                      <Chip size="small" label={v.tipo} />
                    </TableCell>
                    <TableCell>{v.loteNombre ?? (v.loteId != null ? `#${v.loteId}` : '—')}</TableCell>
                    <TableCell align="right">{v.cabezas}</TableCell>
                    <TableCell align="right">{v.pesoPromedioKg ?? '—'}</TableCell>
                    <TableCell align="right">{formatearMoneda(v.total ?? null)}</TableCell>
                    <TableCell>{v.comprador ?? '—'}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} fullWidth maxWidth="sm">
        <DialogTitle>Registrar venta</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              select
              label="Lote"
              fullWidth
              required
              value={loteId}
              onChange={(e) => setLoteId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              {lotesActivos.map((l) => (
                <MenuItem key={l.id} value={l.id}>
                  {l.nombre} ({l.cabezasActuales ?? 0} cab.)
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Fecha"
              type="date"
              fullWidth
              InputLabelProps={{ shrink: true }}
              value={fecha}
              onChange={(e) => setFecha(e.target.value)}
            />
            <TextField select label="Tipo" fullWidth value={tipo} onChange={(e) => setTipo(e.target.value as PorcinosTipoVenta)}>
              {TIPOS_VENTA.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>
                  {t.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Cabezas"
              type="number"
              fullWidth
              required
              value={cabezas}
              onChange={(e) => setCabezas(e.target.value)}
              inputProps={{ min: 1 }}
            />
            <TextField
              label="Peso promedio (kg)"
              fullWidth
              value={pesoProm}
              onChange={(e) => setPesoProm(e.target.value)}
            />
            <TextField
              label="Precio por kg"
              fullWidth
              value={precioKg}
              onChange={(e) => setPrecioKg(e.target.value)}
            />
            <TextField
              label="Total (opcional, se calcula si hay precio y peso)"
              fullWidth
              value={total}
              onChange={(e) => setTotal(e.target.value)}
            />
            <TextField label="Comprador" fullWidth value={comprador} onChange={(e) => setComprador(e.target.value)} />
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={2}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
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

export default VentasPorcinosScreen;
