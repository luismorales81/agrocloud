/**
 * Modal de detalle de costos de una labor.
 * Extraído de LaboresManagement para reducir tamaño del componente principal.
 */

import React from 'react';
import { Icon } from '../../core/components/Icon';
import type { Labor, LaborMaquinaria, LaborManoObra, InsumoUsado } from '../../types/labores.types';

export interface LaborDetalleCostosModalProps {
  labor: Labor;
  onClose: () => void;
  formatCurrency: (valor: number) => string;
}

export const LaborDetalleCostosModal: React.FC<LaborDetalleCostosModalProps> = ({
  labor,
  onClose,
  formatCurrency,
}) => {
  const costoInsumos =
    labor.insumos_usados && labor.insumos_usados.length > 0
      ? labor.insumos_usados.reduce((sum: number, ins: InsumoUsado) => {
          const costo =
            ins.costoTotal ||
            ins.costo_total ||
            (ins.costoUnitario || ins.costo_unitario || 0) *
              (ins.cantidadUsada || ins.cantidad_usada || ins.cantidad || 0);
          return sum + (costo || 0);
        }, 0)
      : (labor.costo_insumos || 0);

  const costoMaquinaria = labor.costo_maquinaria || 0;
  const costoManoObra = labor.costo_mano_obra || 0;
  const total = costoInsumos + costoMaquinaria + costoManoObra;

  return (
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
    >
      <div
        style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          padding: '20px',
          maxWidth: '800px',
          width: '90%',
          maxHeight: '80vh',
          overflowY: 'auto',
        }}
      >
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '20px',
            borderBottom: '1px solid #e5e7eb',
            paddingBottom: '10px',
          }}
        >
          <h2 style={{ margin: 0, color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Icon name="DollarSign" size={20} /> Detalles de Costos - {labor.tipo}
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#6b7280',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Icon name="X" size={20} />
          </button>
        </div>

        {/* Resumen de Costos */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '15px',
            marginBottom: '30px',
          }}
        >
          <div
            style={{
              padding: '15px',
              backgroundColor: '#e0e7ff',
              borderRadius: '8px',
              textAlign: 'center',
              border: '2px solid #6366f1',
            }}
          >
            <div
              style={{
                fontSize: '14px',
                color: '#4338ca',
                marginBottom: '5px',
                fontWeight: '600',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
              }}
            >
              <Icon name="FlaskConical" size={16} /> Insumos
            </div>
            <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#4338ca' }}>
              {formatCurrency(costoInsumos)}
            </div>
          </div>
          <div
            style={{
              padding: '15px',
              backgroundColor: '#dbeafe',
              borderRadius: '8px',
              textAlign: 'center',
              border: '2px solid #3b82f6',
            }}
          >
            <div
              style={{
                fontSize: '14px',
                color: '#1d4ed8',
                marginBottom: '5px',
                fontWeight: '600',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
              }}
            >
              <Icon name="Wrench" size={16} /> Maquinaria
            </div>
            <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#1d4ed8' }}>
              {formatCurrency(costoMaquinaria)}
            </div>
          </div>
          <div
            style={{
              padding: '15px',
              backgroundColor: '#fef3c7',
              borderRadius: '8px',
              textAlign: 'center',
              border: '2px solid #f59e0b',
            }}
          >
            <div
              style={{
                fontSize: '14px',
                color: '#d97706',
                marginBottom: '5px',
                fontWeight: '600',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
              }}
            >
              <Icon name="Users" size={16} /> Mano de Obra
            </div>
            <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#d97706' }}>
              {formatCurrency(costoManoObra)}
            </div>
          </div>
          <div
            style={{
              padding: '15px',
              backgroundColor: '#d1fae5',
              borderRadius: '8px',
              textAlign: 'center',
              border: '2px solid #10b981',
              gridColumn: 'span 1',
            }}
          >
            <div
              style={{
                fontSize: '14px',
                color: '#059669',
                marginBottom: '5px',
                fontWeight: '600',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px',
              }}
            >
              <Icon name="DollarSign" size={16} /> Total
            </div>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#059669' }}>
              {formatCurrency(total)}
            </div>
          </div>
        </div>

        {/* Detalles de Maquinaria */}
        {labor.maquinarias && labor.maquinarias.length > 0 && (
          <div style={{ marginBottom: '30px' }}>
            <h3
              style={{
                color: '#374151',
                marginBottom: '15px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <Icon name="Wrench" size={20} /> Maquinaria Utilizada
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {labor.maquinarias.map((maq: LaborMaquinaria, index: number) => (
                <div
                  key={maq.id_labor_maquinaria || `maquinaria-${index}`}
                  style={{
                    padding: '15px',
                    backgroundColor: '#f8fafc',
                    borderRadius: '8px',
                    border: '1px solid #e2e8f0',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 'bold', color: '#374151' }}>{maq.descripcion}</div>
                      <div style={{ fontSize: '14px', color: '#6b7280' }}>
                        Tipo: {maq.tipo ?? '-'} {maq.proveedor && `- ${maq.proveedor}`}
                      </div>
                    </div>
                    <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#1d4ed8' }}>
                      {formatCurrency(maq.costo)}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Detalles de Mano de Obra */}
        {labor.mano_obra && labor.mano_obra.length > 0 && (
          <div style={{ marginBottom: '30px' }}>
            <h3
              style={{
                color: '#374151',
                marginBottom: '15px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <Icon name="Users" size={20} /> Mano de Obra
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {labor.mano_obra.map((mo: LaborManoObra, index: number) => {
                const cantidadPersonas = mo.cantidadPersonas || mo.cantidad_personas || 1;
                const costoTotal = mo.costoTotal || mo.costo_total || 0;
                const horasTrabajo = mo.horasTrabajo || mo.horas_trabajo;
                return (
                  <div
                    key={mo.id_labor_mano_obra || (mo as any).idLaborManoObra || `mano-obra-detail-${index}`}
                    style={{
                      padding: '15px',
                      backgroundColor: '#f8fafc',
                      borderRadius: '8px',
                      border: '1px solid #e2e8f0',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 'bold', color: '#374151', marginBottom: '4px' }}>
                          {mo.descripcion}
                        </div>
                        <div
                          style={{
                            fontSize: '14px',
                            color: '#6b7280',
                            marginBottom: '2px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                          }}
                        >
                          <Icon name="Users" size={14} /> Personas: <strong>{cantidadPersonas}</strong>
                        </div>
                        {mo.proveedor && (
                          <div
                            style={{
                              fontSize: '14px',
                              color: '#6b7280',
                              marginBottom: '2px',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px',
                            }}
                          >
                            <Icon name="Building" size={14} /> Proveedor: {mo.proveedor}
                          </div>
                        )}
                        {horasTrabajo && (
                          <div
                            style={{
                              fontSize: '14px',
                              color: '#6b7280',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px',
                            }}
                          >
                            <Icon name="Clock" size={14} /> Horas: {horasTrabajo}
                          </div>
                        )}
                        {mo.observaciones && (
                          <div
                            style={{
                              fontSize: '12px',
                              color: '#9ca3af',
                              marginTop: '4px',
                              fontStyle: 'italic',
                            }}
                          >
                            {mo.observaciones}
                          </div>
                        )}
                      </div>
                      <div
                        style={{
                          fontSize: '18px',
                          fontWeight: 'bold',
                          color: '#d97706',
                          textAlign: 'right',
                          minWidth: '120px',
                        }}
                      >
                        {formatCurrency(costoTotal)}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Detalles de Insumos Utilizados */}
        {labor.insumos_usados && labor.insumos_usados.length > 0 && (
          <div style={{ marginBottom: '30px' }}>
            <h3
              style={{
                color: '#374151',
                marginBottom: '15px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <Icon name="FlaskConical" size={20} /> Insumos Utilizados
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {labor.insumos_usados.map((insumo: InsumoUsado, index: number) => {
                const cantidadUsada =
                  insumo.cantidadUsada || insumo.cantidad_usada || insumo.cantidad || 0;
                const costoUnitario =
                  insumo.costoUnitario || insumo.costo_unitario || insumo.precio_unitario || 0;
                const costoTotalInsumo =
                  insumo.costoTotal ||
                  insumo.costo_total ||
                  cantidadUsada * costoUnitario;
                const unidadMedida =
                  insumo.unidadMedida || insumo.unidad_medida || insumo.unidad || '';
                const insumoNombre =
                  insumo.insumoNombre ||
                  insumo.insumo_nombre ||
                  insumo.nombre ||
                  'Insumo sin nombre';
                return (
                  <div
                    key={
                      insumo.insumo_id ||
                      insumo.idInsumo ||
                      insumo.id ||
                      `insumo-detail-${index}`
                    }
                    style={{
                      padding: '15px',
                      backgroundColor: '#f8fafc',
                      borderRadius: '8px',
                      border: '1px solid #e2e8f0',
                    }}
                  >
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'start',
                      }}
                    >
                      <div style={{ flex: 1 }}>
                        <div
                          style={{
                            fontWeight: 'bold',
                            color: '#374151',
                            marginBottom: '4px',
                          }}
                        >
                          {insumoNombre}
                        </div>
                        <div
                          style={{
                            fontSize: '14px',
                            color: '#6b7280',
                            marginBottom: '2px',
                          }}
                        >
                          <Icon
                            name="Package"
                            size={14}
                            style={{ marginRight: '4px', verticalAlign: 'middle' }}
                          />{' '}
                          Cantidad: <strong>{cantidadUsada} {unidadMedida}</strong>
                        </div>
                        <div
                          style={{
                            fontSize: '14px',
                            color: '#6b7280',
                            marginBottom: '2px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                          }}
                        >
                          <Icon name="DollarSign" size={14} /> Precio Unitario:{' '}
                          {formatCurrency(costoUnitario)}
                        </div>
                        {insumo.observaciones && (
                          <div
                            style={{
                              fontSize: '12px',
                              color: '#9ca3af',
                              marginTop: '4px',
                              fontStyle: 'italic',
                            }}
                          >
                            {insumo.observaciones}
                          </div>
                        )}
                      </div>
                      <div
                        style={{
                          fontSize: '18px',
                          fontWeight: 'bold',
                          color: '#059669',
                          textAlign: 'right',
                          minWidth: '120px',
                        }}
                      >
                        {formatCurrency(costoTotalInsumo)}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Información de la Labor */}
        <div
          style={{
            padding: '15px',
            backgroundColor: '#f9fafb',
            borderRadius: '8px',
            border: '1px solid #e5e7eb',
          }}
        >
          <h4 style={{ color: '#374151', marginBottom: '10px' }}>Información de la Labor</h4>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '10px',
            }}
          >
            <div>
              <strong>Lote:</strong> {labor.lote_nombre}
            </div>
            <div>
              <strong>Fecha:</strong> {new Date(labor.fecha).toLocaleDateString('es-ES')}
            </div>
            <div>
              <strong>Estado:</strong> {labor.estado}
            </div>
            <div>
              <strong>Responsable:</strong> {labor.responsable}
            </div>
          </div>
          {labor.observaciones && (
            <div style={{ marginTop: '10px' }}>
              <strong>Observaciones:</strong> {labor.observaciones}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
