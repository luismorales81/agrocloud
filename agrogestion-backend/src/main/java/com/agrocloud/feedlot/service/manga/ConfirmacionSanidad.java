package com.agrocloud.feedlot.service.manga;

import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.entity.FeedlotEventoSanitario;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotOrigenInsumo;
import com.agrocloud.feedlot.model.enums.FeedlotTipoEventoSanitario;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ConfirmacionSanidad implements EstrategiaConfirmacionManga {

    private final FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    private final InventoryService inventoryService;

    public ConfirmacionSanidad(
            FeedlotEventoSanitarioRepository eventoSanitarioRepository,
            InventoryService inventoryService) {
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public FeedlotTipoSesionManga tipo() {
        return FeedlotTipoSesionManga.SANIDAD;
    }

    @Override
    public void confirmar(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filasOk, ContextoConfirmacionManga contexto) {
        if (sesion.getTipoEvento() == null) {
            throw new IllegalArgumentException("El tipo de evento sanitario es obligatorio");
        }
        if (sesion.getAplicador() == null) {
            throw new IllegalArgumentException("El aplicador es obligatorio");
        }
        FeedlotTipoEventoSanitario tipo = sesion.getTipoEvento();
        if ((tipo == FeedlotTipoEventoSanitario.VACUNA || tipo == FeedlotTipoEventoSanitario.TRATAMIENTO)
                && sesion.getOrigenInsumo() == null) {
            throw new IllegalArgumentException("El origen del insumo es obligatorio para vacuna o tratamiento");
        }
        FeedlotOrigenInsumo origen = sesion.getOrigenInsumo() != null
                ? sesion.getOrigenInsumo()
                : FeedlotOrigenInsumo.SIN_INSUMO;

        if (origen == FeedlotOrigenInsumo.STOCK_EMPRESA) {
            if (sesion.getInsumoId() == null) {
                throw new IllegalArgumentException("El insumo es obligatorio cuando se usa stock de la empresa");
            }
            BigDecimal cantidad = sesion.getCantidadTotal();
            if (cantidad == null && sesion.getDosisPorAnimal() != null) {
                cantidad = sesion.getDosisPorAnimal().multiply(BigDecimal.valueOf(filasOk.size()));
                sesion.setCantidadTotal(cantidad);
            }
            if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad a descontar del stock es obligatoria");
            }
            Long empresaId = sesion.getEmpresaId();
            if (!inventoryService.hayStockSuficiente(empresaId, sesion.getInsumoId(), cantidad)) {
                throw new ResourceConflictException("Stock insuficiente para confirmar la sesión de sanidad");
            }
            Long usuarioId = contexto.getUsuario() != null ? contexto.getUsuario().getId() : null;
            InventoryResult res = inventoryService.consumir(
                    empresaId, sesion.getInsumoId(), cantidad, InventoryOrigin.FEEDLOT, sesion.getId(), usuarioId);
            if (!res.exito()) {
                throw new ResourceConflictException(res.mensaje() != null ? res.mensaje() : "Error de inventario");
            }
            sesion.setMovimientoInventarioRef(sesion.getId());
        }

        for (FeedlotSesionMangaFila fila : filasOk) {
            FeedlotEventoSanitario e = new FeedlotEventoSanitario();
            e.setLote(sesion.getLote());
            e.setEmpresaId(sesion.getEmpresaId());
            e.setFecha(sesion.getFecha());
            e.setTipo(sesion.getTipoEvento());
            e.setDescripcion(sesion.getDescripcion());
            e.setInsumoId(origen == FeedlotOrigenInsumo.SIN_INSUMO ? null : sesion.getInsumoId());
            e.setDiasRetiro(sesion.getDiasRetiro());
            e.setAnimalId(fila.getAnimal().getId());
            e.setSesionId(sesion.getId());
            e.setAplicador(sesion.getAplicador());
            e.setOrigenInsumo(origen);
            eventoSanitarioRepository.save(e);
        }
    }

    @Override
    public void revertir(FeedlotSesionManga sesion) {
        if (sesion.getMovimientoInventarioRef() != null && sesion.getInsumoId() != null
                && sesion.getCantidadTotal() != null) {
            Long usuarioId = sesion.getUsuarioId();
            InventoryResult res = inventoryService.reponer(
                    sesion.getEmpresaId(),
                    sesion.getInsumoId(),
                    sesion.getCantidadTotal(),
                    InventoryOrigin.FEEDLOT,
                    sesion.getId(),
                    usuarioId);
            if (!res.exito()) {
                throw new ResourceConflictException(res.mensaje() != null ? res.mensaje() : "No se pudo reponer el stock");
            }
            sesion.setMovimientoInventarioRef(null);
        }
        eventoSanitarioRepository.eliminarPorSesionId(sesion.getId());
    }
}
