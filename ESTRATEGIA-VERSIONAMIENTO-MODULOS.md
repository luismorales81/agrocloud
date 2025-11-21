# Estrategia de Versionamiento y Desarrollo por Módulos

## 📋 Resumen Ejecutivo

Este documento define la estrategia de versionamiento semántico y el flujo de trabajo para desarrollar el sistema AgroGestion de forma modular, permitiendo desarrollo paralelo, releases independientes y mejor trazabilidad.

---

## 🎯 Objetivos

1. **Desarrollo Modular**: Permitir desarrollo paralelo de diferentes módulos
2. **Versionamiento Semántico**: Seguir estándares internacionales (SemVer)
3. **Trazabilidad**: Rastrear qué módulo y versión está en cada entorno
4. **Releases Independientes**: Publicar módulos sin afectar otros
5. **Rollback Selectivo**: Poder revertir módulos específicos sin afectar el resto

---

## 📦 Sistema de Versionamiento Semántico (SemVer)

### Formato: `MAJOR.MINOR.PATCH[-MODULE][-BUILD]`

**Ejemplo**: `1.2.3-modulo-agroquimicos-20250116`

### Componentes:

- **MAJOR** (1): Cambios incompatibles con versiones anteriores
- **MINOR** (2): Nuevas funcionalidades compatibles hacia atrás
- **PATCH** (3): Correcciones de bugs compatibles
- **MODULE** (opcional): Identificador del módulo principal afectado
- **BUILD** (opcional): Fecha o número de build

### Versión Base del Proyecto

- **Backend**: `1.0.0` → Actualizar a `1.1.0` para el próximo release
- **Frontend**: `0.0.0` → Actualizar a `1.0.0` para el próximo release

---

## 🌳 Estructura de Ramas

### Ramas Principales

```
main (develop)
├── production (release estable)
├── testing (pre-release)
└── feature/modulo-*
    ├── feature/modulo-agroquimicos-*
    ├── feature/modulo-cultivos-*
    ├── feature/modulo-balance-*
    └── ...
```

### Convención de Nombres de Ramas

#### Ramas de Feature (Módulos)
```
feature/modulo-{nombre-modulo}-{descripcion-corta}
feature/modulo-agroquimicos-gestion-dosis
feature/modulo-cultivos-planificacion-siembra
feature/modulo-balance-reportes-financieros
```

#### Ramas de Hotfix
```
hotfix/modulo-{nombre-modulo}-{descripcion-corta}
hotfix/modulo-auth-correccion-login
hotfix/modulo-agroquimicos-fix-calculo-dosis
```

#### Ramas de Release
```
release/v{version}-{modulo}
release/v1.2.0-agroquimicos
release/v1.1.5-balance
```

---

## 📚 Módulos Identificados

### Módulos Principales del Sistema

| Módulo | Código | Descripción | Estado Actual |
|--------|--------|-------------|---------------|
| **Autenticación** | `auth` | Login, registro, recuperación contraseña, EULA | ✅ Estable |
| **Administración Global** | `admin-global` | Gestión multiempresa, SuperAdmin | ✅ Estable |
| **Empresas** | `empresas` | CRUD empresas, usuarios-empresa | ✅ Estable |
| **Campos y Lotes** | `campos` | Gestión de campos, lotes, parcelas | ✅ Estable |
| **Cultivos** | `cultivos` | Gestión de cultivos, siembras | ✅ Estable |
| **Cosechas** | `cosechas` | Historial de cosechas, rendimientos | ✅ Estable |
| **Insumos** | `insumos` | Gestión de insumos, inventario | ✅ Estable |
| **Agroquímicos** | `agroquimicos` | Gestión de agroquímicos, dosis, aplicaciones | 🚧 En desarrollo |
| **Labores** | `labores` | Gestión de labores agrícolas | ✅ Estable |
| **Maquinaria** | `maquinaria` | Gestión de maquinaria | ✅ Estable |
| **Finanzas** | `finanzas` | Ingresos, egresos, balance | ✅ Estable |
| **Rendimientos** | `rendimientos` | Análisis de rendimientos, estadísticas | ✅ Estable |
| **Dashboard** | `dashboard` | Dashboards y reportes | ✅ Estable |
| **Clima** | `weather` | Integración con API de clima | ✅ Estable |
| **Roles y Permisos** | `roles` | Sistema de roles y permisos | ✅ Estable |

---

## 🔄 Flujo de Trabajo por Módulos

### 1. Iniciar Desarrollo de un Módulo

```bash
# 1. Asegurarse de estar en testing actualizada
git checkout testing
git pull origin testing

# 2. Crear rama de feature para el módulo
git checkout -b feature/modulo-{nombre}-{descripcion}

# Ejemplo:
git checkout -b feature/modulo-agroquimicos-gestion-dosis
```

### 2. Desarrollo del Módulo

```bash
# Trabajar en la rama de feature
# Hacer commits frecuentes con mensajes descriptivos
git add .
git commit -m "feat(modulo-agroquimicos): Agregar cálculo de dosis por hectárea"
git commit -m "fix(modulo-agroquimicos): Corregir validación de dosis máxima"
```

### 3. Convención de Mensajes de Commit

Usar formato: `tipo(modulo): descripción`

**Tipos**:
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `docs`: Documentación
- `style`: Formato (sin cambios de código)
- `refactor`: Refactorización
- `test`: Tests
- `chore`: Tareas de mantenimiento

**Ejemplos**:
```
feat(modulo-agroquimicos): Agregar endpoint para calcular dosis
fix(modulo-auth): Corregir validación de token expirado
docs(modulo-balance): Actualizar documentación de API
refactor(modulo-cultivos): Optimizar consulta de cultivos activos
```

### 4. Merge a Testing

```bash
# 1. Actualizar la rama de feature con los últimos cambios de testing
git checkout feature/modulo-{nombre}-{descripcion}
git pull origin testing
git rebase origin/testing  # o git merge origin/testing

# 2. Resolver conflictos si los hay
# 3. Push de la rama de feature
git push origin feature/modulo-{nombre}-{descripcion}

# 4. Crear Pull Request en GitHub de feature → testing
# 5. Revisar y aprobar PR
# 6. Merge a testing
```

### 5. Release a Production

```bash
# 1. Crear rama de release
git checkout testing
git pull origin testing
git checkout -b release/v{version}-{modulo}

# Ejemplo:
git checkout -b release/v1.2.0-agroquimicos

# 2. Actualizar versiones en archivos
# - Backend: pom.xml
# - Frontend: package.json
# - CHANGELOG.md

# 3. Commit de versiones
git add .
git commit -m "chore: Bump version to 1.2.0-modulo-agroquimicos"

# 4. Merge a production
git checkout production
git pull origin production
git merge release/v{version}-{modulo}

# 5. Tag de versión
git tag -a v{version}-{modulo} -m "Release v{version}-{modulo}: {descripción}"
git push origin production
git push origin v{version}-{modulo}
```

---

## 📝 Archivos de Versionamiento

### Backend (pom.xml)

```xml
<groupId>com.agrocloud</groupId>
<artifactId>agrocloud-backend</artifactId>
<version>1.1.0</version>
```

### Frontend (package.json)

```json
{
  "name": "agrocloud-frontend",
  "version": "1.0.0"
}
```

### CHANGELOG.md (Nuevo)

Mantener un registro de cambios por módulo y versión.

---

## 🏷️ Sistema de Tags

### Formato de Tags

```
v{MAJOR}.{MINOR}.{PATCH}-{MODULE}
v1.2.0-agroquimicos
v1.1.5-balance
v2.0.0-auth
```

### Tags Especiales

- `v{version}`: Release completo del sistema
- `v{version}-{modulo}`: Release de módulo específico
- `latest`: Última versión estable (production)

---

## 📊 Matriz de Versiones por Módulo

Mantener un registro de qué versión de cada módulo está en cada entorno:

| Módulo | Testing | Production | Última Versión |
|--------|---------|------------|----------------|
| auth | 1.0.0 | 1.0.0 | 1.0.0 |
| agroquimicos | 1.1.0 | 1.0.0 | 1.1.0 |
| balance | 1.0.5 | 1.0.5 | 1.0.5 |
| ... | ... | ... | ... |

---

## 🔍 Monitoreo y Trazabilidad

### Endpoints de Versión (Recomendado)

Agregar endpoints para consultar versiones:

```java
@GetMapping("/api/version")
public ResponseEntity<Map<String, String>> getVersion() {
    Map<String, String> version = new HashMap<>();
    version.put("backend", "1.1.0");
    version.put("frontend", "1.0.0");
    version.put("modules", getModulesVersions());
    return ResponseEntity.ok(version);
}
```

### Headers HTTP

Incluir versión en headers de respuesta:
```
X-API-Version: 1.1.0
X-Module-Version: agroquimicos-1.1.0
```

---

## 🚀 Próximos Pasos de Implementación

### Fase 1: Configuración Inicial (Inmediato)

1. ✅ Actualizar versiones base:
   - Backend: `1.0.0` → `1.1.0`
   - Frontend: `0.0.0` → `1.0.0`

2. ✅ Crear archivo `CHANGELOG.md`

3. ✅ Configurar tags iniciales:
   ```bash
   git tag -a v1.0.0 -m "Release inicial v1.0.0"
   git push origin v1.0.0
   ```

### Fase 2: Estructura de Ramas (Esta Semana)

1. Crear ramas base para módulos principales
2. Documentar módulos existentes
3. Establecer convenciones de nombres

### Fase 3: Automatización (Próximas 2 Semanas)

1. Scripts para bump de versiones
2. GitHub Actions para CI/CD por módulos
3. Generación automática de CHANGELOG

### Fase 4: Monitoreo (Próximo Mes)

1. Endpoints de versión
2. Dashboard de versiones
3. Alertas de desincronización

---

## 📋 Checklist para Nuevo Módulo

- [ ] Crear rama `feature/modulo-{nombre}-{descripcion}`
- [ ] Actualizar documentación del módulo
- [ ] Implementar funcionalidad
- [ ] Escribir tests
- [ ] Actualizar CHANGELOG.md
- [ ] Crear PR a testing
- [ ] Revisar y aprobar PR
- [ ] Merge a testing
- [ ] Probar en testing
- [ ] Crear release branch
- [ ] Bump versiones
- [ ] Merge a production
- [ ] Crear tag de versión
- [ ] Actualizar matriz de versiones

---

## 🔗 Referencias

- [Semantic Versioning 2.0.0](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)

---

**Última actualización**: 2025-01-16
**Versión del documento**: 1.0.0

