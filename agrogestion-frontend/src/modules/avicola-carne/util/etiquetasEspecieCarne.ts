import type { AvicolaCarneEspecie } from '../types';

const ETIQUETAS: Record<string, string> = {
  POLLO_PARRILLERO: 'Pollo parrillero',
  PAVO: 'Pavo',
  PATO: 'Pato',
  PERDIZ: 'Perdiz',
  GALLINA_PONEDORA: 'Gallina ponedora',
  OTRO: 'Otro',
};

export function etiquetaEspecieCarne(codigo: string | undefined): string {
  if (!codigo) return '—';
  const clave = codigo as AvicolaCarneEspecie;
  return ETIQUETAS[clave] ?? codigo;
}
