package com.agrocloud.trazabilidad.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.trazabilidad.domain.TrazabilidadCertificacion;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.HechosExpedienteTrazabilidad;
import com.agrocloud.trazabilidad.dto.HechosTrazabilidadDocumento;
import com.agrocloud.trazabilidad.dto.IncidenciaValidacionTrazabilidad;
import com.agrocloud.trazabilidad.dto.TrazabilidadReporteListado;
import com.agrocloud.trazabilidad.infrastructure.TrazabilidadReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construcción y persistencia del reporte: snapshot JSON, hash, PDF, hash PDF.
 */
@Service
@Transactional
public class ReporteTrazabilidadService {

    private final TrazabilidadReporteRepository reporteRepository;
    private final GeneradorPdfTrazabilidadComercial generadorPdfComercial;
    private final GeneradorPdfExpedienteTrazabilidad generadorPdfExpediente;
    private final ObjectMapper objectMapperMapeo;

    public ReporteTrazabilidadService(
            TrazabilidadReporteRepository reporteRepository,
            GeneradorPdfTrazabilidadComercial generadorPdfComercial,
            GeneradorPdfExpedienteTrazabilidad generadorPdfExpediente,
            ObjectMapper objectMapperBase) {
        this.reporteRepository = reporteRepository;
        this.generadorPdfComercial = generadorPdfComercial;
        this.generadorPdfExpediente = generadorPdfExpediente;
        this.objectMapperMapeo = objectMapperBase.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public TrazabilidadReporte generarYPersistirSiValido(
            TrazabilidadReporte.TipoEntidadAlcance tipo,
            long entidadId,
            TrazabilidadCertificacion cert,
            HechosTrazabilidadDocumento hechos,
            TrazabilidadReporte.ResultadoReporte resultado,
            List<IncidenciaValidacionTrazabilidad> incidencias,
            User usuario,
            Empresa empresa) {
        if (resultado != TrazabilidadReporte.ResultadoReporte.VALIDO) {
            throw new IllegalStateException("Solo se persiste con resultado VALIDO");
        }
        try {
            Map<String, Object> snap = new HashMap<>();
            snap.put("versionMotor", TrazabilidadReporte.VERSION_MOTOR);
            snap.put("certificacion", cert.getCodigo());
            snap.put("resultado", resultado.name());
            snap.put("entidadTipo", tipo.name());
            snap.put("entidadId", entidadId);
            snap.put("hechos", hechos);
            snap.put("incidencias", incidencias);
            String json = objectMapperMapeo.writerWithDefaultPrettyPrinter().writeValueAsString(snap);
            String hashJson = sha256HexBytes(json.getBytes(StandardCharsets.UTF_8));

            TrazabilidadReporte r = new TrazabilidadReporte();
            r.setEmpresa(empresa);
            r.setUsuario(usuario);
            r.setEntidadTipo(tipo);
            r.setEntidadId(entidadId);
            r.setCertificacionCodigo(cert.getCodigo());
            r.setResultado(resultado);
            r.setVersionMotor(TrazabilidadReporte.VERSION_MOTOR);
            r.setSnapshotJson(json);
            r.setHashSnapshot(hashJson);
            r = reporteRepository.saveAndFlush(r);

            byte[] pdf = generadorPdfComercial.generar(r, hechos, empresa, usuario);
            r.setContenidoPdf(pdf);
            r.setHashPdf(sha256HexBytes(pdf));
            return reporteRepository.save(r);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar o guardar el reporte de trazabilidad: " + e.getMessage(), e);
        }
    }

    public TrazabilidadReporte generarExpedienteYPersistir(
            TrazabilidadReporte.TipoEntidadAlcance tipo,
            long entidadId,
            HechosExpedienteTrazabilidad hechos,
            User usuario,
            Empresa empresa) {
        try {
            Map<String, Object> snap = new HashMap<>();
            snap.put("versionMotor", TrazabilidadReporte.VERSION_MOTOR);
            snap.put("certificacion", TrazabilidadExpedienteConstruccionService.CODIGO_EXPEDIENTE);
            snap.put("resultado", TrazabilidadReporte.ResultadoReporte.VALIDO.name());
            snap.put("entidadTipo", tipo.name());
            snap.put("entidadId", entidadId);
            snap.put("expediente", hechos);
            String json = objectMapperMapeo.writerWithDefaultPrettyPrinter().writeValueAsString(snap);
            String hashJson = sha256HexBytes(json.getBytes(StandardCharsets.UTF_8));

            TrazabilidadReporte r = new TrazabilidadReporte();
            r.setEmpresa(empresa);
            r.setUsuario(usuario);
            r.setEntidadTipo(tipo);
            r.setEntidadId(entidadId);
            r.setCertificacionCodigo(TrazabilidadExpedienteConstruccionService.CODIGO_EXPEDIENTE);
            r.setResultado(TrazabilidadReporte.ResultadoReporte.VALIDO);
            r.setVersionMotor(TrazabilidadReporte.VERSION_MOTOR);
            r.setSnapshotJson(json);
            r.setHashSnapshot(hashJson);
            r = reporteRepository.saveAndFlush(r);

            byte[] pdf = generadorPdfExpediente.generar(r, hechos, empresa, usuario);
            r.setContenidoPdf(pdf);
            r.setHashPdf(sha256HexBytes(pdf));
            return reporteRepository.save(r);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar expediente de trazabilidad: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<TrazabilidadReporteListado> listarResumenesPorEmpresa(Empresa empresa) {
        return reporteRepository.listarResumenesPorEmpresaId(empresa.getId());
    }

    @Transactional(readOnly = true)
    public byte[] obtenerPdf(long id, Empresa empresa) {
        TrazabilidadReporte r = reporteRepository.findByIdAndEmpresa(id, empresa)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));
        if (r.getContenidoPdf() == null || r.getContenidoPdf().length == 0) {
            throw new IllegalStateException("El PDF aun no esta disponible");
        }
        return r.getContenidoPdf();
    }

    private static String sha256HexBytes(byte[] b) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(b);
            StringBuilder sb = new StringBuilder();
            for (byte v : h) {
                sb.append(String.format("%02x", v));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
