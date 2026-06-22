import React from 'react';
import CalendarioDashboard from '../../../components/CalendarioDashboard';

/**
 * Calendario del módulo feedlot: tareas recurrentes con ámbito FEEDLOT.
 */
const CalendarioFeedlotScreen: React.FC = () => {
  return <CalendarioDashboard modoCalendario="feedlot" />;
};

export default CalendarioFeedlotScreen;
