/**
 * Convierte agentes de agency-agents (.md con frontmatter) a reglas Cursor (.mdc).
 * Uso: node scripts/convertir-agentes-cursor.mjs [ruta-agency-agents] [salida]
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const raizRepo = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const origenAgency =
  process.argv[2] || path.join(raizRepo, "vendor", "agency-agents");
const destino =
  process.argv[3] || path.join(raizRepo, ".cursor", "rules", "agency");

const AGENTES_AGROGESTION = [
  "engineering/engineering-backend-architect.md",
  "engineering/engineering-frontend-developer.md",
  "engineering/engineering-software-architect.md",
  "engineering/engineering-code-reviewer.md",
  "engineering/engineering-security-engineer.md",
  "engineering/engineering-database-optimizer.md",
  "engineering/engineering-devops-automator.md",
  "engineering/engineering-codebase-onboarding-engineer.md",
  "testing/testing-api-tester.md",
  "testing/testing-reality-checker.md",
  "testing/testing-performance-benchmarker.md",
  "product/product-manager.md",
];

function slugificar(nombre) {
  return nombre
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

function parsearFrontmatter(contenido) {
  const match = contenido.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n([\s\S]*)$/);
  if (!match) return { campos: {}, cuerpo: contenido };
  const bloque = match[1];
  const cuerpo = match[2];
  const campos = {};
  for (const linea of bloque.split(/\r?\n/)) {
    const m = linea.match(/^(\w+):\s*(.*)$/);
    if (m) campos[m[1]] = m[2].trim();
  }
  return { campos, cuerpo };
}

function convertirArchivo(rutaOrigen, rutaDestino) {
  const contenido = fs.readFileSync(rutaOrigen, "utf8");
  const { campos, cuerpo } = parsearFrontmatter(contenido);
  const nombre = campos.name || path.basename(rutaOrigen, ".md");
  const descripcion =
    campos.description || `Agente Agency: ${nombre}`;
  const slug = slugificar(nombre);
  const mdc = `---
description: ${descripcion.replace(/"/g, '\\"')}
globs: ""
alwaysApply: false
---

${cuerpo.trim()}
`;
  const archivo = path.join(rutaDestino, `${slug}.mdc`);
  fs.writeFileSync(archivo, mdc, "utf8");
  return slug;
}

function main() {
  if (!fs.existsSync(origenAgency)) {
    console.error(
      "No se encuentra agency-agents en:",
      origenAgency,
      "\nEjecuta: git clone --depth 1 https://github.com/msitarzewski/agency-agents.git vendor/agency-agents"
    );
    process.exit(1);
  }

  fs.mkdirSync(destino, { recursive: true });

  const generados = [];
  for (const rel of AGENTES_AGROGESTION) {
    const origen = path.join(origenAgency, rel);
    if (!fs.existsSync(origen)) {
      console.warn("Omitido (no existe):", rel);
      continue;
    }
    const slug = convertirArchivo(origen, destino);
    generados.push(slug);
  }

  console.log(`Convertidos ${generados.length} agentes -> ${destino}`);
  generados.forEach((s) => console.log("  -", s + ".mdc"));
}

main();
