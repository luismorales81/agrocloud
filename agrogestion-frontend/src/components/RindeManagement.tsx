import React, { useState, useEffect } from 'react';

interface RindeLote {
  id?: number;
  lote_id: number;
  cultivo_id: number;
  superficie_ha: number;
  fecha_siembra: string;
  densidad_siembra: number;
  variedad_semilla: string;
  fertilizante_nitrogeno: number;
  fertilizante_fosforo: number;
  fertilizante_potasio: number;
  otros_insumos: string;
  fecha_cosecha: string;
  cantidad_cosechada: number;
  unidad_cosecha: 'kg' | 'tn' | 'qq';
  humedad_cosecha: number;
  rinde_real: number;
  rinde_esperado: number;
  diferencia_rinde: number;
  porcentaje_cumplimiento: number;
  clima_favorable: boolean;
  plagas_enfermedades: boolean;
  riego_suficiente: boolean;
  observaciones: string;
}

interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  campo_id: number;
}

interface Cultivo {
  id: number;
  nombre: string;
  variedad: string;
  rendimiento_esperado: number;
  unidad_rendimiento: string;
}

const RindeManagement: React.FC = () => {
  const [rindes, setRindes] = useState<RindeLote[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [cultivos, setCultivos] = useState<Cultivo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [selectedLote, setSelectedLote] = useState<Lote | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<RindeLote>({
    lote_id: 0,
    cultivo_id: 0,
    superficie_ha: 0,
    fecha_siembra: '',
    densidad_siembra: 0,
    variedad_semilla: '',
    fertilizante_nitrogeno: 0,
    fertilizante_fosforo: 0,
    fertilizante_potasio: 0,
    otros_insumos: '',
    fecha_cosecha: '',
    cantidad_cosechada: 0,
    unidad_cosecha: 'kg',
    humedad_cosecha: 0,
    rinde_real: 0,
    rinde_esperado: 0,
    diferencia_rinde: 0,
    porcentaje_cumplimiento: 0,
    clima_favorable: true,
    plagas_enfermedades: false,
    riego_suficiente: true,
    observaciones: ''
  });

  // Cargar datos
  const loadData = async () => {
    try {
      setLoading(true);
      
      // Simulación de carga de lotes
      const mockLotes: Lote[] = [
        { id: 1, nombre: 'Lote A1', superficie: 25.50, cultivo: 'Soja', campo_id: 1 },
        { id: 2, nombre: 'Lote A2', superficie: 30.25, cultivo: 'Maíz', campo_id: 1 },
        { id: 3, nombre: 'Lote B1', superficie: 40.00, cultivo: 'Trigo', campo_id: 2 },
        { id: 4, nombre: 'Lote B2', superficie: 35.75, cultivo: 'Soja', campo_id: 2 }
      ];
      setLotes(mockLotes);

      // Simulación de carga de cultivos
      const mockCultivos: Cultivo[] = [
        { id: 1, nombre: 'Soja', variedad: 'DM 53i54', rendimiento_esperado: 3500, unidad_rendimiento: 'kg/ha' },
        { id: 2, nombre: 'Soja', variedad: 'DM 58i60', rendimiento_esperado: 3800, unidad_rendimiento: 'kg/ha' },
        { id: 3, nombre: 'Maíz', variedad: 'DK 72-10', rendimiento_esperado: 12500, unidad_rendimiento: 'kg/ha' },
        { id: 4, nombre: 'Maíz', variedad: 'DK 79-10', rendimiento_esperado: 14000, unidad_rendimiento: 'kg/ha' },
        { id: 5, nombre: 'Trigo', variedad: 'Klein Pantera', rendimiento_esperado: 4500, unidad_rendimiento: 'kg/ha' }
      ];
      setCultivos(mockCultivos);

      // Simulación de carga de rindes
      const mockRindes: RindeLote[] = [
        {
          id: 1,
          lote_id: 1,
          cultivo_id: 1,
          superficie_ha: 25.50,
          fecha_siembra: '2024-03-15',
          densidad_siembra: 300000,
          variedad_semilla: 'DM 53i54',
          fertilizante_nitrogeno: 0,
          fertilizante_fosforo: 40,
          fertilizante_potasio: 0,
          otros_insumos: '{"herbicida": "Glifosato 3L/ha"}',
          fecha_cosecha: '2024-07-15',
          cantidad_cosechada: 89250,
          unidad_cosecha: 'kg',
          humedad_cosecha: 13.5,
          rinde_real: 3500,
          rinde_esperado: 3500,
          diferencia_rinde: 0,
          porcentaje_cumplimiento: 100,
          clima_favorable: true,
          plagas_enfermedades: false,
          riego_suficiente: true,
          observaciones: 'Rinde excelente, condiciones climáticas óptimas'
        },
        {
          id: 2,
          lote_id: 2,
          cultivo_id: 3,
          superficie_ha: 30.25,
          fecha_siembra: '2024-03-10',
          densidad_siembra: 70000,
          variedad_semilla: 'DK 72-10',
          fertilizante_nitrogeno: 120,
          fertilizante_fosforo: 60,
          fertilizante_potasio: 30,
          otros_insumos: '{"fungicida": "Azoxystrobin 0.5L/ha"}',
          fecha_cosecha: '2024-07-20',
          cantidad_cosechada: 378125,
          unidad_cosecha: 'kg',
          humedad_cosecha: 14.2,
          rinde_real: 12500,
          rinde_esperado: 12500,
          diferencia_rinde: 0,
          porcentaje_cumplimiento: 100,
          clima_favorable: true,
          plagas_enfermedades: false,
          riego_suficiente: true,
          observaciones: 'Maíz con muy buen desarrollo, fertilización adecuada'
        }
      ];
      setRindes(mockRindes);
    } catch (error) {
      console.error('Error cargando datos:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // Calcular rinde automáticamente
  const calcularRinde = () => {
    if (formData.cantidad_cosechada > 0 && formData.superficie_ha > 0) {
      const rindeReal = formData.cantidad_cosechada / formData.superficie_ha;
      const rindeEsperado = formData.rinde_esperado;
      const diferencia = rindeReal - rindeEsperado;
      const porcentaje = (rindeReal / rindeEsperado) * 100;

      setFormData(prev => ({
        ...prev,
        rinde_real: Math.round(rindeReal * 100) / 100,
        diferencia_rinde: Math.round(diferencia * 100) / 100,
        porcentaje_cumplimiento: Math.round(porcentaje * 100) / 100
      }));
    }
  };

  // Actualizar rinde esperado cuando cambia el cultivo
  const handleCultivoChange = (cultivoId: number) => {
    const cultivo = cultivos.find(c => c.id === cultivoId);
    if (cultivo) {
      setFormData(prev => ({
        ...prev,
        cultivo_id: cultivoId,
        rinde_esperado: cultivo.rendimiento_esperado
      }));
    }
  };

  // Guardar rinde
  const saveRinde = async () => {
    try {
      setLoading(true);
      
      if (!formData.lote_id || !formData.cultivo_id || formData.superficie_ha <= 0) {
        alert('Por favor complete todos los campos obligatorios');
        return;
      }

      const newRinde: RindeLote = {
        id: rindes.length + 1,
        ...formData
      };

      setRindes(prev => [...prev, newRinde]);
      setFormData({
        lote_id: 0,
        cultivo_id: 0,
        superficie_ha: 0,
        fecha_siembra: '',
        densidad_siembra: 0,
        variedad_semilla: '',
        fertilizante_nitrogeno: 0,
        fertilizante_fosforo: 0,
        fertilizante_potasio: 0,
        otros_insumos: '',
        fecha_cosecha: '',
        cantidad_cosechada: 0,
        unidad_cosecha: 'kg',
        humedad_cosecha: 0,
        rinde_real: 0,
        rinde_esperado: 0,
        diferencia_rinde: 0,
        porcentaje_cumplimiento: 0,
        clima_favorable: true,
        plagas_enfermedades: false,
        riego_suficiente: true,
        observaciones: ''
      });
      setShowForm(false);
      alert('Rinde guardado exitosamente');
    } catch (error) {
      console.error('Error guardando rinde:', error);
      alert('Error al guardar el rinde');
    } finally {
      setLoading(false);
    }
  };

  // Exportar a CSV
  const exportToCSV = () => {
    const headers = [
      'Lote', 'Cultivo', 'Superficie (ha)', 'Fecha Siembra', 'Fecha Cosecha',
      'Cantidad Cosechada', 'Unidad', 'Rinde Real (kg/ha)', 'Rinde Esperado (kg/ha)',
      'Diferencia', 'Cumplimiento (%)', 'Observaciones'
    ];

    const csvData = rindes.map(rinde => {
      const lote = lotes.find(l => l.id === rinde.lote_id);
      const cultivo = cultivos.find(c => c.id === rinde.cultivo_id);
      
      return [
        lote?.nombre || '',
        cultivo?.nombre || '',
        rinde.superficie_ha,
        rinde.fecha_siembra,
        rinde.fecha_cosecha,
        rinde.cantidad_cosechada,
        rinde.unidad_cosecha,
        rinde.rinde_real,
        rinde.rinde_esperado,
        rinde.diferencia_rinde,
        rinde.porcentaje_cumplimiento,
        rinde.observaciones
      ];
    });

    const csvContent = [headers, ...csvData]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `rindes_lotes_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Filtrar rindes
  const filteredRindes = rindes.filter(rinde => {
    const lote = lotes.find(l => l.id === rinde.lote_id);
    const cultivo = cultivos.find(c => c.id === rinde.cultivo_id);
    const searchLower = searchTerm.toLowerCase();
    
    return (
      lote?.nombre.toLowerCase().includes(searchLower) ||
      cultivo?.nombre.toLowerCase().includes(searchLower) ||
      rinde.variedad_semilla.toLowerCase().includes(searchLower)
    );
  });

  // Obtener estadísticas
  const estadisticas = {
    totalLotes: rindes.length,
    promedioRinde: rindes.length > 0 ? rindes.reduce((sum, r) => sum + r.rinde_real, 0) / rindes.length : 0,
    mejorRinde: rindes.length > 0 ? Math.max(...rindes.map(r => r.rinde_real)) : 0,
    peorRinde: rindes.length > 0 ? Math.min(...rindes.map(r => r.rinde_real)) : 0,
    promedioCumplimiento: rindes.length > 0 ? rindes.reduce((sum, r) => sum + r.porcentaje_cumplimiento, 0) / rindes.length : 0
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>📊 Gestión de Rindes</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Calcula y analiza los rindes de tus lotes automáticamente
        </p>
      </div>

      {/* Botones de acción */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
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
          ➕ Nuevo Rinde
        </button>
        <button
          onClick={exportToCSV}
          style={{
            background: '#3b82f6',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: 'bold'
          }}
        >
          📊 Exportar CSV
        </button>
      </div>

      {/* Estadísticas */}
      <div style={{ 
        background: '#fef3c7', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #f59e0b'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#92400e' }}>📈 Estadísticas Generales</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {estadisticas.totalLotes}
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Lotes con Rinde</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {Math.round(estadisticas.promedioRinde)} kg/ha
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Rinde Promedio</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {Math.round(estadisticas.mejorRinde)} kg/ha
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Mejor Rinde</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {Math.round(estadisticas.promedioCumplimiento)}%
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Cumplimiento Promedio</div>
          </div>
        </div>
      </div>

      {/* Búsqueda */}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          placeholder="🔍 Buscar por lote, cultivo o variedad..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{
            width: '100%',
            padding: '12px',
            border: '1px solid #ddd',
            borderRadius: '8px',
            fontSize: '16px'
          }}
        />
      </div>

      {/* Formulario de nuevo rinde */}
      {showForm && (
        <div style={{ 
          background: '#f9fafb', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #e5e7eb'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>📝 Nuevo Registro de Rinde</h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
            {/* Selección de lote */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Lote:</label>
              <select
                value={formData.lote_id}
                onChange={(e) => {
                  const loteId = Number(e.target.value);
                  const lote = lotes.find(l => l.id === loteId);
                  setFormData(prev => ({
                    ...prev,
                    lote_id: loteId,
                    superficie_ha: lote?.superficie || 0
                  }));
                  setSelectedLote(lote || null);
                }}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value={0}>Seleccionar lote</option>
                {lotes.map(lote => (
                  <option key={lote.id} value={lote.id}>
                    {lote.nombre} - {lote.cultivo} ({lote.superficie} ha)
                  </option>
                ))}
              </select>
            </div>

            {/* Cultivo */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cultivo:</label>
              <select
                value={formData.cultivo_id}
                onChange={(e) => handleCultivoChange(Number(e.target.value))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value={0}>Seleccionar cultivo</option>
                {cultivos.map(cultivo => (
                  <option key={cultivo.id} value={cultivo.id}>
                    {cultivo.nombre} - {cultivo.variedad}
                  </option>
                ))}
              </select>
            </div>

            {/* Superficie */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Superficie (ha):</label>
              <input
                type="number"
                step="0.01"
                value={formData.superficie_ha}
                onChange={(e) => setFormData(prev => ({ ...prev, superficie_ha: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Fecha de siembra */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha de Siembra:</label>
              <input
                type="date"
                value={formData.fecha_siembra}
                onChange={(e) => setFormData(prev => ({ ...prev, fecha_siembra: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Densidad de siembra */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Densidad de Siembra (plantas/ha):</label>
              <input
                type="number"
                value={formData.densidad_siembra}
                onChange={(e) => setFormData(prev => ({ ...prev, densidad_siembra: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Variedad de semilla */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Variedad de Semilla:</label>
              <input
                type="text"
                value={formData.variedad_semilla}
                onChange={(e) => setFormData(prev => ({ ...prev, variedad_semilla: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Ej: DM 53i54"
              />
            </div>

            {/* Fertilizantes */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Nitrógeno (kg/ha):</label>
              <input
                type="number"
                step="0.1"
                value={formData.fertilizante_nitrogeno}
                onChange={(e) => setFormData(prev => ({ ...prev, fertilizante_nitrogeno: Number(e.target.value) }))}
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
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fósforo (kg/ha):</label>
              <input
                type="number"
                step="0.1"
                value={formData.fertilizante_fosforo}
                onChange={(e) => setFormData(prev => ({ ...prev, fertilizante_fosforo: Number(e.target.value) }))}
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
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Potasio (kg/ha):</label>
              <input
                type="number"
                step="0.1"
                value={formData.fertilizante_potasio}
                onChange={(e) => setFormData(prev => ({ ...prev, fertilizante_potasio: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Datos de cosecha */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha de Cosecha:</label>
              <input
                type="date"
                value={formData.fecha_cosecha}
                onChange={(e) => setFormData(prev => ({ ...prev, fecha_cosecha: e.target.value }))}
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
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cantidad Cosechada:</label>
              <input
                type="number"
                step="0.1"
                value={formData.cantidad_cosechada}
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, cantidad_cosechada: Number(e.target.value) }));
                  setTimeout(calcularRinde, 100);
                }}
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
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Unidad:</label>
              <select
                value={formData.unidad_cosecha}
                onChange={(e) => setFormData(prev => ({ ...prev, unidad_cosecha: e.target.value as 'kg' | 'tn' | 'qq' }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="kg">Kilogramos (kg)</option>
                <option value="tn">Toneladas (tn)</option>
                <option value="qq">Quintales (qq)</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Humedad Cosecha (%):</label>
              <input
                type="number"
                step="0.1"
                value={formData.humedad_cosecha}
                onChange={(e) => setFormData(prev => ({ ...prev, humedad_cosecha: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Rinde esperado */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Rinde Esperado (kg/ha):</label>
              <input
                type="number"
                step="0.1"
                value={formData.rinde_esperado}
                onChange={(e) => {
                  setFormData(prev => ({ ...prev, rinde_esperado: Number(e.target.value) }));
                  setTimeout(calcularRinde, 100);
                }}
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

          {/* Factores que afectan el rinde */}
          <div style={{ marginTop: '15px' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#374151' }}>🌾 Factores que Afectan el Rinde</h4>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <input
                  type="checkbox"
                  checked={formData.clima_favorable}
                  onChange={(e) => setFormData(prev => ({ ...prev, clima_favorable: e.target.checked }))}
                />
                Clima Favorable
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <input
                  type="checkbox"
                  checked={formData.plagas_enfermedades}
                  onChange={(e) => setFormData(prev => ({ ...prev, plagas_enfermedades: e.target.checked }))}
                />
                Problemas de Plagas/Enfermedades
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <input
                  type="checkbox"
                  checked={formData.riego_suficiente}
                  onChange={(e) => setFormData(prev => ({ ...prev, riego_suficiente: e.target.checked }))}
                />
                Riego Suficiente
              </label>
            </div>
          </div>

          {/* Observaciones */}
          <div style={{ marginTop: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Observaciones:</label>
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
              placeholder="Observaciones sobre el rinde obtenido..."
            />
          </div>

          {/* Resultados del cálculo */}
          {(formData.rinde_real > 0 || formData.rinde_esperado > 0) && (
            <div style={{ 
              marginTop: '15px', 
              padding: '15px', 
              background: '#ecfdf5', 
              borderRadius: '8px',
              border: '1px solid #10b981'
            }}>
              <h4 style={{ margin: '0 0 10px 0', color: '#065f46' }}>📊 Resultados del Cálculo</h4>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '15px' }}>
                <div>
                  <strong>Rinde Real:</strong> {formData.rinde_real} kg/ha
                </div>
                <div>
                  <strong>Rinde Esperado:</strong> {formData.rinde_esperado} kg/ha
                </div>
                <div>
                  <strong>Diferencia:</strong> 
                  <span style={{ 
                    color: formData.diferencia_rinde >= 0 ? '#10b981' : '#ef4444',
                    fontWeight: 'bold'
                  }}>
                    {formData.diferencia_rinde >= 0 ? '+' : ''}{formData.diferencia_rinde} kg/ha
                  </span>
                </div>
                <div>
                  <strong>Cumplimiento:</strong> 
                  <span style={{ 
                    color: formData.porcentaje_cumplimiento >= 100 ? '#10b981' : '#f59e0b',
                    fontWeight: 'bold'
                  }}>
                    {formData.porcentaje_cumplimiento}%
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Botones */}
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              onClick={saveRinde}
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
              {loading ? '💾 Guardando...' : '💾 Guardar Rinde'}
            </button>
            <button
              onClick={() => setShowForm(false)}
              style={{
                background: '#6b7280',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Tabla de rindes */}
      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6',
          fontWeight: 'bold'
        }}>
          📊 Rindes Registrados ({filteredRindes.length})
        </div>
        
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            🔄 Cargando rindes...
          </div>
        ) : filteredRindes.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            {searchTerm ? 'No se encontraron rindes que coincidan con la búsqueda' : 'No hay rindes registrados'}
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ 
              width: '100%', 
              borderCollapse: 'collapse',
              fontSize: '14px'
            }}>
              <thead>
                <tr style={{ background: '#f8f9fa' }}>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Lote</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cultivo</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Superficie</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rinde Real</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rinde Esperado</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cumplimiento</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha Cosecha</th>
                </tr>
              </thead>
              <tbody>
                {filteredRindes.map(rinde => {
                  const lote = lotes.find(l => l.id === rinde.lote_id);
                  const cultivo = cultivos.find(c => c.id === rinde.cultivo_id);
                  
                  return (
                    <tr key={rinde.id} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        <strong>{lote?.nombre}</strong>
                      </td>
                      <td style={{ padding: '12px' }}>
                        {cultivo?.nombre} - {rinde.variedad_semilla}
                      </td>
                      <td style={{ padding: '12px' }}>
                        {rinde.superficie_ha} ha
                      </td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                          {rinde.rinde_real} kg/ha
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>
                        {rinde.rinde_esperado} kg/ha
                      </td>
                      <td style={{ padding: '12px' }}>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold',
                          background: rinde.porcentaje_cumplimiento >= 100 ? '#dcfce7' : '#fef3c7',
                          color: rinde.porcentaje_cumplimiento >= 100 ? '#166534' : '#92400e'
                        }}>
                          {rinde.porcentaje_cumplimiento}%
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>
                        {new Date(rinde.fecha_cosecha).toLocaleDateString('es-ES')}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default RindeManagement;
