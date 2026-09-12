package com.agrocloud.chatia;

import com.agrocloud.chatia.herramientas.RegistroHerramientasConsulta;
import com.agrocloud.chatia.service.ServicioCifradoClaves;
import com.agrocloud.chatia.service.ServicioClienteGemini;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServicioCifradoClavesTest {

    @Test
    void cifrarYDescifrar_debeRecuperarTextoOriginal() {
        ServicioCifradoClaves servicio = new ServicioCifradoClaves("clave-prueba-chat-ia-32bytes!!");
        String original = "AIzaSyEjemploClaveGemini123456";
        String cifrado = servicio.cifrar(original);
        assertThat(cifrado).isNotEqualTo(original);
        assertThat(servicio.descifrar(cifrado)).isEqualTo(original);
    }

    @Test
    void normalizarModulos_debeIncluirCoreYCultivos() {
        var modulos = RegistroHerramientasConsulta.normalizarModulos(java.util.List.of("CROPS", "PORCINOS"));
        assertThat(modulos).contains("CORE", "CULTIVOS", "PORCINOS");
    }

    @Test
    void normalizarCodigoModulo_debeMapearAvicolaCrianza() {
        assertThat(RegistroHerramientasConsulta.normalizarCodigoModulo("avicola-crianza"))
                .isEqualTo("AVICOLA_CRIANZA");
    }

    @Test
    void normalizarRespuestaHerramienta_listaDebeEnvolverseEnObjeto() {
        ServicioClienteGemini cliente = new ServicioClienteGemini(new com.fasterxml.jackson.databind.ObjectMapper());
        var nodo = cliente.normalizarRespuestaHerramienta(java.util.List.of(
                java.util.Map.of("id", 1, "nombre", "Lote 1")));
        assertThat(nodo.isObject()).isTrue();
        assertThat(nodo.has("datos")).isTrue();
        assertThat(nodo.get("datos").isArray()).isTrue();
    }
}
