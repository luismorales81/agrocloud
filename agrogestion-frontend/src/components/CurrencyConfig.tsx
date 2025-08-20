import React, { useState, useEffect } from 'react';
import currencyService from '../services/CurrencyService';

interface CurrencyConfigProps {
  onCurrencyChange?: (currency: 'ARS' | 'USD') => void;
  compact?: boolean;
}

const CurrencyConfig: React.FC<CurrencyConfigProps> = ({ onCurrencyChange, compact = false }) => {
  const [currency, setCurrency] = useState<'ARS' | 'USD'>('ARS');
  const [loading, setLoading] = useState(false);
  const [rateInfo, setRateInfo] = useState<{
    rate: number;
    lastUpdate: string;
    from: string;
    to: string;
  } | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCurrencyConfig();
    loadRate();
  }, []);

  const loadCurrencyConfig = () => {
    const config = currencyService.getCurrencyConfig();
    setCurrency(config.currency);
  };

  const loadRate = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Intentar cargar desde localStorage primero
      const storedRate = currencyService.getRateInfo();
      if (storedRate) {
        setRateInfo(storedRate);
      }
      
      // Actualizar cotización desde la API
      await currencyService.updateCurrencyRates();
      const updatedRate = currencyService.getRateInfo();
      setRateInfo(updatedRate);
    } catch (error) {
      console.error('Error cargando cotización:', error);
      setError('Error al cargar cotización');
    } finally {
      setLoading(false);
    }
  };

  const handleCurrencyChange = (newCurrency: 'ARS' | 'USD') => {
    setCurrency(newCurrency);
    const config = currencyService.getCurrencyConfig();
    config.currency = newCurrency;
    currencyService.setCurrencyConfig(config);
    onCurrencyChange?.(newCurrency);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('es-AR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (compact) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        padding: '0.5rem',
        backgroundColor: '#f8fafc',
        borderRadius: '0.375rem',
        border: '1px solid #e2e8f0'
      }}>
        <span style={{ fontSize: '0.875rem', color: '#64748b' }}>Moneda:</span>
        <select
          value={currency}
          onChange={(e) => handleCurrencyChange(e.target.value as 'ARS' | 'USD')}
          style={{
            padding: '0.25rem 0.5rem',
            border: '1px solid #cbd5e1',
            borderRadius: '0.25rem',
            fontSize: '0.875rem',
            backgroundColor: 'white'
          }}
        >
          <option value="ARS">ARS (Pesos)</option>
          <option value="USD">USD (Dólares)</option>
        </select>
        
        {rateInfo && (
          <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
            <span>💵 {currencyService.formatRate(rateInfo.rate)}</span>
          </div>
        )}
        
        <button
          onClick={loadRate}
          disabled={loading}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            fontSize: '0.875rem',
            color: '#3b82f6',
            padding: '0.25rem'
          }}
          title="Actualizar cotización"
        >
          {loading ? '🔄' : '🔄'}
        </button>
      </div>
    );
  }

  return (
    <div style={{
      backgroundColor: 'white',
      padding: '1.5rem',
      borderRadius: '0.5rem',
      border: '1px solid #e5e7eb',
      boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
    }}>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1rem'
      }}>
        <h3 style={{
          fontSize: '1.125rem',
          fontWeight: '600',
          color: '#111827'
        }}>
          💱 Configuración de Moneda
        </h3>
        <button
          onClick={loadRate}
          disabled={loading}
          style={{
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            padding: '0.5rem 1rem',
            borderRadius: '0.375rem',
            cursor: loading ? 'not-allowed' : 'pointer',
            fontSize: '0.875rem',
            opacity: loading ? 0.6 : 1
          }}
        >
          {loading ? 'Actualizando...' : '🔄 Actualizar'}
        </button>
      </div>

      <div style={{ marginBottom: '1.5rem' }}>
        <label style={{
          display: 'block',
          marginBottom: '0.5rem',
          fontWeight: '500',
          color: '#374151'
        }}>
          Moneda de visualización:
        </label>
        <select
          value={currency}
          onChange={(e) => handleCurrencyChange(e.target.value as 'ARS' | 'USD')}
          style={{
            width: '100%',
            padding: '0.75rem',
            border: '1px solid #d1d5db',
            borderRadius: '0.375rem',
            fontSize: '1rem',
            backgroundColor: 'white'
          }}
        >
          <option value="ARS">ARS - Pesos Argentinos</option>
          <option value="USD">USD - Dólares Estadounidenses</option>
        </select>
      </div>

      {error && (
        <div style={{
          padding: '0.75rem',
          backgroundColor: '#fef2f2',
          border: '1px solid #fecaca',
          borderRadius: '0.375rem',
          color: '#991b1b',
          marginBottom: '1rem'
        }}>
          ⚠️ {error}
        </div>
      )}

      {rateInfo && (
        <div>
          <h4 style={{
            fontSize: '1rem',
            fontWeight: '600',
            color: '#111827',
            marginBottom: '1rem'
          }}>
            📊 Cotización Actual
          </h4>
          
          <div style={{
            padding: '1.5rem',
            backgroundColor: '#eff6ff',
            border: '1px solid #3b82f6',
            borderRadius: '0.5rem',
            textAlign: 'center',
            marginBottom: '1rem'
          }}>
            <div style={{ 
              fontSize: '2rem', 
              fontWeight: 'bold', 
              color: '#1e40af',
              marginBottom: '0.5rem'
            }}>
              {currencyService.formatRate(rateInfo.rate)}
            </div>
            <div style={{ 
              fontSize: '1rem', 
              color: '#3b82f6',
              marginBottom: '0.5rem'
            }}>
              💱 Conversión ARS ↔ USD
            </div>
            <div style={{ 
              fontSize: '0.875rem', 
              color: '#6b7280'
            }}>
              Última actualización: {formatDate(rateInfo.lastUpdate)}
            </div>
          </div>
          
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem',
            marginBottom: '1rem'
          }}>
            <div style={{
              padding: '1rem',
              backgroundColor: '#f0fdf4',
              border: '1px solid #22c55e',
              borderRadius: '0.375rem',
              textAlign: 'center'
            }}>
              <div style={{ 
                fontSize: '0.875rem', 
                color: '#15803d', 
                marginBottom: '0.5rem',
                fontWeight: '600'
              }}>
                💰 1 USD
              </div>
              <div style={{ 
                fontSize: '1.25rem', 
                fontWeight: 'bold', 
                color: '#16a34a'
              }}>
                ${rateInfo.rate.toLocaleString('es-AR', { 
                  minimumFractionDigits: 2, 
                  maximumFractionDigits: 2 
                })} ARS
              </div>
            </div>
            
            <div style={{
              padding: '1rem',
              backgroundColor: '#fef3c7',
              border: '1px solid #eab308',
              borderRadius: '0.375rem',
              textAlign: 'center'
            }}>
              <div style={{ 
                fontSize: '0.875rem', 
                color: '#a16207', 
                marginBottom: '0.5rem',
                fontWeight: '600'
              }}>
                💱 1 ARS
              </div>
              <div style={{ 
                fontSize: '1.25rem', 
                fontWeight: 'bold', 
                color: '#ca8a04'
              }}>
                ${(1 / rateInfo.rate).toLocaleString('es-AR', { 
                  minimumFractionDigits: 4, 
                  maximumFractionDigits: 4 
                })} USD
              </div>
            </div>
          </div>
        </div>
      )}

      <div style={{
        marginTop: '1.5rem',
        padding: '1rem',
        backgroundColor: '#f9fafb',
        borderRadius: '0.375rem',
        border: '1px solid #e5e7eb'
      }}>
        <h4 style={{
          fontSize: '0.875rem',
          fontWeight: '600',
          color: '#374151',
          marginBottom: '0.5rem'
        }}>
          ℹ️ Información
        </h4>
        <p style={{
          fontSize: '0.875rem',
          color: '#6b7280',
          lineHeight: '1.5'
        }}>
          Los valores se mostrarán en la moneda seleccionada. La cotización se obtiene de exchangerate.host y se actualiza automáticamente. 
          La conversión se realiza usando la tasa de cambio oficial ARS/USD.
        </p>
      </div>
    </div>
  );
};

export default CurrencyConfig;
