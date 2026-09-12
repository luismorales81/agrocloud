package com.agrocloud.chatia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado simétrico AES-256-GCM para claves API de usuarios.
 */
@Service
public class ServicioCifradoClaves {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANO_IV = 12;
    private static final int TAMANO_TAG = 128;

    private final SecretKey claveSecreta;

    public ServicioCifradoClaves(
            @Value("${chat.ia.encryption-key}") String claveConfiguracion) {
        this.claveSecreta = derivarClave(claveConfiguracion);
    }

    public String cifrar(String textoPlano) {
        if (textoPlano == null || textoPlano.isBlank()) {
            throw new IllegalArgumentException("El texto a cifrar no puede estar vacío");
        }
        try {
            byte[] iv = new byte[TAMANO_IV];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta, new GCMParameterSpec(TAMANO_TAG, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            byte[] combinado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, combinado, 0, iv.length);
            System.arraycopy(cifrado, 0, combinado, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(combinado);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar la clave API", e);
        }
    }

    public String descifrar(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.isBlank()) {
            throw new IllegalArgumentException("El texto cifrado no puede estar vacío");
        }
        try {
            byte[] combinado = Base64.getDecoder().decode(textoCifrado);
            byte[] iv = new byte[TAMANO_IV];
            byte[] cifrado = new byte[combinado.length - TAMANO_IV];
            System.arraycopy(combinado, 0, iv, 0, TAMANO_IV);
            System.arraycopy(combinado, TAMANO_IV, cifrado, 0, cifrado.length);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta, new GCMParameterSpec(TAMANO_TAG, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar la clave API", e);
        }
    }

    private SecretKey derivarClave(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo derivar la clave de cifrado", e);
        }
    }
}
