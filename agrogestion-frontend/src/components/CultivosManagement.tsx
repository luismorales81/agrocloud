import React, { useState, useEffect } from 'react';

interface Cultivo {
  id?: number;
  nombre: string;
  variedad: string;
  ciclo_dias: number;
  rendimiento_esperado: number;
  unidad_rendimiento: string;
  descripcion: string;
  estado: 'activo' | 'inactivo';
  fecha_creacion?: string;
}

const CultivosManagement: React.FC = () => {
  const [cultivos, setCultivos] = useState<Cultivo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [editingCultivo, setEditingCultivo] = useState<Cultivo | null>(null);
  const [formData, setFormData] = useState<Cultivo>({
    nombre: '',
    variedad: '',
    ciclo_dias: 0,
    rendimiento_esperado: 0,
    unidad_rendimiento: 'kg/ha',
    descripcion: '',
    estado: 'activo'
  });

  // Unidades de rendimiento disponibles
  const unidadesRendimiento = [
    { value: 'kg/ha', label: 'kg/ha (Kilogramos por hectárea)' },
    { value: 'tn/ha', label: 'tn/ha (Toneladas por hectárea)' },
    { value: 'qq/ha', label: 'qq/ha (Quintales por hectárea)' },
    { value: 'kg/m2', label: 'kg/m² (Kilogramos por metro cuadrado)' }
  ];

  // Cargar cultivos desde la API (simulación)
  const loadCultivos = async () => {
    try {
      setLoading(true);
      // Simulación de carga desde API
      const mockCultivos: Cultivo[] = [
        {
          id: 1,
          nombre: 'Soja',
          variedad: 'DM 53i54',
          ciclo_dias: 120,
          rendimiento_esperado: 3500,
          unidad_rendimiento: 'kg/ha',
          descripcion: 'Soja de ciclo corto, resistente a sequía y enfermedades',
          estado: 'activo',
          fecha_creacion: '2024-01-15'
        },
        {
          id: 2,
          nombre: 'Maíz',
          variedad: 'DK 72-10',
          ciclo_dias: 140,
          rendimiento_esperado: 12,
          unidad_rendimiento: 'tn/ha',
          descripcion: 'Maíz híbrido de alto rendimiento, adaptado a diferentes suelos',
          estado: 'activo',
          fecha_creacion: '2024-01-20'
        },
        {
          id: 3,
          nombre: 'Trigo',
          variedad: 'Klein Pantera',
          ciclo_dias: 180,
          rendimiento_esperado: 4500,
          unidad_rendimiento: 'kg/ha',
          descripcion: 'Trigo de calidad panadera, resistente a royas',
          estado: 'activo',
          fecha_creacion: '2024-02-01'
        },
        {
          id: 4,
          nombre: 'Girasol',
          variedad: 'Paraíso 33',
          ciclo_dias: 110,
          rendimiento_esperado: 2.5,
          unidad_rendimiento: 'tn/ha',
          descripcion: 'Girasol confitero de alto contenido oleico',
          estado: 'inactivo',
          fecha_creacion: '2024-02-10'
        }
      ];
      setCultivos(mockCultivos);
    } catch (error) {
      console.error('Error cargando cultivos:', error);
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
        // Actualizar cultivo existente
        const updatedCultivos = cultivos.map(cultivo => 
          cultivo.id === editingCultivo.id 
            ? { ...formData, id: cultivo.id, fecha_creacion: cultivo.fecha_creacion }
            : cultivo
        );
        setCultivos(updatedCultivos);
      } else {
        // Crear nuevo cultivo
        const newCultivo: Cultivo = {
          id: cultivos.length + 1,
          ...formData,
          fecha_creacion: new Date().toISOString().split('T')[0]
        };
        setCultivos(prev => [...prev, newCultivo]);
      }
      
      resetForm();
    } catch (error) {
      console.error('Error guardando cultivo:', error);
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
  const deleteCultivo = (cultivoId: number) => {
    setCultivos(prev => prev.filter(cultivo => cultivo.id !== cultivoId));
  };

  // Resetear formulario
  const resetForm = () => {
    setFormData({
      nombre: '',
      variedad: '',
      ciclo_dias: 0,
      rendimiento_esperado: 0,
      unidad_rendimiento: 'kg/ha',
      descripcion: '',
      estado: 'activo'
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
                value={formData.ciclo_dias}
                onChange={(e) => setFormData(prev => ({ ...prev, ciclo_dias: parseInt(e.target.value) || 0 }))}
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
                  value={formData.rendimiento_esperado}
                  onChange={(e) => setFormData(prev => ({ ...prev, rendimiento_esperado: parseFloat(e.target.value) || 0 }))}
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
                  value={formData.unidad_rendimiento}
                  onChange={(e) => setFormData(prev => ({ ...prev, unidad_rendimiento: e.target.value }))}
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
                Estado:
              </label>
              <select
                value={formData.estado}
                onChange={(e) => setFormData(prev => ({ ...prev, estado: e.target.value as 'activo' | 'inactivo' }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="activo">✅ Activo</option>
                <option value="inactivo">❌ Inactivo</option>
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
                      background: cultivo.estado === 'activo' ? '#dcfce7' : '#fee2e2',
                      color: cultivo.estado === 'activo' ? '#166534' : '#991b1b'
                    }}>
                      {cultivo.estado === 'activo' ? '✅ Activo' : '❌ Inactivo'}
                    </span>
                  </div>
                  <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                    <strong>Ciclo:</strong> {cultivo.ciclo_dias} días | 
                    <strong> Rendimiento:</strong> {cultivo.rendimiento_esperado} {cultivo.unidad_rendimiento}
                  </p>
                  {cultivo.descripcion && (
                    <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                      <strong>Descripción:</strong> {cultivo.descripcion}
                    </p>
                  )}
                  {cultivo.fecha_creacion && (
                    <p style={{ margin: '0', color: '#666', fontSize: '12px' }}>
                      <strong>Creado:</strong> {formatDate(cultivo.fecha_creacion)}
                    </p>
                  )}
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
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
