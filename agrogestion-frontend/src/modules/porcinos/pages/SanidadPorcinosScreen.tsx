import React, { useCallback, useEffect, useMemo, useState } from 'react';
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
import { insumosService } from '../../../services/domain/insumosLaboresServices';
import { Autocomplete } from '../../../components/ui/Autocomplete';
import { Icon } from '../../../components/icons';
import {
  etiquetaInsumoOpcion,
  mapearInsumosDesdeApi,
  type InsumoOpcionFeedlot,
} from '../../feedlot/utils/mapearInsumos';
import type { PorcinosEventoSanitario, PorcinosLote, PorcinosTipoEventoSanitario } from '../typesApiV2';
import {
  listarEventosSanitariosEmpresa,
  listarLotes,
  mensajeError,
  registrarEventoSanitario,
} from '../services/porcinosApi';

const TIPOS_SANIDAD: { valor: PorcinosTipoEventoSanitario; etiqueta: string }[] = [
  { valor: 'VACUNA', etiqueta: 'Vacuna' },
  { valor: 'TRATAMIENTO', etiqueta: 'Tratamiento' },
  { valor: 'DIAGNOSTICO', etiqueta: 'Diagnóstico' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

function formatearFecha(fechaIso: string | undefined): string {
  if (!fechaIso) return '—';
  try {
    return new Date(fechaIso).toLocaleDateString('es-AR');
  } catch {
    return fechaIso.slice(0, 10);
  }
}

const SanidadPorcinosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [eventos, setEventos] = useState<PorcinosEventoSanitario[]>([]);
  const [lotesActivos, setLotesActivos] = useState<PorcinosLote[]>([]);
  const [insumos, setInsumos] = useState<InsumoOpcionFeedlot[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dialogo, setDialogo] = useState(false);

  const hoy = () => new Date().toISOString().slice(0, 10);
  const [loteId, setLoteId] = useState<number | ''>('');
  const [fecha, setFecha] = useState(hoy());
  const [tipo, setTipo] = useState<PorcinosTipoEventoSanitario>('VACUNA');
  const [descripcion, setDescripcion] = useState('');
  const [insumoId, setInsumoId] = useState<number | ''>('');
  const [cantidad, setCantidad] = useState('');
  const [diasRetiro, setDiasRetiro] = useState('');
  const [observaciones, setObservaciones] = useState('');

  const nomInsumo = (id?: number | null) =>
    id != null ? insumos.find((i) => i.id === id)?.nombre ?? `Insumo ${id}` : '—';

  const cargar = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const [ev, lotes, rawInsumos] = await Promise.all([
        listarEventosSanitariosEmpresa(),
        listarLotes({ estado: 'ACTIVO' }),
        insumosService.listar().catch(() => []),
      ]);
      setEventos(ev);
      setLotesActivos(lotes);
      setInsumos(mapearInsumosDesdeApi(rawInsumos));
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  const opcionesInsumo = useMemo(
    () =>
      insumos.map((i) => ({
        value: i.id,
        label: etiquetaInsumoOpcion(i),
        data: i,
      })),
    [insumos]
  );

  const abrirDialogo = () => {
    setLoteId(lotesActivos[0]?.id ?? '');
    setFecha(hoy());
    setTipo('VACUNA');
    setDescripcion('');
    setInsumoId('');
    setCantidad('');
    setDiasRetiro('');
    setObservaciones('');
    setDialogo(true);
  };

  const guardar = async () => {
    if (loteId === '') {
      setError('Seleccioná un lote.');
      return;
    }
    try {
      await registrarEventoSanitario(Number(loteId), {
        fecha,
        tipo,
        descripcion: descripcion.trim() || null,
        insumoId: insumoId === '' ? null : Number(insumoId),
        cantidadInsumo: cantidad.trim() === '' ? null : parseFloat(cantidad.replace(',', '.')),
        diasRetiro: diasRetiro.trim() === '' ? null : parseInt(diasRetiro, 10),
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
          <Icon name="Syringe" size={28} />
          <Typography variant="h4" component="h1">
            Sanidad
          </Typography>
        </Stack>
        <Button variant="contained" onClick={abrirDialogo} disabled={lotesActivos.length === 0}>
          Registrar evento
        </Button>
      </Stack>

      <Typography variant="body2" color="text.secondary" paragraph>
        Eventos sanitarios de todos los lotes. Los insumos aplicados descuentan del inventario general.
      </Typography>

      {cargando && <CircularProgress size={24} />}
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
                <TableCell>Fecha</TableCell>
                <TableCell>Lote</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell>Descripción</TableCell>
                <TableCell>Insumo</TableCell>
                <TableCell align="right">Retiro (días)</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {eventos.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography color="text.secondary" sx={{ py: 2 }}>
                      No hay eventos sanitarios registrados.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                eventos.map((e) => (
                  <TableRow key={e.id}>
                    <TableCell>{formatearFecha(e.fecha)}</TableCell>
                    <TableCell>{e.loteNombre ?? (e.loteId != null ? `#${e.loteId}` : '—')}</TableCell>
                    <TableCell>
                      <Chip size="small" label={e.tipo} />
                    </TableCell>
                    <TableCell>{e.descripcion ?? '—'}</TableCell>
                    <TableCell>{nomInsumo(e.insumoId)}</TableCell>
                    <TableCell align="right">{e.diasRetiro ?? '—'}</TableCell>
                    <TableCell align="right">
                      {e.loteId != null && (
                        <Button size="small" onClick={() => navigate(`/porcinos/lotes/${e.loteId}`)}>
                          Ver lote
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogo} onClose={() => setDialogo(false)} fullWidth maxWidth="sm">
        <DialogTitle>Registrar evento sanitario</DialogTitle>
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
                  {l.nombre}
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
            <TextField select label="Tipo" fullWidth value={tipo} onChange={(e) => setTipo(e.target.value as PorcinosTipoEventoSanitario)}>
              {TIPOS_SANIDAD.map((t) => (
                <MenuItem key={t.valor} value={t.valor}>
                  {t.etiqueta}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Descripción"
              fullWidth
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
            />
            <Autocomplete<InsumoOpcionFeedlot>
              label="Insumo (opcional, descuenta inventario)"
              options={opcionesInsumo}
              value={insumoId === '' ? undefined : insumoId}
              onChange={(value) => setInsumoId(value != null && value !== '' ? Number(value) : '')}
              placeholder="Buscar medicamento o insumo…"
              emptyMessage="No hay insumos en inventario"
              maxHeight={280}
            />
            <TextField
              label="Cantidad de insumo"
              fullWidth
              value={cantidad}
              onChange={(e) => setCantidad(e.target.value)}
              helperText="Obligatoria si seleccionás un insumo"
            />
            <TextField
              label="Días de retiro"
              type="number"
              fullWidth
              value={diasRetiro}
              onChange={(e) => setDiasRetiro(e.target.value)}
              inputProps={{ min: 0 }}
            />
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

export default SanidadPorcinosScreen;
