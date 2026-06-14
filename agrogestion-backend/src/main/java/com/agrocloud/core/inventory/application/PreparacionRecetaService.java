package com.agrocloud.core.inventory.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.dto.CalcularPreparacionRecetaResponse;
import com.agrocloud.porcinos.application.InventarioPorcinoService;
import com.agrocloud.dto.ComponenteCalculoDTO;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la preparación de recetas (InsumoCompuesto)
 * Maneja el descuento automático de ingredientes del inventario
 */
@Service("preparacionRecetaServiceInventario")
public class PreparacionRecetaService {

    @Autowired
    @Qualifier("insumoCompuestoRepositoryInventario")
        private InsumoCompuestoRepository insumoCompuestoRepository;

    @Autowired
    private InventarioPorcinoService inventarioPorcinoService;

    @Autowired
    @Qualifier("empresaContextServiceCore")
    private EmpresaContextService empresaContextService;

    @Autowired
    @Qualifier("componenteInsumoCompuestoRepositoryInventario")
        private ComponenteInsumoCompuestoRepository componenteRepository;

    @Autowired
    @Qualifier("insumoRepositoryInventario")
        private InsumoRepository insumoRepository;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Preparar una receta (descontar ingredientes del inventario)
     * 
     * @param insumoCompuestoId ID de la receta a preparar
     * @param cantidadPreparada Cantidad de receta preparada (en la unidad de la receta, ej: kg)
     * @param fechaPreparacion Fecha de preparación (opcional, usa fecha actual si es null)
     * @param usuario Usuario que realiza la operación
     * @return La receta preparada
     * @throws IllegalArgumentException Si no hay stock suficiente de algún ingrediente
     */
    @Transactional
    public InsumoCompuesto prepararReceta(Long insumoCompuestoId, BigDecimal cantidadPreparada, 
                                         LocalDate fechaPreparacion, User usuario) {
        if (insumoCompuestoId == null) {
            throw new IllegalArgumentException("El ID de la receta es obligatorio");
        }
        if (cantidadPreparada == null || cantidadPreparada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad preparada debe ser mayor a cero");
        }

        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId());
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Obtener la receta
        Optional<InsumoCompuesto> recetaOpt = insumoCompuestoRepository.findById(insumoCompuestoId);
        if (recetaOpt.isEmpty()) {
            throw new IllegalArgumentException("Receta no encontrada con ID: " + insumoCompuestoId);
        }

        InsumoCompuesto receta = recetaOpt.get();

        // Validar que la receta pertenece a la empresa
        if (!receta.getEmpresa().getId().equals(empresaOpt.get().getId())) {
            throw new IllegalArgumentException("La receta no pertenece a la empresa activa");
        }

        // Validar que la receta está activa
        if (!receta.getActivo()) {
            throw new IllegalArgumentException("La receta está inactiva");
        }

        // Descontar ingredientes del inventario
        inventarioPorcinoService.descontarIngredientesReceta(receta, cantidadPreparada, usuario);

        // Reponer stock del insumo compuesto (receta preparada) vía core.inventory
        // Aplicar rendimiento: si el rendimiento es 0.95, de 100 kg preparados se obtienen 95 kg
        BigDecimal cantidadProducida = cantidadPreparada.multiply(receta.getRendimiento())
            .setScale(2, RoundingMode.HALF_UP);
        Long empresaId = empresaOpt.get().getId();
        Long recetaId = receta.getId();
        InventoryResult r = inventoryService.reponerInsumoCompuesto(empresaId, recetaId, cantidadProducida,
                InventoryOrigin.PORCINOS, null, usuario.getId());
        if (!r.exito()) {
            throw new IllegalArgumentException(r.mensaje() != null ? r.mensaje() : "Error al actualizar stock de la receta");
        }

        InsumoCompuesto recetaActualizada = insumoCompuestoRepository.findById(recetaId)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada tras actualizar stock"));

        return recetaActualizada;
    }

    /**
     * Calcular preparación de receta sin descontar del inventario
     * Muestra cuánto se necesita de cada componente por kg de receta,
     * el stock disponible, y el máximo preparable
     * 
     * @param insumoCompuestoId ID de la receta
     * @param cantidadSolicitada Cantidad a calcular (por defecto 1 kg si es null)
     * @param usuario Usuario que realiza la consulta
     * @return Respuesta con cálculo detallado de componentes
     */
    @Transactional(readOnly = true)
    public CalcularPreparacionRecetaResponse calcularPreparacionReceta(
            Long insumoCompuestoId, BigDecimal cantidadSolicitada, User usuario) {
        if (insumoCompuestoId == null) {
            throw new IllegalArgumentException("El ID de la receta es obligatorio");
        }

        // Si no se especifica cantidad, usar 1 kg por defecto
        if (cantidadSolicitada == null || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            cantidadSolicitada = BigDecimal.ONE;
        }

        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(usuario.getId());
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Obtener la receta con componentes cargados
        Optional<InsumoCompuesto> recetaOpt = insumoCompuestoRepository.findByIdAndEmpresaAndActivoTrue(
            insumoCompuestoId, empresaOpt.get());
        if (recetaOpt.isEmpty()) {
            throw new IllegalArgumentException("Receta no encontrada con ID: " + insumoCompuestoId);
        }

        InsumoCompuesto receta = recetaOpt.get();
        List<ComponenteInsumoCompuesto> componentes = componenteRepository.findByInsumoCompuesto(receta);
        
        if (componentes.isEmpty()) {
            throw new IllegalArgumentException("La receta no tiene ingredientes configurados");
        }

        CalcularPreparacionRecetaResponse response = new CalcularPreparacionRecetaResponse();
        response.setRecetaId(receta.getId());
        response.setRecetaNombre(receta.getNombre());
        response.setUnidadMedida(receta.getUnidadMedida());
        response.setCantidadSolicitada(cantidadSolicitada);

        List<ComponenteCalculoDTO> componentesCalculo = new ArrayList<>();
        BigDecimal maximoPreparableGlobal = null;
        boolean stockSuficienteGlobal = true;

        for (ComponenteInsumoCompuesto componente : componentes) {
            ComponenteCalculoDTO calculo = new ComponenteCalculoDTO();
            
            // Obtener nombre del componente
            String nombreComponente = componente.getNombreComponente();
            calculo.setNombreComponente(nombreComponente);
            calculo.setTipoComponente(componente.getTipoComponente().name());
            calculo.setUnidadMedida(componente.getUnidadMedida() != null ? componente.getUnidadMedida() : "kg");

            // Calcular cantidad por kg de receta
            BigDecimal cantidadPorKgReceta = BigDecimal.ZERO;
            if (componente.getCantidadFija() != null) {
                // Si tiene cantidad fija, se asume que es por 100 kg de receta
                cantidadPorKgReceta = componente.getCantidadFija().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            } else if (componente.getPorcentaje() != null) {
                // Porcentaje: 65% = 0.65 kg por kg de receta
                cantidadPorKgReceta = componente.getPorcentaje().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            }
            calculo.setCantidadPorKgReceta(cantidadPorKgReceta);

            // Calcular cantidad necesaria para la cantidad solicitada
            BigDecimal cantidadNecesaria = cantidadPorKgReceta.multiply(cantidadSolicitada);
            calculo.setCantidadNecesaria(cantidadNecesaria);

            // Obtener stock disponible (solo para insumos directos)
            BigDecimal stockDisponible = BigDecimal.ZERO;
            BigDecimal maximoPreparable = null;
            boolean stockSuficiente = true;
            String mensajeStock = null;

            if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO && 
                componente.getInsumo() != null && componente.getInsumo().getId() != null) {
                Long insumoId = componente.getInsumo().getId();
                Long empresaId = empresaOpt.get().getId();
                BigDecimal stock = inventoryService.consultarStock(empresaId, insumoId);
                if (stock != null) {
                    stockDisponible = stock;
                    Optional<Insumo> insumoOpt = insumoRepository.findById(insumoId);
                    String unidadMedida = insumoOpt.map(Insumo::getUnidadMedida).orElse("kg");
                    if (cantidadPorKgReceta.compareTo(BigDecimal.ZERO) > 0) {
                        maximoPreparable = stockDisponible.divide(cantidadPorKgReceta, 2, RoundingMode.DOWN);
                    }
                    stockSuficiente = stockDisponible.compareTo(cantidadNecesaria) >= 0;
                    if (!stockSuficiente) {
                        mensajeStock = String.format("Stock insuficiente. Disponible: %s %s, Necesario: %s %s",
                            stockDisponible, unidadMedida, cantidadNecesaria, unidadMedida);
                        stockSuficienteGlobal = false;
                    }
                } else {
                    mensajeStock = "Insumo no encontrado";
                    stockSuficiente = false;
                    stockSuficienteGlobal = false;
                }
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.GRANO_PROPIO) {
                // Para granos propios, por ahora no calculamos stock (se maneja en InventarioGrano)
                mensajeStock = "Stock de grano propio no calculado automáticamente";
            } else if (componente.getTipoComponente() == ComponenteInsumoCompuesto.TipoComponente.INSUMO_COMPUESTO) {
                // Para insumos compuestos anidados, por ahora no calculamos recursivamente
                mensajeStock = "Receta anidada - cálculo no implementado";
            }

            calculo.setStockDisponible(stockDisponible);
            calculo.setMaximoPreparable(maximoPreparable);
            calculo.setStockSuficiente(stockSuficiente);
            calculo.setMensajeStock(mensajeStock);

            componentesCalculo.add(calculo);

            // Calcular el máximo preparable global (mínimo de todos los componentes)
            if (maximoPreparable != null) {
                if (maximoPreparableGlobal == null || maximoPreparable.compareTo(maximoPreparableGlobal) < 0) {
                    maximoPreparableGlobal = maximoPreparable;
                }
            }
        }

        response.setComponentes(componentesCalculo);
        response.setMaximoPreparableConStock(maximoPreparableGlobal);
        response.setStockSuficienteGlobal(stockSuficienteGlobal);

        if (stockSuficienteGlobal) {
            response.setMensaje(String.format("Stock suficiente para preparar %s %s de receta. Máximo preparable: %s %s",
                cantidadSolicitada, receta.getUnidadMedida(),
                maximoPreparableGlobal != null ? maximoPreparableGlobal : "N/A", receta.getUnidadMedida()));
        } else {
            response.setMensaje(String.format("Stock insuficiente para preparar %s %s. Verifique los componentes individuales.",
                cantidadSolicitada, receta.getUnidadMedida()));
        }

        return response;
    }
}

