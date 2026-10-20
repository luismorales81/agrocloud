import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Alert, Box, Button, Chip, Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material';
import type { FeedlotAnimalDetalle } from '../types';
import { mensajeError, obtenerAnimalFeedlot } from '../services/feedlotApi';

const DetalleAnimalFeedlotScreen: React.FC = () => {
  const { id, animalId } = useParams<{ id: string; animalId: string }>();
  const navigate = useNavigate();
  const [animal, setAnimal] = useState<FeedlotAnimalDetalle | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const n = animalId ? parseInt(animalId, 10) : NaN;
    if (Number.isNaN(n)) return;
    obtenerAnimalFeedlot(n)
      .then(setAnimal)
      .catch((e) => setError(mensajeError(e)));
  }, [animalId]);

  return (
    <Box sx={{ p: 3 }}>
      <Button onClick={() => navigate(`/feedlot/lotes/${id}`)} sx={{ mb: 2 }}>
        Volver al lote
      </Button>
      {error && <Alert severity="error">{error}</Alert>}
      {animal && (
        <>
          <Typography variant="h5" gutterBottom>
            Animal {animal.caravanaVisual ?? animal.eid}
          </Typography>
          <Chip label={animal.estado} sx={{ mr: 1 }} />
          {animal.retiroVigente && <Chip color="warning" label="Retiro vigente" />}
          <Typography sx={{ mt: 2 }}>EID: {animal.eid ?? '—'}</Typography>
          <Typography>Caravana visual: {animal.caravanaVisual ?? '—'}</Typography>
          <Paper sx={{ mt: 2, p: 2 }}>
            <Typography variant="subtitle1">Pesadas</Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Peso kg</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(animal.pesadas ?? []).map((p) => (
                  <TableRow key={p.id}>
                    <TableCell>{p.fecha}</TableCell>
                    <TableCell>{p.pesoKg}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
          <Paper sx={{ mt: 2, p: 2 }}>
            <Typography variant="subtitle1">Sanidad</Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Tipo</TableCell>
                  <TableCell>Retiro</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(animal.eventosSanitarios ?? []).map((e) => (
                  <TableRow key={e.id}>
                    <TableCell>{e.fecha}</TableCell>
                    <TableCell>{e.tipo}</TableCell>
                    <TableCell>{e.diasRetiro ?? '—'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Paper>
        </>
      )}
    </Box>
  );
};

export default DetalleAnimalFeedlotScreen;
