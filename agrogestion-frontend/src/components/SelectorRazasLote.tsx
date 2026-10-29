import React from 'react';
import { Checkbox, FormControl, InputLabel, ListItemText, MenuItem, Select } from '@mui/material';

export interface OpcionRazaLote {
  id: number;
  nombre: string;
  activo?: boolean;
}

interface SelectorRazasLoteProps {
  razas: OpcionRazaLote[];
  seleccionadas: number[];
  onChange: (ids: number[]) => void;
  deshabilitado?: boolean;
}

/** Selector múltiple de razas ya cargadas en el catálogo del módulo. */
const SelectorRazasLote: React.FC<SelectorRazasLoteProps> = ({
  razas,
  seleccionadas,
  onChange,
  deshabilitado = false,
}) => {
  const opciones = razas.filter((raza) => raza.activo !== false || seleccionadas.includes(raza.id));

  return (
    <FormControl fullWidth required size="small" disabled={deshabilitado}>
      <InputLabel id="razas-del-lote">Razas</InputLabel>
      <Select
        labelId="razas-del-lote"
        multiple
        label="Razas"
        value={seleccionadas}
        onChange={(evento) => {
          const valor = evento.target.value;
          const ids = typeof valor === 'string' ? valor.split(',').map(Number) : valor;
          onChange(ids);
        }}
        renderValue={(ids) =>
          razas
            .filter((raza) => ids.includes(raza.id))
            .map((raza) => raza.nombre)
            .join(' · ') || 'Seleccionar…'
        }
      >
        {opciones.map((raza) => (
          <MenuItem key={raza.id} value={raza.id}>
            <Checkbox checked={seleccionadas.includes(raza.id)} />
            <ListItemText primary={raza.activo === false ? `${raza.nombre} (inactiva)` : raza.nombre} />
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

export default SelectorRazasLote;
