import React, { useState, useEffect } from 'react';
import api from '../services/api';

interface Lote {
  id?: number;
  nombre: string;
  superficie: number | '';
  cultivo: string;
  campo_id: number;
  estado: string;
}

interface Cultivo {
  id: number;
  nombre: string;
  rendimientoEsperado?: number;
  unidadRendimiento?: string;
  cicloDias?: number;
  variedad?: string;
}

interface Insumo {
  id: number;
  nombre: string;
  precioUnitario: number;
  unidadMedida: string;
  stockActual?: number;
}

interface MaquinariaInventario {
  id: number;
  nombre: string;
  tipo: string;
  costoPorHora: number;
}

interface InsumoUsado {
  insumoId: number;
  insumoNombre: string;
  cantidadUsada: number;
  unidadMedida: string;
  costoTotal: number;
}

interface MaquinariaAsignada {
  descripcion: string;
  proveedor?: string;
  tipoMaquinaria: string;
  costoTotal: number;
  observaciones?: string;
}

interface ManoObra {
  descripcion: string;
  cantidadPersonas: number;
  proveedor?: string;
  costoTotal: number;
  horasTrabajo?: number;
  observaciones?: string;
}

interface SiembraModalProps {
  lote: Lote;
  onClose: () => void;
  onSuccess: () => void;
}

const SiembraModalHibrido: React.FC<SiembraModalProps> = ({ lote, onClose, onSuccess }) => {
  const [cultivos, setCultivos] = useState<Cultivo[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [maquinariasInventario, setMaquinariasInventario] = useState<MaquinariaInventario[]>([]);
  const [mostrarRecursos, setMostrarRecursos] = useState(false);
  const [pestanaActiva, setPestanaActiva] = useState<'insumos' | 'maquinaria' | 'manoObra'>('insumos');
  
  const [formData, setFormData] = useState({
    cultivoId: '',
    fechaSiembra: new Date().toISOString().split('T')[0],
    observaciones: ''
  });
  
  const [insumosUsados, setInsumosUsados] = useState<InsumoUsado[]>([]);
  const [maquinarias, setMaquinarias] = useState<MaquinariaAsignada[]>([]);
  const [manoObras, setManoObras] = useState<ManoObra[]>([]);
  
  const [formInsumo, setFormInsumo] = useState({
    insumoId: '',
    cantidad: ''
  });
  
  const [tipoMaquinaria, setTipoMaquinaria] = useState<'propia' | 'alquilada'>('propia');
  const [formMaquinaria, setFormMaquinaria] = useState({
    maquinariaId: '', // Para maquinaria propia del inventario
    descripcion: '',
    proveedor: '',
    horasUso: '',
    costo: '',
    observaciones: ''
  });
  
  const [formManoObra, setFormManoObra] = useState({
    descripcion: '',
    cantidadPersonas: '',
    proveedor: '',
    costo: '',
    horas: '',
    observaciones: ''
  });
  
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    await Promise.all([
      cargarCultivos(),
      cargarInsumos(),
      cargarMaquinarias()
    ]);
  };

  const cargarCultivos = async () => {
    try {
      const response = await api.get('/api/v1/cultivos');

      if (response.status >= 200 && response.status < 300) {
        console.log('Cultivos cargados:', response.data);
        setCultivos(response.data);
      } else {
        console.error('Error al cargar cultivos, status:', response.status);
      }
    } catch (error) {
      console.error('Error cargando cultivos:', error);
    }
  };

  const cargarInsumos = async () => {
    try {
      const response = await api.get('/api/insumos');

      if (response.status >= 200 && response.status < 300) {
        setInsumos(response.data);
      }
    } catch (error) {
      console.error('Error cargando insumos:', error);
    }
  };

  const cargarMaquinarias = async () => {
    try {
      const response = await api.get('/api/maquinaria');

      if (response.status >= 200 && response.status < 300) {
        const maquinariasMapeadas = response.data.map((maq: any) => ({
          id: maq.id,
          nombre: maq.nombre,
          tipo: maq.tipo || 'No especificado',
          costoPorHora: maq.costoPorHora || 0
        }));
        setMaquinariasInventario(maquinariasMapeadas);
      }
    } catch (error) {
      console.error('Error cargando maquinarias:', error);
    }
  };

  // Agregar insumo
  const agregarInsumo = () => {
    if (!formInsumo.insumoId || !formInsumo.cantidad || parseFloat(formInsumo.cantidad) <= 0) {
      alert('Por favor complete todos los campos del insumo');
      return;
    }

    const insumo = insumos.find(i => i.id === parseInt(formInsumo.insumoId));
    if (!insumo) return;

    const cantidad = parseFloat(formInsumo.cantidad);
    
    // Calcular stock disponible considerando lo ya agregado en este modal
    const cantidadYaUsada = insumosUsados
      .filter(i => i.insumoId === insumo.id)
      .reduce((sum, i) => sum + i.cantidadUsada, 0);
    const stockDisponible = (insumo.stockActual || 0) - cantidadYaUsada;
    
    // Verificar stock disponible
    if (cantidad > stockDisponible) {
      alert(`⚠️ Stock insuficiente de ${insumo.nombre}.\nStock disponible: ${stockDisponible} ${insumo.unidadMedida}\nCantidad requerida: ${cantidad} ${insumo.unidadMedida}${cantidadYaUsada > 0 ? `\n(Ya agregado en este modal: ${cantidadYaUsada} ${insumo.unidadMedida})` : ''}`);
      return;
    }
    
    const costoTotal = cantidad * insumo.precioUnitario;

    const nuevoInsumo: InsumoUsado = {
      insumoId: insumo.id,
      insumoNombre: insumo.nombre,
      cantidadUsada: cantidad,
      unidadMedida: insumo.unidadMedida,
      costoTotal: costoTotal
    };

    setInsumosUsados([...insumosUsados, nuevoInsumo]);
    setFormInsumo({ insumoId: '', cantidad: '' });
  };

  // Agregar maquinaria
  const agregarMaquinaria = () => {
    if (tipoMaquinaria === 'propia') {
      // Maquinaria del inventario
      if (!formMaquinaria.maquinariaId || !formMaquinaria.horasUso || parseFloat(formMaquinaria.horasUso) <= 0) {
        alert('Por favor seleccione maquinaria e ingrese horas de uso');
        return;
      }

      const maquinaria = maquinariasInventario.find(m => m.id === parseInt(formMaquinaria.maquinariaId));
      if (!maquinaria) return;

      const horas = parseFloat(formMaquinaria.horasUso);
      const costo = horas * maquinaria.costoPorHora;

      const nuevaMaquinaria: MaquinariaAsignada = {
        descripcion: `${maquinaria.nombre} - ${maquinaria.tipo}`,
        proveedor: null, // Propia no tiene proveedor
        tipoMaquinaria: 'PROPIA',
        costoTotal: costo,
        observaciones: `${horas} horas × $${maquinaria.costoPorHora}/h`
      };

      setMaquinarias([...maquinarias, nuevaMaquinaria]);
      setFormMaquinaria({ maquinariaId: '', descripcion: '', proveedor: '', horasUso: '', costo: '', observaciones: '' });
    } else {
      // Maquinaria alquilada
      if (!formMaquinaria.descripcion || !formMaquinaria.costo || parseFloat(formMaquinaria.costo) <= 0 || !formMaquinaria.proveedor) {
        alert('Por favor ingrese descripción, proveedor y costo de la maquinaria alquilada');
        return;
      }

      const nuevaMaquinaria: MaquinariaAsignada = {
        descripcion: formMaquinaria.descripcion,
        proveedor: formMaquinaria.proveedor,
        tipoMaquinaria: 'ALQUILADA',
        costoTotal: parseFloat(formMaquinaria.costo),
        observaciones: formMaquinaria.observaciones || null
      };

      setMaquinarias([...maquinarias, nuevaMaquinaria]);
      setFormMaquinaria({ maquinariaId: '', descripcion: '', proveedor: '', horasUso: '', costo: '', observaciones: '' });
    }
  };

  // Agregar mano de obra
  const agregarManoObra = () => {
    if (!formManoObra.descripcion || !formManoObra.cantidadPersonas || !formManoObra.costo) {
      alert('Por favor complete descripción, cantidad de personas y costo');
      return;
    }

    const nuevaManoObra: ManoObra = {
      descripcion: formManoObra.descripcion,
      cantidadPersonas: parseInt(formManoObra.cantidadPersonas),
      proveedor: formManoObra.proveedor || null,
      costoTotal: parseFloat(formManoObra.costo),
      horasTrabajo: formManoObra.horas ? parseFloat(formManoObra.horas) : null,
      observaciones: formManoObra.observaciones || null
    };

    setManoObras([...manoObras, nuevaManoObra]);
    setFormManoObra({ descripcion: '', cantidadPersonas: '', proveedor: '', costo: '', horas: '', observaciones: '' });
  };

  // Calcular costos totales
  const calcularCostos = () => {
    const costoInsumos = insumosUsados.reduce((sum, i) => sum + i.costoTotal, 0);
    const costoMaquinaria = maquinarias.reduce((sum, m) => sum + m.costoTotal, 0);
    const costoManoObra = manoObras.reduce((sum, mo) => sum + mo.costoTotal, 0);
    const total = costoInsumos + costoMaquinaria + costoManoObra;
    
    return { costoInsumos, costoMaquinaria, costoManoObra, total };
  };

  const handleSembrar = async () => {
    if (!formData.cultivoId) {
      alert('Por favor seleccione un cultivo');
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem('token');
      
      // Preparar datos de insumos
      const insumosData = insumosUsados.map(i => ({
        insumoId: i.insumoId,
        cantidadUsada: i.cantidadUsada
      }));
      
      // Preparar datos de maquinaria
      const maquinariaData = maquinarias.map(m => ({
        descripcion: m.descripcion,
        proveedor: m.proveedor,
        tipoMaquinaria: m.tipoMaquinaria,
        costoTotal: m.costoTotal,
        observaciones: m.observaciones
      }));
      
      // Preparar datos de mano de obra
      const manoObraData = manoObras.map(mo => ({
        descripcion: mo.descripcion,
        cantidadPersonas: mo.cantidadPersonas,
        proveedor: mo.proveedor,
        costoTotal: mo.costoTotal,
        horasTrabajo: mo.horasTrabajo,
        observaciones: mo.observaciones
      }));

      const siembraData = {
        cultivoId: parseInt(formData.cultivoId),
        fechaSiembra: formData.fechaSiembra,
        observaciones: formData.observaciones,
        insumos: insumosData,
        maquinaria: maquinariaData,
        manoObra: manoObraData
      };
      
      const response = await api.post(`/api/v1/lotes/${lote.id}/sembrar`, siembraData);

      if (response.status >= 200 && response.status < 300) {
        const data = response.data;
        const costos = calcularCostos();
        const mensaje = costos.total > 0 
          ? `✅ ${data.message || 'Lote sembrado exitosamente'}\n💰 Costo total registrado: $${costos.total.toLocaleString()}`
          : `✅ ${data.message || 'Lote sembrado exitosamente'}`;
        alert(mensaje);
        onSuccess();
        onClose();
      } else {
        alert(`❌ Error: ${response.data?.message || 'No se pudo sembrar el lote'}`);
      }
    } catch (error) {
      console.error('Error al sembrar:', error);
      alert('❌ Error de conexión. Por favor, intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const costos = calcularCostos();

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 2000
    }}>
      <div style={{
        background: 'white',
        borderRadius: '12px',
        padding: '24px',
        maxWidth: mostrarRecursos ? '900px' : '600px',
        width: '95%',
        maxHeight: '95vh',
        display: 'flex',
        flexDirection: 'column',
        transition: 'max-width 0.3s ease'
      }}>
        {/* Header */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '20px',
          borderBottom: '2px solid #4CAF50',
          paddingBottom: '10px'
        }}>
          <h2 style={{ margin: 0, color: '#2e7d32', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌱 Sembrar Lote
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#666'
            }}
          >
            ✕
          </button>
        </div>

        {/* Contenido scrolleable */}
        <div style={{ 
          flex: 1,
          overflowY: 'auto',
          marginBottom: '16px',
          paddingRight: '8px'
        }}>
          {/* Info del lote */}
          <div style={{
            background: '#f0f9ff',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '20px',
            border: '1px solid #bfdbfe'
          }}>
            <div style={{ fontSize: '14px', color: '#1e40af' }}>
              <strong>Lote:</strong> {lote.nombre} | <strong>Superficie:</strong> {lote.superficie} ha
            </div>
          </div>

          {/* Datos básicos */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {/* Cultivo */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Cultivo a Sembrar *
            </label>
            <select
              value={formData.cultivoId}
              onChange={(e) => setFormData({ ...formData, cultivoId: e.target.value })}
              required
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            >
              <option value="">Seleccionar cultivo</option>
              {cultivos.map(cultivo => (
                <option key={cultivo.id} value={cultivo.id}>
                  {cultivo.nombre} {cultivo.variedad ? `- ${cultivo.variedad}` : ''}
                </option>
              ))}
            </select>
          </div>

          {/* Información del cultivo seleccionado */}
          {formData.cultivoId && (() => {
            const cultivoSeleccionado = cultivos.find(c => c.id === parseInt(formData.cultivoId));
            if (!cultivoSeleccionado) return null;
            
            return (
              <div style={{
                background: '#f0f9ff',
                border: '1px solid #0ea5e9',
                borderRadius: '8px',
                padding: '12px',
                fontSize: '13px'
              }}>
                <div style={{ fontWeight: 'bold', color: '#0c4a6e', marginBottom: '8px' }}>
                  📊 Datos del Cultivo:
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '8px', color: '#0369a1' }}>
                  {cultivoSeleccionado.cicloDias && (
                    <div><strong>Ciclo:</strong> {cultivoSeleccionado.cicloDias} días</div>
                  )}
                  {cultivoSeleccionado.rendimientoEsperado && (
                    <div><strong>Rinde esperado:</strong> {cultivoSeleccionado.rendimientoEsperado} {cultivoSeleccionado.unidadRendimiento || 'kg/ha'}</div>
                  )}
                  {lote.superficie && typeof lote.superficie === 'number' && cultivoSeleccionado.cicloDias && (
                    <div><strong>Cosecha estimada:</strong> {new Date(new Date(formData.fechaSiembra).getTime() + cultivoSeleccionado.cicloDias * 24 * 60 * 60 * 1000).toLocaleDateString('es-ES')}</div>
                  )}
                </div>
              </div>
            );
          })()}

          {/* Fecha de Siembra */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Fecha de Siembra *
            </label>
            <input
              type="date"
              value={formData.fechaSiembra}
              onChange={(e) => setFormData({ ...formData, fechaSiembra: e.target.value })}
              required
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            />
            <small style={{ color: '#6b7280', fontSize: '12px', display: 'block', marginTop: '4px' }}>
              La densidad de siembra, rendimiento esperado y ciclo de días se toman automáticamente del cultivo seleccionado
            </small>
          </div>

          {/* Observaciones */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Observaciones
            </label>
            <textarea
              value={formData.observaciones}
              onChange={(e) => setFormData({ ...formData, observaciones: e.target.value })}
              placeholder="Condiciones del suelo, clima, etc."
              rows={2}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px',
                resize: 'vertical'
              }}
            />
          </div>

          {/* Mensaje informativo */}
          {!mostrarRecursos && (
            <div style={{
              background: '#fffbeb',
              border: '1px solid #fcd34d',
              borderRadius: '8px',
              padding: '12px',
              fontSize: '13px',
              color: '#78350f'
            }}>
              💡 <strong>Opcional:</strong> Puedes agregar recursos (insumos, maquinaria, mano de obra) para registrar costos reales, 
              o hacerlo después editando la labor de siembra en la sección "Labores".
            </div>
          )}

          {/* Botón para mostrar/ocultar recursos */}
          {!mostrarRecursos && (
            <button
              onClick={() => setMostrarRecursos(true)}
              type="button"
              style={{
                padding: '12px',
                background: '#f3f4f6',
                color: '#374151',
                border: '2px dashed #d1d5db',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px'
              }}
            >
              📦 Agregar Recursos (Insumos, Maquinaria, M.Obra)
            </button>
          )}

          {/* Sección de recursos expandible */}
          {mostrarRecursos && (
            <div style={{
              border: '2px solid #e5e7eb',
              borderRadius: '8px',
              padding: '16px',
              background: '#fafafa',
              marginTop: '12px'
            }}>
              <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '16px'
              }}>
                <h3 style={{ margin: 0, color: '#374151', fontSize: '16px' }}>
                  📦 Recursos Utilizados
                </h3>
                <button
                  onClick={() => setMostrarRecursos(false)}
                  type="button"
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#6b7280',
                    cursor: 'pointer',
                    fontSize: '12px'
                  }}
                >
                  ✕ Ocultar
                </button>
              </div>

              {/* Pestañas */}
              <div style={{ display: 'flex', gap: '4px', marginBottom: '16px', borderBottom: '2px solid #e5e7eb' }}>
                <button
                  onClick={() => setPestanaActiva('insumos')}
                  type="button"
                  style={{
                    padding: '8px 16px',
                    background: pestanaActiva === 'insumos' ? '#4CAF50' : 'transparent',
                    color: pestanaActiva === 'insumos' ? 'white' : '#6b7280',
                    border: 'none',
                    borderRadius: '4px 4px 0 0',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: 'bold'
                  }}
                >
                  🌾 Insumos ({insumosUsados.length})
                </button>
                <button
                  onClick={() => setPestanaActiva('maquinaria')}
                  type="button"
                  style={{
                    padding: '8px 16px',
                    background: pestanaActiva === 'maquinaria' ? '#4CAF50' : 'transparent',
                    color: pestanaActiva === 'maquinaria' ? 'white' : '#6b7280',
                    border: 'none',
                    borderRadius: '4px 4px 0 0',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: 'bold'
                  }}
                >
                  🚜 Maquinaria ({maquinarias.length})
                </button>
                <button
                  onClick={() => setPestanaActiva('manoObra')}
                  type="button"
                  style={{
                    padding: '8px 16px',
                    background: pestanaActiva === 'manoObra' ? '#4CAF50' : 'transparent',
                    color: pestanaActiva === 'manoObra' ? 'white' : '#6b7280',
                    border: 'none',
                    borderRadius: '4px 4px 0 0',
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: 'bold'
                  }}
                >
                  👷 M.Obra ({manoObras.length})
                </button>
              </div>

              {/* Contenido de pestañas */}
              <div style={{ 
                background: 'white', 
                padding: '12px', 
                borderRadius: '8px', 
                minHeight: '200px',
                maxHeight: '400px',
                overflowY: 'auto'
              }}>
                {/* PESTAÑA INSUMOS */}
                {pestanaActiva === 'insumos' && (
                  <div>
                    <div style={{ marginBottom: '12px', display: 'grid', gridTemplateColumns: '2fr 1fr auto', gap: '8px' }}>
                      <select
                        value={formInsumo.insumoId}
                        onChange={(e) => setFormInsumo({ ...formInsumo, insumoId: e.target.value })}
                        style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                      >
                        <option value="">Seleccionar insumo...</option>
                        {insumos.map(insumo => {
                          const cantidadYaUsada = insumosUsados
                            .filter(i => i.insumoId === insumo.id)
                            .reduce((sum, i) => sum + i.cantidadUsada, 0);
                          const stockDisponible = (insumo.stockActual || 0) - cantidadYaUsada;
                          
                          return (
                            <option key={insumo.id} value={insumo.id}>
                              {insumo.nombre} - Stock: {stockDisponible} {insumo.unidadMedida}
                            </option>
                          );
                        })}
                      </select>
                      <input
                        type="number"
                        value={formInsumo.cantidad}
                        onChange={(e) => setFormInsumo({ ...formInsumo, cantidad: e.target.value })}
                        placeholder="Cantidad"
                        min="0"
                        step="0.01"
                        style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                      />
                      <button
                        onClick={agregarInsumo}
                        type="button"
                        style={{
                          padding: '8px 12px',
                          background: '#4CAF50',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '13px'
                        }}
                      >
                        + Agregar
                      </button>
                    </div>

                    {/* Lista de insumos agregados */}
                    {insumosUsados.length > 0 ? (
                      insumosUsados.map((insumo, idx) => (
                        <div key={idx} style={{
                          background: '#f9fafb',
                          padding: '10px',
                          borderRadius: '4px',
                          marginBottom: '8px',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          border: '1px solid #e5e7eb'
                        }}>
                          <div style={{ fontSize: '13px' }}>
                            <strong>{insumo.insumoNombre}</strong><br />
                            <span style={{ fontSize: '12px', color: '#6b7280' }}>
                              {insumo.cantidadUsada} {insumo.unidadMedida}
                            </span>
                            <div style={{ fontSize: '13px', color: '#059669', fontWeight: 'bold', marginTop: '2px' }}>
                              💰 ${insumo.costoTotal.toLocaleString()}
                            </div>
                          </div>
                          <button
                            onClick={() => setInsumosUsados(insumosUsados.filter((_, i) => i !== idx))}
                            type="button"
                            style={{
                              background: '#f87171',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              padding: '4px 8px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                          >
                            🗑️
                          </button>
                        </div>
                      ))
                    ) : (
                      <div style={{ textAlign: 'center', padding: '20px', color: '#6b7280', fontSize: '13px' }}>
                        No hay insumos agregados
                      </div>
                    )}
                  </div>
                )}

                {/* PESTAÑA MAQUINARIA */}
                {pestanaActiva === 'maquinaria' && (
                  <div>
                    {/* Selector Propia/Alquilada */}
                    <div style={{ marginBottom: '12px', display: 'flex', gap: '8px' }}>
                      <button
                        type="button"
                        onClick={() => setTipoMaquinaria('propia')}
                        style={{
                          flex: 1,
                          padding: '8px',
                          background: tipoMaquinaria === 'propia' ? '#10b981' : '#f3f4f6',
                          color: tipoMaquinaria === 'propia' ? 'white' : '#374151',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '13px',
                          fontWeight: 'bold'
                        }}
                      >
                        🏠 Propia
                      </button>
                      <button
                        type="button"
                        onClick={() => setTipoMaquinaria('alquilada')}
                        style={{
                          flex: 1,
                          padding: '8px',
                          background: tipoMaquinaria === 'alquilada' ? '#f59e0b' : '#f3f4f6',
                          color: tipoMaquinaria === 'alquilada' ? 'white' : '#374151',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '13px',
                          fontWeight: 'bold'
                        }}
                      >
                        🏢 Alquilada
                      </button>
                    </div>

                    {/* Formulario según tipo - PROPIA */}
                    {tipoMaquinaria === 'propia' ? (
                      <div style={{ marginBottom: '12px' }}>
                        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr auto', gap: '8px', marginBottom: '8px' }}>
                          <select
                            value={formMaquinaria.maquinariaId}
                            onChange={(e) => {
                              setFormMaquinaria({ ...formMaquinaria, maquinariaId: e.target.value, horasUso: '', costo: '' });
                            }}
                            style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                          >
                            <option value="">Seleccionar maquinaria...</option>
                            {maquinariasInventario.map(maq => (
                              <option key={maq.id} value={maq.id}>
                                {maq.nombre} - {maq.tipo} (${maq.costoPorHora}/h)
                              </option>
                            ))}
                          </select>
                          <input
                            type="number"
                            value={formMaquinaria.horasUso}
                            onChange={(e) => {
                              const horas = e.target.value;
                              const maqSeleccionada = maquinariasInventario.find(m => m.id === parseInt(formMaquinaria.maquinariaId));
                              const costoCalculado = (parseFloat(horas) || 0) * (maqSeleccionada?.costoPorHora || 0);
                              setFormMaquinaria({
                                ...formMaquinaria,
                                horasUso: horas,
                                costo: costoCalculado.toString()
                              });
                            }}
                            placeholder="Horas"
                            min="0"
                            step="0.1"
                            style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                          />
                          <button
                            onClick={agregarMaquinaria}
                            type="button"
                            style={{
                              padding: '8px 12px',
                              background: '#4CAF50',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '13px'
                            }}
                          >
                            +
                          </button>
                        </div>
                        {formMaquinaria.maquinariaId && formMaquinaria.horasUso && (
                          <div style={{ fontSize: '12px', color: '#6b7280', background: '#f9fafb', padding: '6px', borderRadius: '4px' }}>
                            💰 Costo calculado: {formMaquinaria.horasUso} h × ${maquinariasInventario.find(m => m.id === parseInt(formMaquinaria.maquinariaId))?.costoPorHora || 0}/h = ${formMaquinaria.costo ? parseFloat(formMaquinaria.costo).toLocaleString() : 0}
                          </div>
                        )}
                      </div>
                    ) : (
                      /* Formulario para ALQUILADA */
                      <div style={{ marginBottom: '12px', display: 'grid', gridTemplateColumns: '2fr 1fr 1fr auto', gap: '8px' }}>
                        <input
                          type="text"
                          value={formMaquinaria.descripcion}
                          onChange={(e) => setFormMaquinaria({ ...formMaquinaria, descripcion: e.target.value })}
                          placeholder="Descripción (ej: Sembradora)"
                          style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                        />
                        <input
                          type="text"
                          value={formMaquinaria.proveedor}
                          onChange={(e) => setFormMaquinaria({ ...formMaquinaria, proveedor: e.target.value })}
                          placeholder="Proveedor"
                          style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                        />
                        <input
                          type="number"
                          value={formMaquinaria.costo}
                          onChange={(e) => setFormMaquinaria({ ...formMaquinaria, costo: e.target.value })}
                          placeholder="Costo"
                          min="0"
                          step="0.01"
                          style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                        />
                        <button
                          onClick={agregarMaquinaria}
                          type="button"
                          style={{
                            padding: '8px 12px',
                            background: '#4CAF50',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '13px'
                          }}
                        >
                          +
                        </button>
                      </div>
                    )}

                    {/* Lista de maquinaria agregada */}
                    {maquinarias.length > 0 ? (
                      maquinarias.map((maq, idx) => (
                        <div key={idx} style={{
                          background: '#f9fafb',
                          padding: '10px',
                          borderRadius: '4px',
                          marginBottom: '8px',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'flex-start',
                          border: '1px solid #e5e7eb'
                        }}>
                          <div style={{ fontSize: '13px', flex: 1 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
                              <strong>{maq.descripcion}</strong>
                              <span style={{
                                background: maq.proveedor ? '#fef3c7' : '#dcfce7',
                                color: maq.proveedor ? '#92400e' : '#166534',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                fontSize: '11px',
                                fontWeight: 'bold'
                              }}>
                                {maq.proveedor ? '🏢 Alquilada' : '🏠 Propia'}
                              </span>
                            </div>
                            {maq.proveedor && (
                              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                                Proveedor: {maq.proveedor}
                              </div>
                            )}
                            {maq.observaciones && (
                              <div style={{ fontSize: '11px', color: '#6b7280', fontStyle: 'italic' }}>
                                {maq.observaciones}
                              </div>
                            )}
                            <div style={{ fontSize: '13px', color: '#059669', fontWeight: 'bold', marginTop: '4px' }}>
                              💰 ${maq.costoTotal.toLocaleString()}
                            </div>
                          </div>
                          <button
                            onClick={() => setMaquinarias(maquinarias.filter((_, i) => i !== idx))}
                            type="button"
                            style={{
                              background: '#f87171',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              padding: '4px 8px',
                              cursor: 'pointer',
                              fontSize: '12px',
                              marginLeft: '8px'
                            }}
                          >
                            🗑️
                          </button>
                        </div>
                      ))
                    ) : (
                      <div style={{ textAlign: 'center', padding: '20px', color: '#6b7280', fontSize: '13px' }}>
                        No hay maquinaria agregada
                      </div>
                    )}
                  </div>
                )}

                {/* PESTAÑA MANO DE OBRA */}
                {pestanaActiva === 'manoObra' && (
                  <div>
                    <div style={{ marginBottom: '12px', display: 'grid', gridTemplateColumns: '2fr 1fr 1fr auto', gap: '8px' }}>
                      <input
                        type="text"
                        value={formManoObra.descripcion}
                        onChange={(e) => setFormManoObra({ ...formManoObra, descripcion: e.target.value })}
                        placeholder="Descripción (ej: Operador)"
                        style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                      />
                      <input
                        type="number"
                        value={formManoObra.cantidadPersonas}
                        onChange={(e) => setFormManoObra({ ...formManoObra, cantidadPersonas: e.target.value })}
                        placeholder="Cantidad"
                        min="1"
                        style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                      />
                      <input
                        type="number"
                        value={formManoObra.costo}
                        onChange={(e) => setFormManoObra({ ...formManoObra, costo: e.target.value })}
                        placeholder="Costo total"
                        min="0"
                        step="0.01"
                        style={{ padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px' }}
                      />
                      <button
                        onClick={agregarManoObra}
                        type="button"
                        style={{
                          padding: '8px 12px',
                          background: '#4CAF50',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: 'pointer',
                          fontSize: '13px'
                        }}
                      >
                        + Agregar
                      </button>
                    </div>

                    {/* Lista de mano de obra agregada */}
                    {manoObras.length > 0 ? (
                      manoObras.map((mo, idx) => (
                        <div key={idx} style={{
                          background: '#f9fafb',
                          padding: '10px',
                          borderRadius: '4px',
                          marginBottom: '8px',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'flex-start',
                          border: '1px solid #e5e7eb'
                        }}>
                          <div style={{ fontSize: '13px', flex: 1 }}>
                            <strong>{mo.descripcion}</strong><br />
                            <span style={{ fontSize: '12px', color: '#6b7280' }}>
                              👷 {mo.cantidadPersonas} persona(s)
                              {mo.horasTrabajo && ` • ⏱️ ${mo.horasTrabajo}h`}
                              {mo.proveedor && ` • ${mo.proveedor}`}
                            </span>
                            <div style={{ fontSize: '13px', color: '#059669', fontWeight: 'bold', marginTop: '4px' }}>
                              💰 ${mo.costoTotal.toLocaleString()}
                            </div>
                          </div>
                          <button
                            onClick={() => setManoObras(manoObras.filter((_, i) => i !== idx))}
                            type="button"
                            style={{
                              background: '#f87171',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              padding: '4px 8px',
                              cursor: 'pointer',
                              fontSize: '12px',
                              marginLeft: '8px'
                            }}
                          >
                            🗑️
                          </button>
                        </div>
                      ))
                    ) : (
                      <div style={{ textAlign: 'center', padding: '20px', color: '#6b7280', fontSize: '13px' }}>
                        No hay mano de obra agregada
                      </div>
                    )}
                  </div>
                )}
              </div>

            </div>
          )}
        </div>

        {/* Resumen de Costos SIEMPRE VISIBLE (fuera del scroll) */}
        <div style={{
          background: costos.total > 0 ? '#e8f5e9' : '#f3f4f6',
          padding: '16px',
          borderRadius: '8px',
          border: costos.total > 0 ? '2px solid #4CAF50' : '2px solid #e5e7eb',
          marginBottom: '12px',
          flexShrink: 0
        }}>
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center',
              marginBottom: costos.total > 0 ? '12px' : '0'
            }}>
              <div style={{ fontSize: '16px', fontWeight: 'bold', color: costos.total > 0 ? '#1b5e20' : '#6b7280' }}>
                💰 Costo Total de Siembra
              </div>
              <div style={{ fontSize: '20px', fontWeight: 'bold', color: costos.total > 0 ? '#16a34a' : '#6b7280' }}>
                ${costos.total.toLocaleString()}
              </div>
            </div>
            
            {costos.total > 0 && (
              <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(3, 1fr)', 
                gap: '12px',
                paddingTop: '12px',
                borderTop: '1px solid #d1fae5'
              }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '11px', color: '#6b7280', marginBottom: '4px' }}>INSUMOS</div>
                  <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#059669' }}>
                    ${costos.costoInsumos.toLocaleString()}
                  </div>
                  <div style={{ fontSize: '10px', color: '#6b7280' }}>
                    {insumosUsados.length} item(s)
                  </div>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '11px', color: '#6b7280', marginBottom: '4px' }}>MAQUINARIA</div>
                  <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#059669' }}>
                    ${costos.costoMaquinaria.toLocaleString()}
                  </div>
                  <div style={{ fontSize: '10px', color: '#6b7280' }}>
                    {maquinarias.length} item(s)
                  </div>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '11px', color: '#6b7280', marginBottom: '4px' }}>MANO DE OBRA</div>
                  <div style={{ fontSize: '14px', fontWeight: 'bold', color: '#059669' }}>
                    ${costos.costoManoObra.toLocaleString()}
                  </div>
                  <div style={{ fontSize: '10px', color: '#6b7280' }}>
                    {manoObras.reduce((sum, mo) => sum + mo.cantidadPersonas, 0)} persona(s)
                  </div>
                </div>
              </div>
            )}
            
            {costos.total === 0 && (
              <div style={{ fontSize: '13px', color: '#6b7280', textAlign: 'center' }}>
                No se han agregado recursos. El costo será $0.
              </div>
            )}
        </div>

        {/* Botones finales (fuera del scroll) */}
        <div style={{ display: 'flex', gap: '12px', flexShrink: 0 }}>
            <button
              onClick={onClose}
              disabled={loading}
              type="button"
              style={{
                flex: 1,
                padding: '12px',
                background: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                opacity: loading ? 0.5 : 1
              }}
            >
              Cancelar
            </button>
            <button
              onClick={handleSembrar}
              disabled={loading}
              type="button"
              style={{
                flex: 2,
                padding: '12px',
                background: loading ? '#9ca3af' : '#4CAF50',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              {loading ? '🔄 Sembrando...' : '🌱 Confirmar Siembra'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SiembraModalHibrido;
