import React, { Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useModule } from '../core/hooks/useModule';
import { getModuleRoutes } from '../modules';
import ProtectedRouteComponent from '../components/ProtectedRoute';
import type { Permissions } from '../hooks/usePermissions';

const CargandoPantalla: React.FC = () => (
  <div style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>Cargando...</div>
);

function envolverRuta(elemento: React.ReactNode) {
  return <Suspense fallback={<CargandoPantalla />}>{elemento}</Suspense>;
}

/**
 * Navegador principal que carga las rutas dinámicamente según el módulo activo
 */
const AppNavigator: React.FC = () => {
  const { currentModule } = useModule();

  if (!currentModule) {
    return <Navigate to="/select-module" replace />;
  }

  const moduleRoutes = getModuleRoutes(currentModule);

  // Encontrar el componente del dashboard
  const dashboardRoute = moduleRoutes.find(r => r.path.includes('/dashboard'));
  const DashboardComponent = dashboardRoute?.component;

  return (
    <Routes>
      {/* Ruta raíz del módulo - mostrar dashboard directamente */}
      {DashboardComponent && (
        <Route
          path=""
          element={
            dashboardRoute?.permisos ? (
              envolverRuta(
                <ProtectedRouteComponent permission={dashboardRoute.permisos[0] as keyof Permissions}>
                  <DashboardComponent />
                </ProtectedRouteComponent>
              )
            ) : (
              envolverRuta(<DashboardComponent />)
            )
          }
        />
      )}
      {/* Ruta explícita del dashboard */}
      {DashboardComponent && (
        <Route
          path="dashboard"
          element={
            dashboardRoute?.permisos ? (
              envolverRuta(
                <ProtectedRouteComponent permission={dashboardRoute.permisos[0] as keyof Permissions}>
                  <DashboardComponent />
                </ProtectedRouteComponent>
              )
            ) : (
              envolverRuta(<DashboardComponent />)
            )
          }
        />
      )}
      {/* Resto de las rutas del módulo */}
      {moduleRoutes
        .filter(route => !route.path.includes('/dashboard'))
        .map((route) => {
          // Extraer la parte relativa de la ruta (sin el prefijo del módulo)
          let relativePath = route.path.replace(`/${currentModule}`, '');
          // Remover el slash inicial si existe
          if (relativePath.startsWith('/')) {
            relativePath = relativePath.substring(1);
          }
          
          return (
            <Route
              key={route.path}
              path={relativePath}
              element={
                route.permisos ? (
                  envolverRuta(
                    <ProtectedRouteComponent permission={route.permisos[0] as keyof Permissions}>
                      <route.component />
                    </ProtectedRouteComponent>
                  )
                ) : (
                  envolverRuta(<route.component />)
                )
              }
            />
          );
        })}
      {/* Catch-all para rutas no encontradas dentro del módulo */}
      <Route
        path="*"
        element={<Navigate to={`/${currentModule}/dashboard`} replace />}
      />
    </Routes>
  );
};

export default AppNavigator;

