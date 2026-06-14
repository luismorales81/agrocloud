# AgroGestion / AgroCloud — Propuesta para cliente potencial

**Para:** decisores y equipos de gestión agropecuaria.  
**Versión:** 1.1 · **Fecha:** mayo 2026

---

## 1. Carta de presentación

**AgroGestion** es la solución de gestión pensada para **empresas y equipos** que producen en el campo y necesitan **una sola fuente de verdad**: operación diaria, costos, inventarios y reportes, con **acceso seguro por roles** y **módulos que se activan según su negocio**.

Por detrás, la plataforma técnica **AgroCloud** ofrece un **backend robusto** y un **frontend moderno y usable**, desplegables en la **nube** para que no dependan de servidores propios salvo que así lo deseen.

---

## 2. Qué tenemos hoy

### 2.1 Visión general del producto

Un **SaaS multiempresa**: cada cliente trabaja en su **empresa**, con usuarios, permisos y **módulos contratados o habilitados**. No es un software rígido “de una sola vertical”: puede empezar por **cultivos** y sumar **porcinos** o líneas **avícolas** cuando la operación lo requiera.

### 2.2 Módulos de negocio disponibles (alto nivel)

| Módulo | Qué aporta al cliente |
|--------|------------------------|
| **Cultivos** | Establecimientos, lotes, ciclos de cultivo, labores con costos (insumos, maquinaria, mano de obra), inventario de insumos y granos, aplicaciones y trazabilidad operativa del agronegocio. |
| **Porcinos** | Gestión productiva de la cadena porcina (lotes, consumos, sanidad, movimientos y reportes), alineada con insumos e inventarios donde corresponde. |
| **Avícola — Crianza** | Operación centrada en crianza de aves con flujos y datos propios del subnegocio. |
| **Avícola — Huevos** | Gestión orientada a producción y comercialización de huevo. |
| **Avícola — Carne / parrillero** | Línea de negocio de carne de ave, con API y dominio separado de crianza y huevos. |
| **Avícola — Ponedoras / recría** | Módulo específico para ponedoras y recría, sin confundirse con el de huevos comercial. |

La arquitectura permite que una misma empresa tenga **combinaciones** (por ejemplo, crianza y carne) según lo que habilite el operador del servicio.

### 2.3 Núcleo común (CORE)

- **Empresas, usuarios y roles** con gobierno de accesos.  
- **Autenticación segura** (JWT) y separación de contexto por empresa.  
- **Habilitación de módulos por empresa** (comercialmente: planes base y ampliaciones).  
- **EULA** y marco de uso para relaciones B2B claras.  
- **Calendario y recordatorios** transversales donde aplica.  
- **Administración** para operar el servicio a escala.

### 2.4 Cómo se entrega

- **Backend** listo para entornos cloud (**Render**, **Railway**, **Fly.io**, entre otros) con **Docker** y variables de entorno documentadas.  
- **Frontend** en **React** con **TypeScript** y **Vite**, adaptable a hosting estático (por ejemplo **Vercel**).  
- **Base de datos** relacional con **migraciones versionadas** (Flyway), pensado para evolucionar el producto sin perder control.

---

## 3. Hacia dónde vamos

La hoja de ruta se orienta a **profundizar** lo que ya tienen los productores y a **ampliar** el valor con datos y automatización, siempre con validación en campo.

1. **Más integración operativa:** seguir acercando inventarios, costos y movimientos entre módulos (campo, porcinos, avícola) para que el margen y la trazabilidad se vean en un solo lugar.  
2. **Inteligencia asistida:** asistentes y wizards (por ejemplo configuración de cultivo o planes de recría) para reducir errores de carga y acelerar la puesta en marcha — apoyados en políticas claras de datos y privacidad.  
3. **Cumplimiento y confianza:** líneas de trabajo hacia **trazabilidad comercial certificable** y mejores reportes para auditorías y clientes finales.  
4. **Producto comercializable:** definición de planes (usuarios, límites, módulos), SLAs con el proveedor cloud y, cuando corresponda, **facturación** integrada a pasarelas.

*Los tiempos y el alcance exacto de cada ítem se acuerdan según prioridad del cliente y del roadmap del producto.*

---

## 4. Ventajas para el negocio del cliente

- **Menos fragmentación:** menos planillas sueltas y menos “verdades” contradictorias entre administración y campo.  
- **Costos explicables:** desglose por labor y campaña, base para negociar, ajustar márgenes y comparar establecimientos.  
- **Escalabilidad comercial:** empieza con lo mínimo necesario y **suma módulos** sin cambiar de sistema.  
- **Equipos alineados:** roles para dueño, agrónomo, administración y operación, con la misma información.  
- **Continuidad:** despliegue en nube con prácticas de respaldo y despliegue reproducible (contenedores).  
- **Transparencia:** el cliente puede **probar en demo** los flujos críticos antes de comprometerse.

---

## 5. Aportes tecnológicos (por qué importa a TI y a la organización)

| Área | Aporte |
|------|--------|
| **Arquitectura** | Monolito modular en **Spring Boot**: coherencia de datos, transacciones y un solo despliegue, con **código organizado por dominio** (cultivos, porcinos, avícola). |
| **Seguridad** | **Spring Security**, JWT, control de acceso a **API por módulo habilitado** (no es solo ocultar menús: el servidor valida el permiso). |
| **Frontend** | **React 19**, **TypeScript**, **Tailwind**, componentes reutilizables; rutas y menús **cargados por módulo** para mantener la app mantenible al crecer. |
| **Datos** | **JPA / Spring Data**, esquema evolutivo con **Flyway**; menos sorpresas al actualizar versiones. |
| **Observabilidad y operación** | Perfiles Spring (**dev**, **prod**, etc.), integración típica con **Actuator** y buenas prácticas de logging para soporte. |
| **Evolución** | Especificaciones de producto (**SDD**) y documentación técnica interna que ordenan cambios grandes antes de codificar. |
