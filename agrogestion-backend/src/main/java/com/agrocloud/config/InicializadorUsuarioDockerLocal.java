package com.agrocloud.config;

import com.agrocloud.core.application.CompanyModuleService;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Module;
import com.agrocloud.core.domain.Role;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UserRole;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.ModuleRepository;
import com.agrocloud.core.infrastructure.RoleRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.core.infrastructure.UsuarioEmpresaRepository;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.model.enums.ModuloSistema;
import com.agrocloud.model.enums.RolEmpresa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Crea un superadmin y una empresa solo si APP_CREAR_USUARIO_LOCAL=true (compose local).
 * Habilita todos los módulos de plataforma: Flyway corre antes y no ve esta empresa.
 * No activar en docker-compose.prod.yml.
 */
@Component
@Order(200)
@ConditionalOnProperty(name = "app.crear-usuario-local", havingValue = "true")
public class InicializadorUsuarioDockerLocal implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorUsuarioDockerLocal.class);

    private static final List<String> CODIGOS_MODULO_LOCAL = List.of(
            ModuloSistema.CULTIVOS.getCodigo(),
            ModuloSistema.PORCINOS.getCodigo(),
            ModuloSistema.AVICOLA_CRIANZA.getCodigo(),
            ModuloSistema.AVICOLA_HUEVOS.getCodigo(),
            ModuloSistema.FEEDLOT.getCodigo(),
            ModuloSistema.LECHERIA.getCodigo()
    );

    private final UserRepository repositorioUsuarios;
    private final RoleRepository repositorioRoles;
    private final EmpresaRepository repositorioEmpresas;
    private final UsuarioEmpresaRepository repositorioUsuarioEmpresa;
    private final ModuleRepository repositorioModulos;
    private final CompanyModuleService servicioModulosEmpresa;
    private final PasswordEncoder codificador;

    @Value("${app.usuario-local-email:admin@localhost}")
    private String email;

    @Value("${app.usuario-local-password:}")
    private String clavePlana;

    public InicializadorUsuarioDockerLocal(
            @Qualifier("userRepositoryCore") UserRepository repositorioUsuarios,
            @Qualifier("roleRepositoryCore") RoleRepository repositorioRoles,
            @Qualifier("empresaRepositoryCore") EmpresaRepository repositorioEmpresas,
            @Qualifier("usuarioEmpresaRepositoryCore") UsuarioEmpresaRepository repositorioUsuarioEmpresa,
            ModuleRepository repositorioModulos,
            CompanyModuleService servicioModulosEmpresa,
            PasswordEncoder codificador) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioRoles = repositorioRoles;
        this.repositorioEmpresas = repositorioEmpresas;
        this.repositorioUsuarioEmpresa = repositorioUsuarioEmpresa;
        this.repositorioModulos = repositorioModulos;
        this.servicioModulosEmpresa = servicioModulosEmpresa;
        this.codificador = codificador;
    }

    @Override
    public void run(String... args) {
        try {
            crearSiFalta();
        } catch (Exception e) {
            log.error("No se pudo crear el usuario local Docker: {}", e.getMessage());
        }
    }

    private void crearSiFalta() {
        if (clavePlana == null || clavePlana.isBlank()) {
            log.warn("APP_CREAR_USUARIO_LOCAL=true pero APP_USUARIO_LOCAL_PASSWORD está vacío; no se crea usuario");
            return;
        }
        var usuarioExistente = repositorioUsuarios.findByEmail(email);
        if (usuarioExistente.isPresent()) {
            log.info("Usuario local Docker ya existe: {}", email);
            for (UsuarioEmpresa vinculo : repositorioUsuarioEmpresa.findEmpresasActivasByUsuarioId(usuarioExistente.get().getId())) {
                habilitarModulosPlataforma(vinculo.getEmpresa());
            }
            return;
        }
        Role superadmin = repositorioRoles.findByNombre("SUPERADMIN")
                .orElseThrow(() -> new IllegalStateException("Falta rol SUPERADMIN"));

        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa local Docker");
        empresa.setCuit("30-00000000-0");
        empresa.setEmailContacto(email);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        empresa.setActivo(true);
        empresa = repositorioEmpresas.save(empresa);

        User usuario = new User();
        usuario.setUsername("adminlocal");
        usuario.setEmail(email);
        usuario.setPassword(codificador.encode(clavePlana));
        usuario.setFirstName("Admin");
        usuario.setLastName("Local");
        usuario.setActivo(true);
        usuario.setEulaAceptado(true);
        usuario.getUserRoles().add(new UserRole(usuario, superadmin));
        usuario = repositorioUsuarios.save(usuario);

        UsuarioEmpresa vinculo = new UsuarioEmpresa(usuario, empresa, RolEmpresa.ADMINISTRADOR);
        vinculo.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        vinculo.setFechaInicio(LocalDate.now());
        vinculo.setCreadoPor(usuario);
        repositorioUsuarioEmpresa.save(vinculo);

        habilitarModulosPlataforma(empresa);

        log.info("Usuario local Docker creado: {} (empresa {})", email, empresa.getNombre());
    }

    private void habilitarModulosPlataforma(Empresa empresa) {
        if (empresa == null || empresa.getId() == null) {
            return;
        }
        for (String codigo : CODIGOS_MODULO_LOCAL) {
            try {
                Module modulo = repositorioModulos.findByCode(codigo).orElse(null);
                if (modulo == null) {
                    log.warn("No está en catálogo el módulo {}; se omite", codigo);
                    continue;
                }
                servicioModulosEmpresa.enableModuleForCompany(empresa.getId(), modulo.getId());
            } catch (Exception e) {
                log.warn("No se pudo habilitar módulo {} para empresa {}: {}", codigo, empresa.getId(), e.getMessage());
            }
        }
        log.info("Módulos de plataforma habilitados para empresa {}", empresa.getId());
    }
}
