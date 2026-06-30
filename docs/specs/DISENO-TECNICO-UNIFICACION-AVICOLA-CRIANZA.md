# Diseño técnico — Unificación Avícola crianza / carne

**Estado:** Aprobado  
**SPEC:** `SPEC-MODULO-AVICOLA-CRIANZA.md` v2.0

## Decisión

- **Superviviente:** `AVICOLA_CRIANZA`, paquete `com.agrocloud.avicola.crianza`, UI `avicola-crianza`.
- **Eliminar:** paquete `com.agrocloud.avicola.carne`, módulo frontend `avicola-carne`.
- **Portar:** reglas de `ServicioAvicolaCarneOperaciones`, reportes, cierre manual, calendario ámbito `AVICOLA_CRIANZA`.

## Migración Flyway V1_157

1. `modulo_origen` CARNE → CRIANZA en `avicola_*`.
2. `company_modules`: habilitar crianza donde existía carne; deshabilitar carne.
3. `modules`: `AVICOLA_CARNE.active = false`.
4. `calendario_serie.ambito_calendario`: CARNE → CRIANZA.

## Compatibilidad

- Redirect frontend `/avicola-carne/*` → `/avicola-crianza/*`.
- `InventoryOrigin.AVICOLA_CARNE` conservado solo para movimientos históricos.
