import React, { useState } from 'react';

interface LoginProps {
  onLoginSuccess: (token: string, user: any) => void;
}

interface LoginForm {
  email: string;
  password: string;
}

const Login: React.FC<LoginProps> = ({ onLoginSuccess }) => {
  const [formData, setFormData] = useState<LoginForm>({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        onLoginSuccess(data.token, data.user);
      } else {
        await response.text(); // Ignorar el error data por ahora
        setError('Credenciales inválidas. Por favor, verifica tu email y contraseña.');
      }
    } catch (error) {
      console.error('Error de login:', error);
      setError('Error de conexión. Por favor, intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
    setError(''); // Limpiar error al escribir
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      padding: '20px'
    }}>
      <div style={{
        background: 'white',
        borderRadius: '15px',
        boxShadow: '0 20px 40px rgba(0,0,0,0.1)',
        padding: '40px',
        width: '100%',
        maxWidth: '400px'
      }}>
        {/* Logo y título */}
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          <div style={{
            width: '80px',
            height: '80px',
            background: 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            margin: '0 auto 20px',
            fontSize: '40px',
            color: 'white'
          }}>
            🌾
          </div>
          <h1 style={{
            margin: '0 0 10px 0',
            color: '#333',
            fontSize: '28px',
            fontWeight: 'bold'
          }}>
            AgroGestion
          </h1>
          <p style={{
            margin: '0',
            color: '#666',
            fontSize: '16px'
          }}>
            Sistema de Gestión Agropecuaria
          </p>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit}>
          {error && (
            <div style={{
              background: '#ffebee',
              color: '#c62828',
              padding: '12px',
              borderRadius: '8px',
              marginBottom: '20px',
              fontSize: '14px',
              border: '1px solid #ffcdd2'
            }}>
              ⚠️ {error}
            </div>
          )}

          <div style={{ marginBottom: '20px' }}>
            <label style={{
              display: 'block',
              marginBottom: '8px',
              color: '#333',
              fontWeight: 'bold',
              fontSize: '14px'
            }}>
              📧 Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              required
              style={{
                width: '100%',
                padding: '12px 16px',
                border: '2px solid #e0e0e0',
                borderRadius: '8px',
                fontSize: '16px',
                transition: 'border-color 0.3s ease',
                boxSizing: 'border-box'
              }}
              placeholder="usuario@ejemplo.com"
              onFocus={(e) => e.target.style.borderColor = '#4CAF50'}
              onBlur={(e) => e.target.style.borderColor = '#e0e0e0'}
            />
          </div>

          <div style={{ marginBottom: '30px' }}>
            <label style={{
              display: 'block',
              marginBottom: '8px',
              color: '#333',
              fontWeight: 'bold',
              fontSize: '14px'
            }}>
              🔒 Contraseña
            </label>
            <div style={{ position: 'relative' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                required
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  paddingRight: '50px',
                  border: '2px solid #e0e0e0',
                  borderRadius: '8px',
                  fontSize: '16px',
                  transition: 'border-color 0.3s ease',
                  boxSizing: 'border-box'
                }}
                placeholder="••••••••"
                onFocus={(e) => e.target.style.borderColor = '#4CAF50'}
                onBlur={(e) => e.target.style.borderColor = '#e0e0e0'}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '12px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '18px',
                  color: '#666'
                }}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading || !formData.email || !formData.password}
            style={{
              width: '100%',
              padding: '14px',
              background: loading ? '#ccc' : 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontSize: '16px',
              fontWeight: 'bold',
              cursor: loading ? 'not-allowed' : 'pointer',
              transition: 'transform 0.2s ease',
              boxShadow: '0 4px 15px rgba(76, 175, 80, 0.3)'
            }}
            onMouseEnter={(e) => {
              if (!loading) {
                e.currentTarget.style.transform = 'translateY(-2px)';
              }
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
            }}
          >
            {loading ? '🔄 Iniciando sesión...' : '🚀 Iniciar Sesión'}
          </button>
        </form>

        {/* Información adicional */}
        <div style={{
          marginTop: '30px',
          padding: '20px',
          background: '#f8f9fa',
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <h3 style={{
            margin: '0 0 10px 0',
            color: '#333',
            fontSize: '16px',
            fontWeight: 'bold'
          }}>
            👥 Usuarios de Prueba
          </h3>
          <div style={{ fontSize: '14px', color: '#666', lineHeight: '1.6' }}>
            <p style={{ margin: '0 0 5px 0' }}>
              <strong>Administrador:</strong> admin@agrogestion.com / admin123
            </p>
            <p style={{ margin: '0 0 5px 0' }}>
              <strong>Operario:</strong> operario@agrogestion.com / operario123
            </p>
            <p style={{ margin: '0 0 5px 0' }}>
              <strong>Ingeniero:</strong> ingeniero@agrogestion.com / ingeniero123
            </p>
            <p style={{ margin: '0' }}>
              <strong>Invitado:</strong> invitado@agrogestion.com / invitado123
            </p>
          </div>
        </div>

        {/* Footer */}
        <div style={{
          marginTop: '30px',
          textAlign: 'center',
          color: '#999',
          fontSize: '12px'
        }}>
          <p style={{ margin: '0' }}>
            © 2024 AgroGestion - Sistema de Gestión Agropecuaria
          </p>
          <p style={{ margin: '5px 0 0 0' }}>
            Desarrollado con ❤️ para el sector agrícola
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
