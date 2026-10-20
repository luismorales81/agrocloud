package com.agrocloud.feedlot.service.manga;

import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.entity.*;
import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotSubtipoBaja;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.model.enums.FeedlotTipoVenta;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotMotivoMuerteRepository;
import com.agrocloud.feedlot.repository.FeedlotMuerteRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.agrocloud.feedlot.repository.FeedlotVentaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfirmacionBaja implements EstrategiaConfirmacionManga {

    private final FeedlotAnimalRepository animalRepository;
    private final FeedlotMuerteRepository muerteRepository;
    private final FeedlotVentaRepository ventaRepository;
    private final FeedlotMotivoMuerteRepository motivoMuerteRepository;
    private final FeedlotSesionMangaFilaRepository filaRepository;
    private final ServicioFeedlotLotes servicioLotes;

    public ConfirmacionBaja(
            FeedlotAnimalRepository animalRepository,
            FeedlotMuerteRepository muerteRepository,
            FeedlotVentaRepository ventaRepository,
            FeedlotMotivoMuerteRepository motivoMuerteRepository,
            FeedlotSesionMangaFilaRepository filaRepository,
            ServicioFeedlotLotes servicioLotes) {
        this.animalRepository = animalRepository;
        this.muerteRepository = muerteRepository;
        this.ventaRepository = ventaRepository;
        this.motivoMuerteRepository = motivoMuerteRepository;
        this.filaRepository = filaRepository;
        this.servicioLotes = servicioLotes;
    }

    @Override
    public FeedlotTipoSesionManga tipo() {
        return FeedlotTipoSesionManga.BAJA;
    }

    @Override
    public void confirmar(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filasOk, ContextoConfirmacionManga contexto) {
        if (sesion.getSubtipoBaja() == null) {
            throw new IllegalArgumentException("El subtipo de baja es obligatorio");
        }
        FeedlotSubtipoBaja subtipo = sesion.getSubtipoBaja();
        FeedlotEstadoAnimal estado = switch (subtipo) {
            case MUERTE -> FeedlotEstadoAnimal.MUERTO;
            case FAENA -> FeedlotEstadoAnimal.FAENADO;
            case VENTA_EN_PIE -> FeedlotEstadoAnimal.VENDIDO;
            case DESCARTE -> FeedlotEstadoAnimal.DESCARTADO;
        };
        for (FeedlotSesionMangaFila fila : filasOk) {
            FeedlotAnimal animal = fila.getAnimal();
            animal.setEstado(estado);
            animal.setFechaBaja(sesion.getFecha());
            animalRepository.save(animal);
        }
        FeedlotLote lote = sesion.getLote();
        int n = filasOk.size();
        if (subtipo == FeedlotSubtipoBaja.MUERTE) {
            FeedlotMuerte m = new FeedlotMuerte();
            m.setLote(lote);
            m.setEmpresaId(sesion.getEmpresaId());
            m.setFecha(sesion.getFecha());
            m.setCabezas(n);
            m.setSesionId(sesion.getId());
            if (sesion.getMotivoMuerteId() != null) {
                m.setMotivo(motivoMuerteRepository.buscarPorIdYEmpresaId(sesion.getMotivoMuerteId(), sesion.getEmpresaId())
                        .orElse(null));
            }
            muerteRepository.save(m);
        } else {
            FeedlotVenta v = new FeedlotVenta();
            v.setLote(lote);
            v.setEmpresaId(sesion.getEmpresaId());
            v.setCampanaId(lote.getCampanaId());
            v.setFecha(sesion.getFecha());
            v.setTipo(switch (subtipo) {
                case FAENA -> FeedlotTipoVenta.FAENA;
                case DESCARTE -> FeedlotTipoVenta.DESCARTE;
                default -> FeedlotTipoVenta.VENTA_EN_PIE;
            });
            v.setCabezas(n);
            v.setPrecioKg(sesion.getPrecioKg());
            v.setSesionId(sesion.getId());
            ventaRepository.save(v);
        }
        int disponible = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        int nuevo = Math.max(0, disponible - n);
        lote.setCabezasActuales(nuevo);
        if (nuevo <= 0) {
            servicioLotes.cerrarLoteYLiberarCorral(lote);
        } else {
            servicioLotes.guardarLote(lote);
        }
    }

    @Override
    public void revertir(FeedlotSesionManga sesion) {
        restaurarAnimalesYPlantel(sesion, filaRepository.listarPorSesionId(sesion.getId()));
    }

    private void restaurarAnimalesYPlantel(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filas) {
        for (FeedlotSesionMangaFila fila : filas) {
            if (fila.getAnimal() == null) {
                continue;
            }
            FeedlotAnimal animal = fila.getAnimal();
            animal.setEstado(FeedlotEstadoAnimal.ACTIVO);
            animal.setFechaBaja(null);
            animalRepository.save(animal);
        }
        List<FeedlotMuerte> muertes = muerteRepository.listarPorSesionId(sesion.getId());
        List<FeedlotVenta> ventas = ventaRepository.listarPorSesionId(sesion.getId());
        int cabezas = muertes.stream().mapToInt(m -> m.getCabezas() != null ? m.getCabezas() : 0).sum()
                + ventas.stream().mapToInt(v -> v.getCabezas() != null ? v.getCabezas() : 0).sum();
        muerteRepository.eliminarPorSesionId(sesion.getId());
        ventaRepository.eliminarPorSesionId(sesion.getId());
        FeedlotLote lote = sesion.getLote();
        int actual = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        lote.setCabezasActuales(actual + cabezas);
        if (lote.getEstado() != null && lote.getEstado().name().equals("CERRADO")
                && lote.getFechaCierre() != null && lote.getFechaCierre().equals(sesion.getFecha())) {
            servicioLotes.reabrirLoteYOcuparCorral(lote);
        } else {
            servicioLotes.guardarLote(lote);
        }
    }
}
