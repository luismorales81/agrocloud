# SPEC — Sitio público de AgroCloud

**Versión:** 1.0  
**Fecha:** Octubre 2026  
**Metodología:** SDD  
**Estado:** Aprobada con el plan de implementación  
**Relacionada con:** `docs/despliegue-hetzner.md`, `deploy/Caddyfile`

---

## 1. Objetivo

Publicar un sitio de presentación en el dominio de AgroCloud para dueños, encargados y administración de empresas agropecuarias. Desde ese sitio se entra al sistema y se agenda una charla de 30 minutos.

El sitio describe lo que el producto ya hace. No promete módulos que no existen.

---

## 2. Dominios

| Host | Qué muestra |
|------|-------------|
| `https://agrocloud.com.ar` y `www` | Sitio público (`sitio-publico/`). `www` redirige al dominio sin prefijo. |
| `https://app.agrocloud.com.ar` | Sistema actual (frontend de la aplicación). |

El botón **Entrar al sistema** abre `https://app.agrocloud.com.ar`. Al abrir el sitio en el equipo (`localhost`, `127.0.0.1` o archivo local), ese mismo botón abre `http://localhost:3001`.

`/api` sigue llegando al backend tanto en el dominio público como en `app`, porque el frontend llama a `/api` en el mismo origen.

---

## 3. Corte de publicación

No desplegar el `Caddyfile` nuevo hasta que exista el registro DNS.

1. Crear un registro `A` de `app` hacia la misma IPv4 del VPS (además de `@` y `www`).
2. En el `.env` del VPS:
   - `FRONTEND_URL=https://app.agrocloud.com.ar` (los correos de acceso apuntan al sistema).
   - `CORS_ALLOWED_ORIGINS=https://app.agrocloud.com.ar,https://agrocloud.com.ar,https://www.agrocloud.com.ar`
3. Recién ahí publicar el compose de producción.

Si se cambia Caddy sin el registro `app`, el dominio actual deja de abrir el login y el sistema no resuelve.

La clave de Google Maps, si se usa, debe permitir `https://app.agrocloud.com.ar/*`.

---

## 4. Público y contenido

Texto en castellano, corto, legible en el celular. Sin testimonios, logos de clientes ni cifras que el producto no tenga.

### 4.1 Portada

Qué resuelve: operación, costos e inventario en un solo lugar, con los módulos que la empresa activa. Dos acciones: entrar al sistema y agendar.

### 4.2 Lo común

- Usuarios y roles dentro de la empresa.
- Módulos que se activan según el negocio.
- Inventario y períodos de gestión compartidos.

### 4.3 Módulos

| Módulo | Qué se muestra |
|--------|----------------|
| Cultivos | Campos, lotes y ciclos. Labores con costo de insumos, maquinaria y mano de obra. Inventario de insumos y granos. Calendario, finanzas y reportes. |
| Ganadería | El módulo de engorde a corral (feedlot). Del ingreso a la venta o faena. Pesadas, dietas y sanidad. Sesión de manga y lecturas de bastón. Ganancia diaria, conversión y cierre económico del lote. |
| Porcinos | Madres y padrillos. Servicio, gestación, parto y destete. Lotes de engorde, dietas, sanidad y ventas. |
| Avícola | Un solo rubro: crianza y engorde, producción de huevos, ponedoras. Alimento, sanidad y reportes. No se ofrece carne avícola como módulo aparte. |
| Lechería | Bovino, caprino, ovino y búfalo. Ordeñe y lactancia. Ventas de leche. Sanidad, control lechero y mapa. |

### 4.4 Agenda

Widget embebido de `https://calendly.com/morales-luis-ariel/30min`. Texto: «Charla de 30 minutos para ver si AgroCloud encaja en tu explotación».

---

## 5. Fuera de alcance

- Formulario de contacto propio, además de Calendly.
- Precios, planes comerciales y alta de usuarios desde el sitio.
- Cambiar el login ni las pantallas del sistema.
- Ejecutar el corte en el VPS desde este repositorio.
