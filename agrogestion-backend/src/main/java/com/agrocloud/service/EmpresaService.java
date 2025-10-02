package com.agrocloud.service;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.RolEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de empresas en el sistema multiempresa.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Service
@Transactional
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Autowired
    private UserService userService;

    /**
     * Obtiene todas las empresas
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerTodasLasEmpresas() {
        return empresaRepository.findAll();
    }

    /**
     * Obtiene empresas con paginación
     */
    @Transactional(readOnly = true)
    public Page<Empresa> obtenerEmpresas(Pageable pageable) {
        return empresaRepository.findAll(pageable);
    }

    /**
     * Obtiene empresas con filtros
     */
    @Transactional(readOnly = true)
    public Page<Empresa> obtenerEmpresasConFiltros(String nombre, EstadoEmpresa estado, 
                                                   Boolean activo, Pageable pageable) {
        return empresaRepository.findEmpresasConFiltros(nombre, estado, activo, pageable);
    }

    /**
     * Obtiene una empresa por ID
     */
    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorId(Long id) {
        return empresaRepository.findById(id);
    }

    /**
     * Obtiene una empresa por CUIT
     */
    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorCuit(String cuit) {
        return empresaRepository.findByCuit(cuit);
    }

    /**
     * Obtiene una empresa por email de contacto
     */
    @Transactional(readOnly = true)
    public Optional<Empresa> obtenerEmpresaPorEmail(String email) {
        return empresaRepository.findByEmailContacto(email);
    }

    /**
     * Crea una nueva empresa
     */
    public Empresa crearEmpresa(Empresa empresa, User creadoPor) {
        // Validar que no exista otra empresa con el mismo CUIT
        if (empresa.getCuit() != null && empresaRepository.findByCuit(empresa.getCuit()).isPresent()) {
            throw new RuntimeException("Ya existe una empresa con el CUIT: " + empresa.getCuit());
        }

        // Validar que no exista otra empresa con el mismo email
        if (empresa.getEmailContacto() != null && empresaRepository.findByEmailContacto(empresa.getEmailContacto()).isPresent()) {
            throw new RuntimeException("Ya existe una empresa con el email: " + empresa.getEmailContacto());
        }

        // Configurar datos por defecto
        if (empresa.getEstado() == null) {
            empresa.setEstado(EstadoEmpresa.TRIAL);
        }
        if (empresa.getFechaInicioTrial() == null) {
            empresa.setFechaInicioTrial(LocalDate.now());
        }
        if (empresa.getFechaFinTrial() == null) {
            empresa.setFechaFinTrial(LocalDate.now().plusDays(30));
        }
        if (empresa.getActivo() == null) {
            empresa.setActivo(true);
        }

        empresa.setCreadoPor(creadoPor);
        return empresaRepository.save(empresa);
    }

    /**
     * Actualiza una empresa existente
     */
    public Empresa actualizarEmpresa(Long id, Empresa empresaActualizada) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        // Validar CUIT único (excluyendo la empresa actual)
        if (empresaActualizada.getCuit() != null && 
            !empresaActualizada.getCuit().equals(empresa.getCuit()) &&
            empresaRepository.existsByCuitAndIdNot(empresaActualizada.getCuit(), id)) {
            throw new RuntimeException("Ya existe otra empresa con el CUIT: " + empresaActualizada.getCuit());
        }

        // Validar email único (excluyendo la empresa actual)
        if (empresaActualizada.getEmailContacto() != null && 
            !empresaActualizada.getEmailContacto().equals(empresa.getEmailContacto()) &&
            empresaRepository.existsByEmailContactoAndIdNot(empresaActualizada.getEmailContacto(), id)) {
            throw new RuntimeException("Ya existe otra empresa con el email: " + empresaActualizada.getEmailContacto());
        }

        // Actualizar campos
        if (empresaActualizada.getNombre() != null) {
            empresa.setNombre(empresaActualizada.getNombre());
        }
        if (empresaActualizada.getCuit() != null) {
            empresa.setCuit(empresaActualizada.getCuit());
        }
        if (empresaActualizada.getEmailContacto() != null) {
            empresa.setEmailContacto(empresaActualizada.getEmailContacto());
        }
        if (empresaActualizada.getTelefonoContacto() != null) {
            empresa.setTelefonoContacto(empresaActualizada.getTelefonoContacto());
        }
        if (empresaActualizada.getDireccion() != null) {
            empresa.setDireccion(empresaActualizada.getDireccion());
        }
        if (empresaActualizada.getEstado() != null) {
            empresa.setEstado(empresaActualizada.getEstado());
        }
        if (empresaActualizada.getActivo() != null) {
            empresa.setActivo(empresaActualizada.getActivo());
        }

        return empresaRepository.save(empresa);
    }

    /**
     * Elimina una empresa (soft delete)
     */
    public void eliminarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setActivo(false);
        empresa.setEstado(EstadoEmpresa.INACTIVO);
        empresaRepository.save(empresa);
    }

    /**
     * Activa una empresa
     */
    public Empresa activarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setActivo(true);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        return empresaRepository.save(empresa);
    }

    /**
     * Suspende una empresa
     */
    public Empresa suspenderEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setActivo(false);
        empresa.setEstado(EstadoEmpresa.SUSPENDIDO);
        return empresaRepository.save(empresa);
    }


    /**
     * Agrega un usuario a una empresa
     */
    public UsuarioEmpresa agregarUsuarioAEmpresa(Long empresaId, Long usuarioId, RolEmpresa rol, User creadoPor) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));

        User usuario = userService.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Verificar que el usuario no esté ya en la empresa
        if (usuarioEmpresaRepository.existsByUsuarioIdAndEmpresaId(usuarioId, empresaId)) {
            throw new RuntimeException("El usuario ya pertenece a esta empresa");
        }

        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa(usuario, empresa, rol);
        usuarioEmpresa.setCreadoPor(creadoPor);
        return usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    /**
     * Remueve un usuario de una empresa
     */
    public void removerUsuarioDeEmpresa(Long empresaId, Long usuarioId) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a esta empresa"));

        usuarioEmpresaRepository.delete(usuarioEmpresa);
    }

    /**
     * Cambia el rol de un usuario en una empresa
     */
    public UsuarioEmpresa cambiarRolUsuarioEnEmpresa(Long empresaId, Long usuarioId, RolEmpresa nuevoRol) {
        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("El usuario no pertenece a esta empresa"));

        usuarioEmpresa.setRol(nuevoRol);
        return usuarioEmpresaRepository.save(usuarioEmpresa);
    }

    /**
     * Obtiene estadísticas de empresas
     */
    @Transactional(readOnly = true)
    public Object[] obtenerEstadisticasEmpresas() {
        return empresaRepository.obtenerEstadisticasEmpresas();
    }

    /**
     * Obtiene empresas con trial próximo a vencer
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasTrialProximoVencer(int dias) {
        LocalDate fechaLimite = LocalDate.now().plusDays(dias);
        return empresaRepository.findEmpresasTrialProximoVencer(fechaLimite);
    }

    /**
     * Obtiene empresas con trial vencido
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasTrialVencido() {
        return empresaRepository.findEmpresasTrialVencido(LocalDate.now());
    }

    /**
     * Obtiene empresas con más usuarios
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEmpresasConMasUsuarios(Pageable pageable) {
        return empresaRepository.findEmpresasConMasUsuarios(pageable);
    }

    /**
     * Obtiene empresas con más actividad
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEmpresasConMasActividad() {
        return empresaRepository.findEmpresasConMasActividad();
    }

    /**
     * Verifica si una empresa puede agregar más usuarios
     */
    @Transactional(readOnly = true)
    public boolean puedeAgregarUsuario(Long empresaId) {
        // Por ahora, todas las empresas pueden agregar usuarios sin restricciones
        return true;
    }

    /**
     * Verifica si una empresa puede agregar más campos
     */
    @Transactional(readOnly = true)
    public boolean puedeAgregarCampo(Long empresaId) {
        // Por ahora, todas las empresas pueden agregar campos sin restricciones
        return true;
    }
}
