# Reporte de verificación CRUD automatizada

**Fecha:** Julio 2026 (actualizado)  
**SPEC:** `SPEC-VERIFICACION-CRUD-MODULOS.md`  
**Ejecución:** `mvn test -Dtest=*CrudIntegracionTest` (perfil `test`, BD `agrocloud`)

---

## Resumen ejecutivo

| Métrica | Valor |
|---------|-------|
| Tests CRUD (`*CrudIntegracionTest`) | **43** |
| Pasaron | **43** |
| Fallaron | **0** |
| CRUD completo (C+R+U+D) | Campos, Insumos, Maquinaria, Recordatorios, Campañas, Ingresos, Egresos, Cultivos, Lotes, Madres, **Padrillos**, Tareas recurrentes, Labores, Pesadas feedlot, Consumos feedlot, **Config. estados** |
| CRUD parcial (C+U o C+R+D) | Feedlot (establecimientos, corrales, lotes, dietas), Lechería, Avícola crianza/huevos, **Ponedoras galpones** |
| Catálogos porcinos (POST+GET+DELETE) | Razas, tipos servicio, causas mortalidad, motivos baja, esquemas sanitarios, tipos parto, tipos evento sanitario, ubicaciones internas (+ jerarquía) |
| Smoke (solo lectura) | Roles, Lotes porcinos |

---

## Correcciones aplicadas (esta iteración)

1. **`User.tieneRolEnEmpresa`:** manejo defensivo de `LazyInitializationException` (fix DELETE labor).
2. **`LaborController`:** `findByEmailWithAllRelationsCombined`.
3. **Flyway `V1_0`:** esquema base legacy (`usuarios`, `lotes`, `labores`, `empresas`, etc.) para BD vacía.
4. **Flyway `V1_8`–`V1_11`:** migraciones defensivas (solo alteran si existen tablas destino).
5. **CI:** job `flyway-migrate` verifica migrate en BD vacía; job `test` aplica Flyway antes de tests en `agrocloud_test_integration`.
6. **Tests nuevos:** pesadas/consumos feedlot, catálogos porcinos, padrillos, configuración estados, **galpones ponedoras**.
7. **Gaps UI:** maquinaria baja vía API; formulario galpones ponedoras C/U.
8. **E2E:** Playwright smoke en `agrogestion-frontend/e2e/` (ver `docs/E2E-PLAYWRIGHT.md`).

---

## Tests implementados

| Clase | Módulo | Cobertura |
|-------|--------|-----------|
| `FieldControllerCrudIntegracionTest` | Cultivos | CRUD completo `/api/campos` |
| `InsumoControllerCrudIntegracionTest` | Cultivos | CRUD completo `/api/insumos` |
| `MaquinariaControllerCrudIntegracionTest` | Cultivos | CRUD; DELETE lógico |
| `PlotControllerCrudIntegracionTest` | Cultivos | CRUD lotes |
| `CultivoControllerCrudIntegracionTest` | Cultivos | CRUD cultivos |
| `IngresoCrudIntegracionTest` | Cultivos/Finanzas | CRUD `/api/v1/ingresos` |
| `EgresoCrudIntegracionTest` | Cultivos/Finanzas | CRUD `/api/v1/egresos` |
| `LaborControllerCrudIntegracionTest` | Cultivos | CRUD labores |
| `RecordatorioCrudIntegracionTest` | Admin | CRUD; DELETE lógico |
| `CampanaCrudIntegracionTest` | Admin | Crear, activar, cerrar |
| `CalendarioTareasRecurrentesCrudIntegracionTest` | Admin | CRUD tareas recurrentes |
| `RoleControllerCrudIntegracionTest` | Admin | GET listado roles |
| `FeedlotEstablecimientoCrudIntegracionTest` | Feedlot | C+U establecimientos |
| `FeedlotCorralCrudIntegracionTest` | Feedlot | C+U corrales |
| `FeedlotLoteCrudIntegracionTest` | Feedlot | C+U lotes |
| `FeedlotDietaCrudIntegracionTest` | Feedlot | C+U dietas |
| `FeedlotPesadaCrudIntegracionTest` | Feedlot | CRUD pesadas por lote |
| `FeedlotConsumoCrudIntegracionTest` | Feedlot | CRUD consumos por lote |
| `LecheriaEstablecimientoCrudIntegracionTest` | Lechería | C+U establecimientos |
| `AvicolaCrianzaCrudIntegracionTest` | Avícola | C+U establecimientos |
| `AvicolaHuevosCrudIntegracionTest` | Avícola | C+U establecimientos |
| `AvicolaPonedorasCrudIntegracionTest` | Avícola | CRUD galpones ponedoras |
| `MadreControllerCrudIntegracionTest` | Porcinos | CRUD madres |
| `PadrilloControllerCrudIntegracionTest` | Porcinos | CRUD padrillos |
| `CatalogosPorcinoCrudIntegracionTest` | Porcinos | Todos los catálogos: razas, tipos servicio, causas mortalidad, motivos baja, esquemas sanitarios, tipos parto, eventos sanitarios, ubicaciones (jerarquía), smoke lotes |
| `ConfiguracionEstadosCrudIntegracionTest` | Cultivos | CRUD tipo cultivo + estado; plantilla Excel |

---

## Hallazgos pendientes

Ninguno crítico en el alcance de SPEC-VERIFICACION-CRUD-MODULOS.

### Mejoras opcionales

- Ampliar Playwright e2e (más módulos, CI con credenciales).
- `flyway:repair` en BD `agrocloud` dev si hay checksum mismatch (`scripts/flyway-repair-dev.ps1`).

---

## Cómo ejecutar

```bash
cd agrogestion-backend/src/test/resources
cp application-test.properties.example application-test.properties
# Ajustar usuario/contraseña MySQL y BD agrocloud (esquema migrado)

cd ../..
mvn test -Dtest="*CrudIntegracionTest"
```

Probar Flyway en BD vacía local:

```bash
cd agrogestion-backend
mvn flyway:migrate \
  -Dflyway.url="jdbc:mysql://localhost:3306/agrocloud_flyway_test?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -Dflyway.user=root \
  -Dflyway.password=TU_PASSWORD
```
