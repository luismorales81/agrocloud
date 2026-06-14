# Ejemplo de Uso de @RequiresModule

Este documento muestra cómo usar la anotación `@RequiresModule` para proteger endpoints según módulos y permisos.

## Uso Básico

### Ejemplo 1: Endpoint que requiere módulo de cultivos con permiso de lectura

```java
@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {

    @GetMapping
    @RequiresModule("crops")
    public ResponseEntity<List<CultivoDTO>> getAllCultivos() {
        // Solo usuarios con permiso de lectura sobre el módulo "crops" pueden acceder
        return ResponseEntity.ok(cultivoService.getAll());
    }
}
```

### Ejemplo 2: Endpoint que requiere permiso de escritura

```java
@PostMapping
@RequiresModule(value = "crops", permission = "write")
public ResponseEntity<CultivoDTO> createCultivo(@RequestBody CultivoDTO dto) {
    // Solo usuarios con permiso de escritura sobre el módulo "crops" pueden acceder
    return ResponseEntity.ok(cultivoService.create(dto));
}
```

### Ejemplo 3: Endpoint que requiere permiso de gestión

```java
@DeleteMapping("/{id}")
@RequiresModule(value = "crops", permission = "manage")
public ResponseEntity<Void> deleteCultivo(@PathVariable Long id) {
    // Solo usuarios con permiso de gestión sobre el módulo "crops" pueden acceder
    cultivoService.delete(id);
    return ResponseEntity.noContent().build();
}
```

### Ejemplo 4: Aplicar a nivel de clase

```java
@RestController
@RequestMapping("/api/porcinos")
@RequiresModule("pigs")  // Todos los endpoints de esta clase requieren el módulo "pigs"
public class PorcinoController {

    @GetMapping
    public ResponseEntity<List<PorcinoDTO>> getAll() {
        // Requiere módulo "pigs" con permiso "read" (por defecto)
        return ResponseEntity.ok(porcinoService.getAll());
    }

    @PostMapping
    @RequiresModule(value = "pigs", permission = "write")  // Sobrescribe el permiso de la clase
    public ResponseEntity<PorcinoDTO> create(@RequestBody PorcinoDTO dto) {
        // Requiere módulo "pigs" con permiso "write"
        return ResponseEntity.ok(porcinoService.create(dto));
    }
}
```

## Validaciones Automáticas

El interceptor `ModuleAccessInterceptor` valida automáticamente:

1. ✅ **Usuario autenticado**: El usuario debe estar logueado
2. ✅ **Pertenencia a empresa**: El usuario debe pertenecer a la empresa (obtenida del header `X-Company-Id`)
3. ✅ **Módulo habilitado**: La empresa debe tener el módulo contratado/habilitado
4. ✅ **Permisos del rol**: El usuario debe tener el rol con los permisos necesarios sobre el módulo
5. ✅ **Superadmin bypass**: Si `allowSuperAdmin=true` (por defecto), los superadmins pueden acceder sin validación

## Códigos de Respuesta

- **200 OK**: Acceso permitido
- **401 UNAUTHORIZED**: Usuario no autenticado
- **403 FORBIDDEN**: Usuario no tiene permisos suficientes o no pertenece a la empresa
- **402 PAYMENT_REQUIRED**: La empresa no tiene el módulo contratado/habilitado

## Headers Requeridos

Para que el interceptor funcione correctamente, el frontend debe enviar:

```
X-Company-Id: 123
Authorization: Bearer <token>
```

## Ejemplo de Integración en CultivoController Existente

```java
@RestController
@RequestMapping("/api/cultivos")
public class CultivoController {

    @Autowired
    private CultivoService cultivoService;

    @GetMapping
    @RequiresModule("crops")  // Agregar esta línea
    public ResponseEntity<List<CultivoDTO>> getAllCultivos() {
        // Código existente...
    }

    @PostMapping
    @RequiresModule(value = "crops", permission = "write")  // Agregar esta línea
    public ResponseEntity<CultivoDTO> createCultivo(@RequestBody CultivoDTO dto) {
        // Código existente...
    }

    @PutMapping("/{id}")
    @RequiresModule(value = "crops", permission = "write")  // Agregar esta línea
    public ResponseEntity<CultivoDTO> updateCultivo(@PathVariable Long id, @RequestBody CultivoDTO dto) {
        // Código existente...
    }

    @DeleteMapping("/{id}")
    @RequiresModule(value = "crops", permission = "manage")  // Agregar esta línea
    public ResponseEntity<Void> deleteCultivo(@PathVariable Long id) {
        // Código existente...
    }
}
```











