import React from 'react';
import TrazabilidadExpedienteScreen from './TrazabilidadExpedienteScreen';
import {
  configuracionExpedienteAvicolaCrianza,
  configuracionExpedienteAvicolaHuevos,
  configuracionExpedienteAvicolaPonedoras,
  configuracionExpedienteFeedlot,
  configuracionExpedienteCultivos,
  configuracionExpedientePorcinos,
} from './configuracionesExpediente';

export const ExpedienteTrazabilidadCultivos: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedienteCultivos} />
);

export const ExpedienteTrazabilidadPorcinos: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedientePorcinos} />
);

export const ExpedienteTrazabilidadAvicolaHuevos: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedienteAvicolaHuevos} />
);

export const ExpedienteTrazabilidadAvicolaCrianza: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedienteAvicolaCrianza} />
);

export const ExpedienteTrazabilidadAvicolaCarne: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedienteAvicolaCrianza} />
);

export const ExpedienteTrazabilidadAvicolaPonedoras: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedienteAvicolaPonedoras} />
);

export const ExpedienteTrazabilidadFeedlot: React.FC = () => (
  <TrazabilidadExpedienteScreen configuracion={configuracionExpedienteFeedlot} />
);
