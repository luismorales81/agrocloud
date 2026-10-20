import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  FormControlLabel,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import type {
  FeedlotAccionDesconocido,
  FeedlotAplicador,
  FeedlotCorral,
  FeedlotMapeoColumnas,
  FeedlotOrigenInsumo,
  FeedlotSesionManga,
  FeedlotSubtipoBaja,
  FeedlotTipoEventoSanitario,
} from '../types';
import {
  actualizarSesionManga,
  anularSesionManga,
  confirmarSesionManga,
  generarBajaFaltantes,
  listarCorrales,
  listarEstablecimientos,
  listarInsumos,
  mensajeError,
  obtenerLote,
  obtenerSesionManga,
  subirArchivoSesionManga,
} from '../services/feedlotApi';
import { useEmpresa } from '../../../contexts/EmpresaContext';
import {
  etiquetaInsumoOpcion,
  mapearInsumosDesdeApi,
  type InsumoOpcionFeedlot,
} from '../utils/mapearInsumos';

const DetalleSesionMangaFeedlotScreen: React.FC = () => {
  const { id: idParam } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const id = idParam ? parseInt(idParam, 10) : NaN;
  const [sesion, setSesion] = useState<FeedlotSesionManga | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [archivo, setArchivo] = useState<File | null>(null);
  const [indiceEid, setIndiceEid] = useState(0);
  const [indiceVisual, setIndiceVisual] = useState<number | ''>('');
  const [indicePeso, setIndicePeso] = useState<number | ''>('');
  const [indiceFecha, setIndiceFecha] = useState<number | ''>('');
  const [ajustar, setAjustar] = useState(false);
  const [tipoEvento, setTipoEvento] = useState<FeedlotTipoEventoSanitario>('VACUNA');
  const [aplicador, setAplicador] = useState<FeedlotAplicador>('PERSONAL_ESTABLECIMIENTO');
  const [origen, setOrigen] = useState<FeedlotOrigenInsumo>('STOCK_EMPRESA');
  const [insumoId, setInsumoId] = useState('');
  const [insumos, setInsumos] = useState<InsumoOpcionFeedlot[]>([]);
  const [mostrarMapeo, setMostrarMapeo] = useState(false);
  const [dosis, setDosis] = useState('');
  const [diasRetiro, setDiasRetiro] = useState('');
  const [subtipo, setSubtipo] = useState<FeedlotSubtipoBaja | ''>('');
  const [accionesDesconocidos, setAccionesDesconocidos] = useState<Record<number, FeedlotAccionDesconocido>>({});
  const [faltantesElegidos, setFaltantesElegidos] = useState<number[]>([]);
  const [corralesLibres, setCorralesLibres] = useState<FeedlotCorral[]>([]);
  const [corralNuevoId, setCorralNuevoId] = useState<number | ''>('');
  const [nombreLoteNuevo, setNombreLoteNuevo] = useState('');
  const [cabezasLote, setCabezasLote] = useState<number | null>(null);
  const { esAdministrador } = useEmpresa();
  const isAdmin = esAdministrador();

  const cargar = useCallback(async () => {
    if (Number.isNaN(id)) return;
    try {
      const s = await obtenerSesionManga(id);
      setSesion(s);
      if (s.loteId) {
        try {
          const lote = await obtenerLote(s.loteId);
          setCabezasLote(lote.cabezasActuales ?? null);
        } catch {
          setCabezasLote(null);
        }
      }
      aplicarMapeoPorTipo(s.tipo);
      setAjustar(Boolean(s.ajustarPlantel));
      if (s.tipoEvento) setTipoEvento(s.tipoEvento as FeedlotTipoEventoSanitario);
      if (s.aplicador) setAplicador(s.aplicador as FeedlotAplicador);
      if (s.origenInsumo) setOrigen(s.origenInsumo as FeedlotOrigenInsumo);
      if (s.subtipoBaja) setSubtipo(s.subtipoBaja as FeedlotSubtipoBaja);
    } catch (e) {
      setError(mensajeError(e));
    }
  }, [id]);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  useEffect(() => {
    if (!isAdmin) return;
    void (async () => {
      try {
        const establecimientos = await listarEstablecimientos();
        const listas = await Promise.all(establecimientos.map((e) => listarCorrales(e.id)));
        setCorralesLibres(listas.flat().filter((c) => c.estado === 'DISPONIBLE'));
      } catch {
        setCorralesLibres([]);
      }
    })();
  }, [isAdmin]);

  useEffect(() => {
    void listarInsumos()
      .then((raw) => setInsumos(mapearInsumosDesdeApi(raw)))
      .catch((e) => {
        setInsumos([]);
        setError(mensajeError(e));
      });
  }, []);

  const aplicarMapeoPorTipo = (_tipo?: string) => {
    setIndiceEid(0);
    setIndiceVisual('');
    setIndicePeso('');
    setIndiceFecha('');
  };

  const guardarCabecera = async () => {
    if (!sesion) return;
    setError(null);
    try {
      const actualizado = await actualizarSesionManga(sesion.id, {
        loteId: sesion.loteId ?? 0,
        tipo: sesion.tipo as FeedlotSesionManga['tipo'] as never,
        fecha: sesion.fecha,
        ajustarPlantel: ajustar,
        tipoEvento,
        aplicador,
        origenInsumo: origen,
        insumoId: insumoId ? Number(insumoId) : null,
        dosisPorAnimal: dosis ? Number(dosis) : null,
        diasRetiro: diasRetiro ? Number(diasRetiro) : null,
        subtipoBaja: subtipo === '' ? undefined : subtipo,
      });
      setSesion(actualizado);
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  const subir = async () => {
    if (!sesion || !archivo) {
      setError('Seleccioná un archivo CSV');
      return;
    }
    setError(null);
    const mapeo: FeedlotMapeoColumnas | null = mostrarMapeo
      ? {
          indiceEid,
          indiceCaravanaVisual: indiceVisual === '' ? null : indiceVisual,
          indicePeso: indicePeso === '' ? null : indicePeso,
          indiceFechaHora: indiceFecha === '' ? null : indiceFecha,
          delimitador: 'AUTO',
        }
      : { delimitador: 'AUTO' };
    try {
      await guardarCabecera();
      setSesion(await subirArchivoSesionManga(sesion.id, archivo, mapeo));
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  const confirmar = async () => {
    if (!sesion) return;
    setError(null);
    try {
      const acciones = Object.entries(accionesDesconocidos)
        .filter(([, accion]) => accion && accion !== 'IGNORAR')
        .map(([filaId, accion]) => ({ filaId: Number(filaId), accion }));
      const usaLoteNuevo = acciones.some((a) => a.accion === 'ENROLAR_EN_LOTE_NUEVO');
      setSesion(await confirmarSesionManga(sesion.id, {
        ajustarPlantel: ajustar,
        accionesDesconocidos: acciones,
        loteNuevo: usaLoteNuevo && corralNuevoId !== '' && nombreLoteNuevo.trim()
          ? { corralId: Number(corralNuevoId), nombre: nombreLoteNuevo.trim() }
          : null,
      }));
    } catch (e) {
      const texto = mensajeError(e);
      if (texto.toLowerCase().includes('ajuste de plantel')) {
        setAjustar(true);
      }
      setError(texto);
    }
  };

  const generarBaja = async () => {
    if (!sesion || faltantesElegidos.length === 0) {
      setError('Elegí al menos un faltante');
      return;
    }
    setError(null);
    try {
      const baja = await generarBajaFaltantes(sesion.id, faltantesElegidos, 'MUERTE');
      navigate(`/feedlot/manga/${baja.id}`);
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  const anular = async () => {
    if (!sesion) return;
    setError(null);
    try {
      setSesion(await anularSesionManga(sesion.id));
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  if (!sesion) {
    return (
      <Box sx={{ p: 3 }}>
        {error && <Alert severity="error">{error}</Alert>}
        <Typography>Cargando sesión…</Typography>
      </Box>
    );
  }

  const borrador = sesion.estado === 'BORRADOR';
  const recuento = sesion.resumenRecuento;

  return (
    <Box sx={{ p: 3 }}>
      <Button onClick={() => navigate('/feedlot/manga')} sx={{ mb: 2 }}>
        Volver
      </Button>
      <Typography variant="h5" gutterBottom>
        Sesión de manga #{sesion.id} — {sesion.tipo}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Lote {sesion.loteNombre ?? sesion.loteId} · {sesion.fecha} · {sesion.estado}
      </Typography>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {borrador && sesion.tipo === 'SANIDAD' && (
        <Paper sx={{ p: 2, mb: 2 }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
            <TextField select label="Tipo evento" value={tipoEvento} onChange={(e) => setTipoEvento(e.target.value as FeedlotTipoEventoSanitario)}>
              <MenuItem value="VACUNA">Vacuna</MenuItem>
              <MenuItem value="TRATAMIENTO">Tratamiento</MenuItem>
              <MenuItem value="DIAGNOSTICO">Diagnóstico</MenuItem>
              <MenuItem value="OTRO">Otro</MenuItem>
            </TextField>
            <TextField select label="Aplicador" value={aplicador} onChange={(e) => {
              const v = e.target.value as FeedlotAplicador;
              setAplicador(v);
              setOrigen(v === 'VETERINARIO' ? 'PROVEIDO_POR_APLICADOR' : 'STOCK_EMPRESA');
            }}>
              <MenuItem value="PERSONAL_ESTABLECIMIENTO">Personal</MenuItem>
              <MenuItem value="VETERINARIO">Veterinario</MenuItem>
            </TextField>
            <TextField select label="Origen del insumo" value={origen} onChange={(e) => setOrigen(e.target.value as FeedlotOrigenInsumo)}>
              <MenuItem value="STOCK_EMPRESA">Stock de la empresa</MenuItem>
              <MenuItem value="PROVEIDO_POR_APLICADOR">Lo trajo el aplicador</MenuItem>
              <MenuItem value="SIN_INSUMO">Sin insumo</MenuItem>
            </TextField>
            <TextField
              select
              label="Insumo"
              value={insumoId}
              onChange={(e) => setInsumoId(e.target.value)}
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
            <TextField label="Dosis por animal" value={dosis} onChange={(e) => setDosis(e.target.value)} />
            <TextField label="Días de retiro" value={diasRetiro} onChange={(e) => setDiasRetiro(e.target.value)} />
          </Stack>
        </Paper>
      )}

      {borrador && sesion.tipo === 'BAJA' && (
        <Paper sx={{ p: 2, mb: 2 }}>
          <TextField select label="Qué pasó" value={subtipo} onChange={(e) => setSubtipo(e.target.value === '' ? '' : e.target.value as FeedlotSubtipoBaja)}>
            <MenuItem value="">Elegí…</MenuItem>
            <MenuItem value="MUERTE">Murieron</MenuItem>
            <MenuItem value="FAENA">Van a faena</MenuItem>
            <MenuItem value="VENTA_EN_PIE">Se venden en pie</MenuItem>
            <MenuItem value="DESCARTE">Descarte</MenuItem>
          </TextField>
        </Paper>
      )}

      {borrador && (
        <Paper sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle1" gutterBottom>
            Archivo CSV
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            El archivo identifica lecturas (EID y fecha). El evento ya está en esta sesión.
          </Typography>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">
            <Button variant="outlined" component="label">
              Elegir CSV
              <input hidden type="file" accept=".csv,text/csv,text/plain" onChange={(e) => setArchivo(e.target.files?.[0] ?? null)} />
            </Button>
            <Typography variant="body2">{archivo?.name ?? 'Sin archivo'}</Typography>
            <Button size="small" onClick={() => setMostrarMapeo((v) => !v)}>Ajustar columnas</Button>
            {mostrarMapeo && (
              <>
                <TextField type="number" label="Columna EID" value={indiceEid} onChange={(e) => setIndiceEid(Number(e.target.value))} sx={{ width: 140 }} />
                <TextField type="number" label="Columna caravana" value={indiceVisual} onChange={(e) => setIndiceVisual(e.target.value === '' ? '' : Number(e.target.value))} sx={{ width: 160 }} />
                <TextField type="number" label="Columna peso" value={indicePeso} onChange={(e) => setIndicePeso(e.target.value === '' ? '' : Number(e.target.value))} sx={{ width: 140 }} />
                <TextField type="number" label="Columna fecha" value={indiceFecha} onChange={(e) => setIndiceFecha(e.target.value === '' ? '' : Number(e.target.value))} sx={{ width: 140 }} />
              </>
            )}
            <Button variant="contained" onClick={() => void subir()}>
              Procesar archivo
            </Button>
          </Stack>
          {sesion.tipo === 'ENROLAMIENTO' && (
            <FormControlLabel
              control={<Checkbox checked={ajustar} onChange={(e) => setAjustar(e.target.checked)} />}
              label="Si hay más enrolados que el plantel, ajustar cabezas (solo admin / supervisor)"
            />
          )}
        </Paper>
      )}

      <Paper sx={{ p: 2, mb: 2 }}>
        <Typography variant="subtitle1">
          Preview · OK {sesion.filasOk ?? 0} · errores {sesion.filasError ?? 0}
        </Typography>
        {recuento && (
          <Box sx={{ my: 2 }}>
            <Alert severity="info" sx={{ mb: 1 }}>
              Presentes {recuento.presentes ?? 0}. Faltantes {recuento.faltantes?.length ?? 0}.
              Desconocidos {recuento.desconocidos?.length ?? 0}. Otro lote {recuento.otroLote?.length ?? 0}.
              Inactivos {recuento.inactivos?.length ?? 0}. Cabezas sin enrolar {recuento.cabezasSinEnrolar ?? 0}.
            </Alert>
            {(recuento.desconocidos ?? []).length > 0 && (
              <Paper variant="outlined" sx={{ p: 1, mb: 1 }}>
                <Typography variant="subtitle2">Desconocidos</Typography>
                {!isAdmin && (
                  <Typography variant="caption" color="text.secondary">
                    Solo el administrador puede enrolarlos.
                  </Typography>
                )}
                {(recuento.desconocidos ?? []).map((d) => (
                  <Stack key={d.filaId ?? d.eid} direction="row" spacing={1} alignItems="center" sx={{ my: 0.5 }}>
                    <Typography variant="body2" sx={{ minWidth: 220 }}>
                      {d.eid ?? '—'} {d.caravanaVisual ? `· ${d.caravanaVisual}` : ''}
                    </Typography>
                    <TextField
                      select
                      size="small"
                      disabled={!isAdmin || !borrador}
                      value={accionesDesconocidos[d.filaId ?? 0] ?? 'IGNORAR'}
                      onChange={(e) => setAccionesDesconocidos((prev) => ({
                        ...prev,
                        [d.filaId ?? 0]: e.target.value as FeedlotAccionDesconocido,
                      }))}
                      sx={{ width: 260 }}
                    >
                      <MenuItem value="IGNORAR">Ignorar</MenuItem>
                      <MenuItem value="ENROLAR_EN_ESTE_LOTE">Enrolar en este lote</MenuItem>
                      <MenuItem value="ENROLAR_EN_LOTE_NUEVO">Enrolar en lote nuevo</MenuItem>
                    </TextField>
                  </Stack>
                ))}
                {isAdmin && Object.values(accionesDesconocidos).includes('ENROLAR_EN_ESTE_LOTE') && (
                  <FormControlLabel
                    control={<Checkbox checked={ajustar} onChange={(e) => setAjustar(e.target.checked)} />}
                    label={`Ajustar plantel si supera las cabezas${cabezasLote != null ? ` (${cabezasLote})` : ''}`}
                  />
                )}
                {isAdmin && Object.values(accionesDesconocidos).includes('ENROLAR_EN_LOTE_NUEVO') && (
                  <Stack direction={{ xs: 'column', md: 'row' }} spacing={1} sx={{ mt: 1 }}>
                    <TextField
                      select
                      label="Corral libre"
                      value={corralNuevoId}
                      onChange={(e) => setCorralNuevoId(e.target.value === '' ? '' : Number(e.target.value))}
                      sx={{ minWidth: 220 }}
                    >
                      {corralesLibres.map((c) => (
                        <MenuItem key={c.id} value={c.id}>{c.nombre}</MenuItem>
                      ))}
                    </TextField>
                    <TextField
                      label="Nombre del lote nuevo"
                      value={nombreLoteNuevo}
                      onChange={(e) => setNombreLoteNuevo(e.target.value)}
                    />
                  </Stack>
                )}
              </Paper>
            )}
            {(recuento.otroLote ?? []).length > 0 && (
              <Alert severity="warning" sx={{ mb: 1 }}>
                En otro lote:{' '}
                {(recuento.otroLote ?? []).map((d) => `${d.eid ?? ''} (${d.loteNombre ?? 'lote'} / ${d.corralNombre ?? 'corral'})`).join(' · ')}
              </Alert>
            )}
            {(recuento.inactivos ?? []).length > 0 && (
              <Alert severity="warning" sx={{ mb: 1 }}>
                Inactivos (no se reactivan):{' '}
                {(recuento.inactivos ?? []).map((d) => `${d.eid ?? ''} ${d.estado ?? ''}`).join(' · ')}
              </Alert>
            )}
            {(recuento.faltantes ?? []).length > 0 && (
              <Paper variant="outlined" sx={{ p: 1, mb: 1 }}>
                <Typography variant="subtitle2">Faltantes</Typography>
                {(recuento.faltantes ?? []).map((f) => (
                  <FormControlLabel
                    key={f.id}
                    control={
                      <Checkbox
                        checked={f.id != null && faltantesElegidos.includes(f.id)}
                        onChange={(e) => {
                          if (f.id == null) return;
                          setFaltantesElegidos((prev) => e.target.checked
                            ? [...prev, f.id as number]
                            : prev.filter((x) => x !== f.id));
                        }}
                      />
                    }
                    label={`${f.eid ?? '—'} ${f.caravanaVisual ?? ''}`}
                  />
                ))}
                <Button size="small" variant="outlined" onClick={() => void generarBaja()}>
                  Generar sesión de baja
                </Button>
              </Paper>
            )}
          </Box>
        )}
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Línea</TableCell>
              <TableCell>EID</TableCell>
              <TableCell>Caravana</TableCell>
              <TableCell>Peso</TableCell>
              <TableCell>Resultado</TableCell>
              <TableCell>Mensaje</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {(sesion.filas ?? []).map((f) => (
              <TableRow key={f.id ?? f.numeroLinea}>
                <TableCell>{f.numeroLinea}</TableCell>
                <TableCell>{f.eidNormalizado ?? f.eidCrudo ?? '—'}</TableCell>
                <TableCell>{f.caravanaVisual ?? '—'}</TableCell>
                <TableCell>{f.pesoKg ?? '—'}</TableCell>
                <TableCell>{f.resultado}</TableCell>
                <TableCell>{f.mensaje ?? '—'}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      {(sesion.avisos ?? []).map((a) => (
        <Alert key={a} severity="warning" sx={{ mb: 1 }}>{a}</Alert>
      ))}
      {(sesion.bloqueantes ?? []).map((a) => (
        <Alert key={a} severity="error" sx={{ mb: 1 }}>{a}</Alert>
      ))}

      <Stack direction="row" spacing={2}>
        {borrador && (
          <Button
            variant="contained"
            disabled={sesion.puedeConfirmar === false}
            onClick={() => void confirmar()}
          >
            Confirmar sesión
          </Button>
        )}
        {sesion.estado === 'CONFIRMADA' && (
          <Button color="warning" variant="outlined" onClick={() => void anular()}>
            Anular
          </Button>
        )}
      </Stack>
    </Box>
  );
};

export default DetalleSesionMangaFeedlotScreen;
