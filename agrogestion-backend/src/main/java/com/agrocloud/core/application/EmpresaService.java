package com.agrocloud.core.application;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio Core para la gestión de empresas (multiempresa).
 */
@Service("empresaServiceCore")
@Transactional
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("campanaServiceCore")
    private CampanaService campanaService;

    @Transactional(readOnly = true)
    public List<Empresa> obtenerTodasLasEmpresas() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Empresa> obtenerEmpresas(Pageable pageable) {
        return empresaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Empresa> obtenerEmpresasConFiltros(String nombre, EstadoEmpresa estado,
                                                   Boolean activo, Pageable pageable) {
        return empresaRepository.findEmpresasConFiltros(nombre, estado, activo, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorId(Long id) {
        return empresaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorCuit(String cuit) {
        return empresaRepository.findByCuit(cuit);
    }

    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorEmail(String email) {
        return empresaRepository.findByEmailContacto(email);
    }

    public Empresa crearEmpresa(Empresa empresa, User creadoPor) {
        if (empresa.getCuit() != null && empresaRepository.findByCuit(empresa.getCuit()).isPresent()) {
            throw new RuntimeException("Ya existe una empresa con el CUIT: " + empresa.getCuit());
        }
        if (empresa.getEmailContacto() != null && empresaRepository.findByEmailContacto(empresa.getEmailContacto()).isPresent()) {
            throw new RuntimeException("Ya existe una empresa con el email: " + empresa.getEmailContacto());
        }
        if (empresa.getEstado() == null) empresa.setEstado(EstadoEmpresa.TRIAL);
        if (empresa.getFechaInicioTrial() == null) empresa.setFechaInicioTrial(LocalDate.now());
        if (empresa.getFechaFinTrial() == null) empresa.setFechaFinTrial(LocalDate.now().plusDays(30));
        if (empresa.getActivo() == null) empresa.setActivo(true);
        empresa.setCreadoPor(creadoPor);
        Empresa guardada = empresaRepository.save(empresa);
        campanaService.asegurarCampanaActivaPorDefecto(guardada.getId());
        return guardada;
    }

    public Empresa actualizarEmpresa(Long id, Empresa empresaActualizada) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        if (empresaActualizada.getCuit() != null && !empresaActualizada.getCuit().equals(empresa.getCuit()) &&
            empresaRepository.existsByCuitAndIdNot(empresaActualizada.getCuit(), id)) {
            throw new RuntimeException("Ya existe otra empresa con el CUIT: " + empresaActualizada.getCuit());
        }
        if (empresaActualizada.getEmailContacto() != null && !empresaActualizada.getEmailContacto().equals(empresa.getEmailContacto()) &&
            empresaRepository.existsByEmailContactoAndIdNot(empresaActualizada.getEmailContacto(), id)) {
            throw new RuntimeException("Ya existe otra empresa con el email: " + empresaActualizada.getEmailContacto());
        }
        if (empresaActualizada.getNombre() != null) empresa.setNombre(empresaActualizada.getNombre());
        if (empresaActualizada.getCuit() != null) empresa.setCuit(empresaActualizada.getCuit());
        if (empresaActualizada.getEmailContacto() != null) empresa.setEmailContacto(empresaActualizada.getEmailContacto());
        if (empresaActualizada.getTelefonoContacto() != null) empresa.setTelefonoContacto(empresaActualizada.getTelefonoContacto());
        if (empresaActualizada.getDireccion() != null) empresa.setDireccion(empresaActualizada.getDireccion());
        if (empresaActualizada.getEstado() != null) empresa.setEstado(empresaActualizada.getEstado());
        if (empresaActualizada.getActivo() != null) empresa.setActivo(empresaActualizada.getActivo());
        return empresaRepository.save(empresa);
    }

    public void eliminarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        empresa.setActivo(false);
        empresa.setEstado(EstadoEmpresa.INACTIVO);
        empresaRepository.save(empresa);
    }

    public Empresa activarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        empresa.setActivo(true);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        return empresaRepository.save(empresa);
    }

    public Empresa suspenderEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        empresa.setActivo(false);
        empresa.setEstado(EstadoEmpresa.SUSPENDIDO);
        return empresaRepository.save(empresa);
    }

    public UsuarioEmpresa agregarUsuarioAEmpresa(Long empresaId, Long usuarioId, RolEmpresa rol, User creadoPor) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));
        User usuario = userService.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        if (usuarioEmpresaRepository.existsByUsuarioIdAndEmpresaId(usuarioId, empresaId)) {
            throw new RuntimeException("El usuario ya pertenece a esta empresa");
        }
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa(usuario, empresa, rol);
        usuarioEmpresa.setCreadoPor(creadoPor);
        return usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    public void removerUsuarioDeEmpresa(Long empresaId, Long usuarioId) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a esta empresa"));
        usuarioEmpresaRepository.delete(usuarioEmpresa);
    }

    public UsuarioEmpresa cambiarRolUsuarioEnEmpresa(Long empresaId, Long usuarioId, RolEmpresa nuevoRol) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a esta empresa"));
        usuarioEmpresa.setRol(nuevoRol);
        return usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    @Transactional(readOnly = true)
    public Object[] obtenerEstadisticasEmpresas() {
        return empresaRepository.obtenerEstadisticasEmpresas();
    }

    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasTrialProximoVencer(int dias) {
        return empresaRepository.findEmpresasTrialProximoVencer(LocalDate.now().plusDays(dias));
    }

    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasTrialVencido() {
        return empresaRepository.findEmpresasTrialVencido(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Object[]> obtenerEmpresasConMasUsuarios(Pageable pageable) {
        return empresaRepository.findEmpresasConMasUsuarios(pageable);
    }

    @Transactional(readOnly = true)
    public List<Object[]> obtenerEmpresasConMasActividad() {
        return empresaRepository.findEmpresasConMasActividad();
    }

    @Transactional(readOnly = true)
    public boolean puedeAgregarUsuario(Long empresaId) {
        return true;
    }

    @Transactional(readOnly = true)
    public boolean puedeAgregarCampo(Long empresaId) {
        return true;
    }
}
