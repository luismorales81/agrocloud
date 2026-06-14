import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate } from 'react-router-dom';
import { padrillosService } from '../../services/padrillosService';
import type { Padrillo } from '../../types';

const PadrillosListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [padrillos, setPadrillos] = useState<Padrillo[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarPadrillos();
  }, []);

  const cargarPadrillos = async () => {
    setLoading(true);
    try {
      const data = await padrillosService.listar();
      setPadrillos(data);
    } catch (error) {
      console.error('Error al cargar padrillos:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleEliminar = async (id: number, identificacion: string) => {
    if (!confirm(`¿Está seguro de que desea eliminar el padrillo "${identificacion}"?`)) {
      return;
    }

    try {
      await padrillosService.eliminar(id);
      cargarPadrillos(); // Recargar la lista
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al eliminar el padrillo');
      console.error('Error:', error);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando padrillos...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Circle" size={32} style={{ marginRight: '0.5rem' }} /> Gestión de Padrillos
        </h1>
        <button
          onClick={() => navigate('/porcinos/padrillos/nuevo')}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '500'
          }}
        >
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Nuevo Padrillo
        </button>
      </div>

      {padrillos.length === 0 ? (
        <div style={{
          backgroundColor: 'white',
          padding: '3rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          textAlign: 'center',
          color: '#6b7280'
        }}>
          <Icon name="Circle" size={48} />
          <p>No hay padrillos registrados</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {padrillos.map((padrillo) => (
            <div
              key={padrillo.id}
              style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                transition: 'box-shadow 0.15s'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                    <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                      {padrillo.identificacion}
                    </h3>
                    {padrillo.fechaBaja && (
                      <span style={{
                        display: 'inline-block',
                        padding: '0.25rem 0.75rem',
                        borderRadius: '9999px',
                        fontSize: '0.75rem',
                        fontWeight: '500',
                        backgroundColor: '#fee2e2',
                        color: '#991b1b'
                      }}>
                        Baja
                      </span>
                    )}
                    {!padrillo.fechaBaja && (
                      <span style={{
                        display: 'inline-block',
                        padding: '0.25rem 0.75rem',
                        borderRadius: '9999px',
                        fontSize: '0.75rem',
                        fontWeight: '500',
                        backgroundColor: '#10b98120',
                        color: '#10b981'
                      }}>
                        Activo
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Nacimiento</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                        {new Date(padrillo.fechaNacimiento).toLocaleDateString('es-ES')}
                      </p>
                    </div>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Ingreso</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                        {new Date(padrillo.fechaIngresoGranja).toLocaleDateString('es-ES')}
                      </p>
                    </div>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Origen</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                        {padrillo.origen === 'EXTERNA' ? 'Externa' : 'Interna'}
                      </p>
                    </div>
                    {padrillo.fechaBaja && (
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Baja</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                          {new Date(padrillo.fechaBaja).toLocaleDateString('es-ES')}
                        </p>
                      </div>
                    )}
                    {padrillo.motivoBaja && (
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Motivo Baja</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{padrillo.motivoBaja}</p>
                      </div>
                    )}
                  </div>
                  {padrillo.observaciones && (
                    <div style={{
                      padding: '1rem',
                      backgroundColor: '#f9fafb',
                      borderRadius: '0.375rem',
                      marginTop: '1rem'
                    }}>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Observaciones</p>
                      <p style={{ fontSize: '0.875rem' }}>{padrillo.observaciones}</p>
                    </div>
                  )}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', marginLeft: '1rem' }}>
                  <button
                    onClick={() => navigate(`/porcinos/padrillos/${padrillo.id}/editar`)}
                    style={{
                      padding: '0.5rem 1rem',
                      backgroundColor: '#3b82f6',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontSize: '0.875rem',
                      fontWeight: '500',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <Icon name="Edit" size={16} />
                    Editar
                  </button>
                  <button
                    onClick={() => handleEliminar(padrillo.id!, padrillo.identificacion)}
                    style={{
                      padding: '0.5rem 1rem',
                      backgroundColor: '#ef4444',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontSize: '0.875rem',
                      fontWeight: '500',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <Icon name="Trash2" size={16} />
                    Eliminar
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default PadrillosListScreen;







