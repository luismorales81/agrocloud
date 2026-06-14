import React, { useState, useEffect } from 'react';
import { useLocation, useParams, useNavigate } from 'react-router-dom';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useCurrencyContext } from '../../../../contexts/CurrencyContext';
import { ventaPorcinoService } from '../../services/ventaPorcinoService';
import { parametrosService } from '../../services/parametrosService';
import { recriaService } from '../../services/recriaService';
import type { VentaPorcino, ParametrosEstablecimientoPorcino } from '../../types';

const VentasScreen: React.FC = () => {
  const location = useLocation();
  const params = useParams();
  const navigate = useNavigate();
  const { formatCurrency } = useCurrencyContext();
  const [ventas, setVentas] = useState<VentaPorcino[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [filtros, setFiltros] = useState<{ fechaDesde?: string; fechaHasta?: string; tipo?: string }>({});
  const [ingresosTotales, setIngresosTotales] = useState<number>(0);
  const [parametrosEstablecimiento, setParametrosEstablecimiento] = useState<ParametrosEstablecimientoPorcino | null>(null);
  const [tiposHabilitados, setTiposHabilitados] = useState<string[]>(['ENGORDE', 'REPRODUCTOR']);
  
  // Detectar si viene de la ruta /faena
  const esRutaFaena = location.pathname.includes('/faena');
  const [formData, setFormData] = useState<Partial<VentaPorcino>>({
    tipo: 'ENGORDE',
    fecha: new Date().toISOString().split('T')[0],
    cantidad: 0,
    pesoPromedio: 0,
    precioKg: 0,
    observaciones: '',
  });

  useEffect(() => {
    cargarParametros();
  }, []);

  // Si viene de /faena, filtrar solo faenas
  useEffect(() => {
    if (esRutaFaena && !filtros.tipo) {
      setFiltros(prev => ({ ...prev, tipo: 'FAENA' }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [esRutaFaena]);

  // Si viene de /faena/nueva/:recriaId, abrir modal con datos prellenados
  useEffect(() => {
    if (params.recriaId && esRutaFaena && !showModal) {
      cargarRecriaParaFaena(parseInt(params.recriaId));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.recriaId, esRutaFaena]);

  useEffect(() => {
    cargarDatos();
  }, [filtros]);

  const cargarParametros = async () => {
    try {
      const parametros = await parametrosService.obtenerParametrosEstablecimiento();
      setParametrosEstablecimiento(parametros);
      
      // Definir tipos habilitados según configuración
      const tipos = ['ENGORDE', 'REPRODUCTOR'];
      if (parametros?.realizaFaena) {
        tipos.push('FAENA');
      }
      setTiposHabilitados(tipos);
    } catch (error) {
      console.error('Error al cargar parámetros:', error);
    }
  };

  useEffect(() => {
    calcularIngresoTotal();
  }, [formData.cantidad, formData.pesoPromedio, formData.precioKg]);

  const cargarRecriaParaFaena = async (recriaId: number) => {
    try {
      const recria = await recriaService.obtener(recriaId);
      const pesoTotal = recria.pesoPromedio * recria.cantidadAnimales;
      setFormData({
        tipo: 'FAENA',
        recriaId: recriaId,
        fecha: new Date().toISOString().split('T')[0],
        fechaEnvio: new Date().toISOString().split('T')[0],
        cantidad: recria.cantidadAnimales,
        pesoPromedio: recria.pesoPromedio,
        pesoEnvio: pesoTotal,
        observaciones: '',
      });
      setShowModal(true);
      // Limpiar el parámetro de la URL
      navigate('/porcinos/ventas', { replace: true });
    } catch (error) {
      console.error('Error al cargar recría:', error);
      alert('Error al cargar la recría para faena');
    }
  };

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [ventasData, ingresosData] = await Promise.all([
        ventaPorcinoService.listar(filtros.fechaDesde, filtros.fechaHasta),
        ventaPorcinoService.obtenerIngresosTotales(filtros.fechaDesde, filtros.fechaHasta),
      ]);
      // Filtrar por tipo si hay filtro
      let ventasFiltradas = ventasData;
      if (filtros.tipo) {
        ventasFiltradas = ventasData.filter((v: VentaPorcino) => v.tipo === filtros.tipo);
      }
      setVentas(ventasFiltradas);
      setIngresosTotales(ingresosData?.ingresosTotales || 0);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const calcularIngresoTotal = () => {
    // Para ventas normales (ENGORDE, REPRODUCTOR), calcular si hay precioKg
    if (formData.tipo !== 'FAENA' && formData.cantidad && formData.pesoPromedio && formData.precioKg) {
      const total = formData.cantidad * formData.pesoPromedio * formData.precioKg;
      setFormData(prev => ({ ...prev, ingresoTotal: total }));
    }
    // Para FAENA, el ingreso se calcula en el backend según pesoFaena o pesoEsperadoConMerma
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await ventaPorcinoService.crear(formData);
      setShowModal(false);
      setFormData({
        tipo: (tiposHabilitados[0] || 'ENGORDE') as 'ENGORDE' | 'REPRODUCTOR' | 'FAENA',
        fecha: new Date().toISOString().split('T')[0],
        cantidad: 0,
        pesoPromedio: 0,
        precioKg: 0,
        observaciones: '',
        // Limpiar campos de faena
        pesoEnvio: undefined,
        pesoFaena: undefined,
        rendimiento: undefined,
        fechaEnvio: undefined,
        fechaFaena: undefined,
      });
      cargarDatos();
    } catch (error) {
      console.error('Error al registrar venta:', error);
      alert('Error al registrar venta');
    }
  };

  const obtenerNombreTipo = (tipo: string) => {
    switch (tipo) {
      case 'ENGORDE':
        return 'Engorde';
      case 'REPRODUCTOR':
        return 'Reproductor';
      case 'FAENA':
        return 'Faena';
      default:
        return tipo;
    }
  };


  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando ventas...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name={esRutaFaena ? "Scissors" : "DollarSign"} size={32} style={{ marginRight: '0.5rem' }} /> 
          {esRutaFaena ? 'Gestión de Faena' : 'Gestión de Ventas y Faena'}
        </h1>
        <button
          onClick={() => {
            if (esRutaFaena) {
              setFormData(prev => ({ ...prev, tipo: 'FAENA' }));
            }
            setShowModal(true);
          }}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '500'
          }}
        >
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> 
          {esRutaFaena ? 'Registrar Faena' : 'Registrar Venta'}
        </button>
      </div>

      {/* Filtros */}
      <div style={{
        backgroundColor: 'white',
        padding: '1.5rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <h2 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', color: '#1f2937' }}>
          <Icon name="Search" size={20} style={{ marginRight: '0.5rem' }} /> Filtros
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          {!esRutaFaena && (
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                Tipo de Venta
              </label>
              <select
                value={filtros.tipo || ''}
                onChange={(e) => setFiltros({...filtros, tipo: e.target.value || undefined})}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              >
                <option value="">Todos los tipos</option>
                <option value="ENGORDE">Engorde</option>
                <option value="REPRODUCTOR">Reproductor</option>
                {tiposHabilitados.includes('FAENA') && <option value="FAENA">Faena</option>}
              </select>
            </div>
          )}
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Fecha Desde
            </label>
            <input
              type="date"
              value={filtros.fechaDesde || ''}
              onChange={(e) => setFiltros({...filtros, fechaDesde: e.target.value || undefined})}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Fecha Hasta
            </label>
            <input
              type="date"
              value={filtros.fechaHasta || ''}
              onChange={(e) => setFiltros({...filtros, fechaHasta: e.target.value || undefined})}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button
              onClick={() => setFiltros(esRutaFaena ? { tipo: 'FAENA' } : {})}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontSize: '0.875rem',
                fontWeight: '500',
                width: '100%'
              }}
            >
              Limpiar Filtros
            </button>
          </div>
        </div>
      </div>

      {/* Estadísticas */}
      {ventas.length > 0 && (
        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          marginBottom: '1.5rem',
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1rem'
        }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Ventas</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>{ventas.length}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Cantidad Total Vendida</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
              {ventas.reduce((sum, v) => sum + (v.cantidad || 0), 0)} animales
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Ingresos Totales</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
              {formatCurrency(ingresosTotales)}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Precio Promedio por kg</p>
            <p style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
              {ventas.length > 0
                ? (() => {
                    const ventasConPrecio = ventas.filter(v => v.precioKg);
                    return ventasConPrecio.length > 0
                      ? formatCurrency(ventasConPrecio.reduce((sum, v) => sum + (v.precioKg || 0), 0) / ventasConPrecio.length)
                      : '-';
                  })()
                : '-'}
            </p>
          </div>
        </div>
      )}

      {/* Lista de Ventas */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        overflow: 'hidden'
      }}>
        {ventas.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
            <Icon name="DollarSign" size={48} />
            <p>No hay ventas registradas</p>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Fecha
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Tipo
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Cantidad
                </th>
                <th style={{ padding: '1rem', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Peso Promedio (kg)
                </th>
                <th style={{ padding: '1rem', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Precio/kg
                </th>
                <th style={{ padding: '1rem', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Ingreso Total
                </th>
              </tr>
            </thead>
            <tbody>
              {ventas.map((venta) => (
                <tr
                  key={venta.id}
                  style={{
                    borderBottom: '1px solid #e5e7eb',
                    transition: 'background-color 0.15s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#f9fafb';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = 'white';
                  }}
                >
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    {new Date(venta.fecha).toLocaleDateString('es-ES')}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    <span style={{
                      display: 'inline-block',
                      padding: '0.25rem 0.75rem',
                      borderRadius: '9999px',
                      fontSize: '0.75rem',
                      fontWeight: '500',
                      backgroundColor: venta.tipo === 'FAENA' ? '#fee2e2' : venta.tipo === 'ENGORDE' ? '#dbeafe' : '#fef3c7',
                      color: venta.tipo === 'FAENA' ? '#991b1b' : venta.tipo === 'ENGORDE' ? '#1e40af' : '#92400e'
                    }}>
                      {obtenerNombreTipo(venta.tipo)}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', fontWeight: '500' }}>
                    {venta.cantidad}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'right' }}>
                    {venta.pesoPromedio.toFixed(2)}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'right' }}>
                    {venta.precioKg ? formatCurrency(venta.precioKg) : '-'}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'right', fontWeight: '600', color: '#10b981' }}>
                    {formatCurrency(venta.ingresoTotal)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal Registrar Venta */}
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '600px',
            width: '90%',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Venta
            </h2>
            <form onSubmit={handleSubmit}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Tipo de Venta *
                  </label>
                  <select
                    value={formData.tipo}
                    onChange={(e) => {
                      const nuevoTipo = e.target.value as any;
                      setFormData({
                        ...formData, 
                        tipo: nuevoTipo,
                        // Si cambia de FAENA a otro, limpiar campos de faena y requerir precioKg
                        // Si cambia a FAENA, limpiar precioKg (opcional para faenas)
                        precioKg: nuevoTipo === 'FAENA' ? undefined : formData.precioKg,
                        pesoEnvio: nuevoTipo !== 'FAENA' ? undefined : formData.pesoEnvio,
                        pesoFaena: nuevoTipo !== 'FAENA' ? undefined : formData.pesoFaena,
                        fechaEnvio: nuevoTipo !== 'FAENA' ? undefined : formData.fechaEnvio,
                        fechaFaena: nuevoTipo !== 'FAENA' ? undefined : formData.fechaFaena,
                        rendimiento: nuevoTipo !== 'FAENA' ? undefined : formData.rendimiento,
                      });
                    }}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  >
                    {tiposHabilitados.includes('ENGORDE') && (
                      <option value="ENGORDE">Venta en Pie - Engorde</option>
                    )}
                    {tiposHabilitados.includes('REPRODUCTOR') && (
                      <option value="REPRODUCTOR">Venta en Pie - Reproductor</option>
                    )}
                    {tiposHabilitados.includes('FAENA') && (
                      <option value="FAENA">Faena (Matadero)</option>
                    )}
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Fecha *
                  </label>
                  <input
                    type="date"
                    value={formData.fecha}
                    onChange={(e) => setFormData({...formData, fecha: e.target.value})}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Cantidad *
                    </label>
                    <input
                      type="number"
                      min="1"
                      value={formData.cantidad || ''}
                      onChange={(e) => setFormData({...formData, cantidad: parseInt(e.target.value) || 0})}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem'
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Peso Promedio (kg) *
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={formData.pesoPromedio || ''}
                      onChange={(e) => setFormData({...formData, pesoPromedio: parseFloat(e.target.value) || 0})}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem'
                      }}
                    />
                  </div>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Precio por kg {formData.tipo !== 'FAENA' && '*'}
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={formData.precioKg || ''}
                    onChange={(e) => setFormData({...formData, precioKg: e.target.value ? parseFloat(e.target.value) : undefined})}
                    required={formData.tipo !== 'FAENA'}
                    placeholder={formData.tipo === 'FAENA' ? 'Opcional (se calcula después de faena)' : ''}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  />
                  {formData.tipo === 'FAENA' && (
                    <p style={{ marginTop: '0.25rem', fontSize: '0.75rem', color: '#6b7280' }}>
                      Para faenas, el precio puede ingresarse después de conocer el peso de faena
                    </p>
                  )}
                </div>
                
                {/* Campos específicos de FAENA - solo visibles si tipo === 'FAENA' */}
                {formData.tipo === 'FAENA' && (
                  <>
                    <div>
                      <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                        Fecha de Envío *
                      </label>
                      <input
                        type="date"
                        value={formData.fechaEnvio || formData.fecha || ''}
                        onChange={(e) => setFormData({...formData, fechaEnvio: e.target.value})}
                        required
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.375rem',
                          fontSize: '0.875rem'
                        }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                        Peso de Envío (kg) *
                      </label>
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        value={formData.pesoEnvio || ''}
                        onChange={(e) => setFormData({...formData, pesoEnvio: e.target.value ? parseFloat(e.target.value) : undefined})}
                        required
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.375rem',
                          fontSize: '0.875rem'
                        }}
                      />
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
                      <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                          Fecha de Faena
                        </label>
                        <input
                          type="date"
                          value={formData.fechaFaena || ''}
                          onChange={(e) => setFormData({...formData, fechaFaena: e.target.value || undefined})}
                          style={{
                            width: '100%',
                            padding: '0.75rem',
                            border: '1px solid #d1d5db',
                            borderRadius: '0.375rem',
                            fontSize: '0.875rem'
                          }}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                          Peso de Faena (kg)
                        </label>
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          value={formData.pesoFaena || ''}
                          onChange={(e) => {
                            const pesoFaena = e.target.value ? parseFloat(e.target.value) : undefined;
                            // Calcular rendimiento automáticamente si hay pesoEnvio
                            let rendimiento = undefined;
                            if (pesoFaena && formData.pesoEnvio) {
                              rendimiento = (pesoFaena / formData.pesoEnvio) * 100;
                            }
                            setFormData({...formData, pesoFaena, rendimiento});
                          }}
                          style={{
                            width: '100%',
                            padding: '0.75rem',
                            border: '1px solid #d1d5db',
                            borderRadius: '0.375rem',
                            fontSize: '0.875rem'
                          }}
                        />
                      </div>
                    </div>
                    {formData.rendimiento && (
                      <div style={{
                        padding: '0.75rem',
                        backgroundColor: '#eff6ff',
                        borderRadius: '0.375rem',
                        border: '1px solid #bfdbfe'
                      }}>
                        <p style={{ fontSize: '0.875rem', color: '#1e40af', margin: 0 }}>
                          Rendimiento: <strong>{formData.rendimiento.toFixed(2)}%</strong>
                        </p>
                      </div>
                    )}
                  </>
                )}
                {formData.ingresoTotal && formData.ingresoTotal > 0 && (
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#f0fdf4',
                    borderRadius: '0.375rem',
                    border: '1px solid #bbf7d0'
                  }}>
                    <p style={{ fontSize: '0.75rem', color: '#166534', marginBottom: '0.25rem' }}>Ingreso Total Calculado</p>
                    <p style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#10b981' }}>
                      {formatCurrency(formData.ingresoTotal)}
                    </p>
                  </div>
                )}
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Observaciones
                  </label>
                  <textarea
                    value={formData.observaciones || ''}
                    onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                    rows={3}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem',
                      fontFamily: 'inherit'
                    }}
                  />
                </div>
                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: '#6b7280',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '500'
                    }}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: '#10b981',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '500'
                    }}
                  >
                    Registrar Venta
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default VentasScreen;







