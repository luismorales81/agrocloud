import React from 'react';
import { useModule } from '../core/hooks/useModule';
import { useNavigate } from 'react-router-dom';
import { Icon } from '../components/icons';

const esIconoLucide = (icono: string): boolean => /^[A-Z][a-zA-Z0-9]*$/.test(icono);

const ModuleSelectorScreen: React.FC = () => {
  const { availableModules, setCurrentModule, loading } = useModule();
  const navigate = useNavigate();

  const handleSelectModule = (moduleId: string) => {
    setCurrentModule(moduleId as any);
    navigate(`/${moduleId}/dashboard`);
  };

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        backgroundColor: '#f3f4f6'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{
            width: '64px',
            height: '64px',
            border: '4px solid #e5e7eb',
            borderTopColor: '#3b82f6',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            margin: '0 auto 1rem'
          }}></div>
          <p style={{ color: '#6b7280', fontSize: '1rem' }}>Cargando módulos...</p>
        </div>
      </div>
    );
  }

  if (availableModules.length === 0) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        backgroundColor: '#f3f4f6'
      }}>
        <div style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          textAlign: 'center',
          maxWidth: '400px'
        }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚠️</div>
          <h2 style={{ margin: '0 0 0.5rem 0', color: '#1f2937' }}>No hay módulos disponibles</h2>
          <p style={{ color: '#6b7280', margin: 0 }}>Contacta al administrador para habilitar módulos.</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#f3f4f6',
      padding: '2rem',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center'
    }}>
      <div style={{
        maxWidth: '1200px',
        width: '100%',
        marginBottom: '3rem',
        textAlign: 'center'
      }}>
        <h1 style={{
          fontSize: '2.5rem',
          fontWeight: 'bold',
          color: '#1f2937',
          margin: '0 0 0.5rem 0'
        }}>
          AgroCloud
        </h1>
        <p style={{
          fontSize: '1.125rem',
          color: '#6b7280',
          margin: 0
        }}>
          Selecciona el módulo que deseas utilizar
        </p>
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: '1.5rem',
        width: '100%',
        maxWidth: '1200px'
      }}>
        {availableModules.map((module) => (
          <button
            key={module.id}
            onClick={() => handleSelectModule(module.id)}
            style={{
              backgroundColor: 'white',
              border: '2px solid #e5e7eb',
              borderRadius: '0.75rem',
              padding: '2rem',
              cursor: 'pointer',
              transition: 'all 0.2s',
              textAlign: 'left',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'flex-start',
              boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.borderColor = module.color;
              e.currentTarget.style.boxShadow = `0 4px 6px rgba(0, 0, 0, 0.1), 0 0 0 3px ${module.color}20`;
              e.currentTarget.style.transform = 'translateY(-2px)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.borderColor = '#e5e7eb';
              e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
              e.currentTarget.style.transform = 'translateY(0)';
            }}
          >
            <div style={{
              fontSize: '4rem',
              marginBottom: '1rem',
              lineHeight: 1,
              color: module.color,
            }}>
              {esIconoLucide(module.icono) ? (
                <Icon name={module.icono as any} size={64} color={module.color} />
              ) : (
                <span>{module.icono}</span>
              )}
            </div>
            <h3 style={{
              fontSize: '1.5rem',
              fontWeight: 'bold',
              color: '#1f2937',
              margin: '0 0 0.5rem 0'
            }}>
              {module.nombre}
            </h3>
            <p style={{
              color: '#6b7280',
              margin: 0,
              fontSize: '0.9375rem',
              lineHeight: '1.5'
            }}>
              {module.descripcion}
            </p>
            <div style={{
              marginTop: '1.5rem',
              padding: '0.5rem 1rem',
              backgroundColor: `${module.color}15`,
              color: module.color,
              borderRadius: '0.375rem',
              fontSize: '0.875rem',
              fontWeight: '500',
              alignSelf: 'flex-start'
            }}>
              Ingresar →
            </div>
          </button>
        ))}
      </div>

      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default ModuleSelectorScreen;

