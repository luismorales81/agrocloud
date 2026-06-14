/**
 * Genera un PDF a partir de Markdown (mismo formato que el brief).
 * Uso (desde esta carpeta):
 *   npm run pdf:brief
 *   npm run pdf:propuesta
 *   node generar-brief-interesados-pdf.mjs [archivo.md] [salida.pdf]
 * Requiere: npm install pdfkit
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { once } from "events";
import PDFDocument from "pdfkit";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const COLORES = {
  titulo: "#14532d",
  subtitulo: "#166534",
  texto: "#1c1917",
  gris: "#57534e",
  banda: "#22c55e",
};

function fuentesWindows() {
  const windir = process.env.WINDIR || process.env.SystemRoot || "C:\\Windows";
  const dir = path.join(windir, "Fonts");
  const regular = ["calibri.ttf", "arial.ttf", "segoeui.ttf"].map((f) => path.join(dir, f)).find((p) => fs.existsSync(p));
  const bold = ["calibrib.ttf", "arialbd.ttf", "segoeuib.ttf"].map((f) => path.join(dir, f)).find((p) => fs.existsSync(p));
  return { regular, bold: bold || regular };
}

function limpiarMd(s) {
  return s.replace(/\*\*(.+?)\*\*/g, "$1").replace(/`([^`]+)`/g, "$1").replace(/\*(.+?)\*/g, "$1");
}

function esSeparadorTabla(linea) {
  const t = linea.trim();
  if (!t.startsWith("|")) return false;
  const celdas = t.split("|").map((c) => c.trim()).filter(Boolean);
  return celdas.every((c) => /^[\-:\s|]+$/.test(c));
}

function filaTabla(linea) {
  return linea
    .trim()
    .split("|")
    .map((c) => limpiarMd(c.trim()))
    .filter((c) => c.length > 0);
}

async function main() {
  const entrada = process.argv[2] || "BRIEF-PARA-INTERESADOS.md";
  const salida = process.argv[3] || entrada.replace(/\.md$/i, ".pdf") || "salida.pdf";
  const mdPath = path.isAbsolute(entrada) ? entrada : path.join(__dirname, entrada);
  const pdfPath = path.isAbsolute(salida) ? salida : path.join(__dirname, salida);

  if (!fs.existsSync(mdPath)) {
    console.error("No se encuentra el markdown:", mdPath);
    process.exit(1);
  }
  if (fs.existsSync(pdfPath)) {
    fs.unlinkSync(pdfPath);
  }

  const lineas = fs.readFileSync(mdPath, "utf8").split(/\r?\n/);

  const { regular: ttfReg, bold: ttfBold } = fuentesWindows();

  const doc = new PDFDocument({
    size: "A4",
    margin: 52,
    info: {
      Title: path.basename(mdPath, ".md").replace(/-/g, " "),
      Author: "AgroGestion",
      Subject: "Documento comercial",
    },
  });

  let fuente = "Helvetica";
  let fuenteBold = "Helvetica-Bold";
  if (ttfReg) {
    doc.registerFont("Txt", ttfReg);
    fuente = "Txt";
    doc.registerFont("TxtBold", ttfBold || ttfReg);
    fuenteBold = "TxtBold";
  }

  const stream = fs.createWriteStream(pdfPath);
  doc.pipe(stream);

  const margen = 52;
  const anchoUtil = doc.page.width - margen * 2;

  function nuevaPaginaSi(alturaNecesaria) {
    if (doc.y + alturaNecesaria > doc.page.height - 70) {
      doc.addPage();
    }
  }

  let i = 0;
  while (i < lineas.length) {
    const linea = lineas[i];
    const trim = linea.trim();

    if (trim === "" || trim === "---") {
      i++;
      doc.moveDown(0.25);
      continue;
    }

    if (trim.startsWith("# ")) {
      nuevaPaginaSi(56);
      const t = limpiarMd(trim.slice(2));
      const yBarra = doc.y;
      doc.fillColor(COLORES.titulo).rect(margen, yBarra, anchoUtil, 5).fill();
      doc.fillColor(COLORES.titulo);
      doc.y = yBarra + 12;
      doc.font(fuenteBold).fontSize(17).text(t, { width: anchoUtil, align: "left" });
      doc.moveDown(0.45);
      doc.strokeColor(COLORES.banda).lineWidth(2).moveTo(margen, doc.y).lineTo(margen + anchoUtil, doc.y).stroke();
      doc.moveDown(0.55);
      i++;
      continue;
    }

    if (trim.startsWith("## ")) {
      nuevaPaginaSi(44);
      const t = limpiarMd(trim.slice(3));
      doc.font(fuenteBold).fontSize(12).fillColor(COLORES.subtitulo).text(t, { width: anchoUtil });
      doc.moveDown(0.45);
      i++;
      continue;
    }

    if (trim.startsWith("### ")) {
      nuevaPaginaSi(36);
      const t = limpiarMd(trim.slice(4));
      doc.font(fuenteBold).fontSize(10.5).fillColor(COLORES.subtitulo).text(t, { width: anchoUtil });
      doc.moveDown(0.35);
      i++;
      continue;
    }

    if (trim.startsWith("|")) {
      if (esSeparadorTabla(trim)) {
        i++;
        continue;
      }
      const filas = [];
      while (i < lineas.length && lineas[i].trim().startsWith("|")) {
        const L = lineas[i].trim();
        if (esSeparadorTabla(L)) {
          i++;
          continue;
        }
        filas.push(filaTabla(L));
        i++;
      }
      nuevaPaginaSi(Math.min(filas.length * 18 + 20, 400));
      filas.forEach((celdas, fi) => {
        nuevaPaginaSi(22);
        const esCab = fi === 0;
        doc.font(esCab ? fuenteBold : fuente).fontSize(9).fillColor(esCab ? COLORES.titulo : COLORES.texto);
        const textoFila = celdas.join("          ·          ");
        doc.text(textoFila, { width: anchoUtil, align: "left", lineGap: 1 });
        doc.moveDown(0.15);
      });
      doc.moveDown(0.35);
      continue;
    }

    if (trim.startsWith("- ") || /^\d+\.\s/.test(trim)) {
      const num = trim.match(/^(\d+)\.\s/);
      const texto = limpiarMd(trim.replace(/^-\s+/, "").replace(/^\d+\.\s+/, ""));
      const pref = trim.startsWith("- ") ? "•  " : `${num ? num[1] : ""}. `;
      nuevaPaginaSi(24);
      doc.font(fuente).fontSize(10).fillColor(COLORES.texto).text(pref + texto, {
        width: anchoUtil - 12,
        indent: 6,
        align: "left",
        lineGap: 1,
      });
      doc.moveDown(0.2);
      i++;
      continue;
    }

    nuevaPaginaSi(36);
    doc.font(fuente).fontSize(10).fillColor(COLORES.texto).text(limpiarMd(trim), { width: anchoUtil, align: "justify", lineGap: 2 });
    doc.moveDown(0.35);
    i++;
  }

  nuevaPaginaSi(30);
  doc.moveDown(0.5);
  doc.font(fuente).fontSize(8).fillColor(COLORES.gris).text(
    `Generado desde ${path.basename(mdPath)} · No sustituye asesoramiento legal ni ofertas comerciales firmadas.`,
    margen,
    doc.page.height - 45,
    { width: anchoUtil, align: "center" }
  );

  doc.end();
  await once(stream, "finish");
  console.log("Origen:", mdPath);
  console.log("Destino:", pdfPath);
  console.log("Listo:", pdfPath);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
