package com.agrocloud.core.application;

import com.agrocloud.config.CampanaRequestContext;
import com.agrocloud.core.domain.Egreso;
import com.agrocloud.core.domain.ImputacionEconomica;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.EgresoRepository;
import com.agrocloud.core.infrastructure.IngresoRepository;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import com.agrocloud.dto.SolicitudCompraInsumo;
import com.agrocloud.dto.SolicitudLineaLibro;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import com.agrocloud.model.enums.ModuloSistema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ServicioLibroEmpresa {

    public static final String REF_COMPRA_INSUMO = "COMPRA_INSUMO";
    public static final String REF_VENTA_FEEDLOT = "VENTA_FEEDLOT";
    public static final String REF_VENTA_PORCINOS = "VENTA_PORCINOS";
    public static final String REF_VENTA_AVICOLA = "VENTA_AVICOLA";
    public static final String REF_VENTA_LECHE = "VENTA_LECHE";
    public static final String REF_VENTA_HUEVOS = "VENTA_HUEVOS";
    public static final String REF_VENTA_GRANO = "VENTA_GRANO";
    public static final String REF_MANUAL = "MANUAL";

    private final ServicioSeguridadContexto seguridad;
    private final CompanyModuleService companyModuleService;
    private final UserService userService;
    private final EgresoRepository egresoRepository;
    private final IngresoRepository ingresoRepository;
    private final InsumoRepository insumoRepository;
    private final InventoryService inventoryService;
    private final PlotRepository plotRepository;

    public ServicioLibroEmpresa(
            ServicioSeguridadContexto seguridad,
            CompanyModuleService companyModuleService,
            @Qualifier("userServiceCore") UserService userService,
            @Qualifier("egresoRepositoryCore") EgresoRepository egresoRepository,
            @Qualifier("ingresoRepositoryCore") IngresoRepository ingresoRepository,
            @Qualifier("insumoRepositoryInventario") InsumoRepository insumoRepository,
            InventoryService inventoryService,
            @Qualifier("plotRepositoryCultivos") PlotRepository plotRepository) {
        this.seguridad = seguridad;
        this.companyModuleService = companyModuleService;
        this.userService = userService;
        this.egresoRepository = egresoRepository;
        this.ingresoRepository = ingresoRepository;
        this.insumoRepository = insumoRepository;
        this.inventoryService = inventoryService;
        this.plotRepository = plotRepository;
    }

    @Transactional(readOnly = true)
    public List<Egreso> listarEgresos(String modulo, boolean incluirCompartidas) {
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        String codigo = moduloEnBlanco(modulo) ? null : exigirModulo(modulo).getCodigo();
        return egresoRepository.listarPorEmpresa(empresaId, codigo, incluirCompartidas && codigo != null);
    }

    @Transactional(readOnly = true)
    public List<Ingreso> listarIngresos(String modulo) {
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        String codigo = moduloEnBlanco(modulo) ? null : exigirModulo(modulo).getCodigo();
        return ingresoRepository.listarPorEmpresa(empresaId, codigo);
    }

    @Transactional
    public Egreso registrarCompraInsumo(SolicitudCompraInsumo solicitud) {
        if (solicitud.getInsumoId() == null) {
            throw new IllegalArgumentException("Elegí un insumo ya cargado");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (solicitud.getCostoTotal() == null || solicitud.getCostoTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El costo total debe ser mayor que cero");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        ModuloSistema modulo = exigirModuloHabilitado(solicitud.getModulo());
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        User usuario = usuarioActual();
        Insumo insumo = insumoRepository.buscarPorIdConEmpresa(solicitud.getInsumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        if (insumo.getEmpresa() == null || !empresaId.equals(insumo.getEmpresa().getId())) {
            throw new IllegalArgumentException("El insumo no pertenece a la empresa");
        }

        BigDecimal costoUnitario = solicitud.getCostoTotal()
                .divide(solicitud.getCantidad(), 4, RoundingMode.HALF_UP);
        BigDecimal saldo = insumo.getStockActual() != null ? insumo.getStockActual() : BigDecimal.ZERO;
        insumo.setPrecioUnitario(CalculoCostoInsumo.costoTrasCompra(
                saldo, insumo.getPrecioUnitario(), solicitud.getCantidad(), solicitud.getCostoTotal()));
        insumoRepository.save(insumo);

        Egreso egreso = new Egreso();
        egreso.setConcepto("Compra de " + insumo.getNombre());
        egreso.setTipo(Egreso.TipoEgreso.INSUMO);
        egreso.setReferenciaId(insumo.getId());
        egreso.setCantidad(solicitud.getCantidad());
        egreso.setUnidadMedida(insumo.getUnidadMedida());
        egreso.setCostoUnitario(costoUnitario);
        egreso.setCostoTotal(solicitud.getCostoTotal());
        egreso.setFecha(solicitud.getFecha());
        egreso.setProveedor(solicitud.getProveedor());
        egreso.setObservaciones(solicitud.getObservaciones());
        egreso.setEstado(Egreso.EstadoEgreso.REGISTRADO);
        egreso.setUser(usuario);
        completarLinea(egreso, empresaId, modulo, ImputacionEconomica.COMPARTIDA, REF_COMPRA_INSUMO, insumo.getId());
        egreso = egresoRepository.saveAndFlush(egreso);

        InventoryResult resultado = inventoryService.reponer(
                empresaId,
                insumo.getId(),
                solicitud.getCantidad(),
                origenDe(modulo),
                egreso.getId(),
                usuario.getId());
        if (!resultado.exito()) {
            throw new IllegalStateException(resultado.mensaje() != null ? resultado.mensaje() : "No se pudo reponer el stock");
        }
        return egreso;
    }

    @Transactional
    public Egreso anularCompra(Long egresoId) {
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        User usuario = usuarioActual();
        Egreso egreso = egresoRepository.findById(egresoId)
                .orElseThrow(() -> new ResourceNotFoundException("Egreso no encontrado"));
        if (!empresaId.equals(egreso.getEmpresaId())) {
            throw new ResourceNotFoundException("Egreso no encontrado");
        }
        if (egreso.getTipo() != Egreso.TipoEgreso.INSUMO || egreso.getReferenciaId() == null) {
            throw new IllegalArgumentException("Solo se puede anular una compra de insumo");
        }
        if (egreso.getEstado() == Egreso.EstadoEgreso.CANCELADO || Boolean.FALSE.equals(egreso.getActivo())) {
            throw new IllegalStateException("La compra ya está anulada");
        }
        BigDecimal cantidad = egreso.getCantidad();
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("La compra no tiene cantidad para revertir");
        }
        if (!inventoryService.hayStockSuficiente(empresaId, egreso.getReferenciaId(), cantidad)) {
            throw new ResourceConflictException("No hay stock suficiente para anular la compra. El insumo ya fue consumido.");
        }
        Insumo insumo = insumoRepository.buscarPorIdConEmpresa(egreso.getReferenciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo no encontrado"));
        insumo.setPrecioUnitario(CalculoCostoInsumo.costoTrasAnularCompra(
                insumo.getStockActual(), insumo.getPrecioUnitario(), cantidad, egreso.getCostoUnitario()));
        insumoRepository.save(insumo);

        InventoryOrigin origen = origenDe(exigirModulo(egreso.getModuloOrigen()));
        InventoryResult resultado = inventoryService.consumir(
                empresaId, egreso.getReferenciaId(), cantidad, origen, egreso.getId(), usuario.getId());
        if (!resultado.exito()) {
            throw new ResourceConflictException(resultado.mensaje() != null ? resultado.mensaje() : "No se pudo revertir el stock");
        }
        egreso.setEstado(Egreso.EstadoEgreso.CANCELADO);
        egreso.setActivo(false);
        return egresoRepository.save(egreso);
    }

    @Transactional
    public Egreso registrarEgresoManual(SolicitudLineaLibro solicitud) {
        if (solicitud.getTipo() != null && (solicitud.getTipo().equals("INSUMO") || solicitud.getTipo().equals("INSUMOS"))) {
            throw new IllegalArgumentException("La compra de insumo se registra con reponer, no como egreso suelto");
        }
        if (solicitud.getMonto() == null || solicitud.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        if (solicitud.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        ModuloSistema modulo = exigirModuloHabilitado(solicitud.getModulo());
        Egreso egreso = new Egreso();
        egreso.setConcepto(solicitud.getConcepto());
        egreso.setDescripcion(solicitud.getDescripcion());
        egreso.setTipo(parsearTipoEgreso(solicitud.getTipo()));
        egreso.setCantidad(solicitud.getCantidad());
        egreso.setUnidadMedida(solicitud.getUnidadMedida());
        egreso.setCostoTotal(solicitud.getMonto());
        egreso.setFecha(solicitud.getFecha());
        egreso.setProveedor(solicitud.getProveedor());
        egreso.setObservaciones(solicitud.getObservaciones());
        egreso.setEstado(Egreso.EstadoEgreso.REGISTRADO);
        egreso.setUser(usuarioActual());
        if (solicitud.getLoteId() != null && modulo == ModuloSistema.CULTIVOS) {
            Plot lote = plotRepository.findById(solicitud.getLoteId())
                    .orElseThrow(() -> new IllegalArgumentException("El lote no existe"));
            egreso.setLote(lote);
        }
        completarLinea(egreso, seguridad.obtenerEmpresaIdActual(), modulo, ImputacionEconomica.MODULO, REF_MANUAL, null);
        return egresoRepository.save(egreso);
    }

    @Transactional
    public Ingreso registrarIngresoManual(SolicitudLineaLibro solicitud) {
        if (solicitud.getMonto() == null || solicitud.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        if (solicitud.getFecha() == null || solicitud.getConcepto() == null || solicitud.getConcepto().isBlank()) {
            throw new IllegalArgumentException("Concepto y fecha son obligatorios");
        }
        ModuloSistema modulo = exigirModuloHabilitado(solicitud.getModulo());
        Ingreso ingreso = new Ingreso();
        ingreso.setConcepto(solicitud.getConcepto());
        ingreso.setDescripcion(solicitud.getDescripcion());
        ingreso.setTipoIngreso(parsearTipoIngreso(solicitud.getTipo()));
        ingreso.setMonto(solicitud.getMonto());
        ingreso.setCantidad(solicitud.getCantidad());
        ingreso.setUnidadMedida(solicitud.getUnidadMedida());
        ingreso.setFecha(solicitud.getFecha());
        ingreso.setClienteComprador(solicitud.getClienteComprador());
        ingreso.setObservaciones(solicitud.getObservaciones());
        ingreso.setEstado(Ingreso.EstadoIngreso.REGISTRADO);
        ingreso.setUsuario(usuarioActual());
        if (solicitud.getLoteId() != null && modulo == ModuloSistema.CULTIVOS) {
            Plot lote = plotRepository.findById(solicitud.getLoteId())
                    .orElseThrow(() -> new IllegalArgumentException("El lote no existe"));
            ingreso.setLote(lote);
        }
        completarLinea(ingreso, seguridad.obtenerEmpresaIdActual(), modulo, ImputacionEconomica.MODULO, REF_MANUAL, null);
        return ingresoRepository.save(ingreso);
    }

    /**
     * Crea el ingreso de una venta si el total es mayor que cero y esa venta todavía no tiene uno activo.
     * Devuelve el id existente o el nuevo. Null si el total no justifica ingreso.
     */
    @Transactional
    public Long asegurarIngresoDeVenta(Long ingresoIdExistente, ModuloSistema modulo, Ingreso.TipoIngreso tipo,
                                       BigDecimal monto, LocalDate fecha, String concepto, String cliente,
                                       Long campanaId, String tipoReferencia, Long referenciaHechoId) {
        if (ingresoIdExistente != null) {
            return ingresoIdExistente;
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0 || referenciaHechoId == null) {
            return null;
        }
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        return ingresoRepository.buscarHechoActivo(empresaId, tipoReferencia, referenciaHechoId)
                .map(Ingreso::getId)
                .orElseGet(() -> {
                    Ingreso ingreso = new Ingreso();
                    ingreso.setConcepto(concepto);
                    ingreso.setTipoIngreso(tipo);
                    ingreso.setMonto(monto);
                    ingreso.setFecha(fecha != null ? fecha : LocalDate.now());
                    ingreso.setClienteComprador(cliente);
                    ingreso.setEstado(Ingreso.EstadoIngreso.REGISTRADO);
                    ingreso.setUsuario(usuarioActual());
                    ingreso.setCampanaId(campanaId);
                    completarLinea(ingreso, empresaId, modulo, ImputacionEconomica.MODULO, tipoReferencia, referenciaHechoId);
                    return ingresoRepository.save(ingreso).getId();
                });
    }

    @Transactional
    public Ingreso anularIngreso(Long ingresoId) {
        Long empresaId = seguridad.obtenerEmpresaIdActual();
        Ingreso ingreso = ingresoRepository.findById(ingresoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingreso no encontrado"));
        if (ingreso.getEmpresaId() != null && !empresaId.equals(ingreso.getEmpresaId())) {
            throw new ResourceNotFoundException("Ingreso no encontrado");
        }
        ingreso.setEstado(Ingreso.EstadoIngreso.CANCELADO);
        ingreso.setActivo(false);
        return ingresoRepository.save(ingreso);
    }

    private void completarLinea(Egreso egreso, Long empresaId, ModuloSistema modulo,
                                ImputacionEconomica imputacion, String tipoReferencia, Long referenciaHechoId) {
        egreso.setEmpresaId(empresaId);
        egreso.setModuloOrigen(modulo.getCodigo());
        egreso.setImputacion(imputacion);
        egreso.setTipoReferencia(tipoReferencia);
        egreso.setReferenciaHechoId(referenciaHechoId);
        if (egreso.getCampanaId() == null) {
            egreso.setCampanaId(CampanaRequestContext.getCampanaId());
        }
    }

    private void completarLinea(Ingreso ingreso, Long empresaId, ModuloSistema modulo,
                                ImputacionEconomica imputacion, String tipoReferencia, Long referenciaHechoId) {
        ingreso.setEmpresaId(empresaId);
        ingreso.setModuloOrigen(modulo.getCodigo());
        ingreso.setImputacion(imputacion);
        ingreso.setTipoReferencia(tipoReferencia);
        ingreso.setReferenciaHechoId(referenciaHechoId);
        if (ingreso.getCampanaId() == null) {
            ingreso.setCampanaId(CampanaRequestContext.getCampanaId());
        }
    }

    private User usuarioActual() {
        return userService.findById(seguridad.obtenerUsuarioIdActual())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
    }

    private ModuloSistema exigirModuloHabilitado(String codigo) {
        ModuloSistema modulo = exigirModulo(codigo);
        if (!companyModuleService.hasModuleEnabled(seguridad.obtenerEmpresaIdActual(), modulo.getCodigo())) {
            throw new IllegalArgumentException("El módulo no está habilitado en la empresa");
        }
        return modulo;
    }

    private static ModuloSistema exigirModulo(String codigo) {
        return ModuloSistema.desdeCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Módulo desconocido"));
    }

    private static boolean moduloEnBlanco(String modulo) {
        return modulo == null || modulo.isBlank();
    }

    private static Egreso.TipoEgreso parsearTipoEgreso(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return Egreso.TipoEgreso.OTROS;
        }
        try {
            return Egreso.TipoEgreso.valueOf(tipo.trim());
        } catch (IllegalArgumentException e) {
            return Egreso.TipoEgreso.OTROS;
        }
    }

    private static Ingreso.TipoIngreso parsearTipoIngreso(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return Ingreso.TipoIngreso.OTROS_INGRESOS;
        }
        try {
            return Ingreso.TipoIngreso.valueOf(tipo.trim());
        } catch (IllegalArgumentException e) {
            return Ingreso.TipoIngreso.OTROS_INGRESOS;
        }
    }

    public static InventoryOrigin origenDe(ModuloSistema modulo) {
        return switch (modulo) {
            case CULTIVOS -> InventoryOrigin.CULTIVOS;
            case PORCINOS -> InventoryOrigin.PORCINOS;
            case FEEDLOT -> InventoryOrigin.FEEDLOT;
            case LECHERIA -> InventoryOrigin.LECHERIA;
            case AVICOLA_CRIANZA -> InventoryOrigin.AVICOLA_CRIANZA;
            case AVICOLA_HUEVOS -> InventoryOrigin.AVICOLA_HUEVOS;
            case AVICOLA_PONEDORAS -> InventoryOrigin.AVICOLA_PONEDORAS;
        };
    }
}
