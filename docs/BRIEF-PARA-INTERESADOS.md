# AgroGestion / AgroCloud — Brief para interesados

**Documento orientado a:** inversores, socios comerciales, clientes corporativos y tomadores de decisión.  
**Versión:** 1.0 · **Fecha:** abril 2026

---

## 1. Resumen ejecutivo

**AgroGestion** (plataforma técnica **AgroCloud**) es un **software de gestión agropecuaria en la nube**, pensado para **organizaciones** (empresas productoras, grupos o unidades de negocio) que necesitan **ordenar la operación del campo**, **controlar costos por labor** y **obtener visibilidad económica y de inventario** en un solo sistema.

La solución se entrega como **SaaS multiempresa**: cada cliente trabaja en su propio contexto de empresa, con usuarios y roles. La funcionalidad se organiza en **módulos activables** (por ejemplo **Cultivos** y **Porcinos**), lo que permite **escalar la oferta** desde un paquete base hasta una solución integrada campo–feedlot.

---

## 2. Problema que aborda

En el día a día agropecuario, la información operativa y económica suele quedar fragmentada entre planillas, cuadernos, WhatsApp y sistemas sueltos. Eso genera **falta de trazabilidad**, decisiones tardías y dificultad para responder preguntas básicas de gestión.

**Dolores típicos que resuelve:**

- **Costos poco claros por labor o campaña:** se pierde el desglose y no se puede explicar el número final.
- **Inventario sin trazabilidad:** insumos y granos sin relación clara con aplicaciones, labores, consumos y movimientos.
- **Gestión multiusuario sin gobierno:** falta de roles, permisos y auditoría en equipos.
- **Operaciones mixtas desconectadas:** agricultura y porcinos con herramientas distintas (o sin integración).

**Qué cambia con AgroGestion:** centraliza el registro por **empresa**, habilita módulos según necesidad (Cultivos/Porcinos) y permite consolidar costos desde el nivel de labor hasta reportes de campaña.

---

## 3. Propuesta de valor

1. **Operación y economía unificadas:** del lote y el ciclo de cultivo a costos, cosechas, granos y reportes; extensible a **cadena porcina** cuando el cliente habilita el módulo.  
2. **Modelo B2B serio:** multiempresa, autenticación segura (JWT), separación de accesos y aceptación de **EULA** para relaciones contractuales claras.  
3. **Modularidad comercial y técnica:** el mismo despliegue puede ofrecer solo **Cultivos** (`crops`) o sumar **Porcinos** (`pigs`); la activación es **por empresa**, alineada a **upsell** o a distintos planes sin forks de código.  
4. **Despliegue cloud:** backend preparado para entornos como **Render**, **Railway** o **Fly.io**; frontend adaptable a hosting estático (por ejemplo **Vercel**), según la estrategia de cada operador del producto.

---

## 4. Producto: alcance funcional (alto nivel)

### Módulo Cultivos (referencia en catálogo: `crops`)

- Establecimientos, lotes, cultivos y **máquina de estados** configurable por tipo de cultivo.  
- **Labores** con insumos, maquinaria y mano de obra; costos agregados por labor.  
- Siembra, cosechas, **agroquímicos / aplicaciones**, inventario de insumos y de granos.  
- Finanzas y reportes asociados al negocio agrícola (según implementación desplegada).

### Módulo Porcinos (`pigs`)

- Gestión productiva porcina (recria, consumos, sanidad, reportes y KPIs del dominio), con integración donde corresponde con **insumos** e **inventario de granos** compartidos con cultivos.

### Núcleo (CORE)

- Empresas, usuarios, roles, permisos y **módulos habilitados por empresa**.  
- Calendario y recordatorios transversales donde aplica.  
- Paneles de administración global y por empresa para operación del servicio.

*(El detalle técnico de límites entre CORE y módulos está documentado internamente en `docs/CORE-Y-MODULOS.md`.)*

---

## 5. Segmentos de clientes prioritarios

**Quién compra (decisor):**

- **Dueño / gerente de producción**: necesita control económico y orden operativo.
- **Administración**: busca trazabilidad, comprobantes, y consistencia de datos.

**Quién usa (usuarios):**

- **Agrónomos / encargados**: registran labores, insumos, aplicaciones, avances por lote.
- **Operarios / equipo de campo**: carga simple de actividades y consumos (según el proceso definido).

**Segmentos donde encaja mejor (prioridad):**

1. **Productores medianos y medianos-grandes** con varias parcelas/lotes, insumos y maquinaria.
2. **Equipos multiusuario** (dueño + agrónomo + administración) con necesidad de roles y permisos.
3. **Operaciones mixtas** que buscan integrar **campo + porcinos** en una misma plataforma.
4. **Operadores / integradores** (cooperativas, consultoras, agronegocios) que administran múltiples empresas/clientes.

---

## 6. Modelo de negocio

**Tipo:** SaaS **B2B** (por empresa), con **habilitación por módulos** como palanca comercial.

**Cómo se empaqueta (recomendación comercial):**

- **Plan Base (Cultivos):** operación agrícola + costos por labor + inventario y reportes básicos.
- **Plan Plus (Cultivos + Porcinos):** suma el módulo Porcinos y sus reportes/KPIs.
- **Servicios (opcional):** implementación, parametrización, migración de datos y capacitación.

**Cómo se cobra (opciones típicas):**

- **Suscripción mensual o anual por empresa** (modelo simple y escalable).
- Adicional por **módulo** (upsell natural por activación: Cultivos → Porcinos).
- Adicional por **servicios profesionales** (onboarding, soporte premium, capacitación).

**Cobranza dentro del sistema:** en el estado actual del repositorio **no hay** pasarela de pagos integrada (Mercado Pago/Stripe, etc.). La facturación puede manejarse **por contrato** hasta productizar planes y auto-cobro.

**Unidad de crecimiento:** más empresas activas, más módulos habilitados, más usuarios y mayor retención por histórico de datos.

---

## 7. Diferenciación y mensajes clave

- **Costo por labor** y desglose explícito (insumos + maquinaria + mano de obra), útil para márgenes por lote o campaña.  
- **Arquitectura modular real** (no solo “menús”): control de acceso a API por módulo habilitado.  
- **Stack moderno** (Spring Boot, React/TypeScript) y **despliegue cloud** documentado, relevante para continuidad y escalamiento operativo.

---

## 8. Estado del producto y transparencia

El repositorio combina **funcionalidad estable** con **evolución continua** (migraciones, scripts SQL, documentación de auditorías). Conviene que cualquier interesado **valide en demo o entorno de prueba** las pantallas críticas para su caso (cultivo principal, flujo de labores, reportes).

**Riesgos / huecos típicos a tratar en due diligence comercial:**

- Definir explícitamente **límites de planes** (usuarios, hectáreas, API) si aún no están enforceados en aplicación.  
- Definir **SLA**, backup y continuidad según el proveedor cloud elegido.  
- Roadmap de **facturación automática** si se busca venta self-service.

---

## 9. Próximos pasos sugeridos para interesados

1. **Demo guiada** (30–45 min): flujo cultivo — lote — labor con costos — reporte.  
2. **Reunión de encaje**: volumen de ha, usuarios, necesidad del módulo Porcinos, integraciones deseadas.  
3. **Propuesta comercial** (piloto, implementación, soporte) y **acuerdo de licencia** (EULA + condiciones particulares).

---

## 10. Contacto

*Completar con datos reales del titular del producto (empresa, web, correo, teléfono, responsable comercial).*

---

## Anexo — Identidad y referencia técnica

| Ítem | Valor |
|------|-------|
| Nombre comercial / proyecto | AgroGestion |
| Nombre técnico / backend | AgroCloud |
| Módulos (códigos de referencia) | `crops` (Cultivos), `pigs` (Porcinos) |
| Enfoque de despliegue | Cloud (según proveedor elegido) |

---

*Este brief resume capacidades y modelo de negocio inferidos del diseño del software. No sustituye asesoramiento legal, fiscal ni ofertas comerciales firmadas.*
