import React, { useRef, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { Icon } from '../../../components/icons';
import type { LecheriaImportControlLechero } from '../types';
import { importarControlLechero, mensajeError } from '../services/lecheriaApi';

const ImportControlLecheroScreen: React.FC = () => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [archivo, setArchivo] = useState<File | null>(null);
  const [importando, setImportando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resultado, setResultado] = useState<LecheriaImportControlLechero | null>(null);

  const seleccionarArchivo = (ev: React.ChangeEvent<HTMLInputElement>) => {
    const f = ev.target.files?.[0] ?? null;
    setArchivo(f);
    setResultado(null);
    setError(null);
  };

  const importar = async () => {
    if (!archivo) {
      setError('Seleccioná un archivo CSV.');
      return;
    }
    try {
      setImportando(true);
      setError(null);
      const res = await importarControlLechero(archivo);
      setResultado(res);
      setArchivo(null);
      if (inputRef.current) {
        inputRef.current.value = '';
      }
    } catch (err: unknown) {
      setError(mensajeError(err));
    } finally {
      setImportando(false);
    }
  };

  return (
    <Box sx={{ p: { xs: 2, md: 3 }, maxWidth: 700 }}>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
        <Icon name="Upload" size={28} />
        <Typography variant="h4" component="h1">
          Importar control lechero
        </Typography>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Subí un archivo CSV exportado del control lechero para cargar ordeñes en lote.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {resultado && (
        <Alert severity={resultado.filasError && resultado.filasError > 0 ? 'warning' : 'success'} sx={{ mb: 2 }}>
          <Typography variant="body2">
            Archivo: <strong>{resultado.nombreArchivo}</strong>
          </Typography>
          <Typography variant="body2">
            Filas procesadas: {resultado.filasProcesadas ?? 0} · Errores: {resultado.filasError ?? 0}
          </Typography>
          {resultado.detalleErrores && (
            <Typography variant="caption" component="pre" sx={{ mt: 1, whiteSpace: 'pre-wrap' }}>
              {resultado.detalleErrores}
            </Typography>
          )}
        </Alert>
      )}

      <Paper variant="outlined" sx={{ p: 3 }}>
        <input
          ref={inputRef}
          type="file"
          accept=".csv,text/csv"
          style={{ display: 'none' }}
          onChange={seleccionarArchivo}
        />
        <Stack spacing={2} alignItems="flex-start">
          <Button variant="outlined" onClick={() => inputRef.current?.click()}>
            Elegir archivo CSV
          </Button>
          {archivo && (
            <Typography variant="body2">
              Seleccionado: <strong>{archivo.name}</strong> ({Math.round(archivo.size / 1024)} KB)
            </Typography>
          )}
          <Button
            variant="contained"
            disabled={!archivo || importando}
            onClick={() => void importar()}
            startIcon={importando ? <CircularProgress size={18} color="inherit" /> : undefined}
          >
            {importando ? 'Importando…' : 'Importar'}
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
};

export default ImportControlLecheroScreen;
