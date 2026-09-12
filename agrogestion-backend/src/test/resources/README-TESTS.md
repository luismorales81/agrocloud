# Tests de integración — AgroGestion Backend

## Requisitos

- JDK 17
- MySQL 8 con base de datos `agrocloud_test`
- Maven 3.8+

## Configuración inicial

```bash
# Crear base de datos (nombre dedicado para no mezclar con dev ni historial Flyway corrupto)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS agrocloud_test_integration CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Copiar configuración de test
cd agrogestion-backend/src/test/resources
cp application-test.properties.example application-test.properties
# Editar application-test.properties si usuario/contraseña difieren de root/sin contraseña
```

## Ejecutar tests

```bash
cd agrogestion-backend

# Suite completa
mvn test -Dspring.profiles.active=test

# Solo módulo cultivos
mvn test -Dtest="com.agrocloud.cultivos.*" -Dspring.profiles.active=test

# Un test concreto
mvn test -Dtest=FieldControllerCrudIntegracionTest -Dspring.profiles.active=test
```

## Variables de entorno (CI / local)

| Variable | Descripción |
|----------|-------------|
| `DATABASE_URL` | JDBC URL (default: localhost/agrocloud_test) |
| `DATABASE_USERNAME` / `DB_USER` | Usuario MySQL |
| `DATABASE_PASSWORD` / `DB_PASS` | Contraseña MySQL |
| `JWT_SECRET` | Clave JWT para contexto Spring (mín. 32 chars) |
