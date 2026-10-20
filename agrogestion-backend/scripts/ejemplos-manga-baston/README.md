# Ejemplos de manga — 10 corridas

Cada carpeta es **una corrida**. Entrá a la carpeta y cargá los archivos **en orden 1 → 2 → 3 → 4 → 5**.

Usá el lote **Lote Manga Baston Sep 2026**. No mezcles corridas distintas en el mismo lote (salvo que reinicies datos).

EID: `982 000123456701` … `708`. Compra `709`. Intruso `982 000999111222`.

## Perfiles a crear (una vez)

| Perfil | Tipo | Columnas |
|--------|------|----------|
| *(ninguno)* | — | `EID` `VID` `Weight` `Date` `Time` |
| Identificador bastón | Enrolar / Aplicar / Contar / Baja | `Identificador`→EID, `TagVisual`→caravana, `Dia`→fecha, `Instante`→hora |
| Kilos cabezal | Pesar | lo mismo + `Kilos`→peso |
| CaravanaElec | Contar | `CaravanaElec`→EID |
| Peso vivo | Pesar | `EID` `VID` `Peso vivo`→peso `Date` `Time` |

## Las 10 corridas

| Carpeta | Perfil | 1 | 2 | 3 | 4 | 5 |
|---------|--------|---|---|---|---|---|
| `corrida-01-fabrica` | ninguno | Enrolar | Pesar | Aplicar | Contar | Baja |
| `corrida-02-identificador` | Identificador | Enrolar | Aplicar | Contar | Baja | Enrolar 09 |
| `corrida-03-kilos` | Identificador + Kilos | Enrolar | Pesar ingreso | Pesar día 10 | Contar | Baja |
| `corrida-04-caravana-elec` | Identificador + CaravanaElec | Enrolar | Contar 8 | Contar faltantes | Contar 01–04 | Baja |
| `corrida-05-peso-vivo` | ninguno + Peso vivo | Enrolar | Pesar | Pesar día 10 | Contar (ignora kg) | Baja |
| `corrida-06-bloqueos` | ninguno | Enrolar | Duplicados | Pesar fuera de rango | Contar | EID Excel (bloquea) |
| `corrida-07-aftosa` | Identificador + Kilos | Enrolar | Pesar | Aplicar + nota | Aplicar aftosa | Contar |
| `corrida-08-punto-y-coma` | Identificador + Kilos | Enrolar | Pesar `;` | Aplicar | Contar | Baja |
| `corrida-09-draft-bcs` | Identificador + Kilos | Enrolar | Pesar + Draft/BCS | Pesar día 10 | Contar | Baja |
| `corrida-10-compra` | Identificador + Kilos | Enrolar 8 | Pesar | Enrolar 09 | Contar 8+09 | Baja 07–08 |

Detalle de cada archivo: README dentro de la carpeta.
