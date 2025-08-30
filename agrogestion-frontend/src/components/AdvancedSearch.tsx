import React, { useState, useEffect, useRef } from 'react';

interface SearchResult {
  id: string;
  type: 'campo' | 'lote' | 'insumo' | 'maquinaria' | 'labor' | 'usuario';
  title: string;
  description: string;
  url: string;
  icon: string;
  metadata?: Record<string, any>;
}

interface SearchFilter {
  type: string[];
  dateRange?: {
    start: Date;
    end: Date;
  };
  status?: string[];
}

const AdvancedSearch: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<SearchFilter>({
    type: [],
    status: []
  });
  const [isOpen, setIsOpen] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);

  // Datos mock para búsqueda
  const mockData: SearchResult[] = [
    {
      id: '1',
      type: 'campo',
      title: 'Campo Norte',
      description: 'Campo principal para cultivo de soja - 25.5 ha',
      url: '/fields',
      icon: '🌾',
      metadata: { area: 25.5, cultivo: 'Soja', estado: 'Activo' }
    },
    {
      id: '2',
      type: 'lote',
      title: 'Lote A1',
      description: 'Lote con soja de primera - 12.5 ha',
      url: '/plots',
      icon: '🏞️',
      metadata: { area: 12.5, cultivo: 'Soja', estado: 'En crecimiento' }
    },
    {
      id: '3',
      type: 'insumo',
      title: 'Glifosato 48%',
      description: 'Herbicida sistémico - Stock: 200 litros',
      url: '/inputs',
      icon: '🧪',
      metadata: { stock: 200, unidad: 'LITROS', proveedor: 'Syngenta' }
    },
    {
      id: '4',
      type: 'maquinaria',
      title: 'Tractor Principal',
      description: 'John Deere 5075E - 75 HP',
      url: '/machinery',
      icon: '🚜',
      metadata: { marca: 'John Deere', modelo: '5075E', estado: 'Operativa' }
    },
    {
      id: '5',
      type: 'labor',
      title: 'Siembra de soja',
      description: 'Siembra de soja de primera en Lote A1',
      url: '/labors',
      icon: '🔧',
      metadata: { fecha: '2024-11-15', estado: 'Completada', costo: 1250 }
    },
    {
      id: '6',
      type: 'usuario',
      title: 'Juan Pérez',
      description: 'Técnico agrícola - Activo',
      url: '/users',
      icon: '👤',
      metadata: { rol: 'Técnico', email: 'juan@agrocloud.com', estado: 'Activo' }
    }
  ];

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (query.length >= 2) {
      performSearch();
    } else {
      setResults([]);
      setIsOpen(false);
    }
  }, [query, filters]);

  const performSearch = async () => {
    setLoading(true);
    setIsOpen(true);

    // Simular búsqueda
    setTimeout(() => {
      const filteredResults = mockData.filter(item => {
        // Búsqueda por texto
        const matchesQuery = 
          item.title.toLowerCase().includes(query.toLowerCase()) ||
          item.description.toLowerCase().includes(query.toLowerCase()) ||
          Object.values(item.metadata || {}).some(value => 
            String(value).toLowerCase().includes(query.toLowerCase())
          );

        // Filtros por tipo
        const matchesType = filters.type.length === 0 || filters.type.includes(item.type);

        // Filtros por estado
                const matchesStatus = !filters.status || filters.status.length === 0 ||
          (item.metadata?.estado && filters.status.includes(item.metadata.estado));

        return matchesQuery && matchesType && matchesStatus;
      });

      setResults(filteredResults);
      setLoading(false);
    }, 300);
  };

  const handleResultClick = (result: SearchResult) => {
    // En un entorno real, navegaría a la URL
    console.log('Navegando a:', result.url);
    setIsOpen(false);
    setQuery('');
  };

  const getTypeLabel = (type: string): string => {
    const labels: Record<string, string> = {
      campo: 'Campo',
      lote: 'Lote',
      insumo: 'Insumo',
      maquinaria: 'Maquinaria',
      labor: 'Labor',
      usuario: 'Usuario'
    };
    return labels[type] || type;
  };

  const getTypeColor = (type: string): string => {
    const colors: Record<string, string> = {
      campo: '#10b981',
      lote: '#3b82f6',
      insumo: '#f59e0b',
      maquinaria: '#8b5cf6',
      labor: '#ef4444',
      usuario: '#6b7280'
    };
    return colors[type] || '#6b7280';
  };

  const toggleFilter = (filterType: 'type' | 'status', value: string) => {
    setFilters(prev => {
      const currentFilters = prev[filterType] || [];
      const newFilters = currentFilters.includes(value)
        ? currentFilters.filter(f => f !== value)
        : [...currentFilters, value];
      
      return {
        ...prev,
        [filterType]: newFilters
      };
    });
  };

  const clearFilters = () => {
    setFilters({ type: [], status: [] });
  };

  return (
    <div ref={searchRef} style={{ position: 'relative', width: '100%', maxWidth: '600px' }}>
      {/* Barra de búsqueda */}
      <div style={{ position: 'relative' }}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="🔍 Buscar campos, lotes, insumos, maquinaria..."
          style={{
            width: '100%',
            padding: '12px 45px 12px 15px',
            border: '2px solid #e5e7eb',
            borderRadius: '25px',
            fontSize: '14px',
            outline: 'none',
            transition: 'border-color 0.2s'
          }}
          onFocus={() => query.length >= 2 && setIsOpen(true)}
        />
        
        <button
          onClick={() => setShowFilters(!showFilters)}
          style={{
            position: 'absolute',
            right: '45px',
            top: '50%',
            transform: 'translateY(-50%)',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '5px',
            color: '#6b7280'
          }}
        >
          ⚙️
        </button>
        
        {loading && (
          <div style={{
            position: 'absolute',
            right: '15px',
            top: '50%',
            transform: 'translateY(-50%)'
          }}>
            <div style={{
              width: '16px',
              height: '16px',
              border: '2px solid #e5e7eb',
              borderTop: '2px solid #3b82f6',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite'
            }} />
          </div>
        )}
      </div>

      {/* Filtros */}
      {showFilters && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          background: 'white',
          border: '1px solid #e5e7eb',
          borderRadius: '8px',
          padding: '15px',
          marginTop: '5px',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
          zIndex: 1000
        }}>
          <div style={{ marginBottom: '15px' }}>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '14px', fontWeight: 'bold' }}>
              Tipo de elemento
            </h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {['campo', 'lote', 'insumo', 'maquinaria', 'labor', 'usuario'].map(type => (
                <button
                  key={type}
                  onClick={() => toggleFilter('type', type)}
                  style={{
                    padding: '4px 8px',
                    border: `1px solid ${filters.type.includes(type) ? getTypeColor(type) : '#e5e7eb'}`,
                    borderRadius: '12px',
                    background: filters.type.includes(type) ? getTypeColor(type) : 'white',
                    color: filters.type.includes(type) ? 'white' : '#374151',
                    fontSize: '12px',
                    cursor: 'pointer'
                  }}
                >
                  {getTypeLabel(type)}
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: '15px' }}>
            <h4 style={{ margin: '0 0 10px 0', fontSize: '14px', fontWeight: 'bold' }}>
              Estado
            </h4>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {['Activo', 'Inactivo', 'En crecimiento', 'Completada', 'Operativa'].map(status => (
                <button
                  key={status}
                  onClick={() => toggleFilter('status', status)}
                  style={{
                    padding: '4px 8px',
                    border: `1px solid ${filters.status?.includes(status) ? '#3b82f6' : '#e5e7eb'}`,
                    borderRadius: '12px',
                    background: filters.status?.includes(status) ? '#3b82f6' : 'white',
                    color: filters.status?.includes(status) ? 'white' : '#374151',
                    fontSize: '12px',
                    cursor: 'pointer'
                  }}
                >
                  {status}
                </button>
              ))}
            </div>
          </div>

          <button
            onClick={clearFilters}
            style={{
              padding: '6px 12px',
              border: '1px solid #e5e7eb',
              borderRadius: '6px',
              background: 'white',
              color: '#6b7280',
              fontSize: '12px',
              cursor: 'pointer'
            }}
          >
            Limpiar filtros
          </button>
        </div>
      )}

      {/* Resultados */}
      {isOpen && (results.length > 0 || loading) && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          background: 'white',
          border: '1px solid #e5e7eb',
          borderRadius: '8px',
          marginTop: '5px',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
          maxHeight: '400px',
          overflowY: 'auto',
          zIndex: 1000
        }}>
          {loading ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#6b7280' }}>
              🔍 Buscando...
            </div>
          ) : (
            <>
              <div style={{
                padding: '10px 15px',
                borderBottom: '1px solid #e5e7eb',
                fontSize: '12px',
                color: '#6b7280',
                fontWeight: 'bold'
              }}>
                {results.length} resultado{results.length !== 1 ? 's' : ''} encontrado{results.length !== 1 ? 's' : ''}
              </div>
              
              {results.map((result, index) => (
                <div
                  key={result.id}
                  onClick={() => handleResultClick(result)}
                  style={{
                    padding: '12px 15px',
                    borderBottom: index < results.length - 1 ? '1px solid #f3f4f6' : 'none',
                    cursor: 'pointer',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#f9fafb';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = 'white';
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: '50%',
                      background: getTypeColor(result.type),
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '16px',
                      color: 'white'
                    }}>
                      {result.icon}
                    </div>
                    
                    <div style={{ flex: 1 }}>
                      <div style={{
                        fontSize: '14px',
                        fontWeight: 'bold',
                        color: '#374151',
                        marginBottom: '4px'
                      }}>
                        {result.title}
                      </div>
                      <div style={{
                        fontSize: '12px',
                        color: '#6b7280',
                        marginBottom: '4px'
                      }}>
                        {result.description}
                      </div>
                      <div style={{
                        fontSize: '11px',
                        color: '#9ca3af'
                      }}>
                        {getTypeLabel(result.type)} • {result.metadata?.estado || 'Sin estado'}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </>
          )}
        </div>
      )}

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default AdvancedSearch;
