import React from 'react';

const AccessDenied: React.FC = () => {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '400px',
      padding: '2rem',
      textAlign: 'center'
    }}>
      <div style={{
        fontSize: '4rem',
        marginBottom: '1rem'
      }}>
        🚫
      </div>
      
      <h2 style={{
        color: '#dc2626',
        marginBottom: '1rem',
        fontSize: '1.5rem'
      }}>
        Acceso Denegado
      </h2>
      
      <p style={{
        color: '#6b7280',
        marginBottom: '1.5rem',
        maxWidth: '400px',
        lineHeight: '1.6'
      }}>
        No tienes permisos para acceder a esta sección. 
        Contacta al administrador si necesitas acceso.
      </p>
      
      <button
        onClick={() => window.history.back()}
        style={{
          backgroundColor: '#3b82f6',
          color: 'white',
          border: 'none',
          padding: '0.75rem 1.5rem',
          borderRadius: '0.375rem',
          cursor: 'pointer',
          fontSize: '1rem',
          fontWeight: '500'
        }}
      >
        Volver Atrás
      </button>
    </div>
  );
};

export default AccessDenied;
