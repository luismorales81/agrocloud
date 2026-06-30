import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useRellenarClimaDesdeCoordenadas, ValoresClima } from '../hooks/useRellenarClimaDesdeCoordenadas';

export interface BotonRellenarClimaProps {
  climaLatitud?: number | null;
  climaLongitud?: number | null;
  deshabilitado?: boolean;
  textoBoton?: string;
  mensajeSinCoordenadas?: string;
  onValores: (valores: ValoresClima) => void;
  onError?: (mensaje: string) => void;
}

const BotonRellenarClima: React.FC<BotonRellenarClimaProps> = ({
  climaLatitud,
  climaLongitud,
  deshabilitado = false,
  textoBoton = 'Rellenar con clima del establecimiento',
  mensajeSinCoordenadas = 'Definí la ubicación en el mapa del establecimiento para usar esta acción. Los valores siguen siendo editables a mano.',
  onValores,
  onError,
}) => {
  const { consultarClima, cargando } = useRellenarClimaDesdeCoordenadas();

  const sinCoordenadas = climaLatitud == null || climaLongitud == null;

  const manejarClick = async () => {
    const valores = await consultarClima(climaLatitud, climaLongitud);
    if (valores) {
      onValores(valores);
    }
  };

  return (
    <Box>
      <Button
        type="button"
        variant="outlined"
        size="small"
        disabled={deshabilitado || sinCoordenadas || cargando}
        onClick={() => void manejarClick()}
      >
        {cargando ? 'Consultando clima…' : textoBoton}
      </Button>
      {sinCoordenadas && (
        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 0.5 }}>
          {mensajeSinCoordenadas}
        </Typography>
      )}
    </Box>
  );
};

export default BotonRellenarClima;
