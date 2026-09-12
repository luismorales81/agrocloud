package com.agrocloud.admin;

import com.agrocloud.test.BaseIntegracionCrudTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleControllerCrudIntegracionTest extends BaseIntegracionCrudTest {

    @Test
    void listarRoles_ok() throws Exception {
        getJson("/api/roles").andExpect(status().isOk());
    }
}
