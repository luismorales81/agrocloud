package com.agrocloud.core.inventory.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.cultivos.domain.Cultivo;
import com.agrocloud.cultivos.infrastructure.CultivoRepository;

import com.agrocloud.porcinos.domain.RecetaAlimentacionPorEtapa;
import com.agrocloud.porcinos.infrastructure.RecetaAlimentacionPorEtapaRepository;
import com.agrocloud.core.inventory.domain.ComponenteInsumoCompuesto;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.core.inventory.domain.InsumoCompuesto;
import com.agrocloud.core.inventory.infrastructure.ComponenteInsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoCompuestoRepository;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar insumos compuestos (recetas/formulas de alimento)
 */
@Service("insumoCompuestoServiceInventario")
public class InsumoCompuestoService {

    @Autowired
    @Qualifier("insumoCompuestoRepositoryInventario")
    private InsumoCompuestoRepository insumoCompuestoRepository;

    @Autowired
    @Qualifier("componenteInsumoCompuestoRepositoryInventario")
    private ComponenteInsumoCompuestoRepository componenteRepository;

    @Autowired
    private RecetaAlimentacionPorEtapaRepository recetaEtapaRepository;

    @Autowired
    @Qualifier("insumoRepositoryInventario")
    private InsumoRepository insumoRepository;

    @Autowired
    @Qualifier("cultivoRepositoryCultivos")
    private CultivoRepository cultivoRepository;

    @Autowired
    @Qualifier("empresaContextServiceCore")
    private EmpresaContextService empresaContextService;

    /**
     * Crear o actualizar un insumo compuesto
     */
    @Transactional
    public InsumoCompuesto guardarInsumoCompuesto(InsumoCompuesto insumoCompuesto, List<ComponenteInsumoCompuesto> componentes, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        Empresa empresa = empresaOpt.get();
        insumoCompuesto.setEmpresa(empresa);
        insumoCompuesto.setUsuario(user);

        // Validar componentes
        validarComponentes(componentes);

        // Guardar insumo compuesto
        InsumoCompuesto guardado = insumoCompuestoRepository.save(insumoCompuesto);

        // Eliminar componentes antiguos si es actualización
        if (insumoCompuesto.getId() != null) {
            componenteRepository.deleteByInsumoCompuesto(guardado);
        }

        // Guardar componentes y asignar referencias desde IDs
        for (ComponenteInsumoCompuesto componente : componentes) {
            componente.setInsumoCompuesto(guardado);
            
            // Asignar insumo, cultivo o insumo compuesto según el tipo
            if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO && 
                componente.getInsumo() != null && componente.getInsumo().getId() != null) {
                Optional<Insumo> insumoOpt = insumoRepository.findById(componente.getInsumo().getId());
                insumoOpt.ifPresent(componente::setInsumo);
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.GRANO_PROPIO && 
                       componente.getCultivo() != null && componente.getCultivo().getId() != null) {
                Optional<Cultivo> cultivoOpt = cultivoRepository.findById(componente.getCultivo().getId());
                cultivoOpt.ifPresent(componente::setCultivo);
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO_COMPUESTO && 
                       componente.getInsumoCompuestoPadre() != null && 
                       componente.getInsumoCompuestoPadre().getId() != null) {
                Optional<InsumoCompuesto> insumoCompOpt = insumoCompuestoRepository.findById(
                    componente.getInsumoCompuestoPadre().getId());
                insumoCompOpt.ifPresent(componente::setInsumoCompuestoPadre);
            }
            
            componenteRepository.save(componente);
        }

        // Recalcular costo unitario
        recalcularCostoUnitario(guardado);

        return insumoCompuestoRepository.save(guardado);
    }

    /**
     * Validar que los componentes sean válidos
     */
    private void validarComponentes(List<ComponenteInsumoCompuesto> componentes) {
        if (componentes == null || componentes.isEmpty()) {
            throw new IllegalArgumentException("Un insumo compuesto debe tener al menos un componente");
        }

        BigDecimal sumaPorcentajes = BigDecimal.ZERO;
        boolean tieneCantidadFija = false;

        for (ComponenteInsumoCompuesto componente : componentes) {
            // Validar que tenga un origen (insumo, cultivo o insumo compuesto)
            if (componente.getInsumo() == null && componente.getCultivo() == null && 
                componente.getInsumoCompuestoPadre() == null) {
                throw new IllegalArgumentException("Cada componente debe tener un insumo, cultivo o insumo compuesto asociado");
            }

            // Validar porcentaje o cantidad fija
            if (componente.getPorcentaje() != null) {
                if (componente.getPorcentaje().compareTo(BigDecimal.ZERO) <= 0 || 
                    componente.getPorcentaje().compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
                }
                sumaPorcentajes = sumaPorcentajes.add(componente.getPorcentaje());
            }

            if (componente.getCantidadFija() != null) {
                tieneCantidadFija = true;
            }
        }

        // Si usa porcentajes, deben sumar aproximadamente 100% (tolerancia del 0.1%)
        if (!tieneCantidadFija && sumaPorcentajes.compareTo(BigDecimal.valueOf(99.9)) < 0) {
            throw new IllegalArgumentException(
                String.format("La suma de porcentajes debe ser aproximadamente 100%%. Actual: %.2f%%", sumaPorcentajes));
        }
        if (!tieneCantidadFija && sumaPorcentajes.compareTo(BigDecimal.valueOf(100.1)) > 0) {
            throw new IllegalArgumentException(
                String.format("La suma de porcentajes no puede exceder 100%%. Actual: %.2f%%", sumaPorcentajes));
        }
    }

    /**
     * Recalcular el costo unitario del insumo compuesto basado en sus componentes
     */
    @Transactional
    public void recalcularCostoUnitario(InsumoCompuesto insumoCompuesto) {
        // Asegurar que el insumo compuesto esté actualizado desde la BD
        Optional<InsumoCompuesto> insumoOpt = insumoCompuestoRepository.findById(insumoCompuesto.getId());
        if (insumoOpt.isEmpty()) {
            return;
        }
        InsumoCompuesto insumoActualizado = insumoOpt.get();
        List<ComponenteInsumoCompuesto> componentes = componenteRepository.findByInsumoCompuesto(insumoActualizado);
        
        if (componentes.isEmpty()) {
            insumoCompuesto.setCostoUnitarioCalculado(BigDecimal.ZERO);
            return;
        }

        // Calcular costo para 100 kg de insumo compuesto
        BigDecimal costoTotal100kg = BigDecimal.ZERO;

        for (ComponenteInsumoCompuesto componente : componentes) {
            BigDecimal cantidadNecesaria = BigDecimal.ZERO;
            BigDecimal precioUnitario = BigDecimal.ZERO;

            // Determinar cantidad y precio según tipo de componente
            if (componente.getCantidadFija() != null) {
                cantidadNecesaria = componente.getCantidadFija();
            } else if (componente.getPorcentaje() != null) {
                cantidadNecesaria = componente.getPorcentaje(); // Por cada 100 kg
            }

            // Obtener precio según tipo
            if (componente.getInsumo() != null) {
                precioUnitario = componente.getInsumo().getPrecioUnitario();
            } else if (componente.getCultivo() != null) {
                // Usar precio del cultivo (se puede obtener de DatosEconomicosPorcino o calcular)
                precioUnitario = calcularPrecioCultivo(componente.getCultivo());
            } else if (componente.getInsumoCompuestoPadre() != null) {
                precioUnitario = componente.getInsumoCompuestoPadre().getCostoUnitario();
                if (precioUnitario == null) {
                    // Recalcular recursivamente
                    recalcularCostoUnitario(componente.getInsumoCompuestoPadre());
                    precioUnitario = componente.getInsumoCompuestoPadre().getCostoUnitario();
                }
            }

            if (precioUnitario != null && cantidadNecesaria.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal costoComponente = cantidadNecesaria.multiply(precioUnitario)
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                costoTotal100kg = costoTotal100kg.add(costoComponente);
            }
        }

        // Aplicar rendimiento
        BigDecimal costoUnitario = costoTotal100kg.divide(
            insumoActualizado.getRendimiento(), 2, RoundingMode.HALF_UP);

        insumoActualizado.setCostoUnitarioCalculado(costoUnitario);
        insumoCompuestoRepository.save(insumoActualizado);
    }

    /**
     * Calcular precio de un cultivo (grano propio)
     * Por ahora usa un valor por defecto, pero se puede integrar con DatosEconomicosPorcino
     */
    private BigDecimal calcularPrecioCultivo(Cultivo cultivo) {
        // TODO: Integrar con DatosEconomicosPorcino para obtener precio real
        // Por ahora retornar un valor estimado
        return BigDecimal.valueOf(0.30); // $0.30 por kg de maíz/soja
    }

    /**
     * Obtener insumos compuestos por empresa
     */
    @Transactional(readOnly = true)
    public List<InsumoCompuesto> obtenerInsumosCompuestos(User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        // Los componentes ya se cargan automáticamente con EAGER fetch
        List<InsumoCompuesto> insumos = insumoCompuestoRepository.findByEmpresaAndActivoTrue(empresaOpt.get());
        // Poblar información adicional de componentes
        for (InsumoCompuesto insumo : insumos) {
            poblarInformacionComponentes(insumo.getComponentes());
        }
        return insumos;
    }

    /**
     * Obtener insumo compuesto por ID
     */
    @Transactional(readOnly = true)
    public Optional<InsumoCompuesto> obtenerInsumoCompuestoPorId(Long id, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return Optional.empty();
        }
        // Los componentes ya se cargan automáticamente con EAGER fetch
        Optional<InsumoCompuesto> insumoOpt = insumoCompuestoRepository.findByIdAndEmpresaAndActivoTrue(id, empresaOpt.get());
        if (insumoOpt.isPresent()) {
            // Poblar información adicional de componentes
            poblarInformacionComponentes(insumoOpt.get().getComponentes());
        }
        return insumoOpt;
    }

    /**
     * Poblar información adicional de componentes para exposición al frontend
     */
    private void poblarInformacionComponentes(List<ComponenteInsumoCompuesto> componentes) {
        if (componentes == null) {
            return;
        }
        for (ComponenteInsumoCompuesto componente : componentes) {
            // Poblar nombre del componente
            if (componente.getInsumo() != null) {
                componente.setNombreComponente(componente.getInsumo().getNombre());
                componente.setInsumoId(componente.getInsumo().getId());
            } else if (componente.getCultivo() != null) {
                componente.setNombreComponente(componente.getCultivo().getNombre());
                componente.setCultivoId(componente.getCultivo().getId());
            } else if (componente.getInsumoCompuestoPadre() != null) {
                componente.setNombreComponente(componente.getInsumoCompuestoPadre().getNombre());
                componente.setInsumoCompuestoPadreId(componente.getInsumoCompuestoPadre().getId());
            }
        }
    }

    /**
     * Eliminar (desactivar) un insumo compuesto
     */
    @Transactional
    public void eliminarInsumoCompuesto(Long id, User user) {
        Optional<InsumoCompuesto> insumoOpt = obtenerInsumoCompuestoPorId(id, user);
        if (insumoOpt.isEmpty()) {
            throw new IllegalArgumentException("Insumo compuesto no encontrado");
        }
        InsumoCompuesto insumo = insumoOpt.get();
        insumo.setActivo(false);
        insumoCompuestoRepository.save(insumo);
    }

    /**
     * Asociar receta a una etapa de alimentación
     */
    @Transactional
    public RecetaAlimentacionPorEtapa asociarRecetaAEtapa(
            Long insumoCompuestoId, RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa,
            BigDecimal cantidadDiariaPorAnimal, User user) {
        
        Optional<InsumoCompuesto> insumoOpt = obtenerInsumoCompuestoPorId(insumoCompuestoId, user);
        if (insumoOpt.isEmpty()) {
            throw new IllegalArgumentException("Insumo compuesto no encontrado");
        }

        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Si se marca como por defecto, desmarcar otros de la misma etapa
        List<RecetaAlimentacionPorEtapa> existentes = recetaEtapaRepository
            .findByEmpresaAndEtapaAndActivoTrue(empresaOpt.get(), etapa);
        
        // Si ya existe una receta por defecto, desmarcarla
        Optional<RecetaAlimentacionPorEtapa> recetaDefectoExistente = recetaEtapaRepository
            .findByEmpresaAndEtapaAndEsPorDefectoTrueAndActivoTrue(empresaOpt.get(), etapa);
        
        if (recetaDefectoExistente.isPresent()) {
            // Desmarcar la receta existente como por defecto
            RecetaAlimentacionPorEtapa existente = recetaDefectoExistente.get();
            existente.setEsPorDefecto(false);
            recetaEtapaRepository.save(existente);
        }
        
        RecetaAlimentacionPorEtapa receta = new RecetaAlimentacionPorEtapa();
        receta.setInsumoCompuesto(insumoOpt.get());
        receta.setEtapa(etapa);
        receta.setCantidadDiariaPorAnimal(cantidadDiariaPorAnimal);
        receta.setEmpresa(empresaOpt.get());
        receta.setEsPorDefecto(existentes.isEmpty() || recetaDefectoExistente.isEmpty()); // Primera receta o si no hay por defecto

        return recetaEtapaRepository.save(receta);
    }

    /**
     * Obtener recetas por etapa
     */
    @Transactional(readOnly = true)
    public List<RecetaAlimentacionPorEtapa> obtenerRecetasPorEtapa(
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        return recetaEtapaRepository.findByEmpresaAndEtapaAndActivoTrue(empresaOpt.get(), etapa);
    }

    /**
     * Obtener receta por defecto para una etapa
     */
    @Transactional(readOnly = true)
    public Optional<RecetaAlimentacionPorEtapa> obtenerRecetaPorDefecto(
            RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return Optional.empty();
        }
        return recetaEtapaRepository.findByEmpresaAndEtapaAndEsPorDefectoTrueAndActivoTrue(
            empresaOpt.get(), etapa);
    }
}





