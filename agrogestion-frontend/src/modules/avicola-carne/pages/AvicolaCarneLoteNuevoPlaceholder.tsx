import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Button, Paper, Typography } from '@mui/material';

/**
 * Marcador de posición hasta implementar el formulario completo de alta de lote.
 */
const AvicolaCarneLoteNuevoPlaceholder: React.FC = () => {
  const navigate = useNavigate();
  return (
    <Box sx={{ p: 3, maxWidth: 640 }}>
      <Typography variant="h5" gutterBottom>
        Nuevo lote
      </Typography>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Typography color="text.secondary" paragraph>
          El formulario de alta se implementará en el siguiente paso (SPEC / diseño de campos).
        </Typography>
        <Button variant="contained" onClick={() => navigate('/avicola-carne/lotes')}>
          Volver al listado
        </Button>
      </Paper>
    </Box>
  );
};

export default AvicolaCarneLoteNuevoPlaceholder;
