import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Button from '../components/ui/Button';
import { authService } from '../services/apiServices';
import './Login.css';

const VerificarCorreo: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [mensaje, setMensaje] = useState('Confirmando el correo...');
  const [listo, setListo] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    const token = searchParams.get('token');
    if (!token) {
      setError(true);
      setMensaje('El enlace de confirmación no es válido.');
      setListo(true);
      return;
    }

    let cancelado = false;
    authService.verificarCorreo(token)
      .then(() => {
        if (cancelado) return;
        setMensaje('Correo confirmado. Ya podés ingresar.');
        setListo(true);
      })
      .catch(() => {
        if (cancelado) return;
        setError(true);
        setMensaje('No se pudo confirmar el correo. El enlace puede haber vencido.');
        setListo(true);
      });

    return () => {
      cancelado = true;
    };
  }, [searchParams]);

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 login-background">
      <div className="absolute inset-0 login-overlay"></div>
      <div className="relative z-10 max-w-md w-full">
        <div className="login-card p-8 text-center">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Confirmación de correo</h2>
          <p className={error ? 'text-red-700 mb-6' : 'text-gray-700 mb-6'}>{mensaje}</p>
          {listo && (
            <Button variant="primary" size="lg" className="w-full" onClick={() => navigate('/login')}>
              Ir al ingreso
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default VerificarCorreo;
