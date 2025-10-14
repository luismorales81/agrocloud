# 📝 Scripts de Migración - Nota Importante

## ✅ Scripts TODO-EN-UNO (Recomendados)

Usa estos archivos para migrar la base de datos. Incluyen TODO en un solo archivo:

### 🧪 Para Testing:
```
database-migration-testing-completo.sql
```

**Incluye:**
- ✅ Eliminación de tablas existentes
- ✅ Creación de 19 tablas nuevas
- ✅ 13 roles del sistema
- ✅ 14 cultivos base
- ✅ 5 usuarios de prueba
- ✅ 3 campos, 6 lotes, 8 insumos, 3 maquinarias
- ✅ Verificación final

**Ejecutar:**
```bash
mysql -u root -p agrogestion_db < database-migration-testing-completo.sql
```

O con el script .bat:
```bash
.\aplicar-migracion-testing.bat
```

---

### 🚀 Para Production:
```
database-migration-production-completo.sql
```

**Incluye:**
- ✅ Eliminación de tablas existentes
- ✅ Creación de 19 tablas nuevas
- ✅ 13 roles del sistema
- ✅ 14 cultivos base
- ✅ 1 usuario administrador (password temporal)
- ✅ 1 empresa (debes actualizar datos)
- ✅ Verificación final

**Ejecutar:**
```bash
mysql -u root -p agrogestion_db < database-migration-production-completo.sql
```

O con el script .bat:
```bash
.\aplicar-migracion-production.bat
```

---

## ⚠️ Scripts con SOURCE (Alternativos)

Si prefieres ejecutar los scripts por partes:

```
database-migration-master-testing.sql
database-migration-master-production.sql
```

Estos scripts usan comandos `SOURCE` para ejecutar otros archivos SQL separados.

**⚠️ Problema:** El comando `SOURCE` no siempre funciona bien cuando se ejecuta desde fuera de MySQL (por ejemplo, con pipe `<`).

**Solución:** Usa los scripts completos arriba, que tienen todo incluido.

---

## 🔍 Diferencias

| Característica | Scripts Completos | Scripts Master |
|----------------|-------------------|----------------|
| **Archivos** | 1 archivo TODO-EN-UNO | Múltiples archivos |
| **SOURCE** | No usa SOURCE | Usa SOURCE |
| **Ejecución** | Pipe (`<`) o source | Solo con source |
| **Railway** | ✅ Funciona | ⚠️ Puede fallar |
| **Local** | ✅ Funciona | ✅ Funciona |
| **Recomendado** | ⭐ SÍ | Solo si conoces SOURCE |

---

## 📋 Resumen

### Para Railway (Testing o Production):
```bash
# Conectar a MySQL Railway
mysql -h $MYSQLHOST -P $MYSQLPORT -u $MYSQLUSER -p$MYSQLPASSWORD $MYSQLDATABASE < database-migration-testing-completo.sql
```

### Para Local:
```bash
# Testing
.\aplicar-migracion-testing.bat

# Production
.\aplicar-migracion-production.bat
```

---

## ✅ Qué hace cada script completo:

1. **Desactiva** las restricciones de integridad referencial
2. **Elimina** todas las tablas existentes (en el orden correcto)
3. **Crea** todas las tablas nuevas
4. **Inserta** roles del sistema
5. **Inserta** cultivos base
6. **Inserta** datos específicos (testing o production)
7. **Reactiva** las restricciones
8. **Verifica** que todo se creó correctamente

---

## 🎯 Recomendación

**Usa los scripts completos:**
- `database-migration-testing-completo.sql` ⭐
- `database-migration-production-completo.sql` ⭐

Son más fáciles de ejecutar y funcionan en cualquier entorno.

---

**Fecha:** 10 de Octubre de 2025

