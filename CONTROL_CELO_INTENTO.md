# Control de Celo e Intento #

## Problema

En la pantalla de Control de Celo se muestra el campo **Intento #**. Si el control **no es exitoso** (resultado: no preñada), el servicio queda en estado FALLIDO y desaparece de la lista de "Pendientes de control". Para repetir el proceso reproductivo con la misma madre había que ir a "Nuevo Servicio" y cargar todo de nuevo; además, el nuevo servicio quedaba siempre como **Intento #1**, perdiendo la continuidad del número de intento.

## Solución implementada

### 1. **Mantener el campo "Intento #"** (sí es necesario)

El campo **Intento #** se mantiene porque:
- Sirve para reportes e indicadores (cuántos intentos por madre/ciclo).
- Permite ver en Control de Celo si es el 1º, 2º, 3º intento, etc.
- El número ya **no se pierde** al cargar un nuevo servicio (ver punto 2).

### 2. **Cálculo automático del número de intento** (backend)

Al **crear un nuevo servicio**, el backend calcula automáticamente el **siguiente número de intento** para esa madre:

- Se buscan todos los servicios previos de esa madre (activos, cualquiera sea su estado).
- Se toma el **máximo** `numeroIntento` de esos servicios.
- El nuevo servicio se guarda con **numeroIntento = máximo + 1**.

Así:
- Primer servicio de una madre → **Intento #1**.
- Si el control falla y se carga otro servicio para la misma madre → **Intento #2**.
- Y así sucesivamente (**#3**, **#4**, …).

**Archivo modificado:** `ServicioService.java`  
- Método `calcularSiguienteNumeroIntento(Madre madre)`.  
- Llamada en `crearServicio()` antes de guardar.

### 3. **Botón "Nuevo intento"** (frontend)

En la lista de **Servicios**, para cada servicio en estado **FALLIDO** se muestra el botón **"Nuevo intento"**:

- Al hacer clic, se navega a **Nuevo Servicio** con la **misma madre** ya preseleccionada (`/porcinos/servicios/nuevo?madreId=X`).
- El usuario solo completa fecha, padrillo y observaciones; no tiene que buscar de nuevo la madre.
- El **Intento #** lo sigue calculando el backend (será 2, 3, etc. según intentos previos).

**Archivo modificado:** `ServiciosListScreen.tsx`  
- Botón "Nuevo intento" en la columna de acciones para `estadoServicio === 'FALLIDO'`.

**Para ver servicios fallidos:** en la lista de Servicios, en el filtro **Estado** elegir **"Fallido"** o **"Todos"**. Ahí aparecerá el botón "Nuevo intento" en cada servicio fallido.

### 4. **Opción "Nuevo intento" desde Control de Celo** (frontend)

Cuando el usuario registra un control de celo con resultado **No preñada**, el servicio pasa a FALLIDO y ya no aparece en "Pendientes de control". Para no tener que buscar en la lista de Servicios:

- Tras guardar **No preñada**, la pantalla de Control de Celo muestra un mensaje de confirmación y dos opciones:
  - **Nuevo intento**: abre directamente la pantalla de Nuevo Servicio con la **misma madre** ya preseleccionada (`/porcinos/servicios/nuevo?madreId=X`). El Intento # lo calcula el backend (será 2, 3, etc.).
  - **Volver a lista de Servicios**: vuelve a la lista de servicios.

Así el usuario puede repetir el proceso reproductivo sin salir del flujo ni buscar el servicio fallido en la lista.

**Archivo modificado:** `ControlCeloScreen.tsx` — estado `mostrarNuevoIntento` y vista con mensaje + botones tras registrar "No preñada".

## Resumen

| Antes | Ahora |
|-------|--------|
| Control fallido → servicio FALLIDO, nuevo servicio siempre Intento #1 | Nuevo servicio recibe automáticamente Intento #2, #3, … |
| Había que ir a Nuevo Servicio y buscar de nuevo la madre | Botón "Nuevo intento" abre Nuevo Servicio con la madre ya cargada |
| Tras "No preñada" el registro desaparecía de la vista sin opción clara | Tras "No preñada" se ofrece en la misma pantalla "Nuevo intento" o "Volver a lista" |
| El campo Intento # parecía perderse o no tener sentido | Intento # se calcula solo y mantiene la secuencia por madre |

El campo **Intento #** no se eliminó; se le dio uso real y se facilitó **repetir el proceso reproductivo** con la misma madre sin perder el número de intento. Además, al registrar un control no exitoso se ofrece directamente la opción de **Nuevo intento** en la pantalla de Control de Celo.
