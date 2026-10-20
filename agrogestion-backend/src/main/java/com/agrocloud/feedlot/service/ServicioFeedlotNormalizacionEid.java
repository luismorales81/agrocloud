package com.agrocloud.feedlot.service;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ServicioFeedlotNormalizacionEid {

    private static final Pattern HEX_O_DIGITO = Pattern.compile("[^0-9A-Fa-f]");

    public Optional<String> normalizar(String crudo) {
        if (crudo == null || crudo.isBlank()) {
            return Optional.empty();
        }
        String limpio = HEX_O_DIGITO.matcher(crudo.trim()).replaceAll("");
        if (limpio.isEmpty()) {
            return Optional.empty();
        }
        if (limpio.chars().allMatch(Character::isDigit)) {
            if (limpio.length() > 15) {
                return Optional.empty();
            }
            return Optional.of(pad15(limpio));
        }
        if (limpio.length() > 16) {
            return Optional.empty();
        }
        try {
            BigInteger valor = new BigInteger(limpio, 16);
            String decimal = valor.toString(10);
            if (decimal.length() > 15) {
                return Optional.empty();
            }
            return Optional.of(pad15(decimal));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static String pad15(String valor) {
        if (valor.length() >= 15) {
            return valor;
        }
        return "0".repeat(15 - valor.length()) + valor;
    }
}
