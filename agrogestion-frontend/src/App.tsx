import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { LocationProvider } from './contexts/LocationContext';
import WeatherWidget from './components/WeatherWidget';
import FieldsManagement from './components/FieldsManagement';
import LotesManagement from './components/LotesManagement';
import LaboresManagement from './components/LaboresManagement';
import CultivosManagement from './components/CultivosManagement';
import RindeManagement from './components/RindeManagement';
import ReportsManagement from './components/ReportsManagement';
import InsumosManagement from './components/InsumosManagement';
import MaquinariaManagement from './components/MaquinariaManagement';
import CurrencyConfig from './components/CurrencyConfig';
import { useDeviceCapabilities } from './hooks/useResponsive';
import offlineService from './services/OfflineService';
import OfflineIndicator from './components/OfflineIndicator';
import currencyService from './services/CurrencyService';
import Login from './components/Login';





// Componente de navegación lateral
const Sidebar: React.FC<{ activeSection: string; onSectionChange: (section: string) => void; isMobile: boolean; onMobileMenuToggle: () => void }> = ({ 
  activeSection, 
  onSectionChange,
  isMobile,
  onMobileMenuToggle
}) => {
  const menuItems = [
    { id: 'dashboard', label: 'Inicio', icon: '📊' },
    { id: 'fields', label: 'Campos', icon: '🌾' },
    { id: 'plots', label: 'Lotes', icon: '🏞️' },
    { id: 'crops', label: 'Cultivos', icon: '🌱' },
    { id: 'inputs', label: 'Insumos', icon: '🧪' },
    { id: 'labors', label: 'Labores', icon: '🔧' },
    { id: 'maquinaria', label: 'Maquinaria', icon: '🚜' },
    { id: 'rindes', label: 'Rindes', icon: '📊' },
    { id: 'currency', label: 'Configuración', icon: '⚙️' },
    { id: 'users', label: 'Usuarios', icon: '👥' },
    { id: 'reports', label: 'Reportes', icon: '📈' }
  ];

  if (isMobile) {
    return (
      <div style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        zIndex: 1000,
        display: 'flex',
        justifyContent: 'flex-start'
      }}>
        <div style={{
          width: '280px',
          backgroundColor: '#1f2937',
          height: '100vh',
          padding: '0.5rem 0',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column'
        }}>
                     <div style={{ 
             padding: '0 1rem', 
             marginBottom: '1rem',
             display: 'flex',
             justifyContent: 'space-between',
             alignItems: 'center',
             flexShrink: 0
           }}>
             <div style={{ 
               display: 'flex', 
               alignItems: 'center', 
               gap: '0.75rem'
             }}>
               <div style={{ 
                 width: '32px', 
                 height: '32px',
                 background: 'white',
                 borderRadius: '50%',
                 display: 'flex',
                 alignItems: 'center',
                 justifyContent: 'center',
                 padding: '3px'
               }}>
                 <img 
                   src="/icons/agrocloud-logo.svg" 
                   alt="AgroCloud Logo" 
                   style={{ width: '100%', height: '100%' }}
                 />
               </div>
               <h1 style={{ 
                 fontSize: '1.25rem', 
                 fontWeight: 'bold', 
                 color: 'white'
               }}>
                 AgroCloud
               </h1>
             </div>
            <button
              onClick={onMobileMenuToggle}
              style={{
                background: 'none',
                border: 'none',
                color: 'white',
                fontSize: '1.5rem',
                cursor: 'pointer',
                padding: '0.5rem'
              }}
            >
              ✕
            </button>
          </div>
          
          <nav style={{ 
            flex: 1, 
            overflowY: 'auto',
            paddingBottom: '1rem'
          }}>
            <div style={{
              fontSize: '0.75rem',
              color: '#9ca3af',
              padding: '0.5rem 1rem',
              textAlign: 'center',
              borderBottom: '1px solid #374151',
              marginBottom: '0.5rem'
            }}>
              📱 Desliza para ver más opciones
            </div>
            {menuItems.map((item) => (
              <button
                key={item.id}
                onClick={() => {
                  onSectionChange(item.id);
                  onMobileMenuToggle();
                }}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  backgroundColor: activeSection === item.id ? '#374151' : 'transparent',
                  color: activeSection === item.id ? 'white' : '#d1d5db',
                  border: 'none',
                  textAlign: 'left',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  fontSize: '0.875rem',
                  transition: 'all 0.2s',
                  minHeight: '44px'
                }}
                onMouseEnter={(e) => {
                  if (activeSection !== item.id) {
                    e.currentTarget.style.backgroundColor = '#374151';
                  }
                }}
                onMouseLeave={(e) => {
                  if (activeSection !== item.id) {
                    e.currentTarget.style.backgroundColor = 'transparent';
                  }
                }}
              >
                <span style={{ fontSize: '1.125rem', flexShrink: 0 }}>{item.icon}</span>
                <span style={{ flex: 1, textAlign: 'left' }}>{item.label}</span>
              </button>
            ))}
            <div style={{
              fontSize: '0.75rem',
              color: '#9ca3af',
              padding: '0.5rem 1rem',
              textAlign: 'center',
              borderTop: '1px solid #374151',
              marginTop: '0.5rem'
            }}>
              ⬆️ Fin del menú
            </div>
          </nav>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      width: '250px',
      backgroundColor: '#1f2937',
      height: '100vh',
      padding: '1rem 0',
      position: 'fixed',
      left: 0,
      top: 0
    }}>
             <div style={{ padding: '0 1rem', marginBottom: '2rem' }}>
         <div style={{ 
           display: 'flex', 
           alignItems: 'center', 
           justifyContent: 'center',
           gap: '0.75rem',
           marginBottom: '1rem'
         }}>
           <div style={{ 
             width: '40px', 
             height: '40px',
             background: 'white',
             borderRadius: '50%',
             display: 'flex',
             alignItems: 'center',
             justifyContent: 'center',
             padding: '4px'
           }}>
             <img 
               src="/icons/agrocloud-logo.svg" 
               alt="AgroCloud Logo" 
               style={{ width: '100%', height: '100%' }}
             />
           </div>
           <h1 style={{ 
             fontSize: '1.25rem', 
             fontWeight: 'bold', 
             color: 'white'
           }}>
             AgroCloud
           </h1>
         </div>
       </div>
      
             <nav style={{ 
         height: 'calc(100vh - 120px)', 
         overflowY: 'auto',
         paddingBottom: '1rem'
       }}>
         {menuItems.map((item) => (
           <button
             key={item.id}
             onClick={() => onSectionChange(item.id)}
             style={{
               width: '100%',
               padding: '0.75rem 1rem',
               backgroundColor: activeSection === item.id ? '#374151' : 'transparent',
               color: activeSection === item.id ? 'white' : '#d1d5db',
               border: 'none',
               textAlign: 'left',
               cursor: 'pointer',
               display: 'flex',
               alignItems: 'center',
               gap: '0.75rem',
               fontSize: '0.875rem',
               transition: 'all 0.2s',
               minHeight: '44px'
             }}
             onMouseEnter={(e) => {
               if (activeSection !== item.id) {
                 e.currentTarget.style.backgroundColor = '#374151';
               }
             }}
             onMouseLeave={(e) => {
               if (activeSection !== item.id) {
                 e.currentTarget.style.backgroundColor = 'transparent';
               }
             }}
           >
             <span style={{ fontSize: '1.125rem', flexShrink: 0 }}>{item.icon}</span>
             <span style={{ flex: 1, textAlign: 'left' }}>{item.label}</span>
           </button>
         ))}
       </nav>
    </div>
  );
};

// Componente Dashboard mejorado con datos reales
const Dashboard: React.FC = () => {
  const [activeSection, setActiveSection] = useState('dashboard');
  const [user, setUser] = useState<any>({});
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const deviceCapabilities = useDeviceCapabilities();
  const { isMobile, isOnline } = deviceCapabilities;
  // const { isMobile, isOnline, isStandalone } = deviceCapabilities; // isStandalone no se utiliza
  const [dashboardData, setDashboardData] = useState({
    campos: 0,
    lotes: 0,
    cultivosActivos: 0,
    insumos: 0,
    laboresPendientes: 0,
    laboresEnProgreso: 0,
    laboresCompletadas: 0,
    stockBajo: 0,
    costoTotalInsumos: 0,
    actividadReciente: [] as Array<{
      id: number;
      tipo: string;
      descripcion: string;
      fecha: string;
      usuario: string;
    }>
  });
  const [loading, setLoading] = useState(true);
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD'>('ARS');

  useEffect(() => {
    const userData = JSON.parse(localStorage.getItem('user') || '{}');
    setUser(userData);
    loadDashboardData();
    
    // Cargar configuración de moneda
    const config = currencyService.getCurrencyConfig();
    setSelectedCurrency(config.currency);
    
    // Inicializar servicios offline
    const initOfflineServices = async () => {
      try {
        await offlineService.init();
        await offlineService.registerServiceWorker();
        offlineService.showInstallBanner();
      } catch (error) {
        console.error('Error inicializando servicios offline:', error);
      }
    };
    
    initOfflineServices();
  }, []);

  const formatCurrency = (amount: number): string => {
    return currencyService.formatCurrency(amount, selectedCurrency);
  };

  const handleCurrencyChange = (currency: 'ARS' | 'USD') => {
    setSelectedCurrency(currency);
  };

  const loadDashboardData = async () => {
    setLoading(true);
    
    try {
      // Usar datos mock por ahora para evitar problemas de conexión
      const mockData = {
        campos: 3,
        lotes: 3,
        cultivosActivos: 2,
        insumos: 5,
        laboresPendientes: 1,
        laboresEnProgreso: 1,
        laboresCompletadas: 2,
        stockBajo: 2,
        costoTotalInsumos: 25000000,
        actividadReciente: [
          {
            id: 1,
            tipo: 'labor',
            descripcion: 'Siembra completada en Lote A1',
            fecha: '2024-11-15',
            usuario: 'Juan Pérez'
          },
          {
            id: 2,
            tipo: 'insumo',
            descripcion: 'Nuevo insumo agregado: Semilla Soja DM 53i54',
            fecha: '2024-11-14',
            usuario: 'María González'
          },
          {
            id: 3,
            tipo: 'lote',
            descripcion: 'Lote B1 creado en Campo Norte',
            fecha: '2024-11-13',
            usuario: 'Carlos López'
          },
          {
            id: 4,
            tipo: 'cultivo',
            descripcion: 'Cultivo de Maíz activado',
            fecha: '2024-11-12',
            usuario: 'Ana Martínez'
          }
        ]
      };
      
      setDashboardData(mockData);
      
      // Guardar en cache para uso offline
      try {
        await offlineService.saveToCache('dashboardData', mockData);
      } catch (cacheError) {
        console.warn('Error guardando en cache:', cacheError);
      }
    } catch (error) {
      console.error('Error cargando datos del dashboard:', error);
      // Datos de respaldo en caso de error
      setDashboardData({
        campos: 0,
        lotes: 0,
        cultivosActivos: 0,
        insumos: 0,
        laboresPendientes: 0,
        laboresEnProgreso: 0,
        laboresCompletadas: 0,
        stockBajo: 0,
        costoTotalInsumos: 0,
        actividadReciente: []
      });
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  };

  const toggleMobileMenu = () => {
    setShowMobileMenu(!showMobileMenu);
  };

  const getActivityIcon = (tipo: string) => {
    switch (tipo) {
      case 'labor': return '🔧';
      case 'insumo': return '🧪';
      case 'lote': return '🏞️';
      case 'cultivo': return '🌱';
      case 'campo': return '🌾';
      default: return '📝';
    }
  };

  const getActivityColor = (tipo: string) => {
    switch (tipo) {
      case 'labor': return '#f59e0b';
      case 'insumo': return '#8b5cf6';
      case 'lote': return '#10b981';
      case 'cultivo': return '#3b82f6';
      case 'campo': return '#ef4444';
      default: return '#6b7280';
    }
  };

  const renderSection = () => {
    switch (activeSection) {
      case 'dashboard':
        return (
          <div>
                         <div style={{ 
               background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
               color: 'white', 
               padding: '20px', 
               borderRadius: '10px', 
               marginBottom: '20px' 
             }}>
               <p style={{ 
                 opacity: '0.9',
                 marginBottom: '1rem',
                 fontSize: '1.125rem'
               }}>
                 Bienvenido a su nuevo sistema de gestión agrícola
               </p>
               <div style={{ 
                 display: 'flex', 
                 alignItems: 'center', 
                 gap: '10px',
                 fontSize: '0.875rem',
                 padding: '0.75rem',
                 backgroundColor: 'rgba(255,255,255,0.1)',
                 borderRadius: '0.5rem'
               }}>
                 <span>👤 {user.firstName} {user.lastName}</span>
                 <span>•</span>
                 <span>🎭 {user.role?.charAt(0).toUpperCase() + user.role?.slice(1) || 'Usuario'}</span>
                 <span>•</span>
                 <span>📧 {user.email}</span>
               </div>
             </div>
            
            {loading ? (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '200px' 
              }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>🔄</div>
                  <p>Cargando datos del dashboard...</p>
                </div>
              </div>
            ) : (
              <>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                  gap: '1.5rem',
                  marginBottom: '2rem'
                }}>
                  <div style={{
                    backgroundColor: '#eff6ff',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    border: '1px solid #3b82f6'
                  }}>
                    <h3 style={{ 
                      fontWeight: '600', 
                      color: '#1e40af',
                      marginBottom: '0.5rem'
                    }}>
                      🌾 Campos
                    </h3>
                    <p style={{ 
                      fontSize: '2rem', 
                      fontWeight: 'bold', 
                      color: '#2563eb'
                    }}>
                      {dashboardData.campos}
                    </p>
                    <p style={{ fontSize: '0.875rem', color: '#1e40af' }}>
                      Total de campos registrados
                    </p>
                  </div>
                  
                  <div style={{
                    backgroundColor: '#f0fdf4',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    border: '1px solid #22c55e'
                  }}>
                    <h3 style={{ 
                      fontWeight: '600', 
                      color: '#15803d',
                      marginBottom: '0.5rem'
                    }}>
                      🏞️ Lotes
                    </h3>
                    <p style={{ 
                      fontSize: '2rem', 
                      fontWeight: 'bold', 
                      color: '#16a34a'
                    }}>
                      {dashboardData.lotes}
                    </p>
                    <p style={{ fontSize: '0.875rem', color: '#15803d' }}>
                      Lotes en producción
                    </p>
                  </div>
                  
                  <div style={{
                    backgroundColor: '#fef3c7',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    border: '1px solid #eab308'
                  }}>
                    <h3 style={{ 
                      fontWeight: '600', 
                      color: '#a16207',
                      marginBottom: '0.5rem'
                    }}>
                      🌱 Cultivos Activos
                    </h3>
                    <p style={{ 
                      fontSize: '2rem', 
                      fontWeight: 'bold', 
                      color: '#ca8a04'
                    }}>
                      {dashboardData.cultivosActivos}
                    </p>
                    <p style={{ fontSize: '0.875rem', color: '#a16207' }}>
                      Cultivos en producción
                    </p>
                  </div>

                  <div style={{
                    backgroundColor: '#fef2f2',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    border: '1px solid #ef4444'
                  }}>
                    <h3 style={{ 
                      fontWeight: '600', 
                      color: '#991b1b',
                      marginBottom: '0.5rem'
                    }}>
                      🔧 Labores Pendientes
                    </h3>
                    <p style={{ 
                      fontSize: '2rem', 
                      fontWeight: 'bold', 
                      color: '#dc2626'
                    }}>
                      {dashboardData.laboresPendientes}
                    </p>
                    <p style={{ fontSize: '0.875rem', color: '#991b1b' }}>
                      Labores por realizar
                    </p>
                  </div>

                  <div style={{
                    backgroundColor: '#fdf4ff',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    border: '1px solid #a855f7'
                  }}>
                    <h3 style={{ 
                      fontWeight: '600', 
                      color: '#7c3aed',
                      marginBottom: '0.5rem'
                    }}>
                      🧪 Insumos
                    </h3>
                    <p style={{ 
                      fontSize: '2rem', 
                      fontWeight: 'bold', 
                      color: '#9333ea'
                    }}>
                      {dashboardData.insumos}
                    </p>
                    <p style={{ fontSize: '0.875rem', color: '#7c3aed' }}>
                      Tipos de insumos disponibles
                    </p>
                  </div>

                  <div style={{
                    backgroundColor: '#ecfdf5',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    border: '1px solid #10b981'
                  }}>
                    <h3 style={{ 
                      fontWeight: '600', 
                      color: '#047857',
                      marginBottom: '0.5rem'
                    }}>
                      💰 Valor Inventario
                    </h3>
                    <p style={{ 
                      fontSize: '2rem', 
                      fontWeight: 'bold', 
                      color: '#059669'
                    }}>
                      {formatCurrency(dashboardData.costoTotalInsumos)}
                    </p>
                    <p style={{ fontSize: '0.875rem', color: '#047857' }}>
                      Valor total del inventario
                    </p>
                  </div>
                </div>

                {/* Estadísticas de Labores */}
                <div style={{
                  backgroundColor: 'white',
                  padding: '1.5rem',
                  borderRadius: '0.5rem',
                  border: '1px solid #e5e7eb',
                  marginBottom: '2rem'
                }}>
                  <h3 style={{ 
                    fontSize: '1.25rem', 
                    fontWeight: '600', 
                    color: '#111827',
                    marginBottom: '1rem'
                  }}>
                    📊 Estado de Labores
                  </h3>
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                    gap: '1rem'
                  }}>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
                        {dashboardData.laboresEnProgreso}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>En Progreso</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
                        {dashboardData.laboresCompletadas}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>Completadas</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f59e0b' }}>
                        {dashboardData.stockBajo}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>Stock Bajo</div>
                    </div>
                  </div>
                </div>

                {/* Actividad Reciente */}
                <div style={{
                  backgroundColor: 'white',
                  padding: '1.5rem',
                  borderRadius: '0.5rem',
                  border: '1px solid #e5e7eb'
                }}>
                  <h3 style={{ 
                    fontSize: '1.25rem', 
                    fontWeight: '600', 
                    color: '#111827',
                    marginBottom: '1rem'
                  }}>
                    📈 Actividad Reciente
                  </h3>
                  {dashboardData.actividadReciente.length === 0 ? (
                    <p style={{ color: '#6b7280' }}>
                      No hay actividad reciente para mostrar.
                    </p>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      {dashboardData.actividadReciente.map((actividad) => (
                        <div key={actividad.id} style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.75rem',
                          padding: '0.75rem',
                          backgroundColor: '#f9fafb',
                          borderRadius: '0.375rem',
                          border: '1px solid #e5e7eb'
                        }}>
                          <div style={{
                            fontSize: '1.25rem',
                            color: getActivityColor(actividad.tipo)
                          }}>
                            {getActivityIcon(actividad.tipo)}
                          </div>
                          <div style={{ flex: 1 }}>
                            <p style={{ 
                              margin: '0 0 0.25rem 0', 
                              fontWeight: '500',
                              color: '#111827'
                            }}>
                              {actividad.descripcion}
                            </p>
                            <div style={{ 
                              display: 'flex', 
                              alignItems: 'center', 
                              gap: '0.5rem',
                              fontSize: '0.875rem',
                              color: '#6b7280'
                            }}>
                              <span>👤 {actividad.usuario}</span>
                              <span>•</span>
                              <span>📅 {new Date(actividad.fecha).toLocaleDateString('es-ES')}</span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </>
            )}
          </div>
        );

      case 'fields':
        return <FieldsManagement />;

      case 'plots':
        return <LotesManagement />;

      case 'crops':
        return <CultivosManagement />;

      case 'inputs':
        return <InsumosManagement />;

      case 'labors':
        return <LaboresManagement />;

      case 'maquinaria':
        return <MaquinariaManagement />;

      case 'rindes':
        return <RindeManagement />;





      case 'users':
        return (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.875rem', fontWeight: 'bold', color: '#111827' }}>
                Gestión de Usuarios
              </h2>
              <button style={{
                backgroundColor: '#2563eb',
                color: 'white',
                padding: '0.5rem 1rem',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer'
              }}>
                + Nuevo Usuario
              </button>
            </div>
            <div style={{
              backgroundColor: 'white',
              padding: '1.5rem',
              borderRadius: '0.5rem',
              border: '1px solid #e5e7eb'
            }}>
              <p style={{ color: '#6b7280' }}>No hay usuarios registrados. Crea tu primer usuario para comenzar.</p>
            </div>
          </div>
        );

      case 'reports':
        return <ReportsManagement />;

      case 'currency':
        return <CurrencyConfig onCurrencyChange={handleCurrencyChange} />;

      default:
        return <div>Sección no encontrada</div>
    }
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      backgroundColor: '#f9fafb',
      fontFamily: 'Arial, sans-serif',
      display: 'flex'
    }}>
      {!isMobile && <Sidebar activeSection={activeSection} onSectionChange={setActiveSection} isMobile={false} onMobileMenuToggle={() => {}} />}
      {isMobile && showMobileMenu && <Sidebar activeSection={activeSection} onSectionChange={setActiveSection} isMobile={true} onMobileMenuToggle={toggleMobileMenu} />}
      
      <div style={{ 
        marginLeft: isMobile ? '0' : '250px', 
        flex: 1,
        width: isMobile ? '100%' : 'auto'
      }}>
        <nav style={{
          backgroundColor: 'white',
          borderBottom: '1px solid #e5e7eb',
          padding: isMobile ? '1rem' : '1rem 2rem'
        }}>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            {isMobile && (
              <button
                onClick={toggleMobileMenu}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '1.5rem',
                  cursor: 'pointer',
                  padding: '0.5rem',
                  color: '#374151'
                }}
              >
                ☰
              </button>
            )}
                         <div style={{ 
               display: 'flex', 
               alignItems: 'center', 
               gap: '0.75rem',
               flex: 1,
               justifyContent: isMobile ? 'center' : 'flex-start'
             }}>
               <div style={{ 
                 width: '32px', 
                 height: '32px',
                 background: 'white',
                 borderRadius: '50%',
                 display: 'flex',
                 alignItems: 'center',
                 justifyContent: 'center',
                 flexShrink: 0,
                 padding: '3px'
               }}>
                 <img 
                   src="/icons/agrocloud-logo.svg" 
                   alt="AgroCloud Logo" 
                   style={{ width: '100%', height: '100%' }}
                 />
               </div>
               <h1 style={{ 
                 fontSize: isMobile ? '1.125rem' : '1.25rem', 
                 fontWeight: '600', 
                 color: '#111827',
                 margin: 0
               }}>
                 {(() => {
                   const sectionNames: { [key: string]: string } = {
                     'dashboard': 'Inicio',
                     'fields': 'Campos',
                     'plots': 'Lotes',
                     'crops': 'Cultivos',
                     'inputs': 'Insumos',
                     'labors': 'Labores',
                     'maquinaria': 'Maquinaria',
                     'rindes': 'Rindes',
                     'currency': 'Configuración',
                     'users': 'Usuarios',
                     'reports': 'Reportes'
                   };
                   return sectionNames[activeSection] || activeSection.charAt(0).toUpperCase() + activeSection.slice(1);
                 })()}
               </h1>
             </div>
                         <div style={{ 
               display: 'flex', 
               alignItems: 'center', 
               gap: isMobile ? '0.5rem' : '1rem',
               flexDirection: isMobile ? 'column' : 'row'
             }}>
               
               
               {/* Indicador de estado de conexión */}
               <div style={{
                 display: 'flex',
                 alignItems: 'center',
                 gap: '0.25rem',
                 fontSize: '0.75rem',
                 color: isOnline ? '#10b981' : '#ef4444'
               }}>
                 <div style={{
                   width: '8px',
                   height: '8px',
                   borderRadius: '50%',
                   backgroundColor: isOnline ? '#10b981' : '#ef4444'
                 }} />
                 {isOnline ? 'En línea' : 'Sin conexión'}
               </div>
               
               <CurrencyConfig 
                 onCurrencyChange={handleCurrencyChange}
                 compact={true}
               />
               <WeatherWidget />
               <button
                 onClick={handleLogout}
                 style={{
                   fontSize: isMobile ? '0.75rem' : '0.875rem',
                   color: '#6b7280',
                   background: 'none',
                   border: 'none',
                   cursor: 'pointer',
                   padding: '0.5rem'
                 }}
               >
                 {isMobile ? 'Salir' : 'Cerrar sesión'}
               </button>
             </div>
          </div>
        </nav>
        
        <main style={{ padding: isMobile ? '1rem' : '2rem' }}>
          {renderSection()}
        </main>
      </div>
    </div>
  );
};

// Componente para rutas protegidas
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = !!localStorage.getItem('token');
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

// Componente para rutas públicas
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = !!localStorage.getItem('token');
  
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  
  return <>{children}</>;
};

const AppRoutes: React.FC = () => {
  const handleLoginSuccess = (token: string, user: any) => {
    // El login ya se maneja en el componente Login
    // Solo redirigir al dashboard
    window.location.href = '/dashboard';
  };

  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><Login onLoginSuccess={handleLoginSuccess} /></PublicRoute>} />
      <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <LocationProvider>
      <Router>
        <OfflineIndicator showDetails={true} />
        <AppRoutes />
      </Router>
    </LocationProvider>
  );
};

export default App;
