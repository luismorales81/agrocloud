# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Módulos en Desarrollo
- `modulo-agroquimicos`: Gestión de agroquímicos, dosis y aplicaciones

---

## [1.1.0] - 2025-01-16

### Añadido
- **modulo-auth**: Sistema de recuperación de contraseña con email Zoho Mail
- **modulo-auth**: Sistema EULA completo con aceptación y generación de PDF
- **modulo-admin-global**: Enmascaramiento de datos sensibles (CUIT, email, montos)
- **modulo-admin-global**: Validación SQL mejorada
- **modulo-admin-global**: Logging de seguridad mejorado

### Modificado
- **modulo-auth**: Mejoras en validación de tokens JWT
- **modulo-frontend**: Actualización de Service Worker para producción
- **modulo-frontend**: Detección automática de entorno de producción

### Corregido
- **modulo-frontend**: Corrección en interceptación de localhost en producción
- **modulo-frontend**: Corrección en detección de URL de Railway

---

## [1.0.0] - 2024-12-30

### Añadido
- **modulo-auth**: Sistema de autenticación con JWT
- **modulo-auth**: Login, registro y gestión de usuarios
- **modulo-admin-global**: Dashboard de administración global
- **modulo-empresas**: Gestión multiempresa completa
- **modulo-campos**: Gestión de campos, lotes y parcelas
- **modulo-cultivos**: Gestión de cultivos y siembras
- **modulo-cosechas**: Historial de cosechas y rendimientos
- **modulo-insumos**: Gestión de insumos e inventario
- **modulo-labores**: Gestión de labores agrícolas
- **modulo-maquinaria**: Gestión de maquinaria
- **modulo-finanzas**: Sistema de ingresos, egresos y balance
- **modulo-rendimientos**: Análisis de rendimientos y estadísticas
- **modulo-dashboard**: Dashboards y reportes
- **modulo-weather**: Integración con API de clima
- **modulo-roles**: Sistema de roles y permisos

### Módulos Iniciales
- Autenticación y autorización
- Administración global multiempresa
- Gestión de empresas y usuarios
- Gestión de campos y cultivos
- Gestión financiera
- Reportes y dashboards

---

## Formato de Entradas

### Tipos de Cambios
- **Añadido**: Nuevas funcionalidades
- **Modificado**: Cambios en funcionalidades existentes
- **Deprecado**: Funcionalidades que serán eliminadas
- **Eliminado**: Funcionalidades eliminadas
- **Corregido**: Correcciones de bugs
- **Seguridad**: Correcciones de seguridad

### Estructura por Módulo

Cada entrada debe incluir:
- **Módulo afectado**: `modulo-{nombre}`
- **Tipo de cambio**: Añadido, Modificado, Corregido, etc.
- **Descripción**: Breve descripción del cambio

---

## Referencias

- [Semantic Versioning](https://semver.org/lang/es/)
- [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/)

