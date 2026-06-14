package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.Padrillo;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.PadrilloRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PadrilloService {

    @Autowired
    private PadrilloRepository padrilloRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private ServicioRepository servicioRepository;

    public List<Padrillo> listarPadrillos(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return padrilloRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    public Optional<Padrillo> obtenerPadrillo(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }
        Optional<Padrillo> padrillo = padrilloRepository.findByIdAndActivoTrue(id);
        if (padrillo.isPresent() && padrillo.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return padrillo;
        }
        return Optional.empty();
    }

    @Transactional
    public Padrillo crearPadrillo(Padrillo padrilloData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Validar que no exista otro padrillo con la misma identificación
        Optional<Padrillo> existente = padrilloRepository.findByIdentificacionAndEmpresaAndActivoTrue(
            padrilloData.getIdentificacion(), empresaActiva.get());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un padrillo con esta identificación");
        }

        padrilloData.setEmpresa(empresaActiva.get());
        padrilloData.setUsuario(user);
        padrilloData.setActivo(true);
        return padrilloRepository.save(padrilloData);
    }

    @Transactional
    public Padrillo actualizarPadrillo(Long id, Padrillo padrilloData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        Optional<Padrillo> existente = obtenerPadrillo(id, user);
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("Padrillo no encontrado");
        }

        Padrillo padrillo = existente.get();
        
        // Actualizar identificación solo si cambió, validando que no exista otra con esa identificación
        if (padrilloData.getIdentificacion() != null && 
            !padrilloData.getIdentificacion().equals(padrillo.getIdentificacion())) {
            Optional<Padrillo> otroConMismaIdentificacion = padrilloRepository
                .findByIdentificacionAndEmpresaAndActivoTrue(padrilloData.getIdentificacion(), empresaActiva.get());
            if (otroConMismaIdentificacion.isPresent() && !otroConMismaIdentificacion.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro padrillo con esta identificación");
            }
            padrillo.setIdentificacion(padrilloData.getIdentificacion());
        }
        
        padrillo.setFechaNacimiento(padrilloData.getFechaNacimiento());
        padrillo.setOrigen(padrilloData.getOrigen());
        padrillo.setFechaIngresoGranja(padrilloData.getFechaIngresoGranja());
        padrillo.setFechaBaja(padrilloData.getFechaBaja());
        padrillo.setMotivoBaja(padrilloData.getMotivoBaja());
        padrillo.setObservaciones(padrilloData.getObservaciones());

        return padrilloRepository.save(padrillo);
    }

    @Transactional
    public void eliminarPadrillo(Long id, User user) {
        Optional<Padrillo> padrilloOpt = obtenerPadrillo(id, user);
        if (padrilloOpt.isEmpty()) {
            throw new IllegalArgumentException("Padrillo no encontrado");
        }

        Padrillo padrillo = padrilloOpt.get();

        // VALIDACIÓN DE INTEGRIDAD: No se puede eliminar un padrillo con servicios activos
        List<Servicio> serviciosActivos = servicioRepository.findByEmpresaAndActivoTrue(padrillo.getEmpresa());
        boolean tieneServiciosActivos = serviciosActivos.stream()
            .anyMatch(s -> s.getMachoId() != null && s.getMachoId().equals(padrillo.getId()) &&
                          s.getEstadoServicio() == Servicio.EstadoServicio.PENDIENTE_CONTROL);
        
        if (tieneServiciosActivos) {
            throw new IllegalArgumentException(
                "No se puede eliminar un padrillo con servicios activos pendientes de control.");
        }

        // Baja lógica: marcar como inactivo en lugar de eliminar físicamente
        padrillo.setActivo(false);
        // Establecer fecha de baja si no está establecida
        if (padrillo.getFechaBaja() == null) {
            padrillo.setFechaBaja(LocalDate.now());
        }
        padrilloRepository.save(padrillo);
    }
}



