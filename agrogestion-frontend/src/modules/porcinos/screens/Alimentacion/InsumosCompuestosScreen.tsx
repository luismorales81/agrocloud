/**
 * Pantalla para gestionar Insumos Compuestos (Recetas/Formulas)
 * COMPARTIDA entre módulos de Cultivos y Porcinos
 * 
 * Permite crear recetas/formulas de insumos compuestos que pueden incluir:
 * - Insumos simples (fertilizantes, agroquímicos, etc.)
 * - Granos propios (cultivos)
 * - Otros insumos compuestos (recetas dentro de recetas)
 */
import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate } from 'react-router-dom';
import { insumosCompuestosService } from '../../../../services/insumosCompuestosService';
import { cultivosService } from '../../../../services/apiServices';
import { insumosService } from '../../../../services/apiServices';
import api from '../../../../services/api';
import { API_ENDPOINTS } from '../../../../services/apiEndpoints';
import type {
  InsumoCompuesto,
  ComponenteInsumoCompuesto,
  RecetaAlimentacionPorEtapa,
  EtapaAlimentacion,
} from '../../types';
import BarraSubnavegacionAlimentacionPorcinos from '../../components/BarraSubnavegacionAlimentacionPorcinos';

const InsumosCompuestosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [insumosCompuestos, setInsumosCompuestos] = useState<InsumoCompuesto[]>([]);
  const [insumos, setInsumos] = useState<any[]>([]);
  const [cultivos, setCultivos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [editando, setEditando] = useState<InsumoCompuesto | null>(null);
  const [formData, setFormData] = useState<Partial<InsumoCompuesto>>({
    nombre: '',
    descripcion: '',
    tipo: 'RACION',
    unidadMedida: 'kg',
    rendimiento: 1.0,
    stockMinimo: 0,
  });
  const [componentes, setComponentes] = useState<ComponenteInsumoCompuesto[]>([]);
  const [nuevoComponente, setNuevoComponente] = useState<Partial<ComponenteInsumoCompuesto>>({
    tipoComponente: 'INSUMO',
    porcentaje: undefined,
    cantidadFija: undefined,
    unidadMedida: 'kg',
    ordenMezcla: 0,
  });

  // Estados para modal de asociar receta a etapa (solo Porcinos)
  const [mostrarModalEtapa, setMostrarModalEtapa] = useState(false);
  const [recetaParaAsociar, setRecetaParaAsociar] = useState<InsumoCompuesto | null>(null);
  const [etapaAsociacion, setEtapaAsociacion] = useState<EtapaAlimentacion>('F1');
  const [cantidadDiaria, setCantidadDiaria] = useState<number | string>(0);
  const [asociando, setAsociando] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [insumosData, cultivosData, insumosCompData] = await Promise.all([
        insumosService.listar(),
        cultivosService.listar(),
        insumosCompuestosService.listar(),
      ]);
      setInsumos(insumosData || []);
      setCultivos(cultivosData || []);
      setInsumosCompuestos(insumosCompData);
    } catch (error) {
      console.error('Error cargando datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const abrirFormulario = (insumo?: InsumoCompuesto) => {
    if (insumo) {
      setEditando(insumo);
      setFormData(insumo);
      setComponentes(insumo.componentes || []);
    } else {
      setEditando(null);
      setFormData({
        nombre: '',
        descripcion: '',
        tipo: 'RACION',
        unidadMedida: 'kg',
        rendimiento: 1.0,
        stockMinimo: 0,
      });
      setComponentes([]);
    }
    setMostrarFormulario(true);
  };

  const agregarComponente = () => {
    if (!nuevoComponente.tipoComponente) return;

    // Validar que tenga un origen
    if (nuevoComponente.tipoComponente === 'INSUMO' && !nuevoComponente.insumoId) {
      alert('Debe seleccionar un insumo');
      return;
    }
    if (nuevoComponente.tipoComponente === 'GRANO_PROPIO' && !nuevoComponente.cultivoId) {
      alert('Debe seleccionar un cultivo');
      return;
    }
    if (nuevoComponente.tipoComponente === 'INSUMO_COMPUESTO' && !nuevoComponente.insumoCompuestoPadreId) {
      alert('Debe seleccionar un insumo compuesto');
      return;
    }

    // Validar porcentaje o cantidad fija
    if (!nuevoComponente.porcentaje && !nuevoComponente.cantidadFija) {
      alert('Debe especificar porcentaje o cantidad fija');
      return;
    }

    const componente: ComponenteInsumoCompuesto = {
      ...nuevoComponente,
      ordenMezcla: componentes.length + 1,
    } as ComponenteInsumoCompuesto;

    setComponentes([...componentes, componente]);
    setNuevoComponente({
      tipoComponente: 'INSUMO',
      porcentaje: undefined,
      cantidadFija: undefined,
      unidadMedida: 'kg',
      ordenMezcla: componentes.length + 1,
    });
  };

  const eliminarComponente = (index: number) => {
    setComponentes(componentes.filter((_, i) => i !== index));
  };

  const calcularSumaPorcentajes = () => {
    return componentes
      .filter(c => c.porcentaje)
      .reduce((sum, c) => sum + (c.porcentaje || 0), 0);
  };

  const handleGuardar = async () => {
    if (!formData.nombre) {
      alert('El nombre es obligatorio');
      return;
    }

    if (componentes.length === 0) {
      alert('Debe agregar al menos un componente');
      return;
    }

    // Validar suma de porcentajes (si se usan porcentajes)
    const sumaPorcentajes = calcularSumaPorcentajes();
    const usaPorcentajes = componentes.some(c => c.porcentaje);
    if (usaPorcentajes && (sumaPorcentajes < 99.9 || sumaPorcentajes > 100.1)) {
      alert(`La suma de porcentajes debe ser aproximadamente 100%. Actual: ${sumaPorcentajes.toFixed(2)}%`);
      return;
    }

    setLoading(true);
    try {
      const insumoCompuesto = {
        ...formData,
        componentes: componentes.map(c => ({
          tipoComponente: c.tipoComponente,
          insumoId: c.insumoId,
          cultivoId: c.cultivoId,
          insumoCompuestoPadreId: c.insumoCompuestoPadreId,
          porcentaje: c.porcentaje,
          cantidadFija: c.cantidadFija,
          unidadMedida: c.unidadMedida,
          ordenMezcla: c.ordenMezcla,
          observaciones: c.observaciones,
        })),
      };

      await insumosCompuestosService.guardar(insumoCompuesto);
      await cargarDatos();
      setMostrarFormulario(false);
      alert('Insumo compuesto guardado correctamente');
    } catch (error: any) {
      console.error('Error:', error);
      alert(error.response?.data?.mensaje || 'Error al guardar el insumo compuesto');
    } finally {
      setLoading(false);
    }
  };

  const handleRecalcularCosto = async (id: number) => {
    try {
      await insumosCompuestosService.recalcularCosto(id);
      await cargarDatos();
      alert('Costo recalculado correctamente');
    } catch (error) {
      console.error('Error:', error);
      alert('Error al recalcular el costo');
    }
  };

  // Funciones para asociar receta a etapa (solo Porcinos)
  const abrirModalAsociarEtapa = (receta: InsumoCompuesto) => {
    if (receta.tipo !== 'RACION') {
      alert('Solo se pueden asociar recetas tipo RACION a etapas de alimentación');
      return;
    }
    setRecetaParaAsociar(receta);
    setEtapaAsociacion('F1');
    setCantidadDiaria(0);
    setMostrarModalEtapa(true);
  };

  const handleAsociarEtapa = async () => {
    const cantidadNumero = typeof cantidadDiaria === 'string' ? parseFloat(cantidadDiaria) : cantidadDiaria;
    if (!recetaParaAsociar?.id || !cantidadNumero || cantidadNumero <= 0) {
      alert('Debe completar todos los campos con valores válidos');
      return;
    }

    setAsociando(true);
    try {
      await insumosCompuestosService.asociarRecetaAEtapa(
        recetaParaAsociar.id,
        etapaAsociacion,
        cantidadNumero
      );
      alert('Receta asociada a etapa correctamente');
      setMostrarModalEtapa(false);
      setRecetaParaAsociar(null);
      setEtapaAsociacion('F1');
      setCantidadDiaria(0);
      // Opcional: recargar datos para mostrar asociaciones si están disponibles
      await cargarDatos();
    } catch (error: any) {
      console.error('Error:', error);
      alert(error.response?.data?.mensaje || 'Error al asociar receta a etapa');
    } finally {
      setAsociando(false);
    }
  };

  if (loading && insumosCompuestos.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando insumos compuestos...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
      <BarraSubnavegacionAlimentacionPorcinos />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Utensils" size={32} style={{ marginRight: '0.5rem' }} /> Insumos Compuestos (Recetas de Alimento)
        </h1>
        <button
          onClick={() => abrirFormulario()}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            fontSize: '1rem',
            fontWeight: '500',
          }}
        >
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Nueva Receta
        </button>
      </div>

      {/* Formulario de creación/edición */}
      {mostrarFormulario && (
        <div style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '0.5rem',
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
          marginBottom: '2rem',
        }}>
          <h2 style={{ marginTop: 0, marginBottom: '1.5rem' }}>
            {editando ? <><Icon name="Pencil" size={18} style={{ marginRight: '0.5rem' }} /> Editar</> : <><Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Nueva</>} Insumo Compuesto
          </h2>

          {/* Información básica */}
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', marginBottom: '2rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Nombre *</label>
              <input
                type="text"
                value={formData.nombre || ''}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
                placeholder="Ej: Ración Gestación"
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Tipo *</label>
              <select
                value={formData.tipo || 'RACION'}
                onChange={(e) => setFormData({ ...formData, tipo: e.target.value as any })}
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
              >
                <option value="RACION">Ración</option>
                <option value="NUCLEO">Núcleo</option>
                <option value="MEZCLA">Mezcla</option>
                <option value="PREMEZCLA">Premezcla</option>
                <option value="OTRO">Otro</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Unidad de Medida</label>
              <input
                type="text"
                value={formData.unidadMedida || 'kg'}
                onChange={(e) => setFormData({ ...formData, unidadMedida: e.target.value })}
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Rendimiento</label>
              <input
                type="number"
                step="0.0001"
                min="0"
                max="1"
                value={formData.rendimiento || 1.0}
                onChange={(e) => setFormData({ ...formData, rendimiento: parseFloat(e.target.value) || 1.0 })}
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
                title="Rendimiento de producción (ej: 0.95 = 95% por mermas)"
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Stock Mínimo</label>
              <input
                type="number"
                step="0.01"
                value={formData.stockMinimo || 0}
                onChange={(e) => setFormData({ ...formData, stockMinimo: parseFloat(e.target.value) || 0 })}
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
              />
            </div>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Descripción</label>
            <textarea
              value={formData.descripcion || ''}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', minHeight: '80px' }}
              placeholder="Descripción de la receta..."
            />
          </div>

          {/* Componentes */}
          <div style={{ marginTop: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
            <h3 style={{ marginTop: 0, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Clipboard" size={20} /> Componentes (Ingredientes)</h3>

            {/* Formulario para agregar componente */}
            <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', marginBottom: '1rem', padding: '1rem', backgroundColor: 'white', borderRadius: '0.375rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}>Tipo</label>
                <select
                  value={nuevoComponente.tipoComponente || 'INSUMO'}
                  onChange={(e) => setNuevoComponente({ ...nuevoComponente, tipoComponente: e.target.value as any, insumoId: undefined, cultivoId: undefined, insumoCompuestoPadreId: undefined })}
                  style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                >
                  <option value="INSUMO">Insumo</option>
                  <option value="GRANO_PROPIO">Grano Propio</option>
                  <option value="INSUMO_COMPUESTO">Insumo Compuesto</option>
                </select>
              </div>

              {nuevoComponente.tipoComponente === 'INSUMO' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}>Insumo</label>
                  <select
                    value={nuevoComponente.insumoId || ''}
                    onChange={(e) => setNuevoComponente({ ...nuevoComponente, insumoId: parseInt(e.target.value) || undefined })}
                    style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                  >
                    <option value="">Seleccionar insumo...</option>
                    {insumos.filter(i => i.activo).map(insumo => (
                      <option key={insumo.id} value={insumo.id}>
                        {insumo.nombre} ({insumo.unidad_medida})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {nuevoComponente.tipoComponente === 'GRANO_PROPIO' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}>Cultivo</label>
                  <select
                    value={nuevoComponente.cultivoId || ''}
                    onChange={(e) => setNuevoComponente({ ...nuevoComponente, cultivoId: parseInt(e.target.value) || undefined })}
                    style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                  >
                    <option value="">Seleccionar cultivo...</option>
                    {cultivos.map(cultivo => (
                      <option key={cultivo.id} value={cultivo.id}>
                        {cultivo.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {nuevoComponente.tipoComponente === 'INSUMO_COMPUESTO' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}>Insumo Compuesto</label>
                  <select
                    value={nuevoComponente.insumoCompuestoPadreId || ''}
                    onChange={(e) => setNuevoComponente({ ...nuevoComponente, insumoCompuestoPadreId: parseInt(e.target.value) || undefined })}
                    style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                  >
                    <option value="">Seleccionar insumo compuesto...</option>
                    {insumosCompuestos.filter(ic => ic.id !== editando?.id && ic.activo).map(ic => (
                      <option key={ic.id} value={ic.id}>
                        {ic.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}>Porcentaje (%)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  max="100"
                  value={nuevoComponente.porcentaje || ''}
                  onChange={(e) => setNuevoComponente({ ...nuevoComponente, porcentaje: parseFloat(e.target.value) || undefined, cantidadFija: undefined })}
                  style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                  placeholder="Ej: 60"
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}>O Cantidad Fija</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={nuevoComponente.cantidadFija || ''}
                  onChange={(e) => setNuevoComponente({ ...nuevoComponente, cantidadFija: parseFloat(e.target.value) || undefined, porcentaje: undefined })}
                  style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                  placeholder="Ej: 2.5 kg"
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'flex-end' }}>
                <button
                  type="button"
                  onClick={agregarComponente}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontSize: '0.875rem',
                    fontWeight: '500',
                  }}
                >
                  <Icon name="Plus" size={16} style={{ marginRight: '0.5rem' }} /> Agregar
                </button>
              </div>
            </div>

            {/* Lista de componentes agregados */}
            {componentes.length > 0 && (
              <div style={{ marginTop: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <strong>Componentes agregados ({componentes.length})</strong>
                  {componentes.some(c => c.porcentaje) && (
                    <span style={{ fontSize: '0.875rem', color: calcularSumaPorcentajes() >= 99.9 && calcularSumaPorcentajes() <= 100.1 ? '#10b981' : '#ef4444' }}>
                      Suma: {calcularSumaPorcentajes().toFixed(2)}%
                    </span>
                  )}
                </div>
                <div style={{ display: 'grid', gap: '0.5rem' }}>
                  {componentes.map((componente, index) => {
                    // Obtener nombre del componente
                    let nombre = 'Sin nombre';
                    if (componente.tipoComponente === 'INSUMO' && componente.insumoId) {
                      const insumo = insumos.find(i => i.id === componente.insumoId);
                      nombre = insumo?.nombre || `Insumo #${componente.insumoId}`;
                    } else if (componente.tipoComponente === 'GRANO_PROPIO' && componente.cultivoId) {
                      const cultivo = cultivos.find(c => c.id === componente.cultivoId);
                      nombre = cultivo?.nombre || `Cultivo #${componente.cultivoId}`;
                    } else if (componente.tipoComponente === 'INSUMO_COMPUESTO' && componente.insumoCompuestoPadreId) {
                      const icPadre = insumosCompuestos.find(icp => icp.id === componente.insumoCompuestoPadreId);
                      nombre = icPadre?.nombre || `Insumo Compuesto #${componente.insumoCompuestoPadreId}`;
                    }
                    
                    return (
                      <div
                        key={index}
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '0.75rem',
                          backgroundColor: 'white',
                          borderRadius: '0.375rem',
                          border: '1px solid #e5e7eb',
                        }}
                      >
                        <div>
                          <strong>{nombre || 'Sin nombre'}</strong>
                          <span style={{ marginLeft: '0.5rem', fontSize: '0.875rem', color: '#6b7280' }}>
                            ({componente.tipoComponente})
                          </span>
                          {componente.porcentaje && (
                            <span style={{ marginLeft: '0.5rem', color: '#3b82f6' }}>
                              {componente.porcentaje}%
                            </span>
                          )}
                          {componente.cantidadFija && (
                            <span style={{ marginLeft: '0.5rem', color: '#3b82f6' }}>
                              {componente.cantidadFija} {componente.unidadMedida}
                            </span>
                          )}
                        </div>
                        <button
                          type="button"
                          onClick={() => eliminarComponente(index)}
                          style={{
                            padding: '0.25rem 0.75rem',
                            backgroundColor: '#ef4444',
                            color: 'white',
                            border: 'none',
                            borderRadius: '0.375rem',
                            cursor: 'pointer',
                            fontSize: '0.875rem',
                          }}
                        >
                          <Icon name="Trash2" size={16} style={{ marginRight: '0.5rem' }} /> Eliminar
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>

          {/* Botones de acción */}
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '2rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => setMostrarFormulario(false)}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontSize: '1rem',
                fontWeight: '500',
              }}
            >
              ✕ Cancelar
            </button>
            <button
              type="button"
              onClick={handleGuardar}
              disabled={loading}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: loading ? '#9ca3af' : '#10b981',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '1rem',
                fontWeight: '500',
              }}
            >
              {loading ? <><SemanticIcon semanticName="pending" size={18} style={{ marginRight: '0.5rem' }} /> Guardando...</> : <><Icon name="Save" size={18} style={{ marginRight: '0.5rem' }} /> Guardar</>}
            </button>
          </div>
        </div>
      )}

      {/* Lista de insumos compuestos */}
      {!mostrarFormulario && (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {insumosCompuestos.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem', backgroundColor: 'white', borderRadius: '0.5rem' }}>
              <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📦</div>
              <p style={{ color: '#6b7280', marginBottom: '1rem' }}>No hay insumos compuestos registrados</p>
              <button
                onClick={() => abrirFormulario()}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#10b981',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                }}
              >
                <Icon name="Plus" size={20} style={{ marginRight: '0.5rem' }} /> Crear Primera Receta
              </button>
            </div>
          ) : (
            insumosCompuestos.map(ic => (
              <div
                key={ic.id}
                style={{
                  backgroundColor: 'white',
                  padding: '1.5rem',
                  borderRadius: '0.5rem',
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
                  <div style={{ flex: 1 }}>
                    <h3 style={{ margin: 0, marginBottom: '0.5rem', fontSize: '1.25rem', fontWeight: 'bold' }}>
                      {ic.nombre}
                    </h3>
                    <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '0.5rem' }}>
                      <span style={{ padding: '0.25rem 0.75rem', backgroundColor: '#e0e7ff', color: '#3730a3', borderRadius: '0.25rem', fontSize: '0.875rem' }}>
                        {ic.tipo}
                      </span>
                      {ic.costoUnitarioCalculado && (
                        <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          <Icon name="DollarSign" size={16} style={{ marginRight: '0.25rem' }} /> Costo: ${ic.costoUnitarioCalculado.toFixed(2)} / {ic.unidadMedida}
                        </span>
                      )}
                      <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                        📦 Stock: {ic.stockActual || 0} {ic.unidadMedida}
                      </span>
                    </div>
                    {ic.descripcion && (
                      <p style={{ margin: 0, color: '#6b7280', fontSize: '0.875rem' }}>{ic.descripcion}</p>
                    )}
                    {ic.componentes && ic.componentes.length > 0 && (
                      <div style={{ marginTop: '1rem' }}>
                        <strong style={{ fontSize: '0.875rem' }}>Componentes ({ic.componentes.length}):</strong>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.5rem' }}>
                          {ic.componentes.map((comp, idx) => {
                            // Usar nombreComponente si está disponible (viene del backend), sino buscar en arrays locales
                            let nombre = comp.nombreComponente || 'Componente';
                            if (!comp.nombreComponente) {
                              if (comp.tipoComponente === 'INSUMO' && comp.insumoId) {
                                const insumo = insumos.find(i => i.id === comp.insumoId);
                                nombre = insumo?.nombre || `Insumo #${comp.insumoId}`;
                              } else if (comp.tipoComponente === 'GRANO_PROPIO' && comp.cultivoId) {
                                const cultivo = cultivos.find(c => c.id === comp.cultivoId);
                                nombre = cultivo?.nombre || `Cultivo #${comp.cultivoId}`;
                              } else if (comp.tipoComponente === 'INSUMO_COMPUESTO' && comp.insumoCompuestoPadreId) {
                                const icPadre = insumosCompuestos.find(icp => icp.id === comp.insumoCompuestoPadreId);
                                nombre = icPadre?.nombre || `Insumo Compuesto #${comp.insumoCompuestoPadreId}`;
                              }
                            }
                            
                            return (
                              <span
                                key={idx}
                                style={{
                                  padding: '0.25rem 0.5rem',
                                  backgroundColor: '#f3f4f6',
                                  borderRadius: '0.25rem',
                                  fontSize: '0.75rem',
                                }}
                              >
                                {nombre}
                                {comp.porcentaje && ` (${comp.porcentaje}%)`}
                                {comp.cantidadFija && ` (${comp.cantidadFija} ${comp.unidadMedida})`}
                              </span>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    {ic.tipo === 'RACION' && (
                      <button
                        onClick={() => abrirModalAsociarEtapa(ic)}
                        style={{
                          padding: '0.5rem 1rem',
                          backgroundColor: '#8b5cf6',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem',
                        }}
                        title="Asociar esta receta a una etapa de alimentación (F1, F2, GESTACION, etc.)"
                      >
                        <Icon name="Share2" size={16} style={{ marginRight: '0.5rem' }} /> Asociar a Etapa
                      </button>
                    )}
                    <button
                      onClick={() => handleRecalcularCosto(ic.id!)}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: '#3b82f6',
                        color: 'white',
                        border: 'none',
                        borderRadius: '0.375rem',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                      }}
                      title="Recalcular costo basado en componentes"
                    >
                      <Icon name="RefreshCw" size={16} style={{ marginRight: '0.5rem' }} /> Recalcular
                    </button>
                    <button
                      onClick={() => abrirFormulario(ic)}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: '#f59e0b',
                        color: 'white',
                        border: 'none',
                        borderRadius: '0.375rem',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                      }}
                    >
                      <Icon name="Pencil" size={16} style={{ marginRight: '0.5rem' }} /> Editar
                    </button>
                    <button
                      onClick={async () => {
                        if (confirm('¿Está seguro de eliminar este insumo compuesto?')) {
                          try {
                            await insumosCompuestosService.eliminar(ic.id!);
                            await cargarDatos();
                            alert('Insumo compuesto eliminado');
                          } catch (error) {
                            alert('Error al eliminar');
                          }
                        }
                      }}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: '#ef4444',
                        color: 'white',
                        border: 'none',
                        borderRadius: '0.375rem',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                      }}
                    >
                      <Icon name="Trash2" size={16} style={{ marginRight: '0.5rem' }} /> Eliminar
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Modal para asociar receta a etapa (solo Porcinos) */}
      {mostrarModalEtapa && recetaParaAsociar && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={(e) => {
            if (e.target === e.currentTarget) {
              setMostrarModalEtapa(false);
            }
          }}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '2rem',
              borderRadius: '0.5rem',
              maxWidth: '500px',
              width: '90%',
              maxHeight: '90vh',
              overflowY: 'auto',
              boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
                <Icon name="Link" size={24} style={{ marginRight: '0.5rem', verticalAlign: 'middle' }} />
                Asociar Receta a Etapa
              </h2>
              <button
                onClick={() => setMostrarModalEtapa(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '1.5rem',
                  cursor: 'pointer',
                  color: '#6b7280',
                  padding: '0.25rem 0.5rem',
                }}
              >
                ×
              </button>
            </div>

            <div style={{ marginBottom: '1.5rem', padding: '1rem', backgroundColor: '#f3f4f6', borderRadius: '0.375rem' }}>
              <p style={{ margin: 0, fontSize: '0.875rem', color: '#6b7280' }}>
                <strong>Receta:</strong> <span style={{ color: '#1f2937' }}>{recetaParaAsociar.nombre}</span>
              </p>
              {recetaParaAsociar.tipo && (
                <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.875rem', color: '#6b7280' }}>
                  <strong>Tipo:</strong> <span style={{ color: '#1f2937' }}>{recetaParaAsociar.tipo}</span>
                </p>
              )}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.875rem', color: '#374151' }}>
                  Etapa de Alimentación *
                </label>
                <select
                  value={etapaAsociacion}
                  onChange={(e) => setEtapaAsociacion(e.target.value as EtapaAlimentacion)}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    backgroundColor: 'white',
                  }}
                >
                  <option value="GESTACION">Gestación (Madres gestantes)</option>
                  <option value="LACTANCIA">Lactancia (Madres en lactancia)</option>
                  <option value="F1">F1 (Recría F1 - 0-35 días)</option>
                  <option value="F2">F2 (Recría F2 - 36-70 días)</option>
                  <option value="F3">F3 (Recría F3 - 71-105 días)</option>
                  <option value="F4">F4 (Recría F4 - 106-140 días)</option>
                  <option value="DESARROLLO">Desarrollo (Engorde temprano - 141-180 días)</option>
                  <option value="TERMINACION">Terminación (Engorde final - {'>'}180 días)</option>
                </select>
                <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.75rem', color: '#6b7280' }}>
                  Selecciona la etapa de alimentación donde se usará esta receta
                </p>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', fontSize: '0.875rem', color: '#374151' }}>
                  Cantidad Diaria por Animal (kg) *
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  value={cantidadDiaria}
                  onChange={(e) => {
                    const valor = e.target.value;
                    // Permitir valores vacíos y decimales mientras se escribe
                    if (valor === '' || valor === '.') {
                      setCantidadDiaria(valor);
                    } else {
                      const numValor = parseFloat(valor);
                      if (!isNaN(numValor)) {
                        setCantidadDiaria(numValor);
                      }
                    }
                  }}
                  onBlur={(e) => {
                    // Al perder el foco, asegurar que sea un número válido
                    const valor = parseFloat(e.target.value);
                    if (isNaN(valor) || valor <= 0) {
                      setCantidadDiaria(0);
                    } else {
                      setCantidadDiaria(valor);
                    }
                  }}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                  }}
                  placeholder="Ej: 0.7 o 1.5"
                />
                <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.75rem', color: '#6b7280' }}>
                  Cantidad en kilogramos que consume cada animal por día en esta etapa
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '2rem', justifyContent: 'flex-end' }}>
              <button
                onClick={() => {
                  setMostrarModalEtapa(false);
                  setRecetaParaAsociar(null);
                  setEtapaAsociacion('F1');
                  setCantidadDiaria(0);
                }}
                disabled={asociando}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: '#6b7280',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: asociando ? 'not-allowed' : 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: '500',
                  opacity: asociando ? 0.6 : 1,
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleAsociarEtapa}
                disabled={asociando || (typeof cantidadDiaria === 'number' ? cantidadDiaria <= 0 : parseFloat(String(cantidadDiaria)) <= 0)}
                style={{
                  padding: '0.75rem 1.5rem',
                  backgroundColor: asociando || (typeof cantidadDiaria === 'number' ? cantidadDiaria <= 0 : parseFloat(String(cantidadDiaria)) <= 0) ? '#9ca3af' : '#8b5cf6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: asociando || (typeof cantidadDiaria === 'number' ? cantidadDiaria <= 0 : parseFloat(String(cantidadDiaria)) <= 0) ? 'not-allowed' : 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: '500',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                }}
              >
                {asociando ? (
                  <>
                    <SemanticIcon semanticName="pending" size={18} /> Asociando...
                  </>
                ) : (
                  <>
                    <Icon name="Share2" size={18} /> Asociar
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default InsumosCompuestosScreen;





