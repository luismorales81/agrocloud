import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { alimentacionService } from '../../services/alimentacionService';
import BarraSubnavegacionAlimentacionPorcinos from '../../components/BarraSubnavegacionAlimentacionPorcinos';

type FiltrosHistorial = { fechaDesde: string; fechaHasta: string; loteId: string; madreId: string };

const filtrosVacios: FiltrosHistorial = { fechaDesde: '', fechaHasta: '', loteId: '', madreId: '' };

const HistorialConsumosScreen: React.FC = () => {
  const [consumos, setConsumos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtros, setFiltros] = useState<FiltrosHistorial>(filtrosVacios);

  const cargarConFiltros = async (f: FiltrosHistorial) => {
    setLoading(true);
    try {
      const data = await alimentacionService.obtenerHistorialConsumos({
        fechaDesde: f.fechaDesde || undefined,
        fechaHasta: f.fechaHasta || undefined,
        loteId: f.loteId ? Number(f.loteId) : undefined,
        madreId: f.madreId ? Number(f.madreId) : undefined,
      });
      setConsumos(data);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void cargarConFiltros(filtrosVacios);
  }, []);

  return (
    <div style={{ padding: '2rem' }}>
      <BarraSubnavegacionAlimentacionPorcinos />
      <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <Icon name="BarChart" size={32} /> Historial por fórmula
      </h1>
      <p style={{ color: '#6b7280', marginBottom: '1.5rem', maxWidth: '48rem' }}>
        Consumos registrados en el sistema de fórmulas (lote o madre). Para cargas manuales por categoría vaya a «Consumos manuales».
      </p>
      <div style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '0.5rem', marginBottom: '1rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: '1rem', alignItems: 'end' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, marginBottom: '0.25rem', color: '#374151' }}>Desde</label>
            <input type="date" aria-label="Fecha desde" value={filtros.fechaDesde} onChange={(e) => setFiltros({ ...filtros, fechaDesde: e.target.value })} style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }} />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, marginBottom: '0.25rem', color: '#374151' }}>Hasta</label>
            <input type="date" aria-label="Fecha hasta" value={filtros.fechaHasta} onChange={(e) => setFiltros({ ...filtros, fechaHasta: e.target.value })} style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }} />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, marginBottom: '0.25rem', color: '#374151' }}>Id. lote</label>
            <input type="number" min={0} placeholder="Opcional" value={filtros.loteId} onChange={(e) => setFiltros({ ...filtros, loteId: e.target.value })} style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }} />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, marginBottom: '0.25rem', color: '#374151' }}>Id. madre</label>
            <input type="number" min={0} placeholder="Opcional" value={filtros.madreId} onChange={(e) => setFiltros({ ...filtros, madreId: e.target.value })} style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }} />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <button type="button" onClick={() => void cargarConFiltros(filtros)} style={{ padding: '0.5rem 1rem', backgroundColor: '#2563eb', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer', fontWeight: 500 }}>
              Aplicar filtros
            </button>
            <button
              type="button"
              onClick={() => {
                setFiltros(filtrosVacios);
                void cargarConFiltros(filtrosVacios);
              }}
              style={{ padding: '0.5rem 1rem', backgroundColor: '#f3f4f6', color: '#374151', border: '1px solid #d1d5db', borderRadius: '0.375rem', cursor: 'pointer' }}
            >
              Limpiar
            </button>
          </div>
        </div>
      </div>
      {loading ? <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><SemanticIcon semanticName="pending" size={24} /> Cargando...</div> : (
        <div style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '0.5rem' }}>
          {consumos.length === 0 ? <p>No hay consumos registrados</p> : (
            <table style={{ width: '100%' }}>
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Lote/Madre</th>
                  <th>Fórmula</th>
                  <th>Cantidad</th>
                  <th>Unidad</th>
                </tr>
              </thead>
              <tbody>
                {consumos.map((c) => (
                  <tr key={c.id}>
                    <td>{new Date(c.fecha).toLocaleDateString('es-ES')}</td>
                    <td>{c.loteNombre || c.madreIdentificacion || 'N/A'}</td>
                    <td>{c.formulaNombre || c.formulaId}</td>
                    <td>{c.cantidad}</td>
                    <td>{c.unidadMedida}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
};

export default HistorialConsumosScreen;

