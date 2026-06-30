package com.agrocloud.controller;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.UsuarioEmpresa;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.model.enums.EstadoUsuarioEmpresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class InsumoControllerIntegracionTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private com.agrocloud.core.infrastructure.EmpresaRepository empresaRepository;
    @Autowired private com.agrocloud.core.infrastructure.UsuarioEmpresaRepository usuarioEmpresaRepository;

    private User usuario;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        String sufijo = String.valueOf(System.nanoTime());
        usuario = new User();
        usuario.setUsername("insumos-test-" + sufijo);
        usuario.setEmail("insumos-" + sufijo + "@test.com");
        usuario.setPassword("password123");
        usuario.setFirstName("Test");
        usuario.setLastName("Insumos");
        usuario.setActivo(true);
        usuario = userRepository.save(usuario);

        empresa = new Empresa();
        empresa.setNombre("Empresa Test Insumos " + sufijo);
        empresa.setCuit("30-" + sufijo.substring(Math.max(0, sufijo.length() - 8)) + "-9");
        empresa = empresaRepository.save(empresa);

        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa(
                usuario, empresa, com.agrocloud.model.enums.RolEmpresa.ADMINISTRADOR);
        usuarioEmpresa.setEstado(EstadoUsuarioEmpresa.ACTIVO);
        usuarioEmpresaRepository.saveAndFlush(usuarioEmpresa);

        Insumo balanceado = new Insumo();
        balanceado.setNombre("Balanceado test");
        balanceado.setTipo(Insumo.TipoInsumo.OTROS);
        balanceado.setUnidadMedida("kg");
        balanceado.setPrecioUnitario(new BigDecimal("100"));
        balanceado.setStockMinimo(new BigDecimal("10"));
        balanceado.setStockActual(new BigDecimal("500"));
        balanceado.setActivo(true);
        balanceado.setUser(usuario);
        balanceado.setEmpresa(empresa);
        insumoRepository.save(balanceado);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, java.util.List.of()));
    }

    @Test
    void listarInsumos_devuelveDtoSinError500() throws Exception {
        mockMvc.perform(get("/api/insumos")
                        .header("X-Company-Id", empresa.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Balanceado test"))
                .andExpect(jsonPath("$[0].tipo").value("OTROS"));
    }
}
