import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  listarLotesHuevos,
  obtenerReporteAnalisisPosturaHuevos,
  AvicolaHuevosReporteAnalisisRespuesta,
  type AvicolaHuevoLoteRespuesta,
  mensajeError,
} from '../services/avicolaHuevosApi';
import {
  exportarAnalisisPosturaHuevosConGraficos,
  type FormatoExportacionAvicola,
} from '../../../utilidades/exportacionAvicola';
import { esperarRenderizadoGraficos, type SeccionGraficoExportacion } from '../../../utilidades/exportacionConGraficos';

const haceDias = (d: number) => {
  const x = new Date();
  x.setDate(x.getDate() - d);
  return x.toISOString().slice(0, 10);
};

const ReportesHuevosScreen: React.FC = () => {
  const [lotes, setLotes] = useState<AvicolaHuevoLoteRespuesta[]>([]);
  const [loteId, setLoteId] = useState<number | ''>('');
  const [desde, setDesde] = useState(haceDias(30));
  const [hasta, setHasta] = useState(() => new Date().toISOString().slice(0, 10));
  const [datos, setDatos] = useState<AvicolaHuevosReporteAnalisisRespuesta | null>(null);
  const [cargando, setCargando] = useState(false);
  const [exportando, setExportando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refGraficoPosturaEdad = useRef<HTMLDivElement>(null);
  const refGraficoHuevosPorAve = useRef<HTMLDivElement>(null);
  const refGraficoClima = useRef<HTMLDivElement>(null);
  const refGraficoGastos = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const lista = await listarLotesHuevos();
        if (!cancelado) {
          setLotes(lista);
          if (lista.length === 1) {
            setLoteId(lista[0].id);
          }
        }
      } catch {
        if (!cancelado) setLotes([]);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const cargar = useCallback(async () => {
    if (loteId === '') {
      setError('Elegí un lote de postura.');
      setDatos(null);
      return;
    }
    try {
      setCargando(true);
      setError(null);
      const r = await obtenerReporteAnalisisPosturaHuevos(loteId, desde, hasta);
      setDatos(r);
    } catch (e: unknown) {
      setError(mensajeError(e));
      setDatos(null);
    } finally {
      setCargando(false);
    }
  }, [loteId, desde, hasta]);

  const exportarInforme = async (formato: FormatoExportacionAvicola) => {
    if (!datos) {
      setError('Primero generá el informe con «Generar».');
      return;
    }
    const secciones: SeccionGraficoExportacion[] = [];
    const g1 = refGraficoPosturaEdad.current;
    if (g1) secciones.push({ titulo: 'Postura diaria y edad del plantel', elemento: g1 });
    const g2 = refGraficoHuevosPorAve.current;
    if (g2) secciones.push({ titulo: 'Huevos por ave y día', elemento: g2 });
    const g3 = refGraficoClima.current;
    if (g3) secciones.push({ titulo: 'Temperatura y humedad', elemento: g3 });
    const g4 = refGraficoGastos.current;
    if (g4) secciones.push({ titulo: 'Gastos diarios estimados', elemento: g4 });

    if (secciones.length === 0) {
      setError('No hay gráficos listos para exportar. Generá el informe y esperá un momento.');
      return;
    }

    try {
      setError(null);
      setExportando(true);
      await esperarRenderizadoGraficos();
      await exportarAnalisisPosturaHuevosConGraficos(datos, desde, hasta, formato, secciones);
    } catch (e: unknown) {
      setError(mensajeError(e));
    } finally {
      setExportando(false);
    }
  };

  const puntosPostura = useMemo(() => {
    const s = datos?.seriePostura ?? [];
    return s.map((p) => ({
      etiqueta: p.fecha ? String(p.fecha).slice(5) : '',
      huevos: p.totalHuevos ?? 0,
      edad: p.diasEdadLote ?? 0,
      porAve: p.huevosPorAve != null ? Number(p.huevosPorAve) : null,
      temp: p.temperatura != null ? Number(p.temperatura) : null,
      hum: p.humedad != null ? Number(p.humedad) : null,
    }));
  }, [datos]);

  const puntosGasto = useMemo(() => {
    const s = datos?.serieGastos ?? [];
    return s.map((g) => ({
      etiqueta: g.fecha ? String(g.fecha).slice(5) : '',
      costo: g.costoEstimado != null ? Number(g.costoEstimado) : 0,
    }));
  }, [datos]);

  const totalGasto = datos?.totalCostoConsumosRango != null ? Number(datos.totalCostoConsumosRango) : 0;

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 1200 }}>
      <Typography variant="h5" gutterBottom>
        Reportes de postura
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Postura diaria y edad del plantel (días desde el inicio del lote), temperatura y humedad registradas en producción,
        y gasto estimado por consumos de insumo (cantidad × precio unitario del insumo en el catálogo).
      </Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center" sx={{ mb: 2 }} flexWrap="wrap">
        <FormControl size="small" sx={{ minWidth: 220 }}>
          <InputLabel id="lote-reporte-label">Lote</InputLabel>
          <Select
            labelId="lote-reporte-label"
            label="Lote"
            value={loteId === '' ? '' : String(loteId)}
            onChange={(e) => setLoteId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">
              <em>Seleccionar…</em>
            </MenuItem>
            {lotes.map((l) => (
              <MenuItem key={l.id} value={String(l.id)}>
                {l.nombre ?? `Lote #${l.id}`}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <TextField
          size="small"
          type="date"
          label="Desde"
          InputLabelProps={{ shrink: true }}
          value={desde}
          onChange={(e) => setDesde(e.target.value)}
        />
        <TextField
          size="small"
          type="date"
          label="Hasta"
          InputLabelProps={{ shrink: true }}
          value={hasta}
          onChange={(e) => setHasta(e.target.value)}
        />
        <Button variant="contained" onClick={() => void cargar()} disabled={cargando}>
          Generar
        </Button>
        <Button variant="outlined" disabled={!datos || cargando || exportando} onClick={() => void exportarInforme('excel')}>
          {exportando ? 'Exportando…' : 'Excel (gráficos + datos)'}
        </Button>
        <Button variant="outlined" disabled={!datos || cargando || exportando} onClick={() => void exportarInforme('pdf')}>
          {exportando ? 'Exportando…' : 'PDF (gráficos + datos)'}
        </Button>
      </Stack>

      {cargando && (
        <Stack direction="row" alignItems="center" gap={1} my={2}>
          <CircularProgress size={24} />
          <Typography variant="body2">Cargando…</Typography>
        </Stack>
      )}

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {datos && !cargando && (
        <Box data-export-informe-huevos="analisis-postura">
          <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
            <Typography variant="subtitle1">
              Lote: <strong>{datos.nombreLote}</strong> — inicio{' '}
              <strong>{datos.fechaInicioLote ?? '—'}</strong> — aves actuales{' '}
              <strong>{datos.cantidadAvesActual ?? '—'}</strong>
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Total gastos estimados (consumos en el período):{' '}
              <strong>
                {totalGasto.toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </strong>
            </Typography>
          </Paper>

          <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
            Postura diaria y edad del plantel (días)
          </Typography>
          <Box ref={refGraficoPosturaEdad} sx={{ width: '100%', height: 320, mb: 3, bgcolor: 'background.paper' }}>
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={puntosPostura} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="etiqueta" tick={{ fontSize: 11 }} />
                <YAxis yAxisId="izq" tick={{ fontSize: 11 }} label={{ value: 'Huevos', angle: -90, position: 'insideLeft' }} />
                <YAxis
                  yAxisId="der"
                  orientation="right"
                  tick={{ fontSize: 11 }}
                  label={{ value: 'Edad (días)', angle: 90, position: 'insideRight' }}
                />
                <Tooltip formatter={(v: number, name: string) => [v, name]} />
                <Legend />
                <Bar yAxisId="izq" dataKey="huevos" name="Huevos / día" fill="#ca8a04" radius={[2, 2, 0, 0]} />
                <Line yAxisId="der" type="monotone" dataKey="edad" name="Edad plantel" stroke="#2563eb" dot={false} strokeWidth={2} />
              </ComposedChart>
            </ResponsiveContainer>
          </Box>

          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Huevos por ave y día
          </Typography>
          <Box ref={refGraficoHuevosPorAve} sx={{ width: '100%', height: 260, mb: 3, bgcolor: 'background.paper' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={puntosPostura.filter((p) => p.porAve != null)} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="etiqueta" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} domain={['auto', 'auto']} />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="porAve" name="Huevos / ave" stroke="#16a34a" dot strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </Box>

          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Temperatura y humedad (registro diario de postura)
          </Typography>
          <Box ref={refGraficoClima} sx={{ width: '100%', height: 280, mb: 3, bgcolor: 'background.paper' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={puntosPostura} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="etiqueta" tick={{ fontSize: 11 }} />
                <YAxis yAxisId="t" tick={{ fontSize: 11 }} label={{ value: '°C', angle: -90, position: 'insideLeft' }} />
                <YAxis
                  yAxisId="h"
                  orientation="right"
                  tick={{ fontSize: 11 }}
                  label={{ value: '% HR', angle: 90, position: 'insideRight' }}
                />
                <Tooltip />
                <Legend />
                <Line yAxisId="t" type="monotone" dataKey="temp" name="Temperatura °C" stroke="#dc2626" connectNulls dot={false} />
                <Line yAxisId="h" type="monotone" dataKey="hum" name="Humedad %" stroke="#0891b2" connectNulls dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </Box>

          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Gastos diarios estimados (insumos)
          </Typography>
          <Box ref={refGraficoGastos} sx={{ width: '100%', height: 300, bgcolor: 'background.paper' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={puntosGasto} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="etiqueta" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip formatter={(v: number) => [v.toLocaleString('es-AR', { maximumFractionDigits: 2 }), 'Importe']} />
                <Legend />
                <Bar dataKey="costo" name="Costo estimado" fill="#7c3aed" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Box>
        </Box>
      )}
    </Box>
  );
};

export default ReportesHuevosScreen;
