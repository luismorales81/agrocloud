import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import api, { insumoWizardService } from '../services/api';

interface InsumoWizardProps {
  isOpen: boolean;
  onClose: () => void;
  insumoEditando?: any;
  onGuardar: () => void;
}

interface DatosInsumo {
  // Datos básicos
  nombre: string;
  descripcion: string;
  tipo: string;
  unidadMedida: string;
  precioUnitario: number;
  stockActual: number;
  stockMinimo: number;
  proveedor: string;
  fechaVencimiento: string;
  
  // Datos específicos de agroquímicos
  principioActivo: string;
  concentracion: string;
  claseQuimica: string;
  categoriaToxicologica: string;
  periodoCarenciaDias: number;
  dosisMinimaPorHa: number;
  dosisMaximaPorHa: number;
  unidadDosis: string;
  
  // Dosis por tipo de aplicación (simplificado)
  dosisPorTipo: Array<{
    id?: number;
    tipoAplicacion: string;
    formaAplicacion: string;
    unidad: string;
    dosisRecomendadaPorHa: number;
  }>;
}

const InsumoWizard: React.FC<InsumoWizardProps> = ({ 
  isOpen, 
  onClose, 
  insumoEditando, 
  onGuardar 
}) => {
  // Clases químicas comunes para agroquímicos (ordenadas alfabéticamente)
  const clasesQuimicas = [
    'Aminofosfonato',
    'Avermectina',
    'Benzimidazol',
    'Benzotiadiazol',
    'Carbamato',
    'Diamida',
    'Ditiocarbamato',
    'Estrobilurina',
    'Fenilpirazol',
    'Fenoxiácido',
    'Glicina',
    'Imidazolinona',
    'Neonicotinoide',
    'Organofosforado',
    'Otro',
    'Piretroide',
    'Spinosina',
    'Sulfonilurea',
    'Tetramicida',
    'Triazina',
    'Urea'
  ];

  // Categorías toxicológicas según clasificación internacional
  const categoriasToxicologicas = [
    { value: 'I', label: 'I - Extremadamente tóxico' },
    { value: 'II', label: 'II - Altamente tóxico' },
    { value: 'III', label: 'III - Moderadamente tóxico' },
    { value: 'IV', label: 'IV - Ligeramente tóxico' },
    { value: 'V', label: 'V - Prácticamente no tóxico' },
    { value: 'VI', label: 'VI - No clasificado' }
  ];

  // Tipos de agroquímicos (ordenados alfabéticamente)
  const tiposAgroquimicos = [
    { value: 'ACARICIDA', label: 'Acaricida' },
    { value: 'COADYUVANTE', label: 'Coadyuvante' },
    { value: 'FERTILIZANTE', label: 'Fertilizante' },
    { value: 'FUNGICIDA', label: 'Fungicida' },
    { value: 'HERBICIDA', label: 'Herbicida' },
    { value: 'INSECTICIDA', label: 'Insecticida' },
    { value: 'MOLUSQUICIDA', label: 'Molusquicida' },
    { value: 'NEMATICIDA', label: 'Nematicida' },
    { value: 'OTRO', label: 'Otro' },
    { value: 'RODENTICIDA', label: 'Rodenticida' }
  ];

  // Tipos de aplicación (ordenados alfabéticamente)
  const tiposAplicacion = [
    { value: 'COBERTURA', label: 'Cobertura' },
    { value: 'POS_EMERGENCIA', label: 'Pos-emergencia' },
    { value: 'PRE_EMERGENCIA', label: 'Pre-emergencia' }
  ];

  // Formas de aplicación (ordenadas alfabéticamente)
  const formasAplicacion = [
    { value: 'AEREA', label: 'Aérea' },
    { value: 'MANUAL', label: 'Manual' },
    { value: 'TERRESTRE', label: 'Terrestre' }
  ];

  // Unidades de dosis (ordenadas alfabéticamente)
  const unidadesDosis = [
    { value: 'KG_HA', label: 'kg/ha' },
    { value: 'L_HA', label: 'L/ha' },
    { value: 'ML_HA', label: 'ml/ha' }
  ];
  const { user } = useAuth();
  const { empresa } = useEmpresa();
  const [paso, setPaso] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mostrarConfiguracionDosis, setMostrarConfiguracionDosis] = useState(false);
  
  const [datos, setDatos] = useState<DatosInsumo>({
    nombre: '',
    descripcion: '',
    tipo: 'OTROS',
    unidadMedida: 'KG',
    precioUnitario: 0,
    stockActual: 0,
    stockMinimo: 0,
    proveedor: '',
    fechaVencimiento: '',
    principioActivo: '',
    concentracion: '',
    claseQuimica: '',
    categoriaToxicologica: '',
    periodoCarenciaDias: 0,
    dosisMinimaPorHa: 0,
    dosisMaximaPorHa: 0,
    unidadDosis: 'LTS',
    dosisPorTipo: []
  });

  const [tipoInsumo, setTipoInsumo] = useState<'general' | 'agroquimico' | null>(null);
  const [tipoDetectado, setTipoDetectado] = useState<string | null>(null);

  // Reiniciar estado base al abrir o al cambiar el insumo a editar
  useEffect(() => {
    if (!isOpen) return;
    setPaso(1);
    setError(null);
    setMostrarConfiguracionDosis(false);
    if (!insumoEditando) {
      // Nuevo registro: reset total
      setTipoInsumo(null);
      setTipoDetectado(null);
      setDatos({
        nombre: '',
        descripcion: '',
        tipo: 'OTROS',
        unidadMedida: 'KG',
        precioUnitario: 0,
        stockActual: 0,
        stockMinimo: 0,
        proveedor: '',
        fechaVencimiento: '',
        principioActivo: '',
        concentracion: '',
        claseQuimica: '',
        categoriaToxicologica: '',
        periodoCarenciaDias: 0,
        dosisMinimaPorHa: 0,
        dosisMaximaPorHa: 0,
        unidadDosis: 'LTS',
        dosisPorTipo: []
      });
    }
  }, [isOpen, insumoEditando?.id]);

  // Cargar datos si está editando
  useEffect(() => {
    if (insumoEditando) {
      // Manejar ambos formatos: camelCase y snake_case
      const unidadMedida = insumoEditando.unidadMedida || insumoEditando.unidad_medida || 'KG';
      const precioUnitario = insumoEditando.precioUnitario ?? insumoEditando.precio_unitario ?? 0;
      const stockActual = insumoEditando.stockActual ?? insumoEditando.stock_actual ?? 0;
      const stockMinimo = insumoEditando.stockMinimo ?? insumoEditando.stock_minimo ?? 0;
      const fechaVencimiento = insumoEditando.fechaVencimiento || insumoEditando.fecha_vencimiento || '';
      const principioActivo = insumoEditando.principioActivo || insumoEditando.principio_activo || '';
      const concentracion = insumoEditando.concentracion || '';
      const claseQuimica = insumoEditando.claseQuimica || insumoEditando.clase_quimica || '';
      const categoriaToxicologica = insumoEditando.categoriaToxicologica || insumoEditando.categoria_toxicologica || '';
      const periodoCarenciaDias = insumoEditando.periodoCarenciaDias ?? insumoEditando.periodo_carencia_dias ?? 0;
      const dosisMinimaPorHa = insumoEditando.dosisMinimaPorHa ?? insumoEditando.dosis_minima_por_ha ?? 0;
      const dosisMaximaPorHa = insumoEditando.dosisMaximaPorHa ?? insumoEditando.dosis_maxima_por_ha ?? 0;
      const unidadDosis = insumoEditando.unidadDosis || insumoEditando.unidad_dosis || 'LTS';
      
      setDatos({
        nombre: insumoEditando.nombre || '',
        descripcion: insumoEditando.descripcion || '',
        tipo: insumoEditando.tipo || 'OTROS',
        unidadMedida: unidadMedida,
        precioUnitario: typeof precioUnitario === 'number' ? precioUnitario : parseFloat(precioUnitario) || 0,
        stockActual: typeof stockActual === 'number' ? stockActual : parseFloat(stockActual) || 0,
        stockMinimo: typeof stockMinimo === 'number' ? stockMinimo : parseFloat(stockMinimo) || 0,
        proveedor: insumoEditando.proveedor || '',
        fechaVencimiento: fechaVencimiento,
        principioActivo: principioActivo,
        concentracion: concentracion,
        claseQuimica: claseQuimica,
        categoriaToxicologica: categoriaToxicologica,
        periodoCarenciaDias: typeof periodoCarenciaDias === 'number' ? periodoCarenciaDias : parseInt(periodoCarenciaDias) || 0,
        dosisMinimaPorHa: typeof dosisMinimaPorHa === 'number' ? dosisMinimaPorHa : parseFloat(dosisMinimaPorHa) || 0,
        dosisMaximaPorHa: typeof dosisMaximaPorHa === 'number' ? dosisMaximaPorHa : parseFloat(dosisMaximaPorHa) || 0,
        unidadDosis: unidadDosis,
        dosisPorTipo: []
      });
      
      // Determinar si es agroquímico basado en múltiples criterios
      const esAgroquimico = insumoEditando.esAgroquimico || 
                           insumoEditando.tienePropiedadesAgroquimicas ||
                           insumoEditando.principioActivo ||
                           insumoEditando.concentracion ||
                           (insumoEditando.tipo && ['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA'].includes(insumoEditando.tipo));
      
      if (esAgroquimico) {
        setTipoInsumo('agroquimico');
        setTipoDetectado(insumoEditando.tipo);
      } else {
        setTipoInsumo('general');
        setTipoDetectado(insumoEditando.tipo);
      }
    }
  }, [insumoEditando]);

  // Si está editando, cargar dosis existentes desde el backend
  useEffect(() => {
    const cargarDosisExistentes = async () => {
      if (!insumoEditando?.id) return;
      try {
        const dosis = await insumoWizardService.obtenerDosisPorInsumo(insumoEditando.id);
        if (Array.isArray(dosis) && dosis.length > 0) {
          setDatos(prev => ({
            ...prev,
            dosisPorTipo: dosis.map((d: any) => ({
              id: d.id,
              tipoAplicacion: d.tipoAplicacion,
              formaAplicacion: d.formaAplicacion,
              unidad: d.unidad,
              dosisRecomendadaPorHa: d.dosisRecomendadaPorHa
            }))
          }));
        }
      } catch (e) {
        console.warn('[InsumoWizard] No se pudieron cargar dosis existentes', e);
      }
    };
    cargarDosisExistentes();
  }, [insumoEditando?.id]);


  // Detector automático de tipo de agroquímico
  const detectarTipoAgroquimico = (nombre: string): string | null => {
    const nombreLower = nombre.toLowerCase();
    
    const herbicidas = ['glifosato', 'atrazina', '2,4-d', 'dicamba', 'imazapir', 'flumioxazin', 'herbicida'];
    const fungicidas = ['mancozeb', 'clorotalonil', 'azoxistrobina', 'tebuconazol', 'propiconazol', 'fungicida'];
    const insecticidas = ['imidacloprid', 'lambda-cihalotrina', 'deltametrina', 'cipermetrina', 'clorpirifos', 'insecticida'];
    const fertilizantes = ['urea', 'fosfato', 'potasio', 'nitrógeno', 'fósforo', 'fertilizante'];
    
    if (herbicidas.some(h => nombreLower.includes(h))) return 'HERBICIDA';
    if (fungicidas.some(f => nombreLower.includes(f))) return 'FUNGICIDA';
    if (insecticidas.some(i => nombreLower.includes(i))) return 'INSECTICIDA';
    if (fertilizantes.some(f => nombreLower.includes(f))) return 'FERTILIZANTE';
    
    return null;
  };

  // Detectar automáticamente si es agroquímico basado en el nombre
  const detectarSiEsAgroquimico = (nombre: string): boolean => {
    const tipoDetectado = detectarTipoAgroquimico(nombre);
    return tipoDetectado !== null;
  };

  // Detector automático de principio activo
  const detectarPrincipioActivo = (nombre: string): string => {
    const nombreLower = nombre.toLowerCase();
    
    // Patrones comunes
    if (nombreLower.includes('glifosato')) return 'Glifosato';
    if (nombreLower.includes('atrazina')) return 'Atrazina';
    if (nombreLower.includes('2,4-d')) return '2,4-D';
    if (nombreLower.includes('dicamba')) return 'Dicamba';
    if (nombreLower.includes('mancozeb')) return 'Mancozeb';
    if (nombreLower.includes('imidacloprid')) return 'Imidacloprid';
    if (nombreLower.includes('urea')) return 'Urea';
    
    return '';
  };

  // Detector automático de concentración
  const detectarConcentracion = (nombre: string): string => {
    const match = nombre.match(/(\d+(?:\.\d+)?)\s*%/);
    return match ? `${match[1]}%` : '';
  };

  // Manejar cambio de nombre (detección automática)
  const handleNombreChange = (nombre: string) => {
    setDatos(prev => ({ ...prev, nombre }));
    
    // Solo hacer detección automática si no estamos editando
    if (!insumoEditando && nombre.length > 2) {
      const esAgroquimico = detectarSiEsAgroquimico(nombre);
      const tipoDetectado = detectarTipoAgroquimico(nombre);
      
      if (esAgroquimico && tipoDetectado) {
        // Sugerir automáticamente que es agroquímico
        setTipoInsumo('agroquimico');
        setTipoDetectado(tipoDetectado);
        setDatos(prev => ({
          ...prev,
          tipo: tipoDetectado,
          principioActivo: detectarPrincipioActivo(nombre),
          concentracion: detectarConcentracion(nombre)
        }));
      }
    } else if (tipoInsumo === 'agroquimico' && nombre.length > 2) {
      // Si ya está seleccionado como agroquímico, actualizar datos
      const tipoDetectado = detectarTipoAgroquimico(nombre);
      if (tipoDetectado) {
        setTipoDetectado(tipoDetectado);
        setDatos(prev => ({
          ...prev,
          tipo: tipoDetectado,
          principioActivo: detectarPrincipioActivo(nombre),
          concentracion: detectarConcentracion(nombre)
        }));
      }
    }
  };

  // Navegación del wizard
  const siguientePaso = () => {
    if (paso < 3) setPaso(paso + 1);
  };

  const anteriorPaso = () => {
    if (paso > 1) setPaso(paso - 1);
  };

  // Funciones para manejar dosis
  const agregarDosis = () => {
    const nuevaDosis = {
      tipoAplicacion: 'PRE_EMERGENCIA',
      formaAplicacion: 'TERRESTRE',
      unidad: 'L_HA',
      dosisRecomendadaPorHa: 0
    };
    
    setDatos(prev => ({
      ...prev,
      dosisPorTipo: [...prev.dosisPorTipo, nuevaDosis]
    }));
  };

  const actualizarDosis = (index: number, campo: string, valor: any) => {
    setDatos(prev => ({
      ...prev,
      dosisPorTipo: prev.dosisPorTipo.map((dosis, i) => 
        i === index ? { ...dosis, [campo]: valor } : dosis
      )
    }));
  };

  const eliminarDosis = (index: number) => {
    setDatos(prev => ({
      ...prev,
      dosisPorTipo: prev.dosisPorTipo.filter((_, i) => i !== index)
    }));
  };

  // Guardar insumo
  const guardarInsumo = async () => {
    try {
      setLoading(true);
      setError(null);

      // Validar datos requeridos
      if (!datos.nombre || !datos.tipo) {
        setError('Nombre y tipo son obligatorios');
        return;
      }

      if (tipoInsumo === 'agroquimico') {
        // Obtener la primera dosis configurada o usar valor por defecto
        const primeraDosis = datos.dosisPorTipo.length > 0 ? datos.dosisPorTipo[0] : null;
        const dosisPorHa = primeraDosis?.dosisRecomendadaPorHa || 1.0; // Valor por defecto válido
        const formaAplicacion = primeraDosis?.formaAplicacion || 'TERRESTRE';
        
        // Validar campos obligatorios
        if (!datos.principioActivo || datos.principioActivo.trim() === '') {
          setError('El principio activo es obligatorio para agroquímicos');
          return;
        }
        
        if (!datos.fechaVencimiento) {
          setError('La fecha de vencimiento es obligatoria para agroquímicos');
          return;
        }
        
        // Calcular fecha de vencimiento por defecto si no está definida
        const fechaVencimiento = datos.fechaVencimiento || 
          new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // +1 año
        
        // Crear como insumo
        const response = await insumoWizardService.crearInsumo({
          nombre: datos.nombre || 'Sin nombre',
          descripcion: datos.descripcion || '',
          tipo: datos.tipo || 'OTROS',
          unidadMedida: datos.unidadMedida || 'LTS',
          precioUnitario: datos.precioUnitario || 0,
          stockActual: datos.stockActual || 0,
          stockMinimo: datos.stockMinimo || 0,
          proveedor: datos.proveedor || '',
          fechaVencimiento: fechaVencimiento,
          activo: true,
          // Campos específicos de agroquímicos
          principioActivo: datos.principioActivo || null,
          concentracion: datos.concentracion || null,
          claseQuimica: datos.claseQuimica || null,
          categoriaToxicologica: datos.categoriaToxicologica || null,
          periodoCarenciaDias: datos.periodoCarenciaDias || null,
          dosisMinimaPorHa: datos.dosisMinimaPorHa || null,
          dosisMaximaPorHa: datos.dosisMaximaPorHa || null,
          unidadDosis: datos.unidadDosis || null
        });
        
        // Si es un agroquímico y se creó exitosamente, guardar las dosis configuradas
        const tiposAgroquimicos = ['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA'];
        if (response && response.id && tiposAgroquimicos.includes(datos.tipo) && datos.dosisPorTipo.length > 0) {
          for (const dosis of datos.dosisPorTipo) {
            await insumoWizardService.crearDosisAgroquimico(response.id, {
              tipoAplicacion: dosis.tipoAplicacion,
              formaAplicacion: dosis.formaAplicacion,
              unidad: dosis.unidad,
              dosisRecomendadaPorHa: dosis.dosisRecomendadaPorHa
            });
          }
        }
        
        if (response) {
          alert('Insumo creado exitosamente');
          onGuardar();
          onClose();
        }
      } else {
        // Crear como insumo general
        const response = await insumoWizardService.crearInsumo({
          nombre: datos.nombre,
          descripcion: datos.descripcion,
          tipo: datos.tipo,
          unidadMedida: datos.unidadMedida,
          precioUnitario: datos.precioUnitario,
          stockActual: datos.stockActual,
          stockMinimo: datos.stockMinimo,
          proveedor: datos.proveedor,
          fechaVencimiento: datos.fechaVencimiento,
          activo: true,
          empresa_id: empresa?.id,
          user_id: user?.id
        });
        
        if (response) {
          alert('Insumo creado exitosamente');
          onGuardar();
          onClose();
        }
      }
    } catch (err) {
      setError('Error al guardar el insumo: ' + (err as Error).message);
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Actualizar insumo
  const actualizarInsumo = async () => {
    try {
      setLoading(true);
      setError(null);

      // Validar datos requeridos
      if (!datos.nombre || !datos.tipo) {
        setError('Nombre y tipo son obligatorios');
        return;
      }

      if (tipoInsumo === 'agroquimico') {
        // Obtener la primera dosis configurada o usar valor por defecto
        const primeraDosis = datos.dosisPorTipo.length > 0 ? datos.dosisPorTipo[0] : null;
        const dosisPorHa = primeraDosis?.dosisRecomendadaPorHa || 1.0; // Valor por defecto válido
        const formaAplicacion = primeraDosis?.formaAplicacion || 'TERRESTRE';
        
        // Validar campos obligatorios
        if (!datos.principioActivo || datos.principioActivo.trim() === '') {
          setError('El principio activo es obligatorio para agroquímicos');
          return;
        }
        
        if (!datos.fechaVencimiento) {
          setError('La fecha de vencimiento es obligatoria para agroquímicos');
          return;
        }
        
        // Calcular fecha de vencimiento por defecto si no está definida
        const fechaVencimiento = datos.fechaVencimiento || 
          new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; // +1 año
        
        // Preparar datos para envío según el tipo de insumo
        const datosEnvio = {
          nombre: datos.nombre || 'Sin nombre',
          descripcion: datos.descripcion || '',
          tipo: datos.tipo || 'OTROS',
          unidadMedida: datos.unidadMedida || 'KG',
          precioUnitario: datos.precioUnitario !== undefined ? datos.precioUnitario : 0,
          stockActual: datos.stockActual !== undefined ? datos.stockActual : 0,
          stockMinimo: datos.stockMinimo !== undefined ? datos.stockMinimo : 0,
          proveedor: datos.proveedor || '',
          fechaVencimiento: fechaVencimiento,
          activo: true,
          // Campos específicos de agroquímicos
          principioActivo: datos.principioActivo || null,
          concentracion: datos.concentracion || null,
          claseQuimica: datos.claseQuimica || null,
          categoriaToxicologica: datos.categoriaToxicologica || null,
          periodoCarenciaDias: datos.periodoCarenciaDias !== undefined ? datos.periodoCarenciaDias : null,
          dosisMinimaPorHa: datos.dosisMinimaPorHa !== undefined ? datos.dosisMinimaPorHa : null,
          dosisMaximaPorHa: datos.dosisMaximaPorHa !== undefined ? datos.dosisMaximaPorHa : null,
          unidadDosis: datos.unidadDosis || null
        };
        
        // Log para depuración
        console.log('🔧 [InsumoWizard] Datos a enviar:', datosEnvio);
        console.log('🔧 [InsumoWizard] Datos originales:', datos);
        
        // Actualizar insumo
        const response = await insumoWizardService.actualizarInsumo(insumoEditando.id, datosEnvio);
        
        // Si es un agroquímico y se actualizó exitosamente, actualizar las dosis configuradas
        const tiposAgroquimicos = ['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA'];
        if (response && tiposAgroquimicos.includes(datos.tipo) && datos.dosisPorTipo.length > 0) {
          // Primero eliminar dosis existentes
          await insumoWizardService.eliminarDosisAgroquimico(insumoEditando.id);
          
          // Luego crear las nuevas dosis
          for (const dosis of datos.dosisPorTipo) {
            await insumoWizardService.crearDosisAgroquimico(insumoEditando.id, {
              tipoAplicacion: dosis.tipoAplicacion,
              formaAplicacion: dosis.formaAplicacion,
              unidad: dosis.unidad,
              dosisRecomendadaPorHa: dosis.dosisRecomendadaPorHa
            });
          }
        }
        
        if (response) {
          alert('Insumo actualizado exitosamente');
          onGuardar();
          onClose();
        }
      } else {
        // Actualizar insumo general
        const response = await insumoWizardService.actualizarInsumo(insumoEditando.id, {
          nombre: datos.nombre || 'Sin nombre',
          descripcion: datos.descripcion || '',
          tipo: datos.tipo || 'OTROS',
          unidadMedida: datos.unidadMedida || 'KG',
          precioUnitario: datos.precioUnitario !== undefined ? datos.precioUnitario : 0,
          stockActual: datos.stockActual !== undefined ? datos.stockActual : 0,
          stockMinimo: datos.stockMinimo !== undefined ? datos.stockMinimo : 0,
          proveedor: datos.proveedor || '',
          fechaVencimiento: datos.fechaVencimiento || null,
          activo: true,
          // Campos de agroquímicos (pueden ser null si no aplican)
          principioActivo: null,
          concentracion: null,
          claseQuimica: null,
          categoriaToxicologica: null,
          periodoCarenciaDias: null,
          dosisMinimaPorHa: null,
          dosisMaximaPorHa: null,
          unidadDosis: null
        });
        
        if (response) {
          alert('Insumo actualizado exitosamente');
          onGuardar();
          onClose();
        }
      }
    } catch (err) {
      setError('Error al actualizar el insumo: ' + (err as Error).message);
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Eliminación lógica
  const eliminarInsumo = async () => {
    if (!insumoEditando) return;
    
    if (window.confirm('¿Está seguro de que desea eliminar este insumo?')) {
      try {
        setLoading(true);
        setError(null);
        
        // Eliminar insumo (todos los tipos usan el mismo endpoint)
        await insumoWizardService.eliminarInsumo(insumoEditando.id);
        
        alert('Insumo eliminado exitosamente');
        onGuardar();
        onClose();
      } catch (err) {
        setError('Error al eliminar el insumo: ' + (err as Error).message);
        console.error('Error:', err);
      } finally {
        setLoading(false);
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-3">
      <div className="bg-white rounded-lg shadow-xl max-w-3xl w-full max-h-[82vh] overflow-y-auto">
        {/* Header */}
        <div className="bg-gray-100 text-gray-900 p-3 rounded-t-lg border-b border-gray-200">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-xl font-bold">
                {insumoEditando ? 'Editar Insumo' : 'Nuevo Insumo'}
              </h2>
              <p className="text-gray-600 text-xs">
                {insumoEditando ? 'Modifica los datos del insumo' : 'Wizard inteligente para carga de insumos'}
              </p>
            </div>
            <button
              onClick={onClose}
              className="text-gray-700 hover:text-gray-900 text-xl"
            >
              ×
            </button>
          </div>
          
          {/* Progress Bar */}
          <div className="mt-4">
            <div className="flex items-center space-x-2">
              {[1, 2, 3].map((step) => (
                <div key={step} className="flex items-center">
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                    paso >= step ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-700'
                  }`}>
                    {step}
                  </div>
                  {step < 3 && (
                    <div className={`w-6 h-1 mx-2 ${
                      paso > step ? 'bg-blue-600' : 'bg-gray-300'
                    }`} />
                  )}
                </div>
              ))}
            </div>
            <div className="flex justify-between text-xs mt-1 text-gray-600">
              <span>Selección</span>
              <span>Configuración</span>
              <span>Finalización</span>
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="p-4 space-y-3">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          {/* Paso 1: Selección de Tipo */}
          {paso === 1 && (
            <div className="space-y-4">
              <h3 className="text-xl font-semibold text-gray-800 mb-4">
                {insumoEditando ? 'Tipo de Insumo Detectado' : '¿Qué tipo de insumo desea cargar?'}
              </h3>
              
              
              {tipoInsumo && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-4">
                  <div className="flex items-center">
                    <span className="text-green-500 text-xl mr-3">✅</span>
                    <div>
                      <h4 className="font-semibold text-green-800">Tipo Seleccionado</h4>
                      <p className="text-green-700 text-sm">
                        {tipoInsumo === 'general' ? 'Insumo General' : 'Agroquímico'}
                        {tipoDetectado && ` - ${tipoDetectado}`}
                      </p>
                      {!insumoEditando && tipoDetectado && (
                        <p className="text-green-600 text-xs mt-1">
                          🤖 Detectado automáticamente por el nombre del producto
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              )}
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
                <div 
                  className={`p-4 sm:p-6 border-2 rounded-lg transition-all ${
                    insumoEditando && tipoInsumo !== 'general'
                      ? 'border-gray-200 bg-gray-100 cursor-not-allowed opacity-50'
                      : tipoInsumo === 'general' 
                        ? 'border-blue-500 bg-blue-50 cursor-pointer' 
                        : 'border-gray-200 hover:border-gray-300 cursor-pointer'
                  }`}
                  onClick={() => !insumoEditando || tipoInsumo === 'general' ? setTipoInsumo('general') : null}
                >
                  <div className="text-center">
                    <div className="text-4xl mb-3">📦</div>
                    <h4 className="text-lg font-semibold mb-2">Insumo General</h4>
                    <p className="text-gray-600 text-sm">
                      Fertilizantes, semillas, herramientas, etc.
                    </p>
                    {insumoEditando && tipoInsumo !== 'general' && (
                      <p className="text-red-500 text-xs mt-2 font-medium">
                        🔒 Bloqueado - Este insumo es un agroquímico
                      </p>
                    )}
                  </div>
                </div>

                <div 
                  className={`p-4 sm:p-6 border-2 rounded-lg transition-all ${
                    insumoEditando && tipoInsumo !== 'agroquimico'
                      ? 'border-gray-200 bg-gray-100 cursor-not-allowed opacity-50'
                      : tipoInsumo === 'agroquimico' 
                        ? 'border-purple-500 bg-purple-50 cursor-pointer' 
                        : 'border-gray-200 hover:border-gray-300 cursor-pointer'
                  }`}
                  onClick={() => !insumoEditando || tipoInsumo === 'agroquimico' ? setTipoInsumo('agroquimico') : null}
                >
                  <div className="text-center">
                    <div className="text-4xl mb-3">🧪</div>
                    <h4 className="text-lg font-semibold mb-2">Agroquímico</h4>
                    <p className="text-gray-600 text-sm">
                      Herbicidas, fungicidas, insecticidas, etc.
                    </p>
                    {insumoEditando && tipoInsumo !== 'agroquimico' && (
                      <p className="text-red-500 text-xs mt-2 font-medium">
                        🔒 Bloqueado - Este insumo es general
                      </p>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Paso 2: Configuración Básica */}
          {paso === 2 && (
            <div className="space-y-4">
              <h3 className="text-xl font-semibold text-gray-800 mb-4">
                {tipoInsumo === 'agroquimico' ? 'Configuración del Agroquímico' : 'Configuración del Insumo'}
              </h3>


              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre del Producto <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={datos.nombre}
                    onChange={(e) => handleNombreChange(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Ej: Glifosato 48%"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tipo <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={datos.tipo}
                    onChange={(e) => setDatos(prev => ({ ...prev, tipo: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    {tipoInsumo === 'agroquimico' ? (
                      tiposAgroquimicos.map((tipo) => (
                        <option key={tipo.value} value={tipo.value}>
                          {tipo.label}
                        </option>
                      ))
                    ) : (
                      <>
                        <option value="FERTILIZANTE">Fertilizante</option>
                        <option value="SEMILLA">Semilla</option>
                        <option value="HERRAMIENTA">Herramienta</option>
                        <option value="OTROS">Otros</option>
                      </>
                    )}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Descripción
                  </label>
                  <textarea
                    value={datos.descripcion}
                    onChange={(e) => setDatos(prev => ({ ...prev, descripcion: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    rows={3}
                    placeholder="Descripción del producto..."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Proveedor
                  </label>
                  <input
                    type="text"
                    value={datos.proveedor}
                    onChange={(e) => setDatos(prev => ({ ...prev, proveedor: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Nombre del proveedor"
                  />
                </div>
              </div>

              {/* Campos específicos para agroquímicos */}
              {tipoInsumo === 'agroquimico' && (
                <div className="mt-6 p-4 bg-purple-50 border border-purple-200 rounded-lg">
                  <h4 className="font-semibold text-purple-800 mb-4">Propiedades del Agroquímico</h4>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Principio Activo
                      </label>
                      <input
                        type="text"
                        value={datos.principioActivo}
                        onChange={(e) => setDatos(prev => ({ ...prev, principioActivo: e.target.value }))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                        placeholder="Ej: Glifosato"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Concentración
                      </label>
                      <input
                        type="text"
                        value={datos.concentracion}
                        onChange={(e) => setDatos(prev => ({ ...prev, concentracion: e.target.value }))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                        placeholder="Ej: 48%"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Clase Química *
                      </label>
                      <select
                        value={datos.claseQuimica}
                        onChange={(e) => setDatos(prev => ({ ...prev, claseQuimica: e.target.value }))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                        required
                      >
                        <option value="">Seleccione una clase química</option>
                        {clasesQuimicas.map((clase) => (
                          <option key={clase} value={clase}>
                            {clase}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Categoría Toxicológica
                      </label>
                      <select
                        value={datos.categoriaToxicologica}
                        onChange={(e) => setDatos(prev => ({ ...prev, categoriaToxicologica: e.target.value }))}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                      >
                        <option value="">Seleccione una categoría</option>
                        {categoriasToxicologicas.map((categoria) => (
                          <option key={categoria.value} value={categoria.value}>
                            {categoria.label}
                          </option>
                        ))}
                      </select>
                    </div>

                  </div>
                </div>
              )}
            </div>
          )}

          {/* Paso 3: Configuración Final */}
          {paso === 3 && (
            <div className="space-y-3">
              <h3 className="text-xl font-semibold text-gray-800 mb-4">
                Configuración Final
              </h3>

              {/* Configuración de Dosis para Agroquímicos */}
              {tipoInsumo === 'agroquimico' && (
                <div className="bg-purple-50 border border-purple-200 rounded-lg p-2">
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="text-base font-semibold text-purple-800">
                      🧪 Configuración de Dosis por Tipo de Aplicación
                    </h4>
                    <button
                      type="button"
                      onClick={agregarDosis}
                      className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1 rounded text-sm"
                    >
                      ➕ Agregar Dosis
                    </button>
                  </div>
                  
                  {datos.dosisPorTipo.length === 0 ? (
                    <p className="text-purple-600 text-sm">
                      No hay dosis configuradas. Agregue al menos una dosis para este agroquímico.
                    </p>
                  ) : (
                    <div className="space-y-3">
                      {datos.dosisPorTipo.map((dosis, index) => (
                        <div key={index} className="bg-white border border-purple-200 rounded-lg p-2">
                          <div className="flex items-center justify-between mb-2">
                            <h5 className="font-medium text-gray-800">
                              Dosis {index + 1}
                            </h5>
                            <button
                              type="button"
                              onClick={() => eliminarDosis(index)}
                              className="text-red-500 hover:text-red-700 text-sm"
                            >
                              🗑️ Eliminar
                            </button>
                          </div>
                          
                          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
                            <div>
                              <label className="block text-xs font-medium text-gray-700 mb-1">
                                Tipo de Aplicación
                              </label>
                              <select
                                value={dosis.tipoAplicacion}
                                onChange={(e) => actualizarDosis(index, 'tipoAplicacion', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                              >
                                {tiposAplicacion.map((tipo) => (
                                  <option key={tipo.value} value={tipo.value}>
                                    {tipo.label}
                                  </option>
                                ))}
                              </select>
                            </div>
                            
                            <div>
                              <label className="block text-xs font-medium text-gray-700 mb-1">
                                Forma de Aplicación
                              </label>
                              <select
                                value={dosis.formaAplicacion}
                                onChange={(e) => actualizarDosis(index, 'formaAplicacion', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                              >
                                {formasAplicacion.map((forma) => (
                                  <option key={forma.value} value={forma.value}>
                                    {forma.label}
                                  </option>
                                ))}
                              </select>
                            </div>
                            
                            <div>
                              <label className="block text-xs font-medium text-gray-700 mb-1">
                                Unidad
                              </label>
                              <select
                                value={dosis.unidad}
                                onChange={(e) => actualizarDosis(index, 'unidad', e.target.value)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                              >
                                {unidadesDosis.map((unidad) => (
                                  <option key={unidad.value} value={unidad.value}>
                                    {unidad.label}
                                  </option>
                                ))}
                              </select>
                            </div>
                            
                            <div>
                              <label className="block text-xs font-medium text-gray-700 mb-1">
                                Dosis Recomendada por Ha
                              </label>
                              <input
                                type="number"
                                step="0.01"
                                value={dosis.dosisRecomendadaPorHa}
                                onChange={(e) => actualizarDosis(index, 'dosisRecomendadaPorHa', parseFloat(e.target.value) || 0)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
                                placeholder="Ej: 2.5"
                              />
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Unidad de Medida <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={datos.unidadMedida}
                    onChange={(e) => setDatos(prev => ({ ...prev, unidadMedida: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">Seleccionar unidad</option>
                    <option value="KG">Kilogramos (KG)</option>
                    <option value="LTS">Litros (LTS)</option>
                    <option value="MT">Metros (MT)</option>
                    <option value="UNIDAD">Unidad</option>
                    <option value="TON">Toneladas (TON)</option>
                    <option value="HA">Hectáreas (HA)</option>
                    <option value="BOLSA">Bolsas</option>
                    <option value="CAJA">Cajas</option>
                    <option value="ROLLO">Rollos</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Precio Unitario ($)
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={datos.precioUnitario}
                    onChange={(e) => setDatos(prev => ({ ...prev, precioUnitario: parseFloat(e.target.value) || 0 }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Stock Actual
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={datos.stockActual}
                    onChange={(e) => setDatos(prev => ({ ...prev, stockActual: parseFloat(e.target.value) || 0 }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Stock Mínimo
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={datos.stockMinimo}
                    onChange={(e) => setDatos(prev => ({ ...prev, stockMinimo: parseFloat(e.target.value) || 0 }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Fecha de Vencimiento
                  </label>
                  <input
                    type="date"
                    value={datos.fechaVencimiento}
                    onChange={(e) => setDatos(prev => ({ ...prev, fechaVencimiento: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>

              {/* Resumen */}
              <div className="bg-gray-50 border border-gray-200 rounded-lg p-2">
                <h4 className="font-semibold text-gray-800 mb-2">Resumen del Insumo</h4>
                <div className="text-sm text-gray-600 space-y-1">
                  <p><strong>Nombre:</strong> {datos.nombre}</p>
                  <p><strong>Tipo:</strong> {datos.tipo}</p>
                  <p><strong>Unidad:</strong> {datos.unidadMedida}</p>
                  <p><strong>Stock:</strong> {datos.stockActual} {datos.unidadMedida}</p>
                  {tipoInsumo === 'agroquimico' && datos.principioActivo && (
                    <p><strong>Principio Activo:</strong> {datos.principioActivo}</p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Botones de Navegación */}
          <div className="flex justify-between mt-4 pt-3 border-t border-gray-200">
            <div>
              {insumoEditando && (
                <button
                  onClick={eliminarInsumo}
                  className="bg-red-500 text-white px-3 py-2 rounded-md hover:bg-red-600 transition-colors"
                  disabled={loading}
                >
                  Eliminar
                </button>
              )}
            </div>
            
            <div className="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-3">
              {paso > 1 && (
                <button
                  onClick={anteriorPaso}
                  className="px-3 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  Anterior
                </button>
              )}
              
              {paso < 3 ? (
                <div className="flex flex-col items-end">
                  {paso === 1 && !tipoInsumo && (
                    <p className="text-sm text-red-500 mb-2">Seleccione un tipo de insumo</p>
                  )}
                  {paso === 2 && (!datos.nombre || !datos.tipo) && (
                    <p className="text-sm text-red-500 mb-2">
                      Complete los campos obligatorios: {!datos.nombre && 'Nombre'} {!datos.nombre && !datos.tipo && ', '} {!datos.tipo && 'Tipo'}
                    </p>
                  )}
                  <button
                    onClick={siguientePaso}
                    disabled={
                      (paso === 1 && !tipoInsumo) ||
                      (paso === 2 && (!datos.nombre || !datos.tipo))
                    }
                    className="px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                  >
                    Siguiente
                  </button>
                </div>
              ) : (
                <div className="flex flex-col items-end">
                  {tipoInsumo === 'agroquimico' && datos.dosisPorTipo.length === 0 && (
                    <p className="text-sm text-red-500 mb-2">
                      ⚠️ Los agroquímicos requieren al menos una dosis configurada
                    </p>
                  )}
                  <button
                    onClick={insumoEditando ? actualizarInsumo : guardarInsumo}
                    disabled={
                      loading || 
                      !datos.nombre || 
                      !datos.tipo || 
                      !datos.unidadMedida ||
                      (tipoInsumo === 'agroquimico' && datos.dosisPorTipo.length === 0)
                    }
                    className="px-3 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Guardando...' : (insumoEditando ? 'Actualizar' : 'Crear Insumo')}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InsumoWizard;
