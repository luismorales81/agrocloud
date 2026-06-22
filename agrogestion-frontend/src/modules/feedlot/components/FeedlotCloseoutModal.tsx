import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Stack,
  Typography,
} from '@mui/material';
import type { FeedlotCloseout } from '../types';
import { mensajeError, obtenerCloseout } from '../services/feedlotApi';

interface Props {
  loteId: number;
  abierto: boolean;
  onCerrar: () => void;
  onDescargarPdf?: (loteId: number) => void;
}

function fmt(n: number | string | null | undefined, dec = 2): string {
  if (n == null || n === '') return '—';
  const v = Number(n);
  if (Number.isNaN(v)) return String(n);
  return v.toFixed(dec);
}

const FeedlotCloseoutModal: React.FC<Props> = ({ loteId, abierto, onCerrar, onDescargarPdf }) => {
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [datos, setDatos] = useState<FeedlotCloseout | null>(null);

  useEffect(() => {
    if (!abierto || !loteId) return;
    setCargando(true);
    setError(null);
    obtenerCloseout(loteId)
      .then(setDatos)
      .catch((e: unknown) => setError(mensajeError(e)))
      .finally(() => setCargando(false));
  }, [abierto, loteId]);

  const fila = (etiqueta: string, valor: string) => (
    <Box display="flex" justifyContent="space-between" gap={2}>
      <Typography variant="body2" color="text.secondary">{etiqueta}</Typography>
      <Typography variant="body2" fontWeight={500}>{valor}</Typography>
    </Box>
  );

  return (
    <Dialog open={abierto} onClose={onCerrar} fullWidth maxWidth="sm">
      <DialogTitle>Closeout del lote</DialogTitle>
      <DialogContent>
        {cargando && (
          <Box display="flex" alignItems="center" gap={1} py={2}>
            <CircularProgress size={22} />
            <Typography variant="body2">Calculando closeout…</Typography>
          </Box>
        )}
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {datos && !cargando && (
          <Stack spacing={1.5}>
            <Typography variant="subtitle2">{datos.loteNombre ?? `Lote ${datos.loteId}`}</Typography>
            <Typography variant="caption" color="text.secondary">
              {datos.fechaIngreso} → {datos.fechaReferencia} ({datos.diasEnFeedlot ?? '—'} días) · Método: {datos.metodoCloseout ?? 'DEADS_IN'}
            </Typography>
            <Divider />
            <Typography variant="overline">Indicadores zootécnicos</Typography>
            {fila('Cabezas inicial / actuales', `${datos.cabezasInicial ?? '—'} / ${datos.cabezasActuales ?? '—'}`)}
            {fila('Muertes (total)', String(datos.totalMuertes ?? '—'))}
            {fila('Mortalidad (%)', fmt(datos.mortalidadPct))}
            {fila('Peso ingreso / actual (kg)', `${fmt(datos.pesoIngresoKg)} / ${fmt(datos.pesoActualKg)}`)}
            {fila('GMD (kg/día)', fmt(datos.gmd, 3))}
            {fila('Kg ganados (aprox.)', fmt(datos.kgGanados))}
            {fila('Alimento total (kg)', fmt(datos.totalAlimentoKg))}
            {fila('Conversión alimenticia', fmt(datos.conversionAlimenticia, 3))}
            {fila('Head days', fmt(datos.headDays))}
            <Divider />
            <Typography variant="overline">Economía</Typography>
            {fila('Costo compra', fmt(datos.costoCompra))}
            {fila('Costo alimento', fmt(datos.costoAlimento))}
            {fila('Costo hotelería', fmt(datos.costoHoteleria))}
            {fila('Costo acumulado', fmt(datos.costoAcumulado))}
            {fila('Ingresos ventas', fmt(datos.ingresosVentas))}
            {fila('Margen', fmt(datos.margen))}
            {fila('Breakeven ($/kg)', fmt(datos.breakevenKg))}
          </Stack>
        )}
      </DialogContent>
      <DialogActions>
        {onDescargarPdf && datos && (
          <Button onClick={() => onDescargarPdf(loteId)}>Descargar PDF</Button>
        )}
        <Button onClick={onCerrar}>Cerrar</Button>
      </DialogActions>
    </Dialog>
  );
};

export default FeedlotCloseoutModal;
