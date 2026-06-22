package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.LoteMinimoDTO;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;

import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar recría
 * Incluye validaciones de pesos, densidad y cálculos automáticos
 */
@Service
public class RecriaService {

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    @Qualifier("campanaContextServiceCore")
    private com.agrocloud.core.application.CampanaContextService campanaContextService;

    @Autowired
    private RecriaStockService recriaStockService;

    @Autowired
    private com.agrocloud.porcinos.infrastructure.PartoRepository partoRepository;

    /**
     * Crear recría con validaciones completas
     */
    @Transactional
    public Recria crearRecria(Recria recriaData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Validar lote
        if (recriaData.getLoteId() == null) {
            throw new IllegalArgumentException("Debe especificar un lote para la recría");
        }

        LoteMinimoDTO lote = loteParaPorcinosQuery.obtenerPorId(recriaData.getLoteId())
                .orElseThrow(() -> new IllegalArgumentException("El lote no existe"));

        // VALIDAR PESOS dentro de rangos configurados
        validarPesosRecria(recriaData.getPesoPromedio(), empresaActiva.get());

        // VALIDAR DENSIDAD por corral conforme a los parámetros
        validarDensidadCorral(lote, recriaData.getCantidadAnimales(), empresaActiva.get());

        recriaData.setLoteId(recriaData.getLoteId());
        recriaData.setEmpresa(empresaActiva.get());
        recriaData.setUsuario(user);
        recriaData.setActivo(true);
        recriaData.setCampanaId(campanaContextService.resolverCampanaIdActiva(empresaActiva.get().getId()));

        if (recriaData.getFechaIngreso() == null) {
            recriaData.setFechaIngreso(LocalDate.now());
        }

        if (recriaData.getEtapa() == null) {
            recriaData.setEtapa(Recria.EtapaRecria.F1);
        }

        // Si no se especifica explícitamente, consideramos que este flujo es ingreso externo
        if (recriaData.getOrigen() == null) {
            recriaData.setOrigen(Recria.OrigenRecria.EXTERNO);
        }

        return recriaRepository.save(recriaData);
    }

    /**
     * Validar pesos dentro de rangos configurados
     */
    private void validarPesosRecria(BigDecimal pesoPromedio, Empresa empresa) {
        if (pesoPromedio == null) {
            throw new IllegalArgumentException("El peso promedio es obligatorio");
        }

        if (pesoPromedio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }

        // Rangos típicos por etapa (se pueden configurar en parámetros)
        // Por ahora validamos rangos básicos
        if (pesoPromedio.compareTo(BigDecimal.valueOf(0.5)) < 0) {
            throw new IllegalArgumentException("El peso promedio es muy bajo (mínimo esperado: 0.5 kg)");
        }

        if (pesoPromedio.compareTo(BigDecimal.valueOf(50)) > 0) {
            throw new IllegalArgumentException("El peso promedio es muy alto para recría (máximo esperado: 50 kg)");
        }
    }

    /**
     * Validar densidad por corral
     */
    private void validarDensidadCorral(LoteMinimoDTO lote, Integer cantidadAnimales, Empresa empresa) {
        if (lote.superficieHa() == null || lote.superficieHa() <= 0) return;
        BigDecimal area = BigDecimal.valueOf(lote.superficieHa());
        BigDecimal densidad = BigDecimal.valueOf(cantidadAnimales).divide(area, 2, java.math.RoundingMode.HALF_UP);
        if (densidad.compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException(
                String.format("La densidad es muy alta: %.2f animales/ha (máximo recomendado: 20 animales/ha)", densidad));
        }
    }

    /**
     * Registrar peso en recría y calcular ganancia diaria
     */
    @Transactional
    public Recria registrarPeso(Long recriaId, BigDecimal nuevoPesoPromedio, LocalDate fechaPeso, User user) {
        Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(recriaId);
        if (recriaOpt.isEmpty()) {
            throw new IllegalArgumentException("Recría no encontrada o inactiva");
        }

        Recria recria = recriaOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !recria.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para actualizar esta recría");
        }

        // Validar peso
        validarPesosRecria(nuevoPesoPromedio, empresaActiva.get());

        // Calcular ganancia diaria de peso
        BigDecimal pesoAnterior = recria.getPesoPromedio();
        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS
            .between(recria.getFechaIngreso(), fechaPeso != null ? fechaPeso : LocalDate.now());

        if (diasTranscurridos > 0 && pesoAnterior != null) {
            BigDecimal gananciaTotal = nuevoPesoPromedio.subtract(pesoAnterior);
            BigDecimal gananciaDiaria = gananciaTotal.divide(
                BigDecimal.valueOf(diasTranscurridos), 2, java.math.RoundingMode.HALF_UP);

            // Guardar ganancia diaria en observaciones o crear campo específico
            String observaciones = recria.getObservaciones() != null ? recria.getObservaciones() : "";
            observaciones += String.format("\nPeso actualizado: %.2f kg (Ganancia diaria: %.2f kg/día)", 
                nuevoPesoPromedio, gananciaDiaria);
            recria.setObservaciones(observaciones);
        }

        recria.setPesoPromedio(nuevoPesoPromedio);
        return recriaRepository.save(recria);
    }

    /**
     * Calcular conversión alimenticia por lote
     */
    public BigDecimal calcularConversionAlimenticia(Long recriaId, BigDecimal consumoTotalKg, User user) {
        Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(recriaId);
        if (recriaOpt.isEmpty()) {
            throw new IllegalArgumentException("Recría no encontrada");
        }

        Recria recria = recriaOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !recria.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para calcular conversión en esta recría");
        }

        if (consumoTotalKg == null || consumoTotalKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El consumo total debe ser mayor a cero");
        }

        // Obtener peso inicial y final
        BigDecimal pesoInicial = recria.getPesoPromedio(); // Peso al ingreso
        BigDecimal pesoFinal = recria.getPesoPromedio(); // Peso actual (se puede pasar como parámetro)

        if (pesoInicial == null || pesoFinal == null) {
            throw new IllegalArgumentException("Debe tener pesos inicial y final registrados");
        }

        BigDecimal gananciaPeso = pesoFinal.subtract(pesoInicial);
        if (gananciaPeso.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La ganancia de peso debe ser positiva");
        }

        // Conversión alimenticia = consumo total / ganancia de peso
        BigDecimal conversion = consumoTotalKg.divide(gananciaPeso, 2, java.math.RoundingMode.HALF_UP);

        return conversion;
    }

    /**
     * Cerrar recría (pasar a engorde o venta)
     */
    @Transactional
    public Recria cerrarRecria(Long recriaId, LocalDate fechaSalida, Recria.DestinoRecria destino, User user) {
        Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(recriaId);
        if (recriaOpt.isEmpty()) {
            throw new IllegalArgumentException("Recría no encontrada o inactiva");
        }

        Recria recria = recriaOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !recria.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para cerrar esta recría");
        }

        // REGLA DE NEGOCIO: No se puede cerrar un lote con animales vivos (fórmula oficial: cantidadAnimales - muertes)
        int animalesDisponibles = recriaStockService.animalesDisponibles(recria);
        if (animalesDisponibles > 0) {
            throw new IllegalArgumentException(
                String.format("No se puede cerrar el lote: aún quedan %d animales vivos. " +
                    "Debe registrar todas las bajas (muertes, ventas) antes de cerrar el lote.",
                    animalesDisponibles));
        }

        // VALIDACIÓN: No se puede pasar a engorde si no se cerró recría
        if (destino == Recria.DestinoRecria.ENGORDE && recria.getFechaSalida() == null) {
            // Validar que tenga peso registrado
            if (recria.getPesoPromedio() == null) {
                throw new IllegalArgumentException("Debe registrar peso antes de pasar a engorde");
            }
        }

        recria.setFechaSalida(fechaSalida != null ? fechaSalida : LocalDate.now());
        recria.setDestino(destino);
        return recriaRepository.save(recria);
    }

    /**
     * Listar recrías con filtros opcionales.
     * @param activas true = solo abiertas (sin fecha de salida); false = histórico incluye cerradas
     * @param delPeriodoActivo true = solo registros con campana_id del período activo
     */
    @Transactional(readOnly = true)
    public List<Recria> listarRecrias(User user, Boolean activas, Boolean delPeriodoActivo) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        boolean soloAbiertas = activas == null || activas;
        List<Recria> recrias = soloAbiertas
                ? recriaRepository.findByEmpresaAndActivas(empresaActiva.get())
                : recriaRepository.findByEmpresaTodasActivas(empresaActiva.get());

        if (Boolean.TRUE.equals(delPeriodoActivo)) {
            Long campanaId = campanaContextService.resolverCampanaIdActiva(empresaActiva.get().getId());
            recrias = recrias.stream()
                    .filter(r -> campanaId.equals(r.getCampanaId()))
                    .toList();
        }

        recrias.forEach(this::poblarInformacionAdicional);
        return recrias;
    }

    /**
     * Obtener recrías activas (sin fecha de salida).
     */
    @Transactional(readOnly = true)
    public List<Recria> obtenerRecriasActivas(User user) {
        return listarRecrias(user, true, false);
    }

    /**
     * Obtener recría por ID
     */
    @Transactional(readOnly = true)
    public Optional<Recria> obtenerRecriaPorId(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }

        Optional<Recria> recria = recriaRepository.findByIdAndActivoTrue(id);
        if (recria.isPresent() && recria.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            Recria recriaEncontrada = recria.get();
            poblarInformacionAdicional(recriaEncontrada);
            return Optional.of(recriaEncontrada);
        }
        return Optional.empty();
    }

    /**
     * Poblar información adicional de la recría (lote, madre)
     */
    private void poblarInformacionAdicional(Recria recria) {
        // Poblar información del lote
        if (recria.getLoteId() != null) {
            loteParaPorcinosQuery.obtenerPorId(recria.getLoteId()).map(LoteMinimoDTO::nombre).ifPresent(recria::setLoteNombre);
        }

        // Buscar el parto asociado por fecha de ingreso y empresa
        // La recría se crea desde un parto, así que buscamos el parto que coincide
        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(recria.getEmpresa());
        Optional<Parto> partoOpt = partos.stream()
            .filter(p -> p.getFechaInicio() != null &&
                        p.getFechaInicio().toLocalDate().equals(recria.getFechaIngreso()))
            .findFirst();

        if (partoOpt.isPresent()) {
            Parto parto = partoOpt.get();
            if (parto.getMadre() != null) {
                recria.setMadreNombre(parto.getMadre().getIdentificacion());
            }
        }

        // Si aún no tiene origen persistido, lo derivamos:
        // - Si se pudo asociar a una madre/parto, consideramos origen DESTETE
        // - En caso contrario, lo marcamos como ingreso EXTERNO
        if (recria.getOrigen() == null) {
            if (recria.getMadreNombre() != null && !recria.getMadreNombre().isBlank()) {
                recria.setOrigen(Recria.OrigenRecria.DESTETE);
            } else {
                recria.setOrigen(Recria.OrigenRecria.EXTERNO);
            }
        }
    }
}

