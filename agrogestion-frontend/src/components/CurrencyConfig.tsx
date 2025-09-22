import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { currencyService } from '../services/CurrencyService';

interface CurrencyConfigProps {
  onCurrencyChange?: (currency: 'ARS' | 'USD' | 'EUR') => void;
  compact?: boolean;
}

const CurrencyConfig: React.FC<CurrencyConfigProps> = ({ onCurrencyChange, compact = false }) => {
  const { selectedCurrency, exchangeType, changeCurrency, changeExchangeType, updateRates, rateInfo, loading } = useCurrencyContext();
  const [error, setError] = useState<string | null>(null);
  const [dolarApiData, setDolarApiData] = useState<any>(null);

  useEffect(() => {
    loadDolarApiData();
  }, [exchangeType]);

  const loadDolarApiData = async () => {
    try {
      setError(null);
      const realRates = currencyService.getRealDolarRates();
      
      // Crear objeto de datos para mostrar en la UI
      if (realRates) {
        setDolarApiData({
          oficial: {
            value_avg: realRates.oficial,
            value_sell: realRates.oficial * 1.02, // Aproximación
            value_buy: realRates.oficial * 0.98   // Aproximación
          },
          blue: {
            value_avg: realRates.blue,
            value_sell: realRates.blue * 1.02,    // Aproximación
            value_buy: realRates.blue * 0.98      // Aproximación
          },
          last_update: new Date().toISOString()
        });
      }
    } catch (error) {
      console.error('Error cargando datos de DolarAPI:', error);
      setError('Error al cargar datos de DolarAPI');
    }
  };

  const handleCurrencyChange = (newCurrency: 'ARS' | 'USD' | 'EUR') => {
    changeCurrency(newCurrency);
    onCurrencyChange?.(newCurrency);
  };

  const handleExchangeTypeChange = (newType: 'oficial' | 'blue') => {
    changeExchangeType(newType);
  };

  const handleUpdateRates = async () => {
    try {
      setError(null);
      await updateRates();
      await loadDolarApiData();
    } catch (error) {
      setError('Error al actualizar cotización desde DolarAPI');
    }
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
          value={selectedCurrency}
          onChange={(e) => handleCurrencyChange(e.target.value as 'ARS' | 'USD' | 'EUR')}
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
          <option value="EUR">EUR (Euros)</option>
        </select>
        
        {selectedCurrency === 'USD' && (
          <>
            <span style={{ fontSize: '0.875rem', color: '#64748b' }}>Tipo:</span>
            <select
              value={exchangeType}
              onChange={(e) => handleExchangeTypeChange(e.target.value as 'oficial' | 'blue')}
              style={{
                padding: '0.25rem 0.5rem',
                border: '1px solid #cbd5e1',
                borderRadius: '0.25rem',
                fontSize: '0.875rem',
                backgroundColor: 'white'
              }}
            >
              <option value="oficial">Oficial</option>
              <option value="blue">Blue</option>
            </select>
          </>
        )}
        
        {rateInfo && (
          <div style={{ fontSize: '0.75rem', color: '#64748b' }}>
            <span>💵 {rateInfo.rate.toFixed(2)}</span>
          </div>
        )}
        
        <button
          onClick={handleUpdateRates}
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
          onClick={handleUpdateRates}
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
          value={selectedCurrency}
          onChange={(e) => handleCurrencyChange(e.target.value as 'ARS' | 'USD' | 'EUR')}
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
          <option value="EUR">EUR - Euros</option>
        </select>
      </div>

      {selectedCurrency === 'USD' && (
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{
            display: 'block',
            marginBottom: '0.5rem',
            fontWeight: '500',
            color: '#374151'
          }}>
            Tipo de cambio USD/ARS:
          </label>
          <select
            value={exchangeType}
            onChange={(e) => handleExchangeTypeChange(e.target.value as 'oficial' | 'blue')}
            style={{
              width: '100%',
              padding: '0.75rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              fontSize: '1rem',
              backgroundColor: 'white'
            }}
          >
            <option value="oficial">💵 Dólar Oficial</option>
            <option value="blue">💙 Dólar Blue</option>
          </select>
        </div>
      )}

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
              {rateInfo.rate.toFixed(2)}
            </div>
            <div style={{ 
              fontSize: '1rem', 
              color: '#3b82f6',
              marginBottom: '0.5rem'
            }}>
              💱 Conversión USD ↔ ARS ({rateInfo.exchangeType === 'oficial' ? 'Oficial' : 'Blue'})
            </div>
            <div style={{ 
              fontSize: '0.875rem', 
              color: '#6b7280'
            }}>
              Última actualización: {formatDate(rateInfo.lastUpdate)}
            </div>
          </div>
          
          {dolarApiData && (
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
                  💵 Dólar Oficial
                </div>
                <div style={{ 
                  fontSize: '1.25rem', 
                  fontWeight: 'bold', 
                  color: '#16a34a'
                }}>
                  ${dolarApiData.oficial?.value_avg?.toLocaleString('es-AR', { 
                    minimumFractionDigits: 2, 
                    maximumFractionDigits: 2 
                  })} ARS
                </div>
                <div style={{ 
                  fontSize: '0.75rem', 
                  color: '#6b7280'
                }}>
                  Compra: ${dolarApiData.oficial?.value_buy?.toFixed(2)} | Venta: ${dolarApiData.oficial?.value_sell?.toFixed(2)}
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
                  💙 Dólar Blue
                </div>
                <div style={{ 
                  fontSize: '1.25rem', 
                  fontWeight: 'bold', 
                  color: '#ca8a04'
                }}>
                  ${dolarApiData.blue?.value_avg?.toLocaleString('es-AR', { 
                    minimumFractionDigits: 2, 
                    maximumFractionDigits: 2 
                  })} ARS
                </div>
                <div style={{ 
                  fontSize: '0.75rem', 
                  color: '#6b7280'
                }}>
                  Compra: ${dolarApiData.blue?.value_buy?.toFixed(2)} | Venta: ${dolarApiData.blue?.value_sell?.toFixed(2)}
                </div>
              </div>
            </div>
          )}
          
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
          Los valores se mostrarán en la moneda seleccionada. La cotización se obtiene de DolarAPI.com y se actualiza automáticamente. 
          Puedes elegir entre el dólar oficial y el dólar blue para las conversiones USD/ARS.
        </p>
      </div>
    </div>
  );
};

export default CurrencyConfig;
