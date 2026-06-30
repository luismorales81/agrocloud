package com.agrocloud.core.application;

import com.agrocloud.core.domain.Campana;
import com.agrocloud.core.infrastructure.CampanaRepository;
import com.agrocloud.dto.CampanaDTO;
import com.agrocloud.dto.CrearCampanaRequest;
import com.agrocloud.exception.BadRequestException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.model.enums.EstadoCampana;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service("campanaServiceCore")
@Transactional
public class CampanaService {

    @Autowired
    @Qualifier("campanaRepositoryCore")
    private CampanaRepository campanaRepository;

    @Transactional(readOnly = true)
    public List<CampanaDTO> listarPorEmpresa(Long empresaId) {
        return campanaRepository.findByEmpresaIdOrderByFechaInicioDesc(empresaId).stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampanaDTO obtenerActiva(Long empresaId) {
        return campanaRepository.findByEmpresaIdAndEstado(empresaId, EstadoCampana.ACTIVA)
                .map(this::aDto)
                .orElseGet(() -> campanaRepository.findByEmpresaIdAndEsDefaultTrue(empresaId)
                        .map(this::aDto)
                        .orElseGet(() -> {
                            Campana creada = asegurarCampanaActivaPorDefecto(empresaId);
                            return aDto(creada);
                        }));
    }

    @Transactional(readOnly = true)
    public Campana obtenerEntidad(Long campanaId, Long empresaId) {
        Campana campana = campanaRepository.findById(campanaId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaña no encontrada"));
        if (!campana.getEmpresaId().equals(empresaId)) {
            throw new BadRequestException("La campaña no pertenece a la empresa");
        }
        return campana;
    }

    public CampanaDTO crear(Long empresaId, CrearCampanaRequest request) {
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BadRequestException("La fecha fin debe ser posterior a la fecha inicio");
        }
        if (campanaRepository.existsByEmpresaIdAndCodigo(empresaId, request.getCodigo())) {
            throw new BadRequestException("Ya existe una campaña con ese código");
        }
        Campana campana = new Campana();
        campana.setEmpresaId(empresaId);
        campana.setCodigo(request.getCodigo());
        campana.setNombre(request.getNombre());
        campana.setFechaInicio(request.getFechaInicio());
        campana.setFechaFin(request.getFechaFin());
        campana.setEstado(EstadoCampana.BORRADOR);
        campana.setEsDefault(false);
        return aDto(campanaRepository.save(campana));
    }

    public CampanaDTO activar(Long empresaId, Long campanaId) {
        Campana campana = obtenerEntidad(campanaId, empresaId);
        campanaRepository.findByEmpresaIdAndEstado(empresaId, EstadoCampana.ACTIVA)
                .ifPresent(activa -> {
                    activa.setEstado(EstadoCampana.CERRADA);
                    activa.setEsDefault(false);
                    campanaRepository.save(activa);
                });
        campana.setEstado(EstadoCampana.ACTIVA);
        campana.setEsDefault(true);
        return aDto(campanaRepository.save(campana));
    }

    public CampanaDTO cerrar(Long empresaId, Long campanaId) {
        Campana campana = obtenerEntidad(campanaId, empresaId);
        campana.setEstado(EstadoCampana.CERRADA);
        campana.setEsDefault(false);
        return aDto(campanaRepository.save(campana));
    }

    /**
     * Crea campaña activa por defecto para una empresa nueva o migración.
     * REQUIRES_NEW: puede ejecutarse desde transacciones de solo lectura (p. ej. paneles).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Campana asegurarCampanaActivaPorDefecto(Long empresaId) {
        return campanaRepository.findByEmpresaIdAndEstado(empresaId, EstadoCampana.ACTIVA)
                .orElseGet(() -> {
                    LocalDate hoy = LocalDate.now();
                    int anioInicio = hoy.getMonthValue() >= 10 ? hoy.getYear() : hoy.getYear() - 1;
                    LocalDate inicio = LocalDate.of(anioInicio, 10, 1);
                    LocalDate fin = LocalDate.of(anioInicio + 1, 9, 30);
                    String codigo = anioInicio + "-" + String.format("%02d", (anioInicio + 1) % 100);
                    if (campanaRepository.existsByEmpresaIdAndCodigo(empresaId, codigo)) {
                        return campanaRepository.findByEmpresaIdAndCodigo(empresaId, codigo)
                                .orElseGet(() -> crearCampanaInterna(empresaId, codigo, inicio, fin));
                    }
                    return crearCampanaInterna(empresaId, codigo, inicio, fin);
                });
    }

    private Campana crearCampanaInterna(Long empresaId, String codigo, LocalDate inicio, LocalDate fin) {
        Campana campana = new Campana();
        campana.setEmpresaId(empresaId);
        campana.setCodigo(codigo);
        campana.setNombre("Campaña " + codigo.replace("-", "/"));
        campana.setFechaInicio(inicio);
        campana.setFechaFin(fin);
        campana.setEstado(EstadoCampana.ACTIVA);
        campana.setEsDefault(true);
        return campanaRepository.save(campana);
    }

    public void validarCampanaEditable(Campana campana) {
        if (campana != null && campana.estaCerrada()) {
            throw new BadRequestException("La campaña está cerrada y no admite modificaciones");
        }
    }

    private CampanaDTO aDto(Campana campana) {
        CampanaDTO dto = new CampanaDTO();
        dto.setId(campana.getId());
        dto.setEmpresaId(campana.getEmpresaId());
        dto.setCodigo(campana.getCodigo());
        dto.setNombre(campana.getNombre());
        dto.setFechaInicio(campana.getFechaInicio());
        dto.setFechaFin(campana.getFechaFin());
        dto.setEstado(campana.getEstado());
        dto.setEsDefault(campana.getEsDefault());
        return dto;
    }
}
