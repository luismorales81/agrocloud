import React from 'react';

/**
 * Pantalla de listado de Madres
 */
const MadresListScreen: React.FC = () => {
  return (
    <div style={{ padding: '2rem' }}>
      <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '1rem', color: '#1f2937' }}>
        🐷 Gestión de Madres
      </h1>
      <p style={{ color: '#6b7280', fontSize: '1.125rem' }}>
        Gestiona el inventario de madres reproductoras.
      </p>
      <div style={{
        marginTop: '2rem',
        padding: '2rem',
        backgroundColor: '#f9fafb',
        borderRadius: '0.5rem',
        border: '1px solid #e5e7eb'
      }}>
        <p style={{ color: '#6b7280' }}>
          Esta funcionalidad está en desarrollo.
        </p>
      </div>
    </div>
  );
};

export default MadresListScreen;

