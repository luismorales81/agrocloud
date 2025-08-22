import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// FORZAR REDEPLOY - FIX INFINITE LOADING ISSUE

// Componente de Login simplificado - Sin llamadas al backend
const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    // Simulación de login con diferentes usuarios - SIN llamadas al backend
    setTimeout(() => {
      let userData;
      
      switch (username.toLowerCase()) {
        case 'admin':
          userData = {
            id: 1,
            username: 'admin',
            firstName: 'Administrador',
            lastName: 'Sistema',
            role: 'admin',
            email: 'admin@agrocloud.com'
          };
          break;
        case 'tecnico':
          userData = {
            id: 2,
            username: 'tecnico',
            firstName: 'Técnico',
            lastName: 'Agrícola',
            role: 'tecnico',
            email: 'tecnico@agrocloud.com'
          };
          break;
        case 'productor':
          userData = {
            id: 3,
            username: 'productor',
            firstName: 'Productor',
            lastName: 'Agrícola',
            role: 'productor',
            email: 'productor@agrocloud.com'
          };
          break;
        default:
          userData = {
            id: 4,
            username: username,
            firstName: 'Usuario',
            lastName: 'Demo',
            role: 'usuario',
            email: 'usuario@agrocloud.com'
          };
      }
      
      localStorage.setItem('token', 'fake-token');
      localStorage.setItem('user', JSON.stringify(userData));
      setLoading(false);
      window.location.href = '/dashboard';
    }, 1000);
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      backgroundColor: '#f3f4f6',
      fontFamily: 'Arial, sans-serif'
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        width: '100%',
        maxWidth: '400px'
      }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{ 
            width: '80px', 
            height: '80px', 
            margin: '0 auto 1rem auto',
            background: 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '40px',
            color: 'white'
          }}>
            🌾
          </div>
          <h1 style={{ 
            fontSize: '2rem', 
            fontWeight: 'bold', 
            color: '#1f2937',
            marginBottom: '0.5rem'
          }}>
            AgroCloud
          </h1>
          <p style={{ color: '#6b7280' }}>
            Sistema de Gestión Agrícola
          </p>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ 
              display: 'block', 
              marginBottom: '0.5rem',
              fontWeight: '500',
              color: '#374151'
            }}>
              Usuario
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '1rem'
              }}
              placeholder="Ingrese su usuario"
              required
            />
          </div>
          
          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ 
              display: 'block', 
              marginBottom: '0.5rem',
              fontWeight: '500',
              color: '#374151'
            }}>
              Contraseña
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '1rem'
              }}
              placeholder="Ingrese su contraseña"
              required
            />
          </div>
          
          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: loading ? '#9ca3af' : '#2563eb',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              fontSize: '1rem',
              fontWeight: '500',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
          </button>
        </form>
        
        <div style={{ 
          textAlign: 'center', 
          marginTop: '2rem',
          fontSize: '0.875rem',
          color: '#6b7280'
        }}>
          <p style={{ marginBottom: '0.5rem' }}>Usuarios de prueba:</p>
          <p>admin / admin123</p>
          <p>tecnico / tecnico123</p>
          <p>productor / productor123</p>
        </div>
      </div>
    </div>
  );
};

// Dashboard simplificado
const Dashboard: React.FC = () => {
  const [user] = useState(() => {
    const userData = localStorage.getItem('user');
    return userData ? JSON.parse(userData) : {};
  });

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      backgroundColor: '#f9fafb',
      fontFamily: 'Arial, sans-serif',
      padding: '2rem'
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        maxWidth: '800px',
        margin: '0 auto'
      }}>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: '2rem'
        }}>
          <h1 style={{ 
            fontSize: '2rem', 
            fontWeight: 'bold', 
            color: '#1f2937'
          }}>
            🌾 AgroCloud Dashboard
          </h1>
          <button
            onClick={handleLogout}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#ef4444',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer'
            }}
          >
            Cerrar sesión
          </button>
        </div>
        
        <div style={{ 
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
          color: 'white', 
          padding: '1.5rem', 
          borderRadius: '0.5rem', 
          marginBottom: '2rem' 
        }}>
          <h2 style={{ marginBottom: '0.5rem' }}>
            Bienvenido, {user.firstName} {user.lastName}!
          </h2>
          <p style={{ opacity: '0.9' }}>
            Rol: {user.role?.charAt(0).toUpperCase() + user.role?.slice(1) || 'Usuario'}
          </p>
        </div>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '1rem'
        }}>
          <div style={{
            backgroundColor: '#eff6ff',
            padding: '1.5rem',
            borderRadius: '0.5rem',
            border: '1px solid #3b82f6'
          }}>
            <h3 style={{ color: '#1e40af', marginBottom: '0.5rem' }}>🌾 Campos</h3>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#2563eb' }}>3</p>
          </div>
          
          <div style={{
            backgroundColor: '#f0fdf4',
            padding: '1.5rem',
            borderRadius: '0.5rem',
            border: '1px solid #22c55e'
          }}>
            <h3 style={{ color: '#15803d', marginBottom: '0.5rem' }}>🏞️ Lotes</h3>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#16a34a' }}>3</p>
          </div>
          
          <div style={{
            backgroundColor: '#fef3c7',
            padding: '1.5rem',
            borderRadius: '0.5rem',
            border: '1px solid #eab308'
          }}>
            <h3 style={{ color: '#a16207', marginBottom: '0.5rem' }}>🌱 Cultivos</h3>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ca8a04' }}>2</p>
          </div>
          
          <div style={{
            backgroundColor: '#fdf4ff',
            padding: '1.5rem',
            borderRadius: '0.5rem',
            border: '1px solid #a855f7'
          }}>
            <h3 style={{ color: '#7c3aed', marginBottom: '0.5rem' }}>🧪 Insumos</h3>
            <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#9333ea' }}>5</p>
          </div>
        </div>
        
        <div style={{ 
          marginTop: '2rem',
          padding: '1.5rem',
          backgroundColor: '#f9fafb',
          borderRadius: '0.5rem',
          border: '1px solid #e5e7eb'
        }}>
          <h3 style={{ marginBottom: '1rem', color: '#374151' }}>📈 Actividad Reciente</h3>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            <li style={{ padding: '0.5rem 0', borderBottom: '1px solid #e5e7eb' }}>
              🔧 Siembra completada en Lote A1 - 2024-11-15
            </li>
            <li style={{ padding: '0.5rem 0', borderBottom: '1px solid #e5e7eb' }}>
              🧪 Nuevo insumo agregado: Semilla Soja DM 53i54 - 2024-11-14
            </li>
            <li style={{ padding: '0.5rem 0' }}>
              🏞️ Lote B1 creado en Campo Norte - 2024-11-13
            </li>
          </ul>
        </div>
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
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <Router>
      <AppRoutes />
    </Router>
  );
};

export default App;
