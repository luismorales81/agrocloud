import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate } from 'react-router-dom';
import { alimentacionService } from '../../services/alimentacionService';
import type { EtapaAlimentacion } from '../../types';
import BarraSubnavegacionAlimentacionPorcinos from '../../components/BarraSubnavegacionAlimentacionPorcinos';

const FormulaListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [formulas, setFormulas] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtroEtapa, setFiltroEtapa] = useState<string>('');

  useEffect(() => {
    cargarFormulas();
  }, []);

  const cargarFormulas = async () => {
    setLoading(true);
    try {
      const data = filtroEtapa 
        ? await alimentacionService.obtenerFormulasPorEtapa(filtroEtapa)
        : await alimentacionService.listarFormulas();
      setFormulas(data);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarFormulas();
  }, [filtroEtapa]);

  return (
    <div style={{ padding: '2rem' }}>
      <BarraSubnavegacionAlimentacionPorcinos />
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Wheat" size={32} /> Fórmulas de Alimentación</h1>
        <button onClick={() => navigate('/porcinos/alimentacion/formulas/nueva')} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Plus" size={18} /> Nueva Fórmula</button>
      </div>
      <div style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '0.5rem', marginBottom: '1rem' }}>
        <select value={filtroEtapa} onChange={(e) => setFiltroEtapa(e.target.value)}>
          <option value="">Todas las etapas</option>
          <option value="GESTACION">Gestación</option>
          <option value="LACTANCIA">Lactancia</option>
          <option value="F1">F1</option>
          <option value="F2">F2</option>
          <option value="F3">F3</option>
          <option value="F4">F4</option>
          <option value="DESARROLLO">Desarrollo</option>
          <option value="TERMINACION">Terminación</option>
        </select>
      </div>
      {loading ? <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><SemanticIcon semanticName="pending" size={24} /> Cargando...</div> : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {formulas.map((f) => (
            <div key={f.id} style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3>{f.nombre} - {f.etapa}</h3>
                  <p>Cantidad Recomendada: {f.cantidadRecomendada} {f.unidadMedida}</p>
                  <p>Insumos: {f.insumos?.length || 0}</p>
                  <span style={{ padding: '0.25rem 0.75rem', borderRadius: '0.25rem', backgroundColor: f.activa ? '#10b981' : '#6b7280', color: 'white', fontSize: '0.75rem' }}>
                    {f.activa ? 'Activa' : 'Inactiva'}
                  </span>
                </div>
                <button onClick={() => navigate(`/porcinos/alimentacion/formulas/${f.id}`)}>Ver Detalle</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FormulaListScreen;

