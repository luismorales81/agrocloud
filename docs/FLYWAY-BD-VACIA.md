# Flyway en BD vacía — progreso y uso

## Objetivo

Permitir `flyway:migrate` sobre MySQL vacío (CI y desarrollo) sin depender de dumps manuales.

## Cómo probar en local

```powershell
cd agrogestion-backend
$env:FLYWAY_URL = 'jdbc:mysql://localhost:3306/agrocloud_flyway_test?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:FLYWAY_USER = 'root'
$env:FLYWAY_PASSWORD = 'TU_PASSWORD'

# Solo BD de prueba dedicada (destructivo):
mvn flyway:clean flyway:migrate "-Dflyway.cleanDisabled=false" "-Dflyway.outOfOrder=true"

# Continuar tras fallo parcial:
mvn flyway:repair flyway:migrate "-Dflyway.outOfOrder=true"

mvn flyway:info "-Dflyway.outOfOrder=true"
```

**No ejecutar `flyway:clean` sobre `agrocloud` de desarrollo.**

## Migraciones corregidas (modo defensivo)

| Versión | Cambio |
|---------|--------|
| **V1_0** | Esquema base: usuarios, empresas, lotes, labores, insumos (`stock_actual`), **modules**, **company_modules**, **usuario_empresas** |
| **V1_03** | Respaldo: crea `modules` / `company_modules` si faltan |
| **V1_4–V1_11** | ALTER condicionales si existen tablas legacy |
| **V1_12** | Omite migración `cosechas` si la tabla no existe |
| **V1_18, V1_19, V1_23** | ALTER idempotentes |
| **V1_27** | Índices sin `CREATE INDEX IF NOT EXISTS` (incompatible MySQL) |
| **V1_101** | Quitados CHECK que chocan con FK en MySQL 8 |
| **V1_112** | `permite_stock_negativo` sin `AFTER stock_actual` obligatorio |
| **V1_135** | Data migration omitida en BD vacía; COLLATE en joins |
| **V1_136** | `CREATE TABLE IF NOT EXISTS modules` antes del INSERT |
| **V1_147** | Agrega `activo` en `cultivo_labores` si falta; índice condicional |
| **V1_148** | Backfills de campaña/ciclos condicionales (sin `empresa_id` en `cultivo_campos`) |
| **V1_164** | `realiza_faena` opcional en migración legacy porcinos |

## Estado actual

**✅ Verificado:** `flyway:clean` + `flyway:migrate` sobre BD vacía (`agrocloud_flyway_test`) aplica **101 migraciones** hasta `v20251030.001` sin errores (~45 s).

```powershell
mvn flyway:info "-Dflyway.outOfOrder=true"
# Current version: v20251030.001
```

## CI

Job `flyway-migrate` en `.github/workflows/maven-tests.yml` (MySQL 8 service, BD `agrocloud_flyway_ci_test`). El job **falla el workflow** si migrate no completa (sin `continue-on-error`).

## BDs ya migradas en producción/dev

Si cambió el checksum de migraciones ya aplicadas:

```powershell
mvn flyway:repair "-Dflyway.outOfOrder=true"
```

Solo en la BD afectada, con backup previo.

## Próximos pasos (opcional)

1. `flyway:repair` en BD `agrocloud` de desarrollo si cambiaron checksums — script `agrogestion-backend/scripts/flyway-repair-dev.ps1` (con backup).
2. Tests e2e autenticados en CI requieren secrets `E2E_EMAIL` / `E2E_PASSWORD`.
