package com.agrocloud.chatia.service;

/**
 * Credenciales efectivas para llamar a Gemini: clave de usuario (BYOK) o clave de proyecto.
 */
public record CredencialesChatIa(String claveApi, String modelo, boolean claveDeUsuario) {
}
