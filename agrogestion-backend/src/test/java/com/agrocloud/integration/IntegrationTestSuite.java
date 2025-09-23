package com.agrocloud.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Suite de pruebas de integración que ejecuta todos los tests de endpoints REST.
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Suite
@SuiteDisplayName("Suite de Pruebas de Integración - Endpoints REST")
@SelectClasses({
    FieldControllerIntegrationTest.class,
    InsumoControllerIntegrationTest.class,
    AuthControllerIntegrationTest.class,
    RoleControllerIntegrationTest.class
})
public class IntegrationTestSuite {
    // Esta clase actúa como contenedor para ejecutar todas las pruebas de integración
}
