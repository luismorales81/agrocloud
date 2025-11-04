import React, { useState, useEffect } from 'react';
import { cultivosService } from '../services/apiServices';
import PermissionGate from './PermissionGate';

interface Cultivo {
  id?: number;
  nombre: string;
  tipo: string;
  variedad: string;
  cicloDias: number;
  rendimientoEsperado: number;
  unidadRendimiento: string;
  precioPorTonelada: number;
  descripcion: string;
  estado: 'ACTIVO' | 'INACTIVO';
  createdAt?: string;
  updatedAt?: string;
  activo?: boolean;
}

const CultivosManagement: React.FC = () => {
  const [cultivos, setCultivos] = useState<Cultivo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [editingCultivo, setEditingCultivo] = useState<Cultivo | null>(null);
  const [formData, setFormData] = useState<Cultivo>({
    nombre: '',
    tipo: '',
    variedad: '',
    cicloDias: 0,
    rendimientoEsperado: 0,
    unidadRendimiento: 'kg/ha',
    precioPorTonelada: 0,
    descripcion: '',
    estado: 'ACTIVO'
  });

  // Tipos de cultivo disponibles
  const tiposCultivo = [
    { value: 'Cereal', label: 'Cereal' },
    { value: 'Oleaginosa', label: 'Oleaginosa' },
    { value: 'Leguminosa', label: 'Leguminosa' },
    { value: 'Forrajera', label: 'Forrajera' },
    { value: 'Hortaliza', label: 'Hortaliza' },
    { value: 'Frutal', label: 'Frutal' },
    { value: 'Industrial', label: 'Industrial' }
  ];

  // Unidades de rendimiento disponibles
  const unidadesRendimiento = [
    { value: 'kg/ha', label: 'kg/ha (Kilogramos por hectárea)' },
    { value: 'tn/ha', label: 'tn/ha (Toneladas por hectárea)' },
    { value: 'qq/ha', label: 'qq/ha (Quintales por hectárea)' },
    { value: 'kg/m2', label: 'kg/m² (Kilogramos por metro cuadrado)' }
  ];

  // Cargar cultivos desde la API real
  const loadCultivos = async () => {
    try {
      setLoading(true);
      const data = await cultivosService.listar();
      console.log('Cultivos cargados desde API:', data);
      setCultivos(data);
    } catch (error) {
      console.error('Error cargando cultivos:', error);
      // En caso de error, mostrar mensaje al usuario
      alert('Error al cargar los cultivos. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCultivos();
  }, []);

  // Guardar cultivo
  const saveCultivo = async () => {
    try {
      setLoading(true);
      
      if (editingCultivo) {
        await cultivosService.actualizar(editingCultivo.id!, formData);
      } else {
        await cultivosService.crear(formData);
      }
      
      console.log('Cultivo guardado exitosamente');
      
      // Recargar la lista de cultivos desde la API
      await loadCultivos();
      resetForm();
      
      alert(editingCultivo ? 'Cultivo actualizado exitosamente' : 'Cultivo creado exitosamente');
    } catch (error) {
      console.error('Error guardando cultivo:', error);
      alert('Error al guardar el cultivo. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  // Editar cultivo
  const editCultivo = (cultivo: Cultivo) => {
    setEditingCultivo(cultivo);
    setFormData(cultivo);
    setShowForm(true);
  };

  // Eliminar cultivo
  const deleteCultivo = async (cultivoId: number) => {
    if (!confirm('¿Estás seguro de que quieres eliminar este cultivo?')) {
      return;
    }

    try {
      setLoading(true);
      await cultivosService.eliminar(cultivoId);
      console.log('Cultivo eliminado exitosamente');
      
      // Recargar la lista de cultivos desde la API
      await loadCultivos();
      
      alert('Cultivo eliminado exitosamente');
    } catch (error) {
      console.error('Error eliminando cultivo:', error);
      alert('Error al eliminar el cultivo. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  // Resetear formulario
  const resetForm = () => {
    setFormData({
      nombre: '',
      tipo: '',
      variedad: '',
      cicloDias: 0,
      rendimientoEsperado: 0,
      unidadRendimiento: 'kg/ha',
      precioPorTonelada: 0,
      descripcion: '',
      estado: 'ACTIVO'
    });
    setEditingCultivo(null);
    setShowForm(false);
  };

  // Formatear fecha
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  // Filtrar cultivos por búsqueda
  const filteredCultivos = cultivos.filter(cultivo => {
    const searchLower = searchTerm.toLowerCase();
    return cultivo.nombre.toLowerCase().includes(searchLower) ||
           cultivo.variedad.toLowerCase().includes(searchLower) ||
           cultivo.descripcion.toLowerCase().includes(searchLower);
  });

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #22C55E 0%, #16A34A 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🌱 Gestión de Cultivos</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Administra los cultivos disponibles para tus lotes
        </p>
      </div>

      {/* Botón para agregar nuevo cultivo */}
      <div style={{ marginBottom: '20px' }}>
        <PermissionGate permission="canCreateCultivos">
          <button
            onClick={() => setShowForm(true)}
            style={{
              background: '#22C55E',
              color: 'white',
              border: 'none',
              padding: '12px 24px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold'
            }}
          >
            ➕ Agregar Nuevo Cultivo
          </button>
        </PermissionGate>
      </div>

      {/* Formulario para nuevo/editar cultivo */}
      {showForm && (
        <div style={{ 
          background: '#f9f9f9', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ddd'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#333' }}>
            {editingCultivo ? '✏️ Editar Cultivo' : '📝 Nuevo Cultivo'}
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Nombre del Cultivo:
              </label>
              <input
                type="text"
                value={formData.nombre}
                onChange={(e) => setFormData(prev => ({ ...prev, nombre: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Ej: Soja, Maíz, Trigo"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Tipo de Cultivo:
              </label>
              <select
                value={formData.tipo}
                onChange={(e) => setFormData(prev => ({ ...prev, tipo: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="">Seleccionar tipo</option>
                {tiposCultivo.map(tipo => (
                  <option key={tipo.value} value={tipo.value}>
                    {tipo.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Variedad:
              </label>
              <input
                type="text"
                value={formData.variedad}
                onChange={(e) => setFormData(prev => ({ ...prev, variedad: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Ej: DM 53i54, DK 72-10"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Ciclo (días):
              </label>
              <input
                type="number"
                value={formData.cicloDias}
                onChange={(e) => setFormData(prev => ({ ...prev, cicloDias: parseInt(e.target.value) || 0 }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="120"
                min="1"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Rendimiento Esperado:
              </label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="number"
                  value={formData.rendimientoEsperado}
                  onChange={(e) => setFormData(prev => ({ ...prev, rendimientoEsperado: parseFloat(e.target.value) || 0 }))}
                  style={{
                    flex: 1,
                    padding: '10px',
                    border: '1px solid #ddd',
                    borderRadius: '5px',
                    fontSize: '14px'
                  }}
                  placeholder="3500"
                  step="0.1"
                  min="0"
                />
                <select
                  value={formData.unidadRendimiento}
                  onChange={(e) => setFormData(prev => ({ ...prev, unidadRendimiento: e.target.value }))}
                  style={{
                    padding: '10px',
                    border: '1px solid #ddd',
                    borderRadius: '5px',
                    fontSize: '14px'
                  }}
                >
                  {unidadesRendimiento.map((unidad) => (
                    <option key={unidad.value} value={unidad.value}>
                      {unidad.value}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Precio por Tonelada ($):
              </label>
              <input
                type="number"
                value={formData.precioPorTonelada}
                onChange={(e) => setFormData(prev => ({ ...prev, precioPorTonelada: parseFloat(e.target.value) || 0 }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="450.00"
                step="0.01"
                min="0"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Estado:
              </label>
              <select
                value={formData.estado}
                onChange={(e) => setFormData(prev => ({ ...prev, estado: e.target.value as 'ACTIVO' | 'INACTIVO' }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="ACTIVO">✅ Activo</option>
                <option value="INACTIVO">❌ Inactivo</option>
              </select>
            </div>
          </div>

          <div style={{ marginTop: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Descripción:
            </label>
            <textarea
              value={formData.descripcion}
              onChange={(e) => setFormData(prev => ({ ...prev, descripcion: e.target.value }))}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px',
                minHeight: '80px',
                resize: 'vertical'
              }}
              placeholder="Describe las características del cultivo..."
            />
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              onClick={saveCultivo}
              disabled={loading || !formData.nombre || !formData.variedad}
              style={{
                background: '#22C55E',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: loading || !formData.nombre || !formData.variedad ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                opacity: loading || !formData.nombre || !formData.variedad ? 0.6 : 1
              }}
            >
              {loading ? '💾 Guardando...' : (editingCultivo ? '💾 Actualizar' : '💾 Guardar Cultivo')}
            </button>
            
            <button
              onClick={resetForm}
              style={{
                background: '#f44336',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Búsqueda */}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="🔍 Buscar cultivos por nombre, variedad o descripción..."
          style={{
            width: '100%',
            padding: '12px',
            border: '1px solid #ddd',
            borderRadius: '8px',
            fontSize: '16px'
          }}
        />
      </div>

      {/* Lista de cultivos */}
      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f5f5f5', 
          padding: '15px', 
          borderBottom: '1px solid #ddd',
          fontWeight: 'bold'
        }}>
          📋 Cultivos Registrados ({filteredCultivos.length})
        </div>
        
        {loading ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            🔄 Cargando cultivos...
          </div>
        ) : filteredCultivos.length === 0 ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            {searchTerm ? 'No se encontraron cultivos que coincidan con la búsqueda' : 'No hay cultivos registrados'}
          </div>
        ) : (
          <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
            {filteredCultivos.map((cultivo) => (
              <div key={cultivo.id} style={{ 
                padding: '15px', 
                borderBottom: '1px solid #eee',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start'
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
                    <span style={{ 
                      fontSize: '20px', 
                      marginRight: '10px' 
                    }}>
                      🌱
                    </span>
                    <h4 style={{ margin: '0', color: '#333' }}>
                      {cultivo.nombre} - {cultivo.variedad}
                    </h4>
                    <span style={{
                      marginLeft: '10px',
                      padding: '2px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      background: cultivo.estado === 'ACTIVO' ? '#dcfce7' : '#fee2e2',
                      color: cultivo.estado === 'ACTIVO' ? '#166534' : '#991b1b'
                    }}>
                      {cultivo.estado === 'ACTIVO' ? '✅ Activo' : '❌ Inactivo'}
                    </span>
                  </div>
                  <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                    <strong>Tipo:</strong> {cultivo.tipo} | 
                    <strong> Ciclo:</strong> {cultivo.cicloDias} días | 
                    <strong> Rendimiento:</strong> {cultivo.rendimientoEsperado} {cultivo.unidadRendimiento}
                  </p>
                  {cultivo.precioPorTonelada > 0 && (
                    <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                      <strong>Precio por Tonelada:</strong> ${cultivo.precioPorTonelada.toFixed(2)}
                    </p>
                  )}
                  {cultivo.descripcion && (
                    <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                      <strong>Descripción:</strong> {cultivo.descripcion}
                    </p>
                  )}
                  {cultivo.createdAt && (
                    <p style={{ margin: '0', color: '#666', fontSize: '12px' }}>
                      <strong>Creado:</strong> {formatDate(cultivo.createdAt)}
                    </p>
                  )}
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <PermissionGate permission="canEditCultivos">
                    <button
                      onClick={() => editCultivo(cultivo)}
                      style={{
                        background: '#2196F3',
                        color: 'white',
                        border: 'none',
                        padding: '8px 16px',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      ✏️ Editar
                    </button>
                  </PermissionGate>
                  <PermissionGate permission="canDeleteCultivos">
                    <button
                      onClick={() => deleteCultivo(cultivo.id!)}
                      style={{
                        background: '#f44336',
                        color: 'white',
                        border: 'none',
                        padding: '8px 16px',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      🗑️ Eliminar
                    </button>
                  </PermissionGate>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CultivosManagement;
