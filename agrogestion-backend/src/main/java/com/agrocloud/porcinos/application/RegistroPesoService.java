package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.RegistroPeso;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.RegistroPesoRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroPesoService {

    /** GPD mínimo (kg/día) para alerta cuando no está configurado en parámetros */
    private static final BigDecimal GPD_MINIMO_ALERTA_DEFECTO = new BigDecimal("0.25");

    @Autowired
    private RegistroPesoRepository registroPesoRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    /**
     * Registrar peso con validaciones y cálculos automáticos.
     * No se sobrescriben registros anteriores; historial completo se mantiene.
     * Actualiza peso actual y fecha última pesada del lote (recría).
     */
    @Transactional
    public RegistroPeso registrarPeso(Long recriaId, RegistroPeso registroPesoData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(recriaId);
        if (recriaOpt.isEmpty()) {
            throw new IllegalArgumentException("Lote (recría) no encontrado o inactivo");
        }

        Recria recria = recriaOpt.get();
        if (!recria.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("La recría no pertenece a la empresa activa");
        }

        // Validar peso > 0
        if (registroPesoData.getPesoPromedio() == null || registroPesoData.getPesoPromedio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }

        // Validar fecha <= hoy
        LocalDate fechaPesaje = registroPesoData.getFechaPesaje() != null ? registroPesoData.getFechaPesaje() : LocalDate.now();
        if (fechaPesaje.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de pesada no puede ser futura");
        }

        // VALIDAR RANGOS DE PESO según etapa (advertencia, no bloqueante si se desea)
        validarRangoPeso(registroPesoData.getPesoPromedio(), recria.getEtapa(), empresaActiva.get());

        registroPesoData.setRecria(recria);
        registroPesoData.setEmpresa(empresaActiva.get());
        registroPesoData.setUsuario(user);
        registroPesoData.setFechaPesaje(fechaPesaje);

        // Método de pesaje: default BALANZA si no viene
        if (registroPesoData.getMetodo() == null) {
            registroPesoData.setMetodo(RegistroPeso.MetodoPesaje.BALANZA);
        }

        // Etapa al momento (sin cambiar etapa del lote)
        registroPesoData.setEtapaAlMomento(recria.getEtapa());

        // Cantidad de animales: si no viene, usar la de la recría
        if (registroPesoData.getCantidadAnimales() == null) {
            registroPesoData.setCantidadAnimales(recria.getCantidadAnimales() != null ? recria.getCantidadAnimales() : 0);
        }

        // Calcular GPD (ganancia diaria de peso) y agregar a observaciones
        BigDecimal gananciaDiaria = calcularGananciaDiaria(recria, registroPesoData.getPesoPromedio(), fechaPesaje);
        if (gananciaDiaria.compareTo(BigDecimal.ZERO) > 0) {
            String observaciones = registroPesoData.getObservaciones() != null ? registroPesoData.getObservaciones() : "";
            observaciones += String.format("\nGPD calculado: %.2f kg/día", gananciaDiaria);
            registroPesoData.setObservaciones(observaciones);
        }

        // Guardar registro histórico (nunca sobrescribir)
        RegistroPeso guardado = registroPesoRepository.save(registroPesoData);

        // Actualizar en recría: peso actual = último registro, fecha última pesada (sin cambiar etapa)
        recria.setPesoPromedio(registroPesoData.getPesoPromedio());
        recria.setFechaUltimaPesada(fechaPesaje);
        recriaRepository.save(recria);

        // Alertas: peso fuera de rango esperado
        generarAlertaPesoFueraRango(recria, registroPesoData.getPesoPromedio(), empresaActiva.get());

        // Alerta: GPD por debajo del mínimo configurado
        if (gananciaDiaria.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal gpdMinimo = obtenerGpdMinimoObjetivo(empresaActiva.get());
            if (gananciaDiaria.compareTo(gpdMinimo) < 0) {
                generarAlertaGpdBajo(recria, gananciaDiaria, gpdMinimo);
            }
        }

        return guardado;
    }

    /**
     * Validar rango de peso según etapa
     */
    private void validarRangoPeso(BigDecimal pesoPromedio, Recria.EtapaRecria etapa, Empresa empresa) {
        if (pesoPromedio == null || pesoPromedio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }
        if (etapa == null) {
            return; // Sin etapa no validamos rango
        }

        // Rangos típicos por etapa (se pueden configurar en parámetros)
        BigDecimal pesoMinimo = BigDecimal.ZERO;
        BigDecimal pesoMaximo = BigDecimal.valueOf(200); // Máximo general

        switch (etapa) {
            case F1:
                pesoMinimo = BigDecimal.valueOf(0.5);
                pesoMaximo = BigDecimal.valueOf(10);
                break;
            case F2:
                pesoMinimo = BigDecimal.valueOf(8);
                pesoMaximo = BigDecimal.valueOf(20);
                break;
            case F3:
                pesoMinimo = BigDecimal.valueOf(18);
                pesoMaximo = BigDecimal.valueOf(40);
                break;
            case F4:
            case DESARROLLO:
                pesoMinimo = BigDecimal.valueOf(35);
                pesoMaximo = BigDecimal.valueOf(70);
                break;
            case TERMINACION:
                pesoMinimo = BigDecimal.valueOf(60);
                pesoMaximo = BigDecimal.valueOf(150);
                break;
        }

        if (pesoPromedio.compareTo(pesoMinimo) < 0) {
            throw new IllegalArgumentException(
                String.format("El peso promedio (%.2f kg) está por debajo del mínimo esperado para %s (%.2f kg)",
                    pesoPromedio, etapa.name(), pesoMinimo));
        }

        if (pesoPromedio.compareTo(pesoMaximo) > 0) {
            throw new IllegalArgumentException(
                String.format("El peso promedio (%.2f kg) está por encima del máximo esperado para %s (%.2f kg)",
                    pesoPromedio, etapa.name(), pesoMaximo));
        }
    }

    /**
     * Calcular ganancia diaria de peso automáticamente
     */
    private BigDecimal calcularGananciaDiaria(Recria recria, BigDecimal nuevoPeso, LocalDate fechaPesaje) {
        BigDecimal pesoAnterior = recria.getPesoPromedio();
        if (pesoAnterior == null || pesoAnterior.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS
            .between(recria.getFechaIngreso(), fechaPesaje);

        if (diasTranscurridos <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal gananciaTotal = nuevoPeso.subtract(pesoAnterior);
        if (gananciaTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return gananciaTotal.divide(BigDecimal.valueOf(diasTranscurridos), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Obtener GPD mínimo objetivo (kg/día) para alertas. Por ahora valor por defecto.
     */
    private BigDecimal obtenerGpdMinimoObjetivo(Empresa empresa) {
        // Si en el futuro se agrega gpdMinimoObjetivo a ParametrosProductivosPorcino, leerlo aquí
        return GPD_MINIMO_ALERTA_DEFECTO;
    }

    /**
     * Generar alerta si el peso está fuera de rangos esperados (lote fuera de rango)
     */
    private void generarAlertaPesoFueraRango(Recria recria, BigDecimal pesoPromedio, Empresa empresa) {
        // Por ahora solo registramos en observaciones; en un sistema completo, entidad Alertas
    }

    /**
     * Generar alerta si GPD está por debajo del mínimo configurado
     */
    private void generarAlertaGpdBajo(Recria recria, BigDecimal gpdActual, BigDecimal gpdMinimo) {
        // Por ahora solo log; en un sistema completo, crear registro de Alerta
        System.out.println(String.format(
            "ALERTA GPD: Lote recría id=%d tiene GPD %.2f kg/día por debajo del mínimo %.2f kg/día",
            recria.getId(), gpdActual, gpdMinimo));
    }

    public List<RegistroPeso> obtenerRegistrosPorRecria(Long recriaId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Recria> recria = recriaRepository.findById(recriaId);
        if (recria.isEmpty() || !recria.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return registroPesoRepository.findByRecriaOrderByFechaPesajeDesc(recria.get());
    }

    /**
     * Calcular ganancia diaria de peso (GDP)
     */
    public BigDecimal calcularGDP(Long recriaId, User user) {
        List<RegistroPeso> registros = obtenerRegistrosPorRecria(recriaId, user);
        if (registros.size() < 2) {
            return BigDecimal.ZERO;
        }

        RegistroPeso primero = registros.get(registros.size() - 1);
        RegistroPeso ultimo = registros.get(0);

        long dias = java.time.temporal.ChronoUnit.DAYS.between(primero.getFechaPesaje(), ultimo.getFechaPesaje());
        if (dias == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal diferenciaPeso = ultimo.getPesoPromedio().subtract(primero.getPesoPromedio());
        return diferenciaPeso.divide(BigDecimal.valueOf(dias), 2, java.math.RoundingMode.HALF_UP);
    }
}



