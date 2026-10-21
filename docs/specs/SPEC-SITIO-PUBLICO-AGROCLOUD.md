# SPEC — Sitio público de AgroCloud

**Versión:** 1.2  
**Fecha:** Octubre 2026  
**Metodología:** SDD  
**Estado:** Aprobada con el pedido de rediseño  
**Relacionada con:** `docs/BRIEF-PARA-INTERESADOS.md`, `docs/PROPUESTA-CLIENTE-AGROGESTION.md`, `docs/despliegue-hetzner.md`, `deploy/Caddyfile`

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

Texto en castellano, voseo, frases cortas, legible en el celular. Sin testimonios, logos de clientes ni cifras que el producto no tenga.

La identidad visual sale de la guía de rediseño: fondo `#FDF4E3`, paneles `#F3E6D2`, verde `#3A5533`, tinta `#2B2B24`, naranja `#E07B2A` y ocre `#E3A72F` solo en dibujos. Títulos en Lora, texto en Source Sans 3. Tarjetas planas, esquinas de 6px, sin sombras fuertes ni degradados.

El tono y las secciones nuevas salen de la presentación «AgroCloud: Gestión agropecuaria en la nube». Se suman al contenido que ya estaba. No se publica el pedido de inversión, ni precios, ni un roadmap de rubros que el producto no tiene (caprina, apícola). Lechería se muestra como módulo actual.

### 4.1 Portada

«Dejá de pelear con cuadernos, planillas y WhatsApp.» Bajada: controlar la empresa en un solo lugar. Dos acciones: pedir una demo y entrar al sistema.

### 4.2 El problema

Cinco tarjetas: sistemas desconectados, costos opacos, inventario sin trazabilidad, equipos desalineados, producción mixta sin puente.

### 4.3 El costo de no cambiar

Decisiones tardías, márgenes opacos, errores de stock y tiempo perdido.

### 4.4 La solución

100% en la nube, módulos a tu medida y costos explicables (insumos, maquinaria y mano de obra).

### 4.5 Núcleo y beneficios

Multiempresa, roles, inventario unificado, mapa y clima. La cadena: menos fragmentación, márgenes claros, equipos alineados.

### 4.6 Módulos

| Módulo | Qué se muestra |
|--------|----------------|
| Cultivos | Campos, lotes y ciclos. Labores con costo de insumos, maquinaria y mano de obra. Inventario de insumos y granos. Calendario, finanzas y reportes. |
| Ganadería | El módulo de engorde a corral (feedlot). Del ingreso a la venta o faena. Pesadas, dietas y sanidad. Sesión de manga y lecturas de bastón. Ganancia diaria, conversión y cierre económico del lote. |
| Porcinos | Madres y padrillos. Servicio, gestación, parto y destete. Lotes de engorde, dietas, sanidad y ventas. |
| Avícola | Un solo rubro: crianza y engorde, producción de huevos, ponedoras. Alimento, sanidad y reportes. No se ofrece carne avícola como módulo aparte. |
| Lechería | Bovino, caprino, ovino y búfalo. Ordeñe y lactancia. Ventas de leche. Sanidad, control lechero y mapa. |

### 4.7 Para quién

Dueño o gerente de producción, administración, y encargado o agrónomo. Encaja en empresas con varios lotes, más de un usuario, o campo y animales en la misma explotación.

### 4.8 Agenda

Widget embebido de `https://calendly.com/morales-luis-ariel/30min`. El cierre retoma: «Dejá de pelear con planillas. Controlá tu operación y explicá tus costos.» La demo dura 30 minutos.

---

## 5. Fuera de alcance

- Formulario de contacto propio, además de Calendly.
- Precios, planes comerciales, facturación y alta de usuarios desde el sitio.
- Stack técnico, proveedores cloud, SLA y hoja de ruta.
- Cambiar el login ni las pantallas del sistema.
- Ejecutar el corte en el VPS desde este repositorio.
