#!/usr/bin/env node

/**
 * Script de verificación de rutas API
 * Detecta uso incorrecto de '/api/' literal en el código
 *
 * Uso: node scripts/check-api-routes.js
 */

import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);

// Configuración
const SRC_DIR = "src";
const ALLOWED_FILES = [
  "src/services/api.ts",
  "src/services/api.js",
];

const FORBIDDEN_PATTERNS = [
  /api\.get\('\/api\//,
  /api\.post\('\/api\//,
  /api\.put\('\/api\//,
  /api\.delete\('\/api\//,
  /api\.patch\('\/api\//,
  /fetch\('\/api\//,
  /axios\.get\('\/api\//,
  /axios\.post\('\/api\//,
  /axios\.put\('\/api\//,
  /axios\.delete\('\/api\//,
  /axios\.patch\('\/api\//,
];

const colors = {
  red: "\x1b[31m",
  green: "\x1b[32m",
  yellow: "\x1b[33m",
  blue: "\x1b[34m",
  magenta: "\x1b[35m",
  cyan: "\x1b[36m",
  white: "\x1b[37m",
  reset: "\x1b[0m",
  bold: "\x1b[1m",
};

function log(message, color = "white") {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function logError(message) {
  console.error(`${colors.red}${colors.bold}❌ ${message}${colors.reset}`);
}

function logWarning(message) {
  console.warn(`${colors.yellow}${colors.bold}⚠️  ${message}${colors.reset}`);
}

function logSuccess(message) {
  console.log(`${colors.green}${colors.bold}✅ ${message}${colors.reset}`);
}

function logInfo(message) {
  console.log(`${colors.blue}${colors.bold}ℹ️  ${message}${colors.reset}`);
}

function getAllSourceFiles(dir) {
  const files = [];

  function traverse(currentDir) {
    const items = fs.readdirSync(currentDir);

    for (const item of items) {
      const fullPath = path.join(currentDir, item);
      const stat = fs.statSync(fullPath);

      if (stat.isDirectory()) {
        if (!["node_modules", ".git", "dist", "build"].includes(item)) {
          traverse(fullPath);
        }
      } else if (stat.isFile() && /\.(ts|tsx|js|jsx)$/.test(item)) {
        files.push(fullPath);
      }
    }
  }

  traverse(dir);
  return files;
}

function checkFile(filePath) {
  const content = fs.readFileSync(filePath, "utf8");
  const lines = content.split("\n");
  const issues = [];

  lines.forEach((line, index) => {
    FORBIDDEN_PATTERNS.forEach((pattern) => {
      if (pattern.test(line)) {
        issues.push({
          line: index + 1,
          content: line.trim(),
          pattern: pattern.toString(),
        });
      }
    });
  });

  return issues;
}

function main() {
  log("🔍 Verificando rutas API en el proyecto...", "cyan");
  log("════════════════════════════════════════════════════════", "cyan");

  if (!fs.existsSync(SRC_DIR)) {
    logError(`Directorio ${SRC_DIR} no encontrado`);
    process.exit(1);
  }

  const sourceFiles = getAllSourceFiles(SRC_DIR);
  logInfo(`Analizando ${sourceFiles.length} archivos fuente...`);

  let totalIssues = 0;
  const filesWithIssues = [];

  sourceFiles.forEach((filePath) => {
    const relativePath = path.relative(process.cwd(), filePath);

    if (ALLOWED_FILES.some((allowed) => relativePath.includes(allowed))) {
      log(`⏭️  Saltando archivo permitido: ${relativePath}`, "yellow");
      return;
    }

    const issues = checkFile(filePath);

    if (issues.length > 0) {
      filesWithIssues.push({
        file: relativePath,
        issues,
      });
      totalIssues += issues.length;
    }
  });

  log("════════════════════════════════════════════════════════", "cyan");

  if (totalIssues === 0) {
    logSuccess("¡Excelente! No se encontraron rutas API problemáticas.");
    logSuccess("Todas las rutas siguen la convención correcta.");
    process.exit(0);
  }

  logError(`Se encontraron ${totalIssues} problemas en ${filesWithIssues.length} archivos:`);
  console.log();

  filesWithIssues.forEach(({ file, issues }) => {
    logError(`📁 ${file}:`);
    issues.forEach(({ line, content, pattern }) => {
      log(`   Línea ${line}: ${content}`, "red");
      log(`   Patrón detectado: ${pattern}`, "yellow");
    });
    console.log();
  });

  log("🔧 Recomendaciones:", "cyan");
  log('1. Reemplaza "/api/..." por "/v1/..." o rutas sin prefijo', "white");
  log('2. Usa solo rutas como: "/v1/lotes", "/campos", "/insumos"', "white");
  log('3. El prefijo "/api" se agrega automáticamente en api.ts', "white");
  log('4. Ejemplo correcto: api.get("/v1/lotes")', "white");
  log('5. Ejemplo incorrecto: api.get("/api/v1/lotes")', "red");

  console.log();
  log("📚 Documentación:", "cyan");
  log('Ver README.md sección "Convención de Rutas API" para más detalles.', "white");

  process.exit(1);
}

const esEjecucionDirecta =
  process.argv[1] && path.resolve(process.argv[1]) === path.resolve(__filename);

if (esEjecucionDirecta) {
  main();
}

export { main, checkFile, getAllSourceFiles };
