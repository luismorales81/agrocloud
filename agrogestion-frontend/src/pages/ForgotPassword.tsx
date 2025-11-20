import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { authService, showNotification } from '../services/api';
import './Login.css';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      showNotification('Por favor ingrese su email', 'error');
      return;
    }

    setLoading(true);
    try {
      await authService.requestPasswordReset(email);
      setSuccess(true);
      showNotification('Se ha enviado un email con las instrucciones', 'success');
    } catch (error: any) {
      showNotification(error.response?.data?.error || 'Error al enviar el email', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 login-background">
        {/* Overlay oscuro para mejorar legibilidad */}
        <div className="absolute inset-0 login-overlay"></div>
        
        {/* Contenido */}
        <div className="relative z-10 max-w-md w-full space-y-8">
          <div>
            <h2 className="mt-6 text-center text-4xl font-extrabold text-white login-title">
              Email Enviado
            </h2>
            <p className="mt-2 text-center text-lg text-white login-subtitle">
              Revisa tu bandeja de entrada
            </p>
          </div>
          
          <div className="login-card p-8 text-center">
            <div className="mb-4">
              <svg className="mx-auto h-12 w-12 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="text-gray-700 mb-6">
              Hemos enviado un enlace de recuperación a tu email. 
              Haz clic en el enlace para restablecer tu contraseña.
            </p>
            <Link to="/login">
              <Button variant="primary" size="lg" className="w-full">
                Volver al Login
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 login-background">
      {/* Overlay oscuro para mejorar legibilidad */}
      <div className="absolute inset-0 login-overlay"></div>
      
      {/* Contenido */}
      <div className="relative z-10 max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-4xl font-extrabold text-white login-title">
            Recuperar Contraseña
          </h2>
          <p className="mt-2 text-center text-lg text-white login-subtitle">
            Ingresa tu email para recibir instrucciones
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="login-card p-8">
            <Input
              label="Email"
              type="email"
              placeholder="Ingrese su email"
              value={email}
              onChange={setEmail}
              required
            />
            
            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={loading}
              className="w-full"
            >
              {loading ? 'Enviando...' : 'Enviar Instrucciones'}
            </Button>
          </div>
        </form>
        
        <div className="text-center">
          <Link to="/login" className="text-sm text-white hover:text-gray-200 login-link">
            ¿Recordaste tu contraseña? Inicia sesión
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
