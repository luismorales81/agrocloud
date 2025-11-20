# 📄 Implementación del Sistema EULA (End User License Agreement)

## ✅ Implementación Completada

Se ha implementado un sistema completo de EULA con firma simple dentro del sistema, que cumple con los requisitos legales de Argentina según el Código Civil y Comercial (CCCN arts. 1100-1103).

---

## 🎯 Funcionalidades Implementadas

### Backend (Spring Boot)

1. **Migración SQL** (`V1_23__Add_EULA_Fields_To_Usuarios.sql`)
   - Campos agregados a la tabla `usuarios`:
     - `eula_aceptado` (BOOLEAN)
     - `eula_fecha_aceptacion` (DATETIME)
     - `eula_ip_address` (VARCHAR)
     - `eula_user_agent` (VARCHAR)
     - `eula_version` (VARCHAR)
     - `eula_pdf_path` (VARCHAR)

2. **Entidad User** - Campos EULA agregados con getters/setters

3. **DTOs Creados:**
   - `AceptarEulaRequest.java` - Para aceptar el EULA
   - `EulaEstadoResponse.java` - Para consultar el estado del EULA

4. **Excepción Personalizada:**
   - `EulaNoAceptadoException.java` - Lanzada cuando el usuario intenta acceder sin aceptar EULA

5. **Servicio EulaService:**
   - Genera PDF del EULA con información del usuario
   - Registra aceptación con IP, fecha, hora y user agent
   - Almacena PDF firmado en sistema de archivos
   - Valida si usuario tiene EULA aceptado

6. **Controller EulaController:**
   - `GET /api/eula/estado` - Obtener estado del EULA (requiere autenticación)
   - `GET /api/eula/estado/{email}` - Obtener estado por email (público)
   - `POST /api/eula/aceptar` - Aceptar EULA (requiere autenticación)
   - `POST /api/eula/aceptar/{email}` - Aceptar EULA por email (público, antes del login)
   - `GET /api/eula/pdf/{userId}` - Descargar PDF del EULA firmado
   - `GET /api/eula/texto` - Obtener texto del EULA

7. **Modificaciones:**
   - `AuthService` - Valida EULA antes de permitir login
   - `GlobalExceptionHandler` - Maneja excepción `EulaNoAceptadoException`
   - `SecurityConfig` - Permite acceso público a endpoints `/api/eula/**`

### Frontend (React/TypeScript)

1. **Servicio EULA** (`apiServices.ts`)
   - `obtenerEstado()` - Consultar estado del EULA
   - `aceptarEula()` - Aceptar el EULA
   - `obtenerTexto()` - Obtener texto del EULA
   - `descargarPdf()` - Descargar PDF firmado

2. **Componente EulaModal** (`EulaModal.tsx`)
   - Muestra el texto completo del EULA
   - Checkbox obligatorio para aceptación
   - Captura IP y User Agent automáticamente
   - Diseño responsive y accesible
   - Mensaje legal sobre validez del contrato

3. **Modificaciones Login** (`Login.tsx`)
   - Verifica EULA antes de permitir acceso
   - Muestra modal automáticamente si EULA no está aceptado
   - Maneja errores de EULA no aceptado
   - Reintenta login después de aceptar EULA

---

## 🔒 Seguridad y Legalidad

### Validez Legal en Argentina

El sistema cumple con los requisitos del Código Civil y Comercial:

- ✅ **Checkbox obligatorio** - El usuario debe marcar explícitamente
- ✅ **Registro de IP** - Se guarda la dirección IP del usuario
- ✅ **Fecha y hora** - Timestamp preciso de la aceptación
- ✅ **User Agent** - Información del navegador/dispositivo
- ✅ **Texto completo** - El usuario puede leer el EULA completo
- ✅ **PDF generado** - Documento legal con toda la información

### Información Registrada

Cada aceptación del EULA registra:
- Email del usuario
- Nombre completo (si está disponible)
- Fecha y hora exacta de aceptación
- Dirección IP del cliente
- User Agent (navegador/dispositivo)
- Versión del EULA aceptada
- PDF generado con toda la información

---

## 📋 Flujo de Usuario

1. **Usuario intenta iniciar sesión**
   - Ingresa email y contraseña
   - Presiona "Iniciar Sesión"

2. **Sistema verifica credenciales**
   - Si las credenciales son incorrectas → Error
   - Si las credenciales son correctas → Continúa

3. **Sistema verifica EULA**
   - Si EULA NO aceptado → Muestra modal de EULA
   - Si EULA aceptado → Permite acceso al sistema

4. **Usuario acepta EULA**
   - Lee el texto completo del EULA
   - Marca el checkbox de aceptación
   - Presiona "Aceptar y Continuar"

5. **Sistema registra aceptación**
   - Guarda en base de datos
   - Genera PDF con información completa
   - Almacena PDF en sistema de archivos

6. **Usuario accede al sistema**
   - Login exitoso
   - Redirección al dashboard

---

## 🗂️ Archivos Creados/Modificados

### Backend

**Nuevos archivos:**
- `V1_23__Add_EULA_Fields_To_Usuarios.sql` - Migración SQL
- `AceptarEulaRequest.java` - DTO
- `EulaEstadoResponse.java` - DTO
- `EulaNoAceptadoException.java` - Excepción
- `EulaService.java` - Servicio
- `EulaController.java` - Controller

**Archivos modificados:**
- `User.java` - Campos EULA agregados
- `AuthService.java` - Validación EULA en login
- `GlobalExceptionHandler.java` - Manejo de excepción EULA
- `SecurityConfig.java` - Endpoints públicos de EULA
- `pom.xml` - Dependencias PDFBox y Commons IO

### Frontend

**Nuevos archivos:**
- `EulaModal.tsx` - Componente modal de EULA

**Archivos modificados:**
- `apiServices.ts` - Servicio EULA agregado
- `Login.tsx` - Verificación y manejo de EULA

---

## 🔧 Configuración

### Variables de Entorno (Backend)

```properties
# Ruta donde se almacenan los PDFs del EULA
app.eula.storage.path=./eula-pdfs
```

### Estructura de Directorios

```
agrogestion-backend/
├── eula-pdfs/          # PDFs generados (se crea automáticamente)
│   └── EULA_{userId}_{timestamp}.pdf
└── src/
    └── main/
        └── resources/
            └── db/
                └── migration/
                    └── V1_23__Add_EULA_Fields_To_Usuarios.sql
```

---

## 📝 Contenido del EULA

El EULA incluye:

1. **Objeto** - Descripción del software
2. **Otorgamiento de Licencia** - Términos de uso
3. **Propiedad Intelectual** - Derechos de propiedad
4. **Restricciones** - Limitaciones de uso
5. **Protección de Datos Personales** - Ley 25.326 (Argentina)
6. **Disponibilidad del Servicio** - Garantías y limitaciones
7. **Limitación de Responsabilidad** - Exclusiones de responsabilidad
8. **Planes, Pagos y Renovaciones** - Términos comerciales
9. **Cancelación** - Términos de cancelación
10. **Actualizaciones** - Política de actualizaciones
11. **Jurisdicción y Ley Aplicable** - Ley Argentina
12. **Aceptación** - Declaración de aceptación

---

## 🚀 Próximos Pasos

1. **Ejecutar migración SQL** en la base de datos
2. **Compilar y ejecutar backend** para verificar que no hay errores
3. **Probar flujo completo:**
   - Crear usuario nuevo
   - Intentar login
   - Verificar que aparece modal de EULA
   - Aceptar EULA
   - Verificar que se genera PDF
   - Verificar que se puede acceder al sistema

4. **Verificar PDF generado:**
   - Revisar que contiene toda la información
   - Verificar que la fecha es correcta
   - Confirmar que IP y User Agent están incluidos

---

## ⚠️ Notas Importantes

1. **Usuarios existentes:** Los usuarios que ya existen en el sistema tendrán `eula_aceptado = false` por defecto. Deberán aceptar el EULA en su próximo login.

2. **PDFs almacenados:** Los PDFs se almacenan en el servidor. Considera implementar un sistema de backup o almacenamiento en la nube para producción.

3. **Versión del EULA:** Si necesitas actualizar el EULA en el futuro, cambia la versión en `EulaService.VERSION_EULA` y los usuarios deberán aceptar la nueva versión.

4. **Directorio de PDFs:** Asegúrate de que el directorio `eula-pdfs` tenga permisos de escritura en producción.

---

## 📞 Soporte

Si encuentras algún problema:

1. Verifica los logs del backend para errores
2. Revisa que la migración SQL se ejecutó correctamente
3. Confirma que las dependencias están instaladas (PDFBox)
4. Verifica que el directorio de PDFs tiene permisos de escritura

---

**Fecha de implementación:** 2025-01-16
**Versión del EULA:** 1.0
**Estado:** ✅ Completado y listo para pruebas

