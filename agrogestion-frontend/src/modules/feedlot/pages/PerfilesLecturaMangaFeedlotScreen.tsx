import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Paper,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import type { FeedlotAliasColumnasPerfil, FeedlotPerfilLecturaManga, FeedlotTipoSesionManga } from '../types';
import {
  actualizarPerfilLecturaManga,
  crearPerfilLecturaManga,
  eliminarPerfilLecturaManga,
  extraerEncabezadosPerfilLectura,
  listarPerfilesLecturaManga,
  mensajeError,
} from '../services/feedlotApi';

const TIPOS: { valor: FeedlotTipoSesionManga; etiqueta: string }[] = [
  { valor: 'ENROLAMIENTO', etiqueta: 'Enrolar' },
  { valor: 'PESADA', etiqueta: 'Pesar' },
  { valor: 'SANIDAD', etiqueta: 'Aplicar producto' },
  { valor: 'RECUENTO', etiqueta: 'Contar' },
  { valor: 'BAJA', etiqueta: 'Dar de baja' },
];

const CAMPOS: { valor: keyof FeedlotAliasColumnasPerfil | 'IGNORAR'; etiqueta: string }[] = [
  { valor: 'eid', etiqueta: 'EID' },
  { valor: 'vid', etiqueta: 'Caravana visual' },
  { valor: 'peso', etiqueta: 'Peso' },
  { valor: 'fecha', etiqueta: 'Fecha' },
  { valor: 'hora', etiqueta: 'Hora' },
  { valor: 'dosis', etiqueta: 'Dosis' },
  { valor: 'loteProducto', etiqueta: 'Lote del producto' },
  { valor: 'retiro', etiqueta: 'Retiro (WHP)' },
  { valor: 'esi', etiqueta: 'ESI' },
  { valor: 'IGNORAR', etiqueta: 'Ignorar' },
];

function aliasVacio(): FeedlotAliasColumnasPerfil {
  return { eid: [], vid: [], peso: [], fecha: [], hora: [], dosis: [], loteProducto: [], retiro: [], esi: [] };
}

function armarAlias(asignacion: Record<string, string>): FeedlotAliasColumnasPerfil {
  const a = aliasVacio();
  for (const [encabezado, campo] of Object.entries(asignacion)) {
    if (!campo || campo === 'IGNORAR') continue;
    const lista = a[campo as keyof FeedlotAliasColumnasPerfil];
    if (Array.isArray(lista)) lista.push(encabezado);
  }
  return a;
}

const PerfilesLecturaMangaFeedlotScreen: React.FC = () => {
  const navigate = useNavigate();
  const [perfiles, setPerfiles] = useState<FeedlotPerfilLecturaManga[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [dialogo, setDialogo] = useState(false);
  const [nombre, setNombre] = useState('');
  const [tipo, setTipo] = useState<FeedlotTipoSesionManga>('PESADA');
  const [predeterminado, setPredeterminado] = useState(true);
  const [encabezados, setEncabezados] = useState<string[]>([]);
  const [asignacion, setAsignacion] = useState<Record<string, string>>({});
  const [editandoId, setEditandoId] = useState<number | null>(null);

  const cargar = useCallback(async () => {
    try {
      setPerfiles(await listarPerfilesLecturaManga());
    } catch (e) {
      setError(mensajeError(e));
    }
  }, []);

  useEffect(() => { void cargar(); }, [cargar]);

  const abrirNuevo = () => {
    setEditandoId(null);
    setNombre('');
    setTipo('PESADA');
    setPredeterminado(true);
    setEncabezados([]);
    setAsignacion({});
    setDialogo(true);
  };

  const onMuestra = async (file: File | null) => {
    if (!file) return;
    try {
      setError(null);
      const m = await extraerEncabezadosPerfilLectura(file);
      const heads = m.encabezados ?? [];
      setEncabezados(heads);
      const asig: Record<string, string> = {};
      for (const h of heads) {
        const n = h.toLowerCase();
        if (n === 'eid') asig[h] = 'eid';
        else if (n === 'vid' || n === 'lid') asig[h] = 'vid';
        else if (n.includes('weight') || n.includes('peso')) asig[h] = 'peso';
        else if (n === 'date' || n.includes('fecha')) asig[h] = 'fecha';
        else if (n === 'time' || n.includes('hora')) asig[h] = 'hora';
        else asig[h] = 'IGNORAR';
      }
      setAsignacion(asig);
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  const guardar = async () => {
    try {
      const cuerpo = { nombre, tipo, predeterminado, alias: armarAlias(asignacion) };
      if (editandoId) await actualizarPerfilLecturaManga(editandoId, cuerpo);
      else await crearPerfilLecturaManga(cuerpo);
      setDialogo(false);
      await cargar();
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Box>
          <Typography variant="h5">Lecturas del bastón</Typography>
          <Typography variant="body2" color="text.secondary">
            Configurá cómo se llaman las columnas de tu CSV de Tru-Test para cada tipo de evento.
          </Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button onClick={() => navigate('/feedlot/manga')}>Ir a manga</Button>
          <Button variant="contained" onClick={abrirNuevo}>Nuevo perfil</Button>
        </Stack>
      </Stack>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Evento</TableCell>
              <TableCell>Predeterminado</TableCell>
              <TableCell />
            </TableRow>
          </TableHead>
          <TableBody>
            {perfiles.map((p) => (
              <TableRow key={p.id}>
                <TableCell>{p.nombre}</TableCell>
                <TableCell>{TIPOS.find((t) => t.valor === p.tipo)?.etiqueta ?? p.tipo}</TableCell>
                <TableCell>{p.predeterminado ? 'Sí' : 'No'}</TableCell>
                <TableCell>
                  <Button size="small" color="error" onClick={() => void eliminarPerfilLecturaManga(p.id).then(cargar)}>
                    Borrar
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {perfiles.length === 0 && (
              <TableRow>
                <TableCell colSpan={4}>Todavía no hay perfiles. Sin perfil se usan los nombres de fábrica (EID, VID, Weight, Date, Time).</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>

      <Dialog open={dialogo} onClose={() => setDialogo(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editandoId ? 'Editar perfil' : 'Nuevo perfil de lectura'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} />
            <TextField select label="Evento que actualiza" value={tipo} onChange={(e) => setTipo(e.target.value as FeedlotTipoSesionManga)}>
              {TIPOS.map((t) => <MenuItem key={t.valor} value={t.valor}>{t.etiqueta}</MenuItem>)}
            </TextField>
            <FormControlLabel
              control={<Switch checked={predeterminado} onChange={(e) => setPredeterminado(e.target.checked)} />}
              label="Usar por defecto en este evento"
            />
            <Button variant="outlined" component="label">
              Subir CSV de muestra
              <input hidden type="file" accept=".csv,text/csv,text/plain" onChange={(e) => void onMuestra(e.target.files?.[0] ?? null)} />
            </Button>
            {encabezados.map((h) => (
              <TextField
                key={h}
                select
                label={`Columna «${h}»`}
                value={asignacion[h] ?? 'IGNORAR'}
                onChange={(e) => setAsignacion((a) => ({ ...a, [h]: e.target.value }))}
              >
                {CAMPOS.map((c) => <MenuItem key={c.valor} value={c.valor}>{c.etiqueta}</MenuItem>)}
              </TextField>
            ))}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogo(false)}>Cancelar</Button>
          <Button variant="contained" onClick={() => void guardar()} disabled={!nombre.trim() || encabezados.length === 0}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PerfilesLecturaMangaFeedlotScreen;
