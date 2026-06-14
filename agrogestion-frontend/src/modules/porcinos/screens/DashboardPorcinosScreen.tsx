import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { madresService } from '../services/madresService';
import { gestacionService } from '../services/gestacionService';
import { partosService } from '../services/partosService';
import { recriaService } from '../services/recriaService';
import { ventaPorcinoService } from '../services/ventaPorcinoService';
import { consumoAlimentoService } from '../services/consumoAlimentoService';
import type { Madre, Gestacion, Parto, Recria } from '../types';
import { Icon, SemanticIcon } from '../../../components/icons';

interface KPIs {
  totalMadres: number;
  madresEnCelo: number;
  gestacionesActivas: number;
  partosProximos: number;
  lechonesDestetados: number;
  animalesEnRecria: number;
  mortalidadMadres: number;
  mortalidadLechones: number;
  mortalidadRecria: number;
  ingresosTotales: number;
  consumoTotalAlimento: number;
  lechonesPorMadreAnual: number;
  porcentajePrenez: number;
  conversionAlimenticia: number;
}

const DashboardPorcinosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [kpis, setKpis] = useState<KPIs>({
    totalMadres: 0,
    madresEnCelo: 0,
    gestacionesActivas: 0,
    partosProximos: 0,
    lechonesDestetados: 0,
    animalesEnRecria: 0,
    mortalidadMadres: 0,
    mortalidadLechones: 0,
    mortalidadRecria: 0,
    ingresosTotales: 0,
    consumoTotalAlimento: 0,
    lechonesPorMadreAnual: 0,
    porcentajePrenez: 0,
    conversionAlimenticia: 0,
  });
  const [alertas, setAlertas] = useState<string[]>([]);

  useEffect(() => {
    cargarKPIs();
  }, []);

  const cargarKPIs = async () => {
    setLoading(true);
    try {
      const [
        madres,
        gestaciones,
        partosProximos,
        partos,
        recrias,
        ventas,
        consumos,
      ] = await Promise.all([
        madresService.listar(),
        gestacionService.obtenerActivas(),
        gestacionService.obtenerProximosPartos(7),
        partosService.listar(),
        recriaService.listar(),
        ventaPorcinoService.obtenerIngresosTotales(),
        consumoAlimentoService.listar(),
      ]);

      // Calcular KPIs
      const totalMadres = madres.filter((m: Madre) => m.activo !== false).length;
      const madresEnCelo = madres.filter((m: Madre) => m.estadoActual === 'ADULTA').length;
      const gestacionesActivas = gestaciones.length;
      const partosProximosCount = partosProximos.length;
      
      // Lechones destetados (sumar de partos que tienen destete)
      const lechonesDestetados = partos.reduce((sum: number, p: Parto) => {
        // Asumimos que si hay nacidos vivos, eventualmente se destetan
        // En producción, esto debería venir del backend con datos de destetes
        return sum + (p.nacidosVivos || 0);
      }, 0);

      const animalesEnRecria = recrias.reduce((sum: number, r: Recria) => sum + (r.cantidadAnimales || 0), 0);
      
      // Mortalidad (simplificado - en producción debería venir del backend)
      const mortalidadMadres = 0; // Calcular desde muertes de madres
      const mortalidadLechones = partos.reduce((sum: number, p: Parto) => sum + (p.nacidosMuertos || 0), 0);
      const mortalidadRecria = 0; // Calcular desde muertes en recría

      const ingresosTotales = ventas?.ingresosTotales || 0;
      const consumoTotalAlimento = consumos.reduce((sum: number, c: { cantidadKg?: number }) => sum + (c.cantidadKg || 0), 0);

      // Lechones por madre anual (simplificado)
      const lechonesPorMadreAnual = totalMadres > 0 ? (lechonesDestetados / totalMadres) * 2.5 : 0; // Asumiendo 2.5 partos por año

      // Porcentaje de preñez (simplificado)
      const porcentajePrenez = totalMadres > 0 ? (gestacionesActivas / totalMadres) * 100 : 0;

      // Conversión alimenticia (simplificado - kg alimento / kg ganancia de peso)
      const conversionAlimenticia = 0; // Requiere datos más detallados

      setKpis({
        totalMadres,
        madresEnCelo,
        gestacionesActivas,
        partosProximos: partosProximosCount,
        lechonesDestetados,
        animalesEnRecria,
        mortalidadMadres,
        mortalidadLechones,
        mortalidadRecria,
        ingresosTotales,
        consumoTotalAlimento,
        lechonesPorMadreAnual,
        porcentajePrenez,
        conversionAlimenticia,
      });

      // Generar alertas
      const nuevasAlertas: string[] = [];
      if (partosProximosCount > 0) {
        nuevasAlertas.push(`Hay ${partosProximosCount} partos próximos en los próximos 7 días`);
      }
      if (madresEnCelo > 5) {
        nuevasAlertas.push(`👀 Hay ${madresEnCelo} madres en celo que requieren servicio`);
      }
      if (mortalidadLechones > 10) {
        nuevasAlertas.push(`Mortalidad de lechones alta: ${mortalidadLechones} muertos`);
      }
      setAlertas(nuevasAlertas);
    } catch (error) {
      console.error('Error al cargar KPIs:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatearMoneda = (valor: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
    }).format(valor);
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando dashboard...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <Icon name="BarChart" size={32} />
        Dashboard Porcinos
      </h1>

      {/* Alertas */}
      {alertas.length > 0 && (
        <div style={{
          backgroundColor: '#fef3c7',
          border: '1px solid #fde68a',
          borderRadius: '0.5rem',
          padding: '1rem',
          marginBottom: '2rem'
        }}>
          <h2 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.5rem', color: '#92400e', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <SemanticIcon semanticName="warning" size={20} />
            Alertas
          </h2>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {alertas.map((alerta, index) => (
              <li key={index} style={{ fontSize: '0.875rem', color: '#78350f', marginBottom: '0.25rem' }}>
                {alerta}
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* KPIs Principales */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
        gap: '1.5rem',
        marginBottom: '2rem'
      }}>
        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          borderLeft: '4px solid #3b82f6'
        }}>
          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Total Madres</p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>{kpis.totalMadres}</p>
        </div>

        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          borderLeft: '4px solid #8b5cf6'
        }}>
          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Gestaciones Activas</p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>{kpis.gestacionesActivas}</p>
        </div>

        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          borderLeft: '4px solid #f59e0b'
        }}>
          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Partos Próximos (7 días)</p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>{kpis.partosProximos}</p>
        </div>

        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          borderLeft: '4px solid #10b981'
        }}>
          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Lechones Destetados</p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>{kpis.lechonesDestetados}</p>
        </div>

        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          borderLeft: '4px solid #06b6d4'
        }}>
          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Animales en Recría</p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>{kpis.animalesEnRecria}</p>
        </div>

        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          borderLeft: '4px solid #10b981'
        }}>
          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Ingresos Totales</p>
          <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
            {formatearMoneda(kpis.ingresosTotales)}
          </p>
        </div>
      </div>

      {/* Indicadores Reproductivos */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '2rem'
      }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="TrendingUp" size={24} />
          Indicadores Reproductivos
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem' }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Lechones/Madre/Año</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
              {kpis.lechonesPorMadreAnual.toFixed(1)}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>% Preñez</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
              {kpis.porcentajePrenez.toFixed(1)}%
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Madres en Celo</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#8b5cf6' }}>{kpis.madresEnCelo}</p>
          </div>
        </div>
      </div>

      {/* Mortalidad */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '2rem'
      }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <SemanticIcon semanticName="warning" size={24} />
          Mortalidad
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem' }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Mortalidad Madres</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>{kpis.mortalidadMadres}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Mortalidad Lechones</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>{kpis.mortalidadLechones}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Mortalidad Recría</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>{kpis.mortalidadRecria}</p>
          </div>
        </div>
      </div>

      {/* Alimentación */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '2rem'
      }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Wheat" size={24} />
          Alimentación
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem' }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Consumo Total (kg)</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
              {kpis.consumoTotalAlimento.toFixed(2)}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Conversión Alimenticia</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
              {kpis.conversionAlimenticia > 0 ? kpis.conversionAlimenticia.toFixed(2) : '-'}
            </p>
          </div>
        </div>
      </div>

      {/* Accesos Rápidos */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Zap" size={24} />
          Accesos Rápidos
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <button
            onClick={() => navigate('/porcinos/madres')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="PiggyBank" size={20} style={{ marginRight: '0.5rem' }} /> Gestión de Madres
          </button>
          <button
            onClick={() => navigate('/porcinos/servicios')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="Heart" size={20} style={{ marginRight: '0.5rem' }} /> Servicios
          </button>
          <button
            onClick={() => navigate('/porcinos/gestacion')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="Baby" size={20} style={{ marginRight: '0.5rem' }} /> Gestación
          </button>
          <button
            onClick={() => navigate('/porcinos/partos')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="Baby" size={20} style={{ marginRight: '0.5rem' }} /> Partos
          </button>
          <button
            onClick={() => navigate('/porcinos/recria')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="Circle" size={20} style={{ marginRight: '0.5rem' }} /> Recría
          </button>
          <button
            onClick={() => navigate('/porcinos/alimentacion/consumos')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="Wheat" size={20} style={{ marginRight: '0.5rem' }} /> Consumos
          </button>
          <button
            onClick={() => navigate('/porcinos/alimentacion/calendario')}
            style={{
              padding: '1rem',
              backgroundColor: '#f9fafb',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              textAlign: 'left',
              fontSize: '0.875rem',
              fontWeight: '500',
              color: '#1f2937'
            }}
          >
            <Icon name="Calendar" size={20} style={{ marginRight: '0.5rem' }} /> Calendario de alimentación
          </button>
        </div>
      </div>
    </div>
  );
};

export default DashboardPorcinosScreen;
