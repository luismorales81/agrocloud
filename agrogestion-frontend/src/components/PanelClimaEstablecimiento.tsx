import React from 'react';
import { Alert, Box, Button, Paper, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import OpenMeteoWeatherWidget from './OpenMeteoWeatherWidget';

export interface PanelClimaEstablecimientoProps {
  climaLatitud?: number | null;
  climaLongitud?: number | null;
  nombreEstablecimiento?: string | null;
  /** Ruta al editor de mapa (ej. /avicola-crianza/establecimientos-mapa?id=1). */
  rutaMapa?: string | null;
  compacto?: boolean;
}

const PanelClimaEstablecimiento: React.FC<PanelClimaEstablecimientoProps> = ({
  climaLatitud,
  climaLongitud,
  nombreEstablecimiento,
  rutaMapa,
  compacto = true,
}) => {
  const tieneCoordenadas = climaLatitud != null && climaLongitud != null;

  if (!tieneCoordenadas) {
    return (
      <Alert severity="info" sx={{ mb: 2 }}>
        <Typography variant="body2" gutterBottom>
          {nombreEstablecimiento
            ? `El establecimiento «${nombreEstablecimiento}» no tiene ubicación en mapa.`
            : 'El establecimiento vinculado no tiene ubicación en mapa.'}{' '}
          Sin coordenadas no se puede mostrar el clima local ni autocompletar temperatura y humedad.
        </Typography>
        {rutaMapa && (
          <Button component={RouterLink} to={rutaMapa} size="small" variant="outlined" sx={{ mt: 0.5 }}>
            Abrir ubicación en mapa
          </Button>
        )}
      </Alert>
    );
  }

  return (
    <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
      <Typography variant="subtitle2" color="text.secondary" gutterBottom>
        Clima del establecimiento{nombreEstablecimiento ? `: ${nombreEstablecimiento}` : ''}
      </Typography>
      <Box sx={{ maxWidth: 480 }}>
        <OpenMeteoWeatherWidget
          fieldName={nombreEstablecimiento ?? 'Establecimiento'}
          coordinates={{ lat: climaLatitud, lon: climaLongitud }}
          compact={compacto}
        />
      </Box>
      {rutaMapa && (
        <Button component={RouterLink} to={rutaMapa} size="small" sx={{ mt: 1 }}>
          Editar ubicación en mapa
        </Button>
      )}
    </Paper>
  );
};

export default PanelClimaEstablecimiento;
