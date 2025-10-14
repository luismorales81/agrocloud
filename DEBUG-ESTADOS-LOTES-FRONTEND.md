# 🐛 Debug: Ver Estados de Lotes en el Frontend

## 🔍 Método 1: Consola del Navegador (MÁS RÁPIDO)

### Pasos:

1. **Abre la aplicación** en `http://localhost:3000`

2. **Ve a la sección "Lotes"**

3. **Abre DevTools** (F12)

4. **Ve a la pestaña "Console"**

5. **Pega este código y presiona Enter:**

```javascript
// Ver estados de todos los lotes visibles
const lotesVisibles = Array.from(document.querySelectorAll('table tbody tr')).map((row, index) => {
  const cells = row.querySelectorAll('td');
  if (cells.length >= 4) {
    const nombre = cells[0]?.textContent?.trim() || 'Sin nombre';
    const superficie = cells[1]?.textContent?.trim() || '-';
    const cultivo = cells[2]?.textContent?.trim() || '-';
    const estado = cells[3]?.textContent?.trim() || 'SIN ESTADO';
    const botones = row.querySelector('td:last-child')?.textContent?.trim() || 'Sin botones';
    
    return {
      '#': index + 1,
      'Nombre': nombre,
      'Superficie': superficie,
      'Cultivo': cultivo,
      'Estado': estado,
      'Botones': botones
    };
  }
  return null;
}).filter(Boolean);

console.table(lotesVisibles);

// Resumen
console.log('\n📊 RESUMEN:');
console.log(`Total de lotes visibles: ${lotesVisibles.length}`);

const conBotonSembrar = lotesVisibles.filter(l => l.Botones.includes('Sembrar')).length;
const conBotonCosechar = lotesVisibles.filter(l => l.Botones.includes('Cosechar')).length;
const sinBotones = lotesVisibles.filter(l => !l.Botones.includes('Sembrar') && !l.Botones.includes('Cosechar')).length;

console.log(`✅ Lotes con botón SEMBRAR: ${conBotonSembrar}`);
console.log(`✅ Lotes con botón COSECHAR: ${conBotonCosechar}`);
console.log(`⚠️ Lotes SIN botones de siembra/cosecha: ${sinBotones}`);

// Análisis de estados
const estadosUnicos = [...new Set(lotesVisibles.map(l => l.Estado))];
console.log(`\n📋 Estados encontrados: ${estadosUnicos.join(', ')}`);

// Verificar qué estados deberían mostrar botones
console.log('\n🔍 ANÁLISIS POR ESTADO:');
estadosUnicos.forEach(estado => {
  const lotes = lotesVisibles.filter(l => l.Estado === estado);
  const conSembrar = lotes.filter(l => l.Botones.includes('Sembrar')).length;
  const conCosechar = lotes.filter(l => l.Botones.includes('Cosechar')).length;
  
  console.log(`\n${estado}:`);
  console.log(`  - Total: ${lotes.length} lote(s)`);
  console.log(`  - Con botón Sembrar: ${conSembrar}`);
  console.log(`  - Con botón Cosechar: ${conCosechar}`);
  
  // Verificar si es correcto
  const estadoUpper = estado.toUpperCase().replace(/\s+/g, '_');
  const deberiaSembrar = ['DISPONIBLE', 'PREPARADO', 'EN_PREPARACION'].includes(estadoUpper);
  const deberiaCosechar = ['SEMBRADO', 'LISTO_PARA_COSECHA', 'EN_CRECIMIENTO', 'EN_FLORACION', 'EN_FRUTIFICACION'].includes(estadoUpper);
  
  if (deberiaSembrar && conSembrar === 0) {
    console.log(`  ⚠️ PROBLEMA: Debería tener botón SEMBRAR pero no lo tiene`);
  }
  if (deberiaCosechar && conCosechar === 0) {
    console.log(`  ⚠️ PROBLEMA: Debería tener botón COSECHAR pero no lo tiene`);
  }
  if (!deberiaSembrar && !deberiaCosechar) {
    console.log(`  ✅ CORRECTO: No debe tener botones de siembra/cosecha`);
  }
});
```

---

## 📊 Qué Esperar

### Si Todo Está Bien:
```
📊 RESUMEN:
Total de lotes visibles: 5
✅ Lotes con botón SEMBRAR: 2
✅ Lotes con botón COSECHAR: 2
⚠️ Lotes SIN botones de siembra/cosecha: 1

📋 Estados encontrados: DISPONIBLE, SEMBRADO, COSECHADO

🔍 ANÁLISIS POR ESTADO:

DISPONIBLE:
  - Total: 2 lote(s)
  - Con botón Sembrar: 2
  - Con botón Cosechar: 0
  ✅ CORRECTO

SEMBRADO:
  - Total: 2 lote(s)
  - Con botón Sembrar: 0
  - Con botón Cosechar: 2
  ✅ CORRECTO

COSECHADO:
  - Total: 1 lote(s)
  - Con botón Sembrar: 0
  - Con botón Cosechar: 0
  ✅ CORRECTO: No debe tener botones de siembra/cosecha
```

### Si Hay Problemas:
```
⚠️ PROBLEMA: Debería tener botón SEMBRAR pero no lo tiene
⚠️ PROBLEMA: Debería tener botón COSECHAR pero no lo tiene
```

---

## 🔍 Método 2: Ver Datos Crudos del API

### Pega esto en la consola:

```javascript
// Obtener datos directos del API
fetch('http://localhost:8080/api/v1/lotes', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(res => res.json())
.then(data => {
  console.log('\n🌐 DATOS DEL BACKEND:');
  console.log(`Total de lotes: ${data.length || 0}`);
  
  if (Array.isArray(data)) {
    const lotesConEstado = data.map(lote => ({
      'ID': lote.id,
      'Nombre': lote.nombre,
      'Estado Backend': lote.estado,
      'Cultivo': lote.cultivoActual || lote.cultivo || '-',
      'Superficie': lote.areaHectareas || lote.superficie
    }));
    
    console.table(lotesConEstado);
    
    // Contar por estado
    const estadosBackend = {};
    data.forEach(lote => {
      const estado = lote.estado;
      estadosBackend[estado] = (estadosBackend[estado] || 0) + 1;
    });
    
    console.log('\n📊 LOTES POR ESTADO (BACKEND):');
    Object.entries(estadosBackend).forEach(([estado, cantidad]) => {
      console.log(`  ${estado}: ${cantidad} lote(s)`);
    });
  }
})
.catch(err => {
  console.error('❌ Error al obtener lotes del backend:', err);
  console.log('Verifica que:');
  console.log('  1. El backend esté corriendo en http://localhost:8080');
  console.log('  2. Tengas sesión iniciada (token válido)');
});
```

---

## 🎯 Interpretación de Resultados

### Caso 1: NO ves NINGÚN botón de sembrar/cosechar

**Posibles causas:**

1. **Todos tus lotes están en estados finales:**
   - `COSECHADO` → No muestra botones (correcto)
   - `EN_DESCANSO` → No muestra botones (correcto)
   - `ABANDONADO` → No muestra botones (correcto)
   
   **Solución**: Crea un lote nuevo o cambia el estado de uno existente

2. **Hay un problema de formato de estado:**
   - Backend envía: `"DISPONIBLE"`
   - Frontend espera: `"DISPONIBLE"`
   - Si el formato no coincide, los botones no aparecen
   
   **Solución**: Verificar en Método 2 el formato exacto

3. **Problema de caché:**
   - Ya lo descartamos si probaste con otro navegador

---

### Caso 2: Ves ALGUNOS botones pero no todos

**Causa probable:**
- Algunos lotes están en estados correctos
- Otros lotes están en estados que no permiten siembra/cosecha

**Esto es CORRECTO**

---

### Caso 3: Estado dice "SEMBRADO" pero NO aparece botón de cosechar

**Causa:**
- Inconsistencia entre backend y frontend (el bug que encontramos)

**Solución:**
- Necesito actualizar el backend para permitir cosecha desde SEMBRADO

---

## 🔧 Siguiente Paso

Después de ejecutar estos scripts de debug:

1. **Copia el resultado** de la consola
2. **Pégalo aquí** para que pueda analizarlo
3. Te diré exactamente qué está pasando
4. Aplicaremos la corrección específica

---

## ⚡ Atajo Rápido

Si no quieres usar la consola, simplemente:

1. **Toma una captura de pantalla** de la tabla de Lotes
2. Asegúrate de que se vea la columna "Estado"
3. Envíamela

Con eso puedo ver exactamente qué estados tienes y por qué no aparecen los botones.
