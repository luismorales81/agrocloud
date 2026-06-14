package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.VentaPorcino;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.VentaPorcinoRepository;
import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import com.agrocloud.porcinos.domain.ParametrosEstablecimientoPorcino;
import com.agrocloud.porcinos.infrastructure.DatosEconomicosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar ventas de porcinos
 * Incluye validaciones completas y automatismos para descontar animales
 */
@Service
public class VentaPorcinoService {

    @Autowired
    private VentaPorcinoRepository ventaPorcinoRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private RecriaStockService recriaStockService;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private DatosEconomicosPorcinoRepository datosEconomicosRepository;

    @Autowired
    private ParametrosPorcinoService parametrosPorcinoService;

    /**
     * Registrar una venta o faena con validaciones completas
     */
    @Transactional
    public VentaPorcino registrarVenta(VentaPorcino ventaData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // VALIDACIÓN: Si es FAENA, verificar que el establecimiento esté configurado para realizar faenas
        if (ventaData.getTipo() == VentaPorcino.TipoVenta.FAENA) {
            Optional<ParametrosEstablecimientoPorcino> parametrosOpt = 
                parametrosPorcinoService.obtenerParametrosEstablecimiento(user.getId());
            
            if (parametrosOpt.isEmpty() || 
                parametrosOpt.get().getRealizaFaena() == null || 
                !parametrosOpt.get().getRealizaFaena()) {
                throw new IllegalArgumentException(
                    "Este establecimiento no está configurado para realizar faenas. " +
                    "Configure los parámetros del establecimiento para habilitar faenas.");
            }
            
            // VALIDACIÓN: Si es FAENA, recría es obligatoria
            if (ventaData.getRecria() == null || ventaData.getRecria().getId() == null) {
                throw new IllegalArgumentException("La faena requiere una recría asociada");
            }
            
            // Validaciones específicas de faena
            if (ventaData.getPesoEnvio() == null || ventaData.getPesoEnvio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El peso de envío debe ser mayor a cero");
            }
            
            // Usar fechaEnvio como fecha principal para faenas, si no está usar fecha
            if (ventaData.getFechaEnvio() == null) {
                ventaData.setFechaEnvio(ventaData.getFecha() != null ? ventaData.getFecha() : LocalDate.now());
            }
        }

        // Validar campos básicos
        if (ventaData.getCantidad() == null || ventaData.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (ventaData.getPesoPromedio() == null || ventaData.getPesoPromedio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso promedio debe ser mayor a cero");
        }
        // VALIDACIÓN: precioKg es obligatorio para ventas normales, opcional para FAENA
        if (ventaData.getTipo() != VentaPorcino.TipoVenta.FAENA && 
            (ventaData.getPrecioKg() == null || ventaData.getPrecioKg().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("El precio por kg es obligatorio y debe ser mayor a cero para ventas normales");
        }

        // VALIDACIÓN: Si hay recría asociada (o es faena), validar disponibilidad
        if (ventaData.getRecria() != null && ventaData.getRecria().getId() != null) {
            Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(ventaData.getRecria().getId());
            if (recriaOpt.isEmpty()) {
                throw new IllegalArgumentException("La recría no existe o está inactiva");
            }

            Recria recria = recriaOpt.get();
            if (!recria.getEmpresa().getId().equals(empresaActiva.get().getId())) {
                throw new IllegalArgumentException("La recría no pertenece a la empresa activa");
            }

            // VALIDACIÓN: Verificar que tenga peso registrado
            if (recria.getPesoPromedio() == null || recria.getPesoPromedio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La recría debe tener peso registrado antes de vender");
            }

            // VALIDACIÓN: Verificar animales disponibles (fórmula oficial: cantidadAnimales - muertes)
            int animalesDisponibles = recriaStockService.animalesDisponibles(recria);
            if (ventaData.getCantidad() > animalesDisponibles) {
                throw new IllegalArgumentException(
                    String.format("La cantidad a vender (%d) excede los animales disponibles (%d)",
                        ventaData.getCantidad(), animalesDisponibles));
            }

            // AUTOMATISMO: Descontar animales del lote/recría (spec: cantidadAnimales se actualiza en venta)
            int nuevaCantidad = recria.getCantidadAnimales() - ventaData.getCantidad();
            if (nuevaCantidad < 0) {
                throw new IllegalArgumentException("La cantidad resultante no puede ser negativa");
            }
            recria.setCantidadAnimales(nuevaCantidad);

            // Si se vendieron todos los animales, cerrar la recría
            if (nuevaCantidad == 0) {
                recria.setFechaSalida(ventaData.getFecha() != null ? ventaData.getFecha() : LocalDate.now());
                recria.setDestino(Recria.DestinoRecria.VENTA);
            }

            try {
                recriaRepository.save(recria);
            } catch (OptimisticLockException e) {
                throw new IllegalStateException("La recría fue modificada por otra operación. Intente de nuevo.", e);
            }
            ventaData.setRecria(recria);
        }

        // Lógica específica para faenas
        if (ventaData.getTipo() == VentaPorcino.TipoVenta.FAENA) {
            // AUTOMATISMO: Calcular porcentaje de merma en transporte
            Optional<DatosEconomicosPorcino> datosEconomicosOpt = 
                datosEconomicosRepository.findByEmpresa(empresaActiva.get());
            
            BigDecimal porcentajeMermaTransporte = BigDecimal.ZERO;
            if (datosEconomicosOpt.isPresent() && 
                datosEconomicosOpt.get().getPorcentajeMermaTransporte() != null) {
                porcentajeMermaTransporte = datosEconomicosOpt.get().getPorcentajeMermaTransporte();
            }

            // Calcular peso esperado después de merma
            BigDecimal pesoEsperadoConMerma = ventaData.getPesoEnvio()
                .multiply(BigDecimal.ONE.subtract(porcentajeMermaTransporte.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));

            // Calcular rendimiento si se proporciona peso de faena
            if (ventaData.getPesoFaena() != null && ventaData.getPesoEnvio() != null) {
                BigDecimal rendimiento = ventaData.getPesoFaena()
                    .divide(ventaData.getPesoEnvio(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                ventaData.setRendimiento(rendimiento);
            }

            // Calcular ingreso total si se proporciona precio
            if (ventaData.getPrecioKg() != null) {
                BigDecimal pesoParaCalculo = ventaData.getPesoFaena() != null ? 
                    ventaData.getPesoFaena() : pesoEsperadoConMerma;
                BigDecimal ingresoTotal = pesoParaCalculo.multiply(ventaData.getPrecioKg());
                ventaData.setIngresoTotal(ingresoTotal);
            } else {
                ventaData.setIngresoTotal(BigDecimal.ZERO);
            }
        } else {
            // Calcular ingreso total para ventas normales (ENGORDE o REPRODUCTOR)
            // precioKg es obligatorio para ventas normales, ya validado arriba
            BigDecimal ingresoTotal = ventaData.getPesoPromedio()
                .multiply(BigDecimal.valueOf(ventaData.getCantidad()))
                .multiply(ventaData.getPrecioKg());
            ventaData.setIngresoTotal(ingresoTotal);
        }

        ventaData.setEmpresa(empresaActiva.get());
        ventaData.setUsuario(user);
        ventaData.setActivo(true);

        if (ventaData.getFecha() == null) {
            ventaData.setFecha(LocalDate.now());
        }

        return ventaPorcinoRepository.save(ventaData);
    }

    public List<VentaPorcino> obtenerVentas(User user, LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        if (fechaDesde != null && fechaHasta != null) {
            return ventaPorcinoRepository.findByEmpresaAndRangoFechas(empresaActiva.get(), fechaDesde, fechaHasta);
        }

        return ventaPorcinoRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    public BigDecimal obtenerIngresosTotales(User user, LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = ventaPorcinoRepository.sumIngresosByRangoFechas(
            empresaActiva.get(), fechaDesde, fechaHasta);
        return total != null ? total : BigDecimal.ZERO;
    }
}
