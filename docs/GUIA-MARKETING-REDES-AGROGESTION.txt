# AgroGestion — Guía de marketing para redes sociales

**Documento para:** equipo comercial, marketing y desarrollo  
**Versión:** 1.0 · **Fecha:** junio 2026  
**Basado en:** código fuente actual, specs aprobadas y documentación de producto

---

## 1. Resumen ejecutivo (para marketing)

**AgroGestion** es un software de gestión agropecuaria en la nube (plataforma técnica **AgroCloud**) pensado para **empresas productoras** que necesitan ordenar la operación diaria, controlar costos y tener **una sola fuente de verdad** entre campo, administración y gerencia.

### Mensaje central (elevator pitch)

> *Dejá de pelear con planillas y WhatsApp. AgroGestion centraliza tu operación — cultivos, porcinos, avícola o feedlot — con costos por labor, inventarios trazables y reportes que explican el número final.*

### Diferenciadores comunicables

| Diferenciador | Cómo decirlo en redes |
|---------------|----------------------|
| **Modularidad real** | Pagás y usás solo lo que necesitás: cultivos hoy, porcinos mañana, avícola cuando crezcas. |
| **Costos explicables** | Cada labor, consumo o faena tiene su desglose: insumos + maquinaria + mano de obra. |
| **Multiusuario con roles** | Dueño, agrónomo, encargado y administración ven lo mismo, con permisos distintos. |
| **Nube, sin servidores propios** | Accedé desde el celular o la oficina; datos seguros en la nube. |
| **Operación mixta** | Campo + ganadería + avícola en una plataforma, no en cinco sistemas. |

### Audiencias prioritarias

1. **Dueño / gerente de producción** — quiere márgenes y control sin microgestionar.
2. **Agrónomo / encargado de campo** — necesita registrar labores y ver el estado de cada lote.
3. **Administración** — busca trazabilidad, inventarios coherentes y reportes exportables.
4. **Integradores / consultoras** — gestionan varias empresas o clientes con módulos activables.

### Nivel de madurez por módulo (transparencia interna)

| Módulo | Madurez | Recomendación de comunicación |
|--------|---------|-------------------------------|
| **Cultivos** | Alta — flujo completo operativo | Contenido estrella; demos y casos de uso |
| **Porcinos** | Alta — ciclo reproductivo + recría + alimentación | Contenido estrella; historias de ciclo completo |
| **Avícola crianza** | Media — operación batch funcional | Mostrar flujos concretos (lote, pesadas, faena) |
| **Avícola huevos** | Media — producción diaria + consumos | Enfocar en postura y registro diario |
| **Avícola carne / ponedoras** | En expansión — panel y lotes/galpones | Comunicar como «próximamente» o beta selecta |
| **Feedlot** | Reciente — implementación v1/v1.5 activa | Gran oportunidad de novedad; KPIs zootécnicos |
| **CORE (plataforma)** | Alta | Transversal a todo el contenido |

---

## 2. Estructura de destacadas de Instagram (Highlights)

Organización sugerida para el perfil. Cada highlight agrupa 5–15 historias reutilizables.

| Highlight | Emoji | Contenido |
|-----------|-------|-----------|
| **Qué es AgroGestion** | ☁️ | Presentación, problema/solución, demo 30 seg |
| **Cultivos** | 🌾 | Campos, lotes, labores, costos, granos |
| **Porcinos** | 🐷 | Ciclo reproductivo, recría, alimentación |
| **Avícola** | 🐔 | Crianza, huevos, carne, ponedoras |
| **Feedlot** | 🐄 | Engorde a corral, GMD, dietas, faena |
| **Reportes** | 📊 | KPIs, paneles, exportaciones |
| **Plataforma** | 🔐 | Roles, módulos, nube, multiempresa |
| **Clientes / Casos** | 🤝 | Testimonios, antes/después (cuando existan) |
| **Tips agro** | 💡 | Contenido educativo sin vender directo |
| **Novedades** | 🚀 | Releases, nuevos módulos, mejoras |

---

## 3. Núcleo de plataforma (CORE) — para destacada «Plataforma»

Funcionalidades transversales que aplican a todos los módulos. Son el «pegamento» comercial.

### 3.1 Multiempresa y acceso seguro

- Cada cliente opera en su **empresa** aislada.
- **Login con JWT**; sesión segura.
- **EULA** para relaciones B2B claras.

### 3.2 Usuarios, roles y permisos

- Roles configurables (dueño, administrador, operador, etc.).
- Permisos **por pantalla y por acción** (ver, crear, modificar, anular).
- El servidor valida permisos en la API — no es solo ocultar menús.

### 3.3 Módulos activables por empresa

- Catálogo: Cultivos, Porcinos, Avícola (crianza, huevos, carne, ponedoras), Feedlot.
- La empresa ve solo los módulos contratados.
- Upsell natural: empezar con cultivos y sumar porcinos.

### 3.4 Períodos de gestión (campañas)

- Concepto transversal: agrupa operaciones por temporada o ciclo productivo.
- Filtra reportes y KPIs por período activo.
- Nombre adaptable según módulo (campaña agrícola, período porcino, etc.).

### 3.5 Calendario y recordatorios

- Calendario operativo en cada módulo.
- Recordatorios de tareas, pesadas, vacunas, labores planificadas.

### 3.6 Inventario unificado (insumos)

- Insumos compartidos entre módulos donde corresponde.
- Movimientos trazables: cada consumo descuenta stock con origen identificado.

### 3.7 Finanzas básicas (ingresos / egresos)

- Registro de ingresos por ventas/faenas.
- Egresos por compras y costos operativos.
- Base para reportes económicos por campaña o lote.

### 3.8 Administración del servicio

- Panel para operador del SaaS: empresas, usuarios, módulos habilitados.
- Configuración por empresa sin tocar código.

**Copy sugerido para historia:**  
*«Un solo login. Varios módulos. Cada usuario ve solo lo que necesita. Así se gestiona una empresa agropecuaria en 2026.»*

---

## 4. Módulo Cultivos 🌾

**Para quién:** productores de granos, oleaginosas, hortícolas y empresas con múltiples campos y lotes.

### 4.1 Funcionalidades desarrolladas

| Área | Qué hace | Beneficio para el productor |
|------|----------|----------------------------|
| **Campos / establecimientos** | Alta y gestión de campos productivos | Mapa mental de la operación |
| **Lotes** | Parcelas con superficie, cultivo asignado, estado | Saber qué hay en cada hectárea |
| **Cultivos y estados** | Máquina de estados configurable (siembra → desarrollo → cosecha) | Seguimiento visual del ciclo |
| **Labores** | Registro de siembra, pulverización, fertilización, cosecha, etc. | Historial operativo completo |
| **Costos por labor** | Insumos + maquinaria propia/alquilada + mano de obra | Saber cuánto costó cada paso |
| **Insumos** | Inventario de semillas, fertilizantes, agroquímicos | Stock real vs. planificado |
| **Agroquímicos / dosis** | Dosis por producto y tipo de aplicación | Menos errores en campo |
| **Maquinaria** | Parque propio, horas de uso, costos | Amortización y costo por labor |
| **Inventario de granos** | Entradas por cosecha, salidas, stock | Trazabilidad post-cosecha |
| **Finanzas** | Ingresos y egresos del negocio agrícola | Flujo de caja por campaña |
| **Reportes** | Producción, costos, rendimientos | Decisiones con datos |
| **Calendario** | Vista de labores y tareas planificadas | Planificación semanal |
| **Configuración** | Usuarios, períodos, estados de cultivo | Parametrización sin desarrollador |

### 4.2 Historias sugeridas (secuencia para highlight)

1. *«¿Cuánto te costó esa pulverización?»* — pantalla de detalle de costos de labor.
2. *«Del lote a la cosecha en un solo lugar»* — progreso de estado del lote.
3. *«Tu silo en números»* — inventario de granos.
4. *«Maquinaria propia o alquilada: todo suma»* — desglose de costos.
5. *«La campaña que cierra con números»* — reporte de campaña.

### 4.3 Hashtags sugeridos

`#agricultura #agtech #gestionagricola #campo #soja #maiz #trigo #productor #agronegocios #softwareagricola`

---

## 5. Módulo Porcinos 🐷

**Para quién:** granjas porcinas, integradores y empresas con ciclo reproductivo completo.

### 5.1 Funcionalidades desarrolladas

| Área | Qué hace | Beneficio |
|------|----------|-----------|
| **Reproductores** | Madres y padrillos con historial completo | Trazabilidad genética y productiva |
| **Servicios** | Monta natural, IA, control de celo | Planificación reproductiva |
| **Gestación y partos** | Seguimiento, chequeos, registro de partos | Alertas de partos próximos |
| **Destetes** | Registro y creación automática de lote de recría | Continuidad del ciclo sin reprocesos |
| **Transferencias** | Movimiento de lechones entre lotes | Flexibilidad operativa |
| **Recría** | Lotes por etapa, pesos, muertes, movimientos | Control de crecimiento |
| **Planes de recría** | Planificación por etapas y metas | Estandarización del engorde |
| **Alimentación** | Fórmulas, insumos compuestos, raciones | Costo alimenticio bajo control |
| **Consumo diario automático** | Descuento automático de inventario según ración | Menos carga manual diaria |
| **Calendario de alimentación** | Vista diaria de consumos teóricos vs. reales | Desvíos visibles al instante |
| **Eventos sanitarios** | Vacunas, tratamientos, diagnósticos | Sanidad documentada |
| **Ventas y faena** | Salida comercial con ingreso registrado | Cierre económico del ciclo |
| **Inventario porcinos** | Stock de insumos del módulo | Integrado con alimentación |
| **Reportes y KPIs** | Preñez, lechones/madre/año, conversión, mortalidad | Tablero gerencial |
| **Configuración** | Parámetros zootécnicos del establecimiento | Adaptado a cada granja |

### 5.2 KPIs destacables para posts

- % de preñez  
- Lechones destetados por madre al año  
- Conversión alimenticia  
- Mortalidad por etapa (madres, lechones, recría)  
- Consumo total de alimento  
- Ingresos por ventas/faena  

### 5.3 Historias sugeridas

1. *«Del servicio al destete sin perder el hilo»* — flujo reproductivo.
2. *«La recría que se crea sola al destetar»* — automatización.
3. *«¿Cuánto comió el lote ayer?»* — consumo diario automático.
4. *«Sanidad con fecha, dosis y responsable»* — eventos sanitarios.
5. *«El tablero que el dueño quiere ver los lunes»* — dashboard de KPIs.

### 5.4 Hashtags sugeridos

`#porcinos #granja #cerdos #produccionporcina #agtech #ganaderia #feed #bioseguridad #gestionpecuaria`

---

## 6. Módulos Avícola 🐔

La plataforma ofrece **cuatro líneas avícolas independientes**, activables por separado.

### 6.1 Avícola Crianza (parrilleros)

| Funcionalidad | Detalle |
|---------------|---------|
| Establecimientos | Alta de galpones / plantas |
| Razas | Catálogo propio del módulo |
| Lotes batch | Ciclo de crianza con aves inicial y actual |
| Pesadas | Curva de crecimiento |
| Mortalidad | Registro con motivo |
| Consumos | Alimento desde inventario CORE |
| Ventas / faena | Faena, venta en pie, descarte |
| Sanidad | Eventos sanitarios por lote |
| Exportación | Operaciones del lote exportables |
| Panel resumen | KPIs del período activo |

**Mensaje clave:** *«Crianza de parrilleros con el mismo rigor que una granja integrada.»*

### 6.2 Avícola Huevos (producción)

| Funcionalidad | Detalle |
|---------------|---------|
| Establecimientos + mapa | Ubicación geográfica de plantas |
| Razas / líneas | Catálogo de genética |
| Lotes de postura | Plantel vivo con seguimiento |
| Producción diaria | Registro día a día por lote |
| Consumos de alimento | Integrado con inventario |
| Ajustes de plantel | Correcciones auditadas |
| Sanidad | Vacunas y tratamientos |
| Reportes | Producción y consumo por período |
| Clima (integración) | Dato contextual en detalle de lote |

**Mensaje clave:** *«Cada huevo cuenta. Registro diario que alimenta tus reportes.»*

### 6.3 Avícola Carne

- Panel de resumen y listado de lotes de engorde avícola.
- API y dominio separados de crianza y huevos.
- En comunicación: posicionar como línea especializada para operadores de carne de ave.

### 6.4 Avícola Ponedoras / Recría

- Gestión de galpones de ponedoras y recría.
- Separado del módulo de huevos comercial.
- Mensaje: *«Recría y postura en módulos distintos, datos que no se mezclan.»*

### 6.5 Hashtags avícola

`#avicola #pollos #huevos #parrillero #ponedoras #produccionavicola #galpon #agtech`

---

## 7. Módulo Feedlot (Engorde a corral) 🐄

**Estado:** implementación reciente — gran oportunidad de contenido «novedad».

**Para quién:** feedlots, engordadores a corral, operadores de confinamiento bovino.

### 7.1 Funcionalidades desarrolladas

| Área | Qué hace |
|------|----------|
| **Panel** | Cabezas en feed, lotes activos, GMD promedio, mortalidad %, consumo total kg |
| **Establecimientos y corrales** | Capacidad por corral, estado (disponible/ocupado) |
| **Lotes de engorde** | Ingreso de hacienda, categoría, raza, tenencia propia o consignación |
| **Pesadas** | Curva de peso y cálculo de GMD (ganancia media diaria) |
| **Consumos** | Alimento con descuento de inventario |
| **Dietas y fases** | Plan nutricional por días desde ingreso |
| **Lecturas de comedero (bunk score)** | Ajuste fino de entrega de alimento |
| **Mortalidad** | Bajas con motivo; actualiza plantel |
| **Sanidad** | Vacunas, tratamientos, diagnósticos |
| **Ventas / faena** | Faena, venta en pie, descarte con precio y peso |
| **Cierre de lote (closeout)** | KPIs y margen al cerrar el ciclo |
| **Ajustes de plantel** | Auditoría de correcciones |
| **Catálogos** | Categorías, razas, motivos de muerte, proveedores |
| **Reportes** | Indicadores zootécnicos del período |
| **Calendario** | Recordatorios operativos |

### 7.2 KPIs para contenido

- **GMD / ADPV** — ganancia media diaria  
- **Conversión alimenticia** — kg alimento / kg ganados  
- **Mortalidad %** — salud del lote  
- **Breakeven** — precio mínimo de venta  
- **Cabezas-día** — base de costos  

### 7.3 Mensaje diferencial

*«No es un Excel con corrales. Es engorde con GMD, conversión y cierre económico por lote.»*

### 7.4 Hashtags feedlot

`#feedlot #ganado #engorde #confinamiento #GMD #ganaderia #carne #novillo #agtech`

---

## 8. Pilares de contenido (estrategia editorial)

Distribución recomendada del calendario:

| Pilar | % | Ejemplos |
|-------|---|----------|
| **Educativo / dolor** | 35% | «5 señales de que perdés plata en el campo», «Qué es el GMD y por qué importa» |
| **Producto / feature** | 30% | Demo de labores, dashboard porcinos, panel feedlot |
| **Prueba social** | 15% | Testimonios, cifras de clientes (cuando existan) |
| **Detrás de cámaras / equipo** | 10% | Desarrollo, roadmap, cultura |
| **Promo / CTA** | 10% | Demo gratuita, contacto, webinar |

### Tono de voz

- **Profesional pero cercano** — hablar como un agrónomo que entiende de software.
- **Concreto** — números, pantallas, flujos reales; evitar buzzwords vacíos.
- **Argentino / latino** — «vos», «campo», «campaña», «cabezas», «lote».
- **Sin prometer lo que no está** — marcar módulos en beta cuando corresponda.

### Formatos por red

| Formato | Uso |
|---------|-----|
| **Reel 15–30 s** | Demo de una función; antes/después |
| **Carrusel** | 5–7 slides educativos o tour de módulo |
| **Historia** | Tips rápidos, encuestas, «¿Cómo llevás X?» |
| **Post estático** | Frase + imagen IA + CTA |
| **Live** | Demo en vivo con Q&A (mensual) |

---

## 9. Prompts para generación de imágenes con IA

Instrucciones generales para todos los prompts:

- **Estilo:** fotografía editorial profesional o ilustración realista limpia; colores naturales; luz dorada de campo.
- **Evitar:** texto dentro de la imagen (se agrega en Canva/CapCut después).
- **Proporciones:** 1080×1080 (feed), 1080×1920 (historias/reels cover).
- **Marca:** paleta verde `#10b981` (cultivos), marrón `#78350f` (feedlot), azul `#0ea5e9` (avícola).

---

### 9.1 Marca y presentación

**Prompt 01 — Hero AgroGestion**
```
Fotografía aérea al atardecer de un paisaje agrícola mixto en Argentina: campos de soja verdes, un galpón avícola moderno al fondo, corrales de feedlot con ganado, estilo editorial National Geographic, luz dorada, sin texto, ultra detallado, 8k
```

**Prompt 02 — Transformación digital**
```
Split conceptual: lado izquierdo cuaderno de campo manchado y calculadora vieja sobre mesa de madera; lado derecho tablet moderna mostrando gráficos verdes abstractos en granja, estilo publicidad tech limpia, fondo desenfocado de campo, sin texto legible en pantalla
```

**Prompt 03 — Equipo en campo**
```
Agrónomo argentino con tablet en mano caminando entre lotes de cultivo, camisa a cuadros, botas, cielo parcialmente nublado, foto periodística profesional, colores naturales, sin logos
```

---

### 9.2 Cultivos

**Prompt 04 — Labores y costos**
```
Tractor pulverizando campo de soja al amanecer con neblina ligera, vista lateral dramática, gotas de pulverización visibles, estilo agro comercial premium, sin texto
```

**Prompt 05 — Lotes y estados**
```
Vista aérea de parcelas agrícolas con líneas claras de lotes de distintos tonos de verde y marrón, como mapa de gestión, luz de mediodía, foto satelital estilizada realista
```

**Prompt 06 — Inventario granos**
```
Interior de silo moderno con granos de maíz dorados cayendo, trabajador con casco observando, iluminación industrial cálida, foto documental de alta calidad
```

**Prompt 07 — Maquinaria**
```
Parque de maquinaria agrícola alineado: cosechadora y sembradora rojas en galpón abierto, amanecer, reflejos metálicos, estilo catálogo industrial
```

---

### 9.3 Porcinos

**Prompt 08 — Ciclo reproductivo**
```
Interior de granja porcina moderna y limpia, fila de jaulas con cerdas sanas, iluminación LED suave, ambiente higiénico profesional, foto documental sin texto
```

**Prompt 09 — Lechones y destete**
```
Lechones rosados en cama de paja limpia, primer plano tierno pero profesional, granja moderna desenfocada al fondo, luz natural suave
```

**Prompt 10 — Alimentación**
```
Operario vertiendo alimento balanceado en comedero de corral de recría porcina, polvo de alimento en el aire iluminado por rayo de sol, estilo reportaje agropecuario
```

**Prompt 11 — Dashboard KPIs (conceptual)**
```
Mesa de oficina rural con monitor mostrando gráficos abstractos verdes y naranjas (sin texto legible), ventana con vista a granja porcina, estilo stock photo premium
```

---

### 9.4 Avícola

**Prompt 12 — Crianza parrillera**
```
Galpón avícola moderno con pollos parrilleros en piso de cama, ventilación visible, luz difusa uniforme, foto industrial limpia
```

**Prompt 13 — Producción de huevos**
```
Cintas transportadoras de huevos marrones en planta clasificadora moderna, reflejos blancos, higiene industrial, fotografía comercial
```

**Prompt 14 — Mapa de establecimientos (conceptual)**
```
Vista aérea de varias naves avícolas blancas en zona rural verde, caminos de tierra conectándolas, estilo infografía fotográfica
```

---

### 9.5 Feedlot

**Prompt 15 — Corrales de engorde**
```
Feedlot bovino al atardecer: corrales con novillos Angus, comedores llenos de grano, polvo dorado en el aire, foto épica estilo revista ganadera
```

**Prompt 16 — Pesada y GMD (conceptual)**
```
Veterinario o encargado observando ganado en báscula rural, animales calmados, atardecer, estilo documental argentino
```

**Prompt 17 — Dietas y comedero**
```
Primer plano de comedero de feedlot con ración balanceada, bovinos comiendo al fondo desenfocados, textura de alimento detallada, luz lateral cálida
```

**Prompt 18 — Panel feedlot (conceptual)**
```
Oficina rústica moderna en establecimiento ganadero, pantalla con curva de crecimiento abstracta (sin texto), vista por ventana a corrales
```

---

### 9.6 Plataforma y confianza

**Prompt 19 — Seguridad y nube**
```
Concepto abstracto: candado digital formado por hojas y circuitos sobre fondo de campo verde, estilo ilustración tech-agro minimalista, colores verde y blanco
```

**Prompt 20 — Multiusuario**
```
Cuatro personas diversas (dueño, agrónoma, administradora, operario) reunidas alrededor de notebook en oficina de campo, colaborando, estilo foto corporativa cálida
```

---

## 10. Calendario de publicaciones (8 semanas)

**Frecuencia:** 3 publicaciones por semana (lun / mié / vie) + historias diarias reutilizables.  
**Horario sugerido (Argentina):** 7:00–8:30 (antes del campo) o 19:00–20:30 (post jornada).

Leyenda: **F** = Feed (post/carrusel/reel) · **H** = Historia · **P** = Prompt de imagen (sección 9)

---

### Semana 1 — Lanzamiento y posicionamiento

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F + H | Presentación | «¿Tu gestión agropecuaria vive en 5 planillas? Hay otra forma.» | P01 |
| Mar | H | Encuesta | «¿Qué módulo te interesa más? 🌾 🐷 🐔 🐄» | — |
| Mié | F | Problema/dolor | «El costo que no ves es el que más duele.» Carrusel 5 dolores | P02 |
| Jue | H | Tip | «Tip: registrá la labor el mismo día que la hacés» | — |
| Vie | F + H | Plataforma CORE | «Un login. Varios módulos. Un solo equipo.» | P20 |
| Sáb | H | Behind scenes | «Así desarrollamos AgroGestion» (foto equipo) | P03 |
| Dom | H | CTA | «Pedí tu demo → link en bio» | P01 |

---

### Semana 2 — Módulo Cultivos

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Campos y lotes | «Cada hectárea con nombre, cultivo y estado.» | P05 |
| Mar | H | Tip cultivo | «¿Sabés en qué estado está cada lote sin llamar al encargado?» | — |
| Mié | F + Reel | Labores y costos | «Pulverización: $X insumos + $Y maquinaria + $Z mano de obra. Así.» | P04 |
| Jue | H | Feature | Demo 15 s: modal de costos de labor | P04 |
| Vie | F | Inventario granos | «Del silo al reporte sin Excel.» | P06 |
| Sáb | H | Educativo | «¿Qué es la trazabilidad operativa?» | — |
| Dom | H | CTA cultivos | «¿Productor de granos? Escribinos.» | P05 |

---

### Semana 3 — Módulo Porcinos (parte 1: reproducción)

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Ciclo reproductivo | «Del servicio al destete: un solo hilo.» Carrusel 6 pasos | P08 |
| Mar | H | KPI | «% de preñez: el número que define tu año» | — |
| Mié | F + Reel | Gestación y partos | «Partos próximos en los próximos 7 días. Alerta automática.» | P09 |
| Jue | H | Pregunta | «¿Cuántos lechones destetás por madre al año?» | — |
| Vie | F | Destete → recría | «Destetás y la recría ya está creada.» | P09 |
| Sáb | H | Tip | «Registrá el celo cuando lo ves, no el viernes» | — |
| Dom | H | CTA | «Granja porcina: pedí demo del módulo» | P08 |

---

### Semana 4 — Módulo Porcinos (parte 2: alimentación y cierre)

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Alimentación | «Fórmulas por etapa. Consumo que se descuenta solo.» | P10 |
| Mar | H | Feature | Demo: calendario de alimentación diaria | P10 |
| Mié | F | Sanidad | «Vacuna, fecha, lote, responsable. Sin cuaderno.» | P08 |
| Jue | H | KPI | «Conversión alimenticia: ¿la medís o la adivinás?» | — |
| Vie | F + Reel | Dashboard KPIs | «El tablero del dueño: un vistazo, todos los números.» | P11 |
| Sáb | H | Ventas/faena | «Cerrá el ciclo: faena con ingreso registrado» | — |
| Dom | H | CTA | «¿Integrador porcino? Hablemos.» | P11 |

---

### Semana 5 — Avícola

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Avícola general | «4 líneas avícolas. Un solo sistema. Datos que no se mezclan.» | P12 |
| Mar | H | Tip | «Crianza ≠ huevos ≠ ponedoras. ¿Por qué importa?» | — |
| Mié | F | Crianza | «Pesadas, mortalidad, faena: lote cerrado con números.» | P12 |
| Jue | H | Feature | Demo: detalle de lote crianza (exportar) | P12 |
| Vie | F | Huevos | «Producción diaria por lote. Cada huevo cuenta.» | P13 |
| Sáb | H | Mapa | «Tus plantas en el mapa.» | P14 |
| Dom | H | CTA avícola | «¿Planta de huevos o parrillero? Demo.» | P13 |

---

### Semana 6 — Feedlot (novedad)

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Lanzamiento feedlot | «NUEVO: Engorde a corral con GMD, conversión y closeout.» | P15 |
| Mar | H | Educativo | «¿Qué es el GMD y por qué el feedlot vive de ese número?» | — |
| Mié | F + Reel | Panel feedlot | «Cabezas en feed, GMD promedio, mortalidad %. Un panel.» | P18 |
| Jue | H | Feature | Demo: curva de peso del lote | P16 |
| Vie | F | Bunk score | «Comedero vacío o lleno: ajustá la ración con datos.» | P17 |
| Sáb | H | Tenencia | «Hacienda propia o consignación: mismo sistema.» | — |
| Dom | H | CTA feedlot | «¿Operás feedlot? Sé de los primeros en probarlo.» | P15 |

---

### Semana 7 — Reportes, costos y decisión

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Costos | «El margen no es magia. Es labores + consumos + faenas.» | P02 |
| Mar | H | Comparativa | «Antes: 3 Excel. Ahora: 1 reporte por campaña.» | — |
| Mié | F | Períodos | «Campaña 24/25 cerrada. Números listos para la reunión.» | P05 |
| Jue | H | Tip gerencial | «3 KPIs que el dueño debería ver cada lunes» | — |
| Vie | F + Reel | Multi-módulo | «Soja en el campo. Novillos en el feedlot. Un balance.» | P01 |
| Sáb | H | Roles | «El agrónomo carga. El dueño mira. Nadie pisa al otro.» | P20 |
| Dom | H | CTA | «Demo de 30 min. Sin compromiso.» | P03 |

---

### Semana 8 — Cierre, prueba social y conversión

| Día | Tipo | Tema | Copy corto | Prompt |
|-----|------|------|------------|--------|
| Lun | F | Resumen 8 módulos | Carrusel «Todo lo que AgroGestion hace hoy» | P01 |
| Mar | H | FAQ | «¿Funciona sin internet?» / «¿Puedo empezar solo con cultivos?» | — |
| Mié | F | Testimonio / caso | *(Placeholder hasta tener cliente)* «Productor X cerró campaña con…» | P03 |
| Jue | H | Seguridad | «Tus datos en la nube, con acceso por roles.» | P19 |
| Vie | F + Reel | Recap mejor contenido | Compilación 30 s de demos | P01 |
| Sáb | H | Encuesta | «¿Qué módulo quieren ver en profundidad el mes que viene?» | — |
| Dom | H | CTA fuerte | «Empezá la próxima campaña con orden. Link en bio.» | P01 |

---

## 11. Plantillas de copy reutilizables

### Hook (primer línea)

- «Si todavía cerrás la campaña con Excel, esto es para vos.»
- «El encargado sabe. El dueño no. Hasta ahora.»
- «¿Cuánto costó esa faena? Si tardás más de 10 segundos en responder, seguí leyendo.»

### CTA (cierre)

- «Pedí una demo de 30 min — link en bio.»
- «Comentá CULTIVOS / PORCINOS / AVÍCOLA / FEEDLOT y te contamos más.»
- «Guardá este post para la próxima reunión de campo.»

### Historia interactiva

- Encuesta: «¿Cómo llevás los costos? Excel / Cuaderno / Software / No los llevo»
- Quiz: «¿Sabés tu GMD promedio? Sí / Más o menos / No»
- Slider: «¿Cuántas hectáreas administrás?»

---

## 12. Sugerencias estratégicas

### 12.1 Priorizar según madurez comercial

1. **Cultivos + Porcinos** — contenido principal los primeros 2 meses.  
2. **Feedlot** — campaña de novedad; ideal para ads segmentados a engordadores.  
3. **Avícola** — contenido por sub-módulo; evitar mezclar crianza con huevos en un mismo post.

### 12.2 Contenido que el equipo de desarrollo puede aportar

- **Screen recordings** de 15–30 s de flujos reales (más impacto que imágenes IA).
- **Datos de demo** anonimizados para carruseles de KPIs.
- **Changelog mensual** para highlight «Novedades».

### 12.3 Herramientas recomendadas

| Uso | Herramienta |
|-----|-------------|
| Imágenes IA | Midjourney, DALL·E, Ideogram, Leonardo |
| Edición | Canva (plantillas 1080×1080 y 1080×1920) |
| Reels | CapCut, pantalla del sistema + voz en off |
| Programación | Metricool, Buffer o Meta Business Suite |
| Links | Linktree o página en web con UTMs por campaña |

### 12.4 Métricas a seguir

- Alcance y guardados (indican intención)  
- Clics en link de bio (UTM: `?utm_source=instagram&utm_medium=organic&utm_campaign=semana-N`)  
- Comentarios con palabra clave (CULTIVOS, DEMO, etc.)  
- DMs recibidos post-publicación  
- Asistencias a demo solicitadas  

### 12.5 Lo que NO comunicar hasta estar listo

- Facturación automática / pasarela de pagos (no implementada).  
- Trazabilidad comercial certificable (en roadmap — ver spec).  
- Wizards con IA (spec en desarrollo).  
- Funcionalidades marcadas como v2 en specs avícolas (clasificación de huevos por calibre, etc.).

### 12.6 Próximos pasos del equipo

1. **Aprobar** identidad visual (logo sobre prompts P01, P19).  
2. **Grabar** 5 demos cortas (cultivos labor, porcinos dashboard, feedlot panel, avícola huevos producción, selector de módulos).  
3. **Crear** plantillas Canva con tipografía y colores de marca.  
4. **Configurar** link de bio con formulario de demo (Typeform / Google Form).  
5. **Definir** responsable de respuesta a DMs en menos de 24 h.  
6. **Iterar** calendario según métricas de semanas 1–2.

---

## 13. Anexo — Matriz módulo × formato de contenido

| Módulo | Reel | Carrusel | Historia | Live demo |
|--------|------|----------|----------|-----------|
| Cultivos | Labores + costos | 5 estados del lote | Tip diario | Recorrido campaña |
| Porcinos | Destete → recría | Ciclo reproductivo | KPI del día | Dashboard gerencial |
| Avícola crianza | Pesada + curva | Operaciones del lote | Faena | Exportar lote |
| Avícola huevos | Producción diaria | Día en la planta | Mapa | Registro diario |
| Feedlot | Panel GMD | Closeout explicado | Bunk score | Curva de peso |
| CORE | Selector módulos | Roles y permisos | FAQ | Login + empresas |

---

*Documento elaborado a partir del inventario funcional del repositorio AgroGestion (junio 2026). Revisar trimestralmente al incorporar nuevos módulos o features.*
