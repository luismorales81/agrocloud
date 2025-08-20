# AgroGestion Frontend

Sistema de gestión agrícola - Frontend en React con TypeScript

## 🚀 Características

- **Autenticación JWT**: Sistema de login seguro con tokens
- **Interfaz Responsive**: Diseño adaptativo con TailwindCSS
- **Componentes Reutilizables**: UI components modulares
- **Gestión de Estado**: Context API para autenticación
- **Interceptores Axios**: Manejo automático de tokens y errores
- **Notificaciones**: Sistema de alertas amigable

## 🛠️ Tecnologías

- **React 18** con TypeScript
- **Vite** como bundler
- **TailwindCSS** para estilos
- **React Router DOM** para navegación
- **Axios** para peticiones HTTP
- **Recharts** para gráficos

## 📋 Prerrequisitos

- Node.js 16+ 
- npm o yarn
- Backend AgroGestion ejecutándose en `http://localhost:8080`

## 🔧 Instalación

1. **Clonar el repositorio**:
```bash
git clone <repository-url>
cd agrogestion-frontend
```

2. **Instalar dependencias**:
```bash
npm install
```

3. **Configurar variables de entorno** (opcional):
```bash
# Crear archivo .env.local
VITE_API_BASE_URL=http://localhost:8080/api
```

4. **Ejecutar en desarrollo**:
```bash
npm run dev
```

5. **Abrir en el navegador**:
```
http://localhost:5173
```

## 📁 Estructura del Proyecto

```
src/
├── components/
│   └── ui/                 # Componentes UI reutilizables
│       ├── Button.tsx
│       └── Input.tsx
├── contexts/
│   └── AuthContext.tsx     # Contexto de autenticación
├── pages/
│   └── Login.tsx           # Página de login
├── services/
│   └── api.ts              # Servicios API con Axios
├── App.tsx                 # Componente principal
├── main.tsx               # Punto de entrada
└── index.css              # Estilos globales
```

## 🔐 Autenticación

### Configuración JWT

El sistema utiliza interceptores de Axios para:

- **Agregar automáticamente** el token JWT a todas las peticiones
- **Manejar errores 401** (token expirado) redirigiendo al login
- **Mostrar notificaciones** de errores amigables

### Usuarios de Prueba

```
admin / admin123
tecnico / tecnico123
productor / productor123
```

## 🎨 Componentes UI

### Button
```tsx
<Button 
  variant="primary" 
  size="md" 
  onClick={handleClick}
>
  Click me
</Button>
```

### Input
```tsx
<Input
  label="Email"
  type="email"
  value={email}
  onChange={setEmail}
  required
/>
```

## 📡 Servicios API

### Configuración Axios

```typescript
// Interceptor de requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor de responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirigir al login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### Servicios Disponibles

- `authService`: Login, logout, verificación de autenticación
- `userService`: CRUD de usuarios
- `fieldService`: CRUD de campos
- `plotService`: CRUD de lotes
- `cropService`: CRUD de cultivos
- `inputService`: CRUD de insumos
- `laborService`: CRUD de labores
- `reportService`: Reportes

## 🔧 Scripts Disponibles

```bash
npm run dev          # Servidor de desarrollo
npm run build        # Build de producción
npm run preview      # Preview del build
npm run lint         # Linting con ESLint
```

## 🌐 Configuración de Desarrollo

### Variables de Entorno

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=AgroGestion
```

### Configuración TailwindCSS

```javascript
// tailwind.config.js
module.exports = {
  content: ["./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: { /* ... */ },
        success: { /* ... */ },
        warning: { /* ... */ },
        danger: { /* ... */ }
      }
    }
  }
}
```

## 🚀 Despliegue

### Build de Producción

```bash
npm run build
```

### Servir Build

```bash
npm run preview
```

### Despliegue en Servidor

1. Ejecutar `npm run build`
2. Copiar contenido de `dist/` al servidor web
3. Configurar servidor para servir `index.html` en rutas SPA

## 🔍 Troubleshooting

### Error: "Cannot find module 'tailwindcss'"
```bash
npm install -D tailwindcss postcss autoprefixer
```

### Error: "Module not found"
```bash
npm install
```

### Error de conexión al backend
- Verificar que el backend esté ejecutándose en `http://localhost:8080`
- Verificar configuración CORS en el backend
- Revisar logs del navegador (F12)

## 📝 Notas de Desarrollo

- **TypeScript**: El proyecto usa TypeScript para mejor DX
- **ESLint**: Configurado para mantener calidad de código
- **Prettier**: Formateo automático de código
- **Husky**: Git hooks para pre-commit

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 📞 Soporte

Para soporte técnico, contactar al equipo de desarrollo.
