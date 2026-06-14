import React from 'react';
import CalendarioDashboard from '../../../components/CalendarioDashboard';

/**
 * Calendario del módulo avícola huevos: recordatorios por lote y tareas recurrentes con ámbito huevos.
 */
const CalendarioHuevosScreen: React.FC = () => {
  return <CalendarioDashboard modoCalendario="avicolaHuevos" />;
};

export default CalendarioHuevosScreen;
