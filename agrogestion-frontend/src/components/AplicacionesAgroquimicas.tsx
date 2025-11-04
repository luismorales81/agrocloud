import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { aplicacionesAgroquimicasService, laboresService, insumosService } from '../services/apiServices';
import PermissionGate from './PermissionGate';

interface AplicacionAgroquimica {
  id?: number;
  laborId: number;
  laborNombre: string;
  insumoId: number;
  insumoNombre: string;
  tipoAplicacion: string;
  cantidadTotalAplicar: number;
  dosisAplicadaPorHa: number;
  superficieAplicadaHa: number;
  unidadMedida: string;
  observaciones: string;
  fechaAplicacion: string;
  fechaRegistro: string;
  activo: boolean;
}

interface Labor {
  id: number;
  tipoLabor: string;
  fecha: string;
  loteId: number;
  loteNombre: string;
  loteSuperficieHa: number;
}

interface Insumo {
  id: number;
  nombre: string;
  tipo: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
}

interface DosisSugerida {
  dosisPorHa: number;
  unidadMedida: string;
  descripcion: string;
}

const AplicacionesAgroquimicas: React.FC = () => {
  const { user } = useAuth();
  const { rolUsuario } = useEmpresa();
  
  const [aplicaciones, setAplicaciones] = useState<AplicacionAgroquimica[]>([]);
  const [labores, setLabores] = useState<Labor[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterInsumo, setFilterInsumo] = useState('todos');
  
  // Estados del formulario
  const [formData, setFormData] = useState({
    laborId: 0,
    insumoId: 0,
    tipoAplicacion: 'FOLIAR',
    cantidadTotalAplicar: 0,
    dosisAplicadaPorHa: 0,
    superficieAplicadaHa: 0,
    unidadMedida: '',
    observaciones: '',
    fechaAplicacion: new Date().toISOString().split('T')[0]
  });
  
  // Estados para el cálculo automático
  const [dosisSugerida, setDosisSugerida] = useState<DosisSugerida | null>(null);
  const [calculando, setCalculando] = useState(false);

  // Tipos de aplicación
  const tiposAplicacion = [
    { value: 'FOLIAR', label: 'Foliar' },
    { value: 'TERRESTRE', label: 'Terrestre' },
    { value: 'AEREA', label: 'Aérea' },
    { value: 'PRECISION', label: 'Precisión' }
  ];

  // Cargar datos
  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      
      // Cargar aplicaciones
      const aplicacionesData = await aplicacionesAgroquimicasService.listar();
      setAplicaciones(Array.isArray(aplicacionesData) ? aplicacionesData : []);
      
      // Cargar labores
      const laboresData = await laboresService.listar();
      setLabores(Array.isArray(laboresData) ? laboresData : []);
      
      // Cargar insumos
      const insumosData = await insumosService.listar();
      setInsumos(Array.isArray(insumosData) ? insumosData : []);
      
    } catch (error) {
      console.error('Error cargando datos:', error);
      alert('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  // Obtener dosis sugerida
  const obtenerDosisSugerida = async () => {
    if (!formData.insumoId || !formData.tipoAplicacion) return;
    
    try {
      setCalculando(true);
      const dosisData = await aplicacionesAgroquimicasService.obtenerDosisSugerida(
        formData.insumoId,
        formData.tipoAplicacion
      );
      setDosisSugerida(dosisData);
      
      // Calcular cantidad total automáticamente
      if (dosisData && formData.superficieAplicadaHa > 0) {
        const cantidadTotal = formData.superficieAplicadaHa * dosisData.dosisPorHa;
        setFormData(prev => ({
          ...prev,
          dosisAplicadaPorHa: dosisData.dosisPorHa,
          unidadMedida: dosisData.unidadMedida,
          cantidadTotalAplicar: cantidadTotal
        }));
      }
    } catch (error) {
      console.error('Error obteniendo dosis sugerida:', error);
    } finally {
      setCalculando(false);
    }
  };

  // Calcular cantidad total
  const calcularCantidadTotal = () => {
    if (formData.superficieAplicadaHa > 0 && formData.dosisAplicadaPorHa > 0) {
      const cantidadTotal = formData.superficieAplicadaHa * formData.dosisAplicadaPorHa;
      setFormData(prev => ({ ...prev, cantidadTotalAplicar: cantidadTotal }));
    }
  };

  // Cuando cambia la labor, obtener la superficie del lote
  const handleLaborChange = (laborId: number) => {
    const labor = labores.find(l => l.id === laborId);
    if (labor) {
      setFormData(prev => ({
        ...prev,
        laborId,
        superficieAplicadaHa: labor.loteSuperficieHa
      }));
      
      // Si ya hay insumo y tipo seleccionados, calcular
      if (formData.insumoId && formData.tipoAplicacion) {
        obtenerDosisSugerida();
      }
    }
  };

  // Cuando cambia el insumo
  const handleInsumoChange = (insumoId: number) => {
    setFormData(prev => ({ ...prev, insumoId }));
    
    // Si ya hay labor y tipo seleccionados, obtener dosis sugerida
    if (formData.laborId && formData.tipoAplicacion) {
      obtenerDosisSugerida();
    }
  };

  // Cuando cambia el tipo de aplicación
  const handleTipoAplicacionChange = (tipo: string) => {
    setFormData(prev => ({ ...prev, tipoAplicacion: tipo }));
    
    // Si ya hay labor e insumo seleccionados, obtener dosis sugerida
    if (formData.laborId && formData.insumoId) {
      obtenerDosisSugerida();
    }
  };

  // Guardar aplicación
  const saveAplicacion = async () => {
    try {
      setLoading(true);
      
      if (!formData.laborId || !formData.insumoId || !formData.tipoAplicacion) {
        alert('Por favor complete todos los campos obligatorios');
        return;
      }
      
      if (formData.cantidadTotalAplicar <= 0) {
        alert('La cantidad total a aplicar debe ser mayor a cero');
        return;
      }
      
      // Validar stock
      const insumo = insumos.find(i => i.id === formData.insumoId);
      if (insumo && insumo.stockActual < formData.cantidadTotalAplicar) {
        alert(`Stock insuficiente. Stock actual: ${insumo.stockActual}, Cantidad requerida: ${formData.cantidadTotalAplicar}`);
        return;
      }
      
      const resultado = await aplicacionesAgroquimicasService.crear(formData);
      
      alert(resultado?.message || 'Aplicación creada exitosamente');
      loadData();
      resetForm();
      
    } catch (error: any) {
      console.error('Error guardando aplicación:', error);
      alert(error.response?.data?.message || 'Error al guardar la aplicación');
    } finally {
      setLoading(false);
    }
  };

  // Eliminar aplicación
  const deleteAplicacion = async (id: number) => {
    if (!window.confirm('¿Está seguro de que desea eliminar esta aplicación?')) return;
    
    try {
      setLoading(true);
      await aplicacionesAgroquimicasService.eliminar(id);
      alert('Aplicación eliminada exitosamente');
      loadData();
    } catch (error) {
      console.error('Error eliminando aplicación:', error);
      alert('Error al eliminar la aplicación');
    } finally {
      setLoading(false);
    }
  };

  // Resetear formulario
  const resetForm = () => {
    setFormData({
      laborId: 0,
      insumoId: 0,
      tipoAplicacion: 'FOLIAR',
      cantidadTotalAplicar: 0,
      dosisAplicadaPorHa: 0,
      superficieAplicadaHa: 0,
      unidadMedida: '',
      observaciones: '',
      fechaAplicacion: new Date().toISOString().split('T')[0]
    });
    setDosisSugerida(null);
    setShowForm(false);
  };

  // Filtrar aplicaciones
  const filteredAplicaciones = aplicaciones.filter(aplicacion => {
    const matchesSearch = aplicacion.insumoNombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         aplicacion.laborNombre.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesInsumo = filterInsumo === 'todos' || aplicacion.insumoId.toString() === filterInsumo;
    
    return matchesSearch && matchesInsumo;
  });

  // Obtener estadísticas
  const estadisticas = {
    totalAplicaciones: aplicaciones.length,
    insumosUtilizados: new Set(aplicaciones.map(a => a.insumoId)).size,
    totalCantidadAplicada: aplicaciones.reduce((sum, a) => sum + a.cantidadTotalAplicar, 0)
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🧪 Gestión de Aplicaciones de Agroquímicos</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Registre y gestione las aplicaciones de agroquímicos con cálculo automático de dosis
        </p>
      </div>

      {/* Estadísticas */}
      <div style={{ 
        background: '#f3f4f6', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #e5e7eb'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>📊 Resumen</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>
              {estadisticas.totalAplicaciones}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Total Aplicaciones</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#8b5cf6' }}>
              {estadisticas.insumosUtilizados}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Insumos Utilizados</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f59e0b' }}>
              {estadisticas.totalCantidadAplicada.toFixed(2)}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Cantidad Total Aplicada</div>
          </div>
        </div>
      </div>

      {/* Botón para crear nueva aplicación */}
      <div style={{ marginBottom: '20px' }}>
        <PermissionGate permission="canCreateInsumos">
          <button
            onClick={() => setShowForm(true)}
            style={{
              background: '#10b981',
              color: 'white',
              border: 'none',
              padding: '12px 24px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            ➕ Nueva Aplicación
          </button>
        </PermissionGate>
      </div>

      {/* Formulario de creación */}
      {showForm && (
        <div style={{ 
          background: 'white', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '2px solid #10b981'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>
            📝 Nueva Aplicación de Agroquímico
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
            {/* Labor */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Labor (Tarea) *
              </label>
              <select
                value={formData.laborId}
                onChange={(e) => handleLaborChange(Number(e.target.value))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value={0}>Seleccionar labor</option>
                {labores.map(labor => (
                  <option key={labor.id} value={labor.id}>
                    {labor.tipoLabor} - {labor.loteNombre} ({labor.loteSuperficieHa} ha)
                  </option>
                ))}
              </select>
            </div>

            {/* Insumo */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Insumo (Agroquímico) *
              </label>
              <select
                value={formData.insumoId}
                onChange={(e) => handleInsumoChange(Number(e.target.value))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value={0}>Seleccionar insumo</option>
                {insumos.map(insumo => (
                  <option key={insumo.id} value={insumo.id}>
                    {insumo.nombre} (Stock: {insumo.stockActual} {insumo.unidadMedida})
                  </option>
                ))}
              </select>
            </div>

            {/* Tipo de aplicación */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Tipo de Aplicación *
              </label>
              <select
                value={formData.tipoAplicacion}
                onChange={(e) => handleTipoAplicacionChange(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                {tiposAplicacion.map(tipo => (
                  <option key={tipo.value} value={tipo.value}>{tipo.label}</option>
                ))}
              </select>
            </div>

            {/* Superficie */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Superficie (hectáreas)
              </label>
              <input
                type="number"
                step="0.01"
                value={formData.superficieAplicadaHa}
                onChange={(e) => {
                  const superficie = Number(e.target.value);
                  setFormData(prev => ({ ...prev, superficieAplicadaHa: superficie }));
                  calcularCantidadTotal();
                }}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="0.00"
              />
            </div>

            {/* Dosis sugerida */}
            {dosisSugerida && (
              <div style={{ 
                gridColumn: '1 / -1',
                padding: '10px',
                background: '#d1fae5',
                borderRadius: '5px',
                border: '1px solid #10b981'
              }}>
                <strong>💡 Dosis Sugerida:</strong> {dosisSugerida.dosisPorHa} {dosisSugerida.unidadMedida}/ha
                {dosisSugerida.descripcion && <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '5px' }}>{dosisSugerida.descripcion}</div>}
              </div>
            )}

            {/* Dosis aplicada por ha */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Dosis Aplicada (por ha)
              </label>
              <input
                type="number"
                step="0.01"
                value={formData.dosisAplicadaPorHa}
                onChange={(e) => {
                  const dosis = Number(e.target.value);
                  setFormData(prev => ({ ...prev, dosisAplicadaPorHa: dosis }));
                  calcularCantidadTotal();
                }}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="0.00"
              />
            </div>

            {/* Cantidad total */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Cantidad Total a Aplicar *
              </label>
              <input
                type="number"
                step="0.01"
                value={formData.cantidadTotalAplicar}
                onChange={(e) => setFormData(prev => ({ ...prev, cantidadTotalAplicar: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px',
                  background: formData.cantidadTotalAplicar > 0 ? '#d1fae5' : 'white'
                }}
                placeholder="0.00"
                disabled={!formData.unidadMedida}
              />
              {formData.unidadMedida && (
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '5px' }}>
                  {formData.unidadMedida}
                </div>
              )}
            </div>

            {/* Fecha de aplicación */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Fecha de Aplicación
              </label>
              <input
                type="date"
                value={formData.fechaAplicacion}
                onChange={(e) => setFormData(prev => ({ ...prev, fechaAplicacion: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>
          </div>

          {/* Observaciones */}
          <div style={{ marginTop: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Observaciones
            </label>
            <textarea
              value={formData.observaciones}
              onChange={(e) => setFormData(prev => ({ ...prev, observaciones: e.target.value }))}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px',
                minHeight: '80px',
                resize: 'vertical'
              }}
              placeholder="Observaciones sobre la aplicación..."
            />
          </div>

          {/* Botones */}
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              onClick={saveAplicacion}
              disabled={loading}
              style={{
                background: '#10b981',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                opacity: loading ? 0.6 : 1
              }}
            >
              {loading ? '⏳ Guardando...' : '💾 Guardar Aplicación'}
            </button>
            <button
              onClick={resetForm}
              disabled={loading}
              style={{
                background: '#6b7280',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Filtros */}
      <div style={{ 
        background: 'white', 
        padding: '15px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #e5e7eb'
      }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Buscar:</label>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Buscar por insumo o labor..."
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Filtrar por insumo:</label>
            <select
              value={filterInsumo}
              onChange={(e) => setFilterInsumo(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            >
              <option value="todos">Todos los insumos</option>
              {insumos.map(insumo => (
                <option key={insumo.id} value={insumo.id.toString()}>{insumo.nombre}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Lista de aplicaciones */}
      <div style={{ 
        background: 'white', 
        padding: '20px', 
        borderRadius: '10px',
        border: '1px solid #e5e7eb'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>📋 Aplicaciones Registradas</h3>
        
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <div style={{ fontSize: '24px' }}>⏳ Cargando...</div>
          </div>
        ) : filteredAplicaciones.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
            <div style={{ fontSize: '48px', marginBottom: '10px' }}>📭</div>
            <div>No hay aplicaciones registradas</div>
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '15px' }}>
            {filteredAplicaciones.map(aplicacion => (
              <div key={aplicacion.id} style={{ 
                padding: '15px', 
                background: '#f9fafb', 
                borderRadius: '8px',
                border: '1px solid #e5e7eb'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '10px' }}>
                  <div>
                    <h4 style={{ margin: '0 0 5px 0', color: '#374151' }}>
                      🧪 {aplicacion.insumoNombre}
                    </h4>
                    <div style={{ fontSize: '14px', color: '#6b7280' }}>
                      📅 {new Date(aplicacion.fechaAplicacion).toLocaleDateString()} | 
                      🔧 {aplicacion.tipoAplicacion} | 
                      📍 {aplicacion.laborNombre}
                    </div>
                  </div>
                  <PermissionGate permission="canDeleteInsumos">
                    <button
                      onClick={() => deleteAplicacion(aplicacion.id!)}
                      style={{
                        background: '#ef4444',
                        color: 'white',
                        border: 'none',
                        padding: '8px 16px',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '13px',
                        fontWeight: 'bold'
                      }}
                    >
                      🗑️ Eliminar
                    </button>
                  </PermissionGate>
                </div>
                
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '10px', marginTop: '10px' }}>
                  <div>
                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Cantidad Total</div>
                    <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#10b981' }}>
                      {aplicacion.cantidadTotalAplicar} {aplicacion.unidadMedida}
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Dosis por ha</div>
                    <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                      {aplicacion.dosisAplicadaPorHa} {aplicacion.unidadMedida}/ha
                    </div>
                  </div>
                  <div>
                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Superficie</div>
                    <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                      {aplicacion.superficieAplicadaHa} ha
                    </div>
                  </div>
                </div>
                
                {aplicacion.observaciones && (
                  <div style={{ marginTop: '10px', padding: '10px', background: 'white', borderRadius: '5px' }}>
                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Observaciones:</div>
                    <div style={{ fontSize: '14px', color: '#374151' }}>{aplicacion.observaciones}</div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AplicacionesAgroquimicas;

