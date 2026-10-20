import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardActionArea,
  CardContent,
  Checkbox,
  FormControlLabel,
  MenuItem,
  Paper,
  Stack,
  Step,
  StepLabel,
  Stepper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import type {
  FeedlotAplicador,
  FeedlotCatalogo,
  FeedlotLote,
  FeedlotMapeoColumnas,
  FeedlotOrigenInsumo,
  FeedlotPerfilLecturaManga,
  FeedlotResumenArchivoManga,
  FeedlotSesionMangaSolicitud,
  FeedlotSubtipoBaja,
  FeedlotTipoEventoSanitario,
  FeedlotTipoSesionManga,
} from '../types';
import {
  analizarArchivoSesionManga,
  crearSesionManga,
  listarInsumos,
  listarLotes,
  listarMotivosMuerte,
  listarPerfilesLecturaManga,
  mensajeError,
  subirArchivoSesionManga,
} from '../services/feedlotApi';
import {
  etiquetaInsumoOpcion,
  mapearInsumosDesdeApi,
  type InsumoOpcionFeedlot,
} from '../utils/mapearInsumos';

const PASOS = ['Evento', 'Lecturas', 'Lote', 'Datos', 'Revisar'];

const EVENTOS: { valor: FeedlotTipoSesionManga; titulo: string; detalle: string }[] = [
  { valor: 'ENROLAMIENTO', titulo: 'Enrolar', detalle: 'Dar de alta las caravanas leídas en el lote' },
  { valor: 'PESADA', titulo: 'Pesar', detalle: 'Registrar kilos (CSV del cabezal o carga a mano)' },
  { valor: 'SANIDAD', titulo: 'Aplicar producto', detalle: 'Vacuna, tratamiento o diagnóstico a quienes pasaron' },
  { valor: 'RECUENTO', titulo: 'Contar', detalle: 'Quiénes están, quiénes faltan, lecturas ajenas' },
  { valor: 'BAJA', titulo: 'Dar de baja', detalle: 'Muerte, faena, venta en pie o descarte' },
];

function fechaIso(valor?: string | null): string {
  if (!valor) return new Date().toISOString().slice(0, 10);
  return valor.slice(0, 10);
}

function fechaCsv(iso?: string | null): string {
  if (!iso) return '';
  const d = iso.slice(0, 10).split('-');
  if (d.length !== 3) return '';
  return `${d[2]}/${d[1]}/${d[0]}`;
}

function horaCsv(iso?: string | null): string {
  if (!iso || iso.length < 16) return '00:00:00';
  return iso.slice(11, 19);
}

function armarCsvConPesos(resumen: FeedlotResumenArchivoManga, pesos: Record<string, string>): File {
  const lineas = ['EID,VID,Weight,Date,Time'];
  for (const l of resumen.lecturasDetalle ?? []) {
    const clave = l.eidNormalizado ?? l.eidCrudo ?? '';
    const peso = (pesos[clave] || (l.pesoKg != null ? String(l.pesoKg) : '')).trim();
    const eid = l.eidCrudo ?? l.eidNormalizado ?? '';
    lineas.push([eid, l.caravanaVisual ?? '', peso, fechaCsv(l.fechaHoraLectura), horaCsv(l.fechaHoraLectura)].join(','));
  }
  return new File([lineas.join('\n') + '\n'], 'pesada-manga.csv', { type: 'text/csv' });
}

const SesionMangaFeedlotScreen: React.FC = () => {
  const navigate = useNavigate();
  const [paso, setPaso] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [archivo, setArchivo] = useState<File | null>(null);
  const [resumen, setResumen] = useState<FeedlotResumenArchivoManga | null>(null);
  const [mostrarMapeo, setMostrarMapeo] = useState(false);
  const [mapeo, setMapeo] = useState<FeedlotMapeoColumnas>({ delimitador: 'AUTO' });
  const [tipo, setTipo] = useState<FeedlotTipoSesionManga | ''>('');
  const [perfiles, setPerfiles] = useState<FeedlotPerfilLecturaManga[]>([]);
  const [perfilLecturaId, setPerfilLecturaId] = useState<number | ''>('');
  const [cargarKilos, setCargarKilos] = useState(false);
  const [pesosManuales, setPesosManuales] = useState<Record<string, string>>({});
  const [lotes, setLotes] = useState<FeedlotLote[]>([]);
  const [loteId, setLoteId] = useState<number | ''>('');
  const [fecha, setFecha] = useState(new Date().toISOString().slice(0, 10));
  const [tipoEvento, setTipoEvento] = useState<FeedlotTipoEventoSanitario>('VACUNA');
  const [aplicador, setAplicador] = useState<FeedlotAplicador>('PERSONAL_ESTABLECIMIENTO');
  const [origen, setOrigen] = useState<FeedlotOrigenInsumo>('STOCK_EMPRESA');
  const [insumos, setInsumos] = useState<InsumoOpcionFeedlot[]>([]);
  const [insumoId, setInsumoId] = useState<number | ''>('');
  const [dosis, setDosis] = useState('');
  const [diasRetiro, setDiasRetiro] = useState('');
  const [subtipo, setSubtipo] = useState<FeedlotSubtipoBaja | ''>('');
  const [motivos, setMotivos] = useState<FeedlotCatalogo[]>([]);
  const [motivoMuerteId, setMotivoMuerteId] = useState<number | ''>('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    void listarLotes({ estado: 'ACTIVO' }).then(setLotes).catch((e) => setError(mensajeError(e)));
    void listarInsumos()
      .then((raw) => setInsumos(mapearInsumosDesdeApi(raw)))
      .catch((e) => {
        setInsumos([]);
        setError(mensajeError(e));
      });
    void listarMotivosMuerte().then(setMotivos).catch(() => setMotivos([]));
  }, []);

  useEffect(() => {
    if (!tipo) {
      setPerfiles([]);
      setPerfilLecturaId('');
      return;
    }
    void listarPerfilesLecturaManga(tipo).then((lista) => {
      setPerfiles(lista);
      const pred = lista.find((p) => p.predeterminado) ?? lista[0];
      setPerfilLecturaId(pred ? pred.id : '');
    }).catch(() => setPerfiles([]));
  }, [tipo]);

  const analizar = useCallback(async (file: File, map?: FeedlotMapeoColumnas, manual?: boolean) => {
    setError(null);
    try {
      const r = await analizarArchivoSesionManga(file, map ?? { delimitador: 'AUTO' }, {
        perfilLecturaId: perfilLecturaId === '' ? null : perfilLecturaId,
        tipo,
        cargaManualPeso: manual ?? cargarKilos,
      });
      setResumen(r);
      if (r.mapeo) setMapeo({ ...r.mapeo, delimitador: r.mapeo.delimitador ?? 'AUTO' });
      if (r.fechaMayoritaria) setFecha(fechaIso(r.fechaMayoritaria));
      if (r.loteSugeridoId && !r.mezclaLotes) setLoteId(r.loteSugeridoId);
      setMostrarMapeo(r.entendioArchivo === false);
    } catch (e) {
      setError(mensajeError(e));
    }
  }, [perfilLecturaId, tipo, cargarKilos]);

  const onArchivo = (file: File | null) => {
    setArchivo(file);
    setResumen(null);
    if (file) void analizar(file);
  };

  const loteElegido = lotes.find((l) => l.id === loteId);
  const pesadaLista = tipo !== 'PESADA' || Boolean(resumen?.tienePeso) || cargarKilos;
  const cabeceraLista = useMemo(() => {
    if (tipo === 'BAJA') return subtipo !== '';
    if (tipo === 'SANIDAD') {
      if (!tipoEvento || !aplicador) return false;
      if (origen === 'STOCK_EMPRESA' && (tipoEvento === 'VACUNA' || tipoEvento === 'TRATAMIENTO')) {
        return insumoId !== '' && dosis.trim() !== '';
      }
      return true;
    }
    return tipo !== '';
  }, [tipo, subtipo, tipoEvento, aplicador, origen, insumoId, dosis]);

  const puedePaso = (n: number) => {
    const bloqueado = (resumen?.bloqueantes?.length ?? 0) > 0;
    if (n === 0) return tipo !== '';
    if (n === 1) return Boolean(resumen && (resumen.lecturas ?? 0) > 0) && !bloqueado && pesadaLista;
    if (n === 2) return loteId !== '';
    if (n === 3) return cabeceraLista;
    return true;
  };

  const siguiente = () => {
    if (!puedePaso(paso)) {
      setError('Completá este paso antes de seguir');
      return;
    }
    setError(null);
    setPaso((p) => Math.min(p + 1, 4));
  };

  const procesarYRevisar = async () => {
    if (loteId === '' || tipo === '' || !archivo || !resumen) {
      setError('Faltan lecturas, evento o lote');
      return;
    }
    if (!cabeceraLista) {
      setError('Faltan datos del evento');
      return;
    }
    setEnviando(true);
    setError(null);
    try {
      const cuerpo: FeedlotSesionMangaSolicitud = {
        loteId,
        tipo,
        fecha,
        perfilLecturaId: perfilLecturaId === '' ? null : perfilLecturaId,
      };
      if (tipo === 'SANIDAD') {
        cuerpo.tipoEvento = tipoEvento;
        cuerpo.aplicador = aplicador;
        cuerpo.origenInsumo = tipoEvento === 'DIAGNOSTICO' ? 'SIN_INSUMO' : origen;
        cuerpo.insumoId = insumoId === '' ? null : insumoId;
        cuerpo.dosisPorAnimal = dosis ? Number(dosis) : null;
        cuerpo.diasRetiro = diasRetiro ? Number(diasRetiro) : null;
      }
      if (tipo === 'BAJA') {
        cuerpo.subtipoBaja = subtipo as FeedlotSubtipoBaja;
        cuerpo.motivoMuerteId = motivoMuerteId === '' ? null : motivoMuerteId;
      }
      const sesion = await crearSesionManga(cuerpo);
      const usarPesos = tipo === 'PESADA' && (!resumen.tienePeso || cargarKilos);
      const file = usarPesos ? armarCsvConPesos(resumen, pesosManuales) : archivo;
      const mapSubida: FeedlotMapeoColumnas | null = usarPesos
        ? { indiceEid: 0, indiceCaravanaVisual: 1, indicePeso: 2, indiceFechaHora: 3, indiceHora: 4, delimitador: ',' }
        : (mapeo.indiceEid != null ? mapeo : { delimitador: 'AUTO' });
      await subirArchivoSesionManga(sesion.id, file, mapSubida);
      navigate(`/feedlot/manga/${sesion.id}`);
    } catch (e) {
      setError(mensajeError(e));
    } finally {
      setEnviando(false);
    }
  };

  const insumoSel = insumos.find((i) => i.id === insumoId);

  return (
    <Box sx={{ p: 3, width: '100%', maxWidth: '100%', boxSizing: 'border-box' }}>
      <Typography variant="h5" gutterBottom>
        Cargar evento de manga
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Elegí el evento a actualizar. El CSV de Tru-Test trae las columnas de la sesión; el perfil indica cómo se llaman en tu bastón.
        {' '}
        <Button size="small" onClick={() => navigate('/feedlot/lecturas-baston')}>Configurar lecturas</Button>
      </Typography>
      <Stepper activeStep={paso} sx={{ mb: 3 }}>
        {PASOS.map((e) => (
          <Step key={e}><StepLabel>{e}</StepLabel></Step>
        ))}
      </Stepper>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {paso === 0 && (
        <Stack spacing={2} sx={{ width: '100%' }}>
          <Box
            component="table"
            sx={{
              width: '100%',
              tableLayout: 'fixed',
              borderCollapse: 'separate',
              borderSpacing: '12px 0',
              marginLeft: '-12px',
              marginRight: '-12px',
            }}
          >
            <Box component="tbody">
              <Box component="tr">
                {EVENTOS.map((ev) => {
                  const elegido = tipo === ev.valor;
                  return (
                    <Box component="td" key={ev.valor} sx={{ width: '20%', verticalAlign: 'stretch', p: 0 }}>
                      <Card
                        variant={elegido ? 'outlined' : 'elevation'}
                        sx={{
                          height: '100%',
                          borderColor: elegido ? 'primary.main' : undefined,
                          borderWidth: elegido ? 2 : undefined,
                        }}
                      >
                        <CardActionArea onClick={() => setTipo(ev.valor)} sx={{ height: '100%' }}>
                          <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                            <Typography variant="subtitle1" sx={{ fontWeight: 600, lineHeight: 1.25 }}>
                              {ev.titulo}
                            </Typography>
                            <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5, lineHeight: 1.35 }}>
                              {ev.detalle}
                            </Typography>
                          </CardContent>
                        </CardActionArea>
                      </Card>
                    </Box>
                  );
                })}
              </Box>
            </Box>
          </Box>
          {tipo && perfiles.length > 0 && (
            <TextField
              select
              label="Perfil de lectura"
              value={perfilLecturaId}
              onChange={(e) => setPerfilLecturaId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              {perfiles.map((p) => (
                <MenuItem key={p.id} value={p.id}>{p.nombre}{p.predeterminado ? ' (predeterminado)' : ''}</MenuItem>
              ))}
            </TextField>
          )}
          {tipo && perfiles.length === 0 && (
            <Alert severity="info">Sin perfil propio se usan los nombres de fábrica de Data Link (EID, VID, Weight, Date, Time).</Alert>
          )}
        </Stack>
      )}

      {paso === 1 && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={2}>
            <Button variant="outlined" component="label">
              Soltar o elegir CSV del bastón
              <input hidden type="file" accept=".csv,text/csv,text/plain" onChange={(e) => onArchivo(e.target.files?.[0] ?? null)} />
            </Button>
            <Typography variant="body2">{archivo?.name ?? 'Sin archivo'}</Typography>
            {resumen && (
              <Alert severity="info">{resumen.mensajeResumen}</Alert>
            )}
            {resumen?.avisos?.map((a) => (
              <Alert key={a} severity="warning">{a}</Alert>
            ))}
            {resumen?.bloqueantes?.map((b) => (
              <Alert key={b} severity="error">{b}</Alert>
            ))}
            {tipo === 'PESADA' && resumen && !resumen.tienePeso && (
              <FormControlLabel
                sx={{ display: 'flex', ml: 0, alignItems: 'center' }}
                control={<Checkbox checked={cargarKilos} onChange={(e) => {
                  const v = e.target.checked;
                  setCargarKilos(v);
                  if (archivo) void analizar(archivo, undefined, v);
                }} />}
                label="Voy a cargar el kilo a mano (archivo del bastón, no del cabezal)"
              />
            )}
            {resumen && (resumen.entendioArchivo === false || mostrarMapeo) && (
              <Box>
                <Typography variant="subtitle2" gutterBottom>Esta columna es…</Typography>
                <Stack direction="row" spacing={1} flexWrap="wrap">
                  {(resumen.encabezados ?? resumen.filaMuestra ?? []).map((h, i) => (
                    <Typography key={i} variant="caption" sx={{ mr: 1 }}>{i}: {h}</Typography>
                  ))}
                </Stack>
                <Stack direction={{ xs: 'column', md: 'row' }} spacing={1} sx={{ mt: 1 }}>
                  <TextField select label="EID" value={mapeo.indiceEid ?? 0} onChange={(e) => setMapeo((m) => ({ ...m, indiceEid: Number(e.target.value) }))} sx={{ width: 120 }}>
                    {[0, 1, 2, 3, 4].map((i) => <MenuItem key={i} value={i}>{i}</MenuItem>)}
                  </TextField>
                  <Button onClick={() => archivo && void analizar(archivo, mapeo)}>Releer</Button>
                </Stack>
              </Box>
            )}
            {resumen?.entendioArchivo && (
              <Button size="small" onClick={() => setMostrarMapeo((v) => !v)}>Ajustar columnas</Button>
            )}
          </Stack>
        </Paper>
      )}

      {paso === 2 && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={2}>
            {resumen?.mezclaLotes && (
              <Alert severity="warning">
                Hay caravanas de más de un lote. Elegí el lote de esta sesión.
                {resumen.eidsOtroLote?.length ? ` Otras: ${resumen.eidsOtroLote.join(', ')}` : ''}
              </Alert>
            )}
            {resumen?.loteSugeridoNombre && !resumen.mezclaLotes && (
              <Alert severity="success">La mayoría de las lecturas están en «{resumen.loteSugeridoNombre}».</Alert>
            )}
            <TextField select label="Lote" value={loteId} onChange={(e) => setLoteId(e.target.value === '' ? '' : Number(e.target.value))}>
              {lotes.map((l) => (
                <MenuItem key={l.id} value={l.id}>{l.nombre} · {l.cabezasActuales ?? 0} cabezas</MenuItem>
              ))}
            </TextField>
            {loteElegido && (
              <Typography variant="body2" color="text.secondary">
                Plantel {loteElegido.cabezasActuales ?? 0} cabezas. Si hay menos enrolados, el lote es híbrido.
              </Typography>
            )}
            <TextField
              type="date"
              label="Fecha operativa"
              InputLabelProps={{ shrink: true }}
              value={fecha}
              onChange={(e) => setFecha(e.target.value)}
              helperText={resumen?.fechaMayoritaria && fechaIso(resumen.fechaMayoritaria) !== fecha
                ? 'Difiere de la fecha del archivo'
                : undefined}
            />
          </Stack>
        </Paper>
      )}

      {paso === 3 && tipo === 'SANIDAD' && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={2}>
            <TextField select label="Qué se aplicó" value={tipoEvento} onChange={(e) => {
              const v = e.target.value as FeedlotTipoEventoSanitario;
              setTipoEvento(v);
              if (v === 'DIAGNOSTICO') setOrigen('SIN_INSUMO');
            }}>
              <MenuItem value="VACUNA">Vacuna</MenuItem>
              <MenuItem value="TRATAMIENTO">Tratamiento</MenuItem>
              <MenuItem value="DIAGNOSTICO">Diagnóstico (sin producto)</MenuItem>
              <MenuItem value="OTRO">Otro</MenuItem>
            </TextField>
            {tipoEvento !== 'DIAGNOSTICO' && (
              <>
                <TextField select label="Origen del producto" value={origen} onChange={(e) => setOrigen(e.target.value as FeedlotOrigenInsumo)}>
                  <MenuItem value="STOCK_EMPRESA">Stock de la empresa</MenuItem>
                  <MenuItem value="PROVEIDO_POR_APLICADOR">Lo trajo el aplicador</MenuItem>
                  <MenuItem value="SIN_INSUMO">Sin insumo</MenuItem>
                </TextField>
                {origen !== 'SIN_INSUMO' && (
                  <TextField
                    select
                    label="Insumo"
                    value={insumoId === '' ? '' : String(insumoId)}
                    onChange={(e) => setInsumoId(e.target.value === '' ? '' : Number(e.target.value))}
                    helperText={
                      insumos.length === 0
                        ? 'No hay insumos en el inventario. Abrí Feedlot → Insumos o recargá la página.'
                        : `${insumos.length} productos en inventario`
                    }
                  >
                    <MenuItem value="">Elegí un producto…</MenuItem>
                    {insumos.map((i) => (
                      <MenuItem key={i.id} value={String(i.id)}>
                        {etiquetaInsumoOpcion(i)}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
                {origen !== 'SIN_INSUMO' && (
                  <TextField label={`Dosis por animal${insumoSel?.unidadMedida ? ` (${insumoSel.unidadMedida})` : ''}`} value={dosis} onChange={(e) => setDosis(e.target.value)} />
                )}
              </>
            )}
            <TextField select label="Quién aplicó" value={aplicador} onChange={(e) => {
              const v = e.target.value as FeedlotAplicador;
              setAplicador(v);
              if (tipoEvento !== 'DIAGNOSTICO') {
                setOrigen(v === 'VETERINARIO' ? 'PROVEIDO_POR_APLICADOR' : 'STOCK_EMPRESA');
              }
            }}>
              <MenuItem value="PERSONAL_ESTABLECIMIENTO">Personal del establecimiento</MenuItem>
              <MenuItem value="VETERINARIO">Veterinario</MenuItem>
            </TextField>
            <TextField label="Días de retiro" value={diasRetiro} onChange={(e) => setDiasRetiro(e.target.value)} />
            {origen === 'STOCK_EMPRESA' && dosis && resumen && (
              <Typography variant="body2">
                Se descontarían {(Number(dosis) * (resumen.lecturas ?? 0)).toFixed(2)} {insumoSel?.unidadMedida ?? ''} del depósito (según lecturas).
              </Typography>
            )}
          </Stack>
        </Paper>
      )}

      {paso === 3 && tipo === 'BAJA' && (
        <Paper sx={{ p: 2 }}>
          <Stack spacing={2}>
            <Typography>¿Qué pasó con estos animales?</Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap">
              {([
                ['MUERTE', 'Murieron'],
                ['FAENA', 'Van a faena'],
                ['VENTA_EN_PIE', 'Se venden en pie'],
                ['DESCARTE', 'Descarte'],
              ] as const).map(([v, t]) => (
                <Button key={v} variant={subtipo === v ? 'contained' : 'outlined'} onClick={() => setSubtipo(v)}>{t}</Button>
              ))}
            </Stack>
            {subtipo === 'MUERTE' && (
              <TextField select label="Motivo (opcional)" value={motivoMuerteId} onChange={(e) => setMotivoMuerteId(e.target.value === '' ? '' : Number(e.target.value))}>
                <MenuItem value="">Sin motivo</MenuItem>
                {motivos.map((m) => <MenuItem key={m.id} value={m.id}>{m.nombre}</MenuItem>)}
              </TextField>
            )}
            {subtipo === 'FAENA' && (
              <Alert severity="warning">No se podrá faenar un animal con retiro sanitario vigente.</Alert>
            )}
          </Stack>
        </Paper>
      )}

      {paso === 3 && tipo === 'PESADA' && (
        <Paper sx={{ p: 2 }}>
          {resumen?.tienePeso && !cargarKilos && (
            <Alert severity="info">El archivo trae {resumen.pesosEncontrados} pesos. Rango válido: 50 a 900 kg.</Alert>
          )}
          {(cargarKilos || !resumen?.tienePeso) && (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>EID</TableCell>
                  <TableCell>Caravana</TableCell>
                  <TableCell>Peso kg</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(resumen?.lecturasDetalle ?? []).map((l) => {
                  const clave = l.eidNormalizado ?? l.eidCrudo ?? '';
                  return (
                    <TableRow key={clave}>
                      <TableCell>{l.eidCrudo}</TableCell>
                      <TableCell>{l.caravanaVisual ?? '—'}</TableCell>
                      <TableCell>
                        <TextField
                          size="small"
                          value={pesosManuales[clave] ?? (l.pesoKg != null ? String(l.pesoKg) : '')}
                          onChange={(e) => setPesosManuales((p) => ({ ...p, [clave]: e.target.value }))}
                        />
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
        </Paper>
      )}

      {paso === 3 && (tipo === 'ENROLAMIENTO' || tipo === 'RECUENTO') && (
        <Alert severity="info">
          {tipo === 'ENROLAMIENTO'
            ? 'Se darán de alta las caravanas que no existan. Si superan el plantel, en la revisión vas a poder ajustar cabezas (admin/supervisor).'
            : 'El recuento no cambia el plantel solo: vas a ver presentes, faltantes y lecturas ajenas.'}
        </Alert>
      )}

      {paso === 4 && (
        <Paper sx={{ p: 2 }}>
          <Typography variant="subtitle1" gutterBottom>Resumen</Typography>
          <Typography variant="body2">
            {resumen?.lecturas} lecturas · {EVENTOS.find((e) => e.valor === tipo)?.titulo} · lote {loteElegido?.nombre} · {fecha}
          </Typography>
          {(resumen?.avisos ?? []).map((a) => <Alert key={a} severity="warning" sx={{ mt: 1 }}>{a}</Alert>)}
          {(resumen?.bloqueantes ?? []).map((b) => <Alert key={b} severity="error" sx={{ mt: 1 }}>{b}</Alert>)}
          {tipo === 'SANIDAD' && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              Vas a registrar {tipoEvento.toLowerCase()} a {resumen?.lecturas} animales
              {insumoSel ? ` con ${insumoSel.nombre}` : ''}
              {dosis ? ` (${dosis} por cabeza)` : ''}.
            </Typography>
          )}
          {tipo === 'BAJA' && <Typography variant="body2" sx={{ mt: 1 }}>Baja por {subtipo}.</Typography>}
        </Paper>
      )}

      <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
        <Button disabled={paso === 0 || enviando} onClick={() => setPaso((p) => p - 1)}>Atrás</Button>
        {paso < 4 && (
          <Button variant="contained" onClick={siguiente} disabled={!puedePaso(paso)}>Continuar</Button>
        )}
        {paso === 4 && (
          <Button variant="contained" disabled={enviando || (resumen?.bloqueantes?.length ?? 0) > 0} onClick={() => void procesarYRevisar()}>
            Procesar y revisar filas
          </Button>
        )}
      </Stack>
    </Box>
  );
};

export default SesionMangaFeedlotScreen;
