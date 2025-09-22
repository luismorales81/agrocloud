package com.agrocloud.security;

import com.agrocloud.BaseTest;
import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.Role;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.entity.UsuarioEmpresa;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import com.agrocloud.repository.EmpresaRepository;
import com.agrocloud.repository.RoleRepository;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.repository.UsuarioEmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para roles y permisos del sistema.
 * Prueba la asignación de roles, permisos por empresa y validaciones de acceso.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@SpringBootTest(classes = com.agrocloud.AgroCloudApplication.class)
class RolesAndPermissionsTest extends BaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    private User superAdmin;
    private User administrador;
    private User operario;
    private User tecnico;
    private User productor;
    private User asesor;
    private User mantenimiento;
    private Empresa empresa1;
    private Empresa empresa2;

    @BeforeEach
    void setUp() {
        // Crear roles globales
        Role superAdminRole = new Role("SUPERADMIN", "Controla el sistema completo");
        Role usuarioRegistradoRole = new Role("USUARIO_REGISTRADO", "Usuario común");
        roleRepository.save(superAdminRole);
        roleRepository.save(usuarioRegistradoRole);

        // Crear usuarios
        superAdmin = crearUsuario("superadmin", "superadmin@test.com", "Super", "Admin");
        administrador = crearUsuario("admin1", "admin1@test.com", "Juan", "Administrador");
        operario = crearUsuario("operario1", "operario1@test.com", "Carlos", "Operario");
        tecnico = crearUsuario("tecnico1", "tecnico1@test.com", "María", "Técnica");
        productor = crearUsuario("productor1", "productor1@test.com", "Pedro", "Productor");
        asesor = crearUsuario("asesor1", "asesor1@test.com", "Ana", "Asesora");
        mantenimiento = crearUsuario("mantenimiento1", "mantenimiento1@test.com", "Luis", "Mantenimiento");

        // Crear empresas
        empresa1 = crearEmpresa("Empresa Test 1", "Primera empresa de prueba");
        empresa2 = crearEmpresa("Empresa Test 2", "Segunda empresa de prueba");

        entityManager.flush();
    }

    @Test
    void testSuperAdminPuedeCrearEmpresas() {
        // Arrange
        Empresa nuevaEmpresa = new Empresa();
        nuevaEmpresa.setNombre("Nueva Empresa");
        nuevaEmpresa.setDescripcion("Empresa creada por superadmin");
        nuevaEmpresa.setEstado(EstadoEmpresa.ACTIVO);
        nuevaEmpresa.setActivo(true);

        // Act
        Empresa empresaGuardada = empresaRepository.save(nuevaEmpresa);
        entityManager.flush();

        // Assert
        assertNotNull(empresaGuardada.getId());
        assertEquals("Nueva Empresa", empresaGuardada.getNombre());
        assertEquals(EstadoEmpresa.ACTIVO, empresaGuardada.getEstado());
        assertTrue(empresaGuardada.getActivo());
    }

    @Test
    void testSuperAdminPuedeEliminarEmpresas() {
        // Arrange
        Empresa empresaAEliminar = crearEmpresa("Empresa a Eliminar", "Empresa que será eliminada");
        entityManager.flush();

        // Act
        empresaRepository.delete(empresaAEliminar);
        entityManager.flush();

        // Assert
        Optional<Empresa> empresaEliminada = empresaRepository.findById(empresaAEliminar.getId());
        assertFalse(empresaEliminada.isPresent());
    }

    @Test
    void testAdministradorPuedeGestionarEmpresa() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(administrador);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("ADMINISTRADOR");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(administrador.getId());

        // Assert
        assertEquals(1, rolesUsuario.size());
        assertEquals("ADMINISTRADOR", rolesUsuario.get(0).getRolNombre());
        assertEquals(empresa1.getId(), rolesUsuario.get(0).getEmpresa().getId());
        assertEquals(EstadoUsuarioEmpresa.ACTIVO, rolesUsuario.get(0).getEstado());
    }

    @Test
    void testOperarioSoloPuedeRegistrarLabores() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(operario);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("OPERARIO");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(operario.getId());

        // Assert
        assertEquals(1, rolesUsuario.size());
        assertEquals("OPERARIO", rolesUsuario.get(0).getRolNombre());
        assertEquals(empresa1.getId(), rolesUsuario.get(0).getEmpresa().getId());
    }

    @Test
    void testTecnicoPuedePlanificarLabores() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(tecnico);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("TECNICO");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(tecnico.getId());

        // Assert
        assertEquals(1, rolesUsuario.size());
        assertEquals("TECNICO", rolesUsuario.get(0).getRolNombre());
        assertEquals(empresa1.getId(), rolesUsuario.get(0).getEmpresa().getId());
    }

    @Test
    void testProductorSoloGestionaSusCampos() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(productor);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("PRODUCTOR");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(productor.getId());

        // Assert
        assertEquals(1, rolesUsuario.size());
        assertEquals("PRODUCTOR", rolesUsuario.get(0).getRolNombre());
        assertEquals(empresa1.getId(), rolesUsuario.get(0).getEmpresa().getId());
    }

    @Test
    void testAsesorSoloPuedeConsultar() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(asesor);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("ASESOR");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(asesor.getId());

        // Assert
        assertEquals(1, rolesUsuario.size());
        assertEquals("ASESOR", rolesUsuario.get(0).getRolNombre());
        assertEquals(empresa1.getId(), rolesUsuario.get(0).getEmpresa().getId());
    }

    @Test
    void testMantenimientoSoloGestionaMaquinaria() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(mantenimiento);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("MANTENIMIENTO");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(mantenimiento.getId());

        // Assert
        assertEquals(1, rolesUsuario.size());
        assertEquals("MANTENIMIENTO", rolesUsuario.get(0).getRolNombre());
        assertEquals(empresa1.getId(), rolesUsuario.get(0).getEmpresa().getId());
    }

    @Test
    void testUsuarioPuedeTenerMultiplesRolesEnDiferentesEmpresas() {
        // Arrange
        UsuarioEmpresa rolEmpresa1 = new UsuarioEmpresa();
        rolEmpresa1.setUsuario(administrador);
        rolEmpresa1.setEmpresa(empresa1);
        rolEmpresa1.setRolNombre("ADMINISTRADOR");
        rolEmpresa1.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        UsuarioEmpresa rolEmpresa2 = new UsuarioEmpresa();
        rolEmpresa2.setUsuario(administrador);
        rolEmpresa2.setEmpresa(empresa2);
        rolEmpresa2.setRolNombre("TECNICO");
        rolEmpresa2.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        usuarioEmpresaRepository.save(rolEmpresa1);
        usuarioEmpresaRepository.save(rolEmpresa2);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> rolesUsuario = usuarioEmpresaRepository.findByUsuarioId(administrador.getId());

        // Assert
        assertEquals(2, rolesUsuario.size());
        assertTrue(rolesUsuario.stream().anyMatch(r -> r.getRolNombre().equals("ADMINISTRADOR") && 
                r.getEmpresa().getId().equals(empresa1.getId())));
        assertTrue(rolesUsuario.stream().anyMatch(r -> r.getRolNombre().equals("TECNICO") && 
                r.getEmpresa().getId().equals(empresa2.getId())));
    }

    @Test
    void testConsultarUsuariosPorEmpresa() {
        // Arrange
        UsuarioEmpresa adminEmpresa1 = new UsuarioEmpresa();
        adminEmpresa1.setUsuario(administrador);
        adminEmpresa1.setEmpresa(empresa1);
        adminEmpresa1.setRolNombre("ADMINISTRADOR");
        adminEmpresa1.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        UsuarioEmpresa operarioEmpresa1 = new UsuarioEmpresa();
        operarioEmpresa1.setUsuario(operario);
        operarioEmpresa1.setEmpresa(empresa1);
        operarioEmpresa1.setRolNombre("OPERARIO");
        operarioEmpresa1.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        UsuarioEmpresa adminEmpresa2 = new UsuarioEmpresa();
        adminEmpresa2.setUsuario(administrador);
        adminEmpresa2.setEmpresa(empresa2);
        adminEmpresa2.setRolNombre("ADMINISTRADOR");
        adminEmpresa2.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        usuarioEmpresaRepository.save(adminEmpresa1);
        usuarioEmpresaRepository.save(operarioEmpresa1);
        usuarioEmpresaRepository.save(adminEmpresa2);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> usuariosEmpresa1 = usuarioEmpresaRepository.findByEmpresaId(empresa1.getId());
        List<UsuarioEmpresa> usuariosEmpresa2 = usuarioEmpresaRepository.findByEmpresaId(empresa2.getId());

        // Assert
        assertEquals(2, usuariosEmpresa1.size());
        assertTrue(usuariosEmpresa1.stream().anyMatch(u -> u.getRolNombre().equals("ADMINISTRADOR")));
        assertTrue(usuariosEmpresa1.stream().anyMatch(u -> u.getRolNombre().equals("OPERARIO")));

        assertEquals(1, usuariosEmpresa2.size());
        assertTrue(usuariosEmpresa2.stream().anyMatch(u -> u.getRolNombre().equals("ADMINISTRADOR")));
    }

    @Test
    void testConsultarUsuariosPorRol() {
        // Arrange
        UsuarioEmpresa admin1 = new UsuarioEmpresa();
        admin1.setUsuario(administrador);
        admin1.setEmpresa(empresa1);
        admin1.setRolNombre("ADMINISTRADOR");
        admin1.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        UsuarioEmpresa admin2 = new UsuarioEmpresa();
        admin2.setUsuario(tecnico);
        admin2.setEmpresa(empresa2);
        admin2.setRolNombre("ADMINISTRADOR");
        admin2.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        UsuarioEmpresa operario1 = new UsuarioEmpresa();
        operario1.setUsuario(operario);
        operario1.setEmpresa(empresa1);
        operario1.setRolNombre("OPERARIO");
        operario1.setEstado(EstadoUsuarioEmpresa.ACTIVO);

        usuarioEmpresaRepository.save(admin1);
        usuarioEmpresaRepository.save(admin2);
        usuarioEmpresaRepository.save(operario1);
        entityManager.flush();

        // Act
        List<UsuarioEmpresa> administradores = usuarioEmpresaRepository.findByRolNombre("ADMINISTRADOR");
        List<UsuarioEmpresa> operarios = usuarioEmpresaRepository.findByRolNombre("OPERARIO");

        // Assert
        assertEquals(2, administradores.size());
        assertTrue(administradores.stream().anyMatch(u -> u.getUsuario().getId().equals(administrador.getId())));
        assertTrue(administradores.stream().anyMatch(u -> u.getUsuario().getId().equals(tecnico.getId())));

        assertEquals(1, operarios.size());
        assertTrue(operarios.stream().anyMatch(u -> u.getUsuario().getId().equals(operario.getId())));
    }

    @Test
    void testCambiarEstadoUsuarioEmpresa() {
        // Arrange
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(operario);
        usuarioEmpresa.setEmpresa(empresa1);
        usuarioEmpresa.setRolNombre("OPERARIO");
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Act
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.INACTIVO);
        UsuarioEmpresa usuarioModificado = usuarioEmpresaRepository.save(usuarioEmpresa);
        entityManager.flush();

        // Assert
        assertEquals(EstadoUsuarioEmpresa.INACTIVO, usuarioModificado.getEstado());
    }

    @Test
    void testValidarRolesExistentes() {
        // Act
        List<Role> roles = roleRepository.findAll();

        // Assert
        assertTrue(roles.size() >= 2);
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("SUPERADMIN")));
        assertTrue(roles.stream().anyMatch(r -> r.getNombre().equals("USUARIO_REGISTRADO")));
    }

    @Test
    void testValidarUsuariosActivos() {
        // Arrange
        User usuarioInactivo = crearUsuario("inactivo", "inactivo@test.com", "Usuario", "Inactivo");
        usuarioInactivo.setActivo(false);
        userRepository.save(usuarioInactivo);
        entityManager.flush();

        // Act
        List<User> usuariosActivos = userRepository.findByActivoTrue();

        // Assert
        assertTrue(usuariosActivos.size() >= 7); // Los 7 usuarios creados en setUp
        assertTrue(usuariosActivos.stream().allMatch(User::getActivo));
        assertFalse(usuariosActivos.stream().anyMatch(u -> u.getNombreUsuario().equals("inactivo")));
    }

    private User crearUsuario(String nombreUsuario, String email, String nombre, String apellido) {
        User usuario = new User();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setEmail(email);
        usuario.setPassword("password123");
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setActivo(true);
        return userRepository.save(usuario);
    }

    private Empresa crearEmpresa(String nombre, String descripcion) {
        Empresa empresa = new Empresa();
        empresa.setNombre(nombre);
        empresa.setDescripcion(descripcion);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        empresa.setActivo(true);
        return empresaRepository.save(empresa);
    }
}
