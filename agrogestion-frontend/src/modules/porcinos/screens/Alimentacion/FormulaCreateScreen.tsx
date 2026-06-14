import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon } from '../../../../core/components/Icon';
import { alimentacionService } from '../../services/alimentacionService';
import type { FormulaCreateDTO, EtapaAlimentacion, InsumoFormula } from '../../types';
import BarraSubnavegacionAlimentacionPorcinos from '../../components/BarraSubnavegacionAlimentacionPorcinos';

const FormulaCreateScreen: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<FormulaCreateDTO>({
    etapa: 'GESTACION',
    nombre: '',
    insumos: [],
    cantidadRecomendada: 0,
    unidadMedida: 'kg',
    observaciones: '',
  });
  const [nuevoInsumo, setNuevoInsumo] = useState<Partial<InsumoFormula>>({
    insumoId: 0,
    cantidad: 0,
    unidadMedida: 'kg',
  });
  const [loading, setLoading] = useState(false);

  const agregarInsumo = () => {
    if (nuevoInsumo.insumoId && nuevoInsumo.cantidad) {
      setFormData({
        ...formData,
        insumos: [...formData.insumos, nuevoInsumo as InsumoFormula],
      });
      setNuevoInsumo({ insumoId: 0, cantidad: 0, unidadMedida: 'kg' });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await alimentacionService.crearFormula(formData);
      navigate('/porcinos/alimentacion/formulas');
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <BarraSubnavegacionAlimentacionPorcinos />
      <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Plus" size={32} /> Nueva Fórmula</h1>
      <form onSubmit={handleSubmit} style={{ backgroundColor: 'white', padding: '2rem', borderRadius: '0.5rem', maxWidth: '600px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label>Etapa *</label>
            <select value={formData.etapa} onChange={(e) => setFormData({...formData, etapa: e.target.value as EtapaAlimentacion})} required>
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
          <div>
            <label>Nombre *</label>
            <input type="text" value={formData.nombre} onChange={(e) => setFormData({...formData, nombre: e.target.value})} required />
          </div>
          <div>
            <label>Cantidad Recomendada *</label>
            <input type="number" step="0.1" value={formData.cantidadRecomendada} onChange={(e) => setFormData({...formData, cantidadRecomendada: parseFloat(e.target.value)})} required />
          </div>
          <div>
            <label>Unidad Medida</label>
            <select value={formData.unidadMedida} onChange={(e) => setFormData({...formData, unidadMedida: e.target.value})}>
              <option value="kg">kg</option>
              <option value="g">g</option>
              <option value="l">l</option>
            </select>
          </div>
          <div>
            <h3>Insumos</h3>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
              <input type="number" placeholder="ID Insumo" value={nuevoInsumo.insumoId} onChange={(e) => setNuevoInsumo({...nuevoInsumo, insumoId: parseInt(e.target.value)})} />
              <input type="number" step="0.1" placeholder="Cantidad" value={nuevoInsumo.cantidad} onChange={(e) => setNuevoInsumo({...nuevoInsumo, cantidad: parseFloat(e.target.value)})} />
              <select value={nuevoInsumo.unidadMedida} onChange={(e) => setNuevoInsumo({...nuevoInsumo, unidadMedida: e.target.value})}>
                <option value="kg">kg</option>
                <option value="g">g</option>
                <option value="l">l</option>
              </select>
              <button type="button" onClick={agregarInsumo}>Agregar</button>
            </div>
            {formData.insumos.length > 0 && (
              <div>
                {formData.insumos.map((insumo, i) => (
                  <div key={i} style={{ padding: '0.5rem', backgroundColor: '#f9fafb', marginBottom: '0.5rem', borderRadius: '0.25rem' }}>
                    Insumo {insumo.insumoId} - {insumo.cantidad} {insumo.unidadMedida}
                  </div>
                ))}
              </div>
            )}
          </div>
          <div>
            <label>Observaciones</label>
            <textarea value={formData.observaciones} onChange={(e) => setFormData({...formData, observaciones: e.target.value})} />
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button type="button" onClick={() => navigate('/porcinos/alimentacion/formulas')}>Cancelar</button>
            <button type="submit" disabled={loading}>{loading ? 'Guardando...' : 'Guardar'}</button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default FormulaCreateScreen;

