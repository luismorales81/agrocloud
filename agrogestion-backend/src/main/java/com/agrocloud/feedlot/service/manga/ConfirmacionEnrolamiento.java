package com.agrocloud.feedlot.service.manga;

import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.feedlot.model.dto.FeedlotAjustePlantelSolicitud;
import com.agrocloud.feedlot.model.entity.FeedlotAnimal;
import com.agrocloud.feedlot.model.entity.FeedlotLote;
import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotEstadoAnimal;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.repository.FeedlotAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotEventoSanitarioRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotSesionMangaFilaRepository;
import com.agrocloud.feedlot.service.ServicioFeedlotLotes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfirmacionEnrolamiento implements EstrategiaConfirmacionManga {

    private final FeedlotAnimalRepository animalRepository;
    private final FeedlotSesionMangaFilaRepository filaRepository;
    private final FeedlotPesadaAnimalRepository pesadaAnimalRepository;
    private final FeedlotEventoSanitarioRepository eventoSanitarioRepository;
    private final ServicioFeedlotLotes servicioLotes;

    public ConfirmacionEnrolamiento(
            FeedlotAnimalRepository animalRepository,
            FeedlotSesionMangaFilaRepository filaRepository,
            FeedlotPesadaAnimalRepository pesadaAnimalRepository,
            FeedlotEventoSanitarioRepository eventoSanitarioRepository,
            ServicioFeedlotLotes servicioLotes) {
        this.animalRepository = animalRepository;
        this.filaRepository = filaRepository;
        this.pesadaAnimalRepository = pesadaAnimalRepository;
        this.eventoSanitarioRepository = eventoSanitarioRepository;
        this.servicioLotes = servicioLotes;
    }

    @Override
    public FeedlotTipoSesionManga tipo() {
        return FeedlotTipoSesionManga.ENROLAMIENTO;
    }

    @Override
    public void confirmar(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filasOk, ContextoConfirmacionManga contexto) {
        enrolarEnLote(sesion, sesion.getLote(), filasOk, contexto, true);
    }

    public void enrolarEnLote(
            FeedlotSesionManga sesion,
            FeedlotLote lote,
            List<FeedlotSesionMangaFila> filasOk,
            ContextoConfirmacionManga contexto,
            boolean exigirAjusteSiExcede) {
        for (FeedlotSesionMangaFila fila : filasOk) {
            if (fila.getAnimal() != null) {
                continue;
            }
            FeedlotAnimal animal = new FeedlotAnimal();
            animal.setEmpresaId(sesion.getEmpresaId());
            animal.setLote(lote);
            animal.setEid(fila.getEidNormalizado());
            animal.setCaravanaVisual(vacioANull(fila.getCaravanaVisual()));
            animal.setFechaAlta(sesion.getFecha());
            animal.setEstado(FeedlotEstadoAnimal.ACTIVO);
            animal.setPesoIngresoKg(fila.getPesoKg());
            animal = animalRepository.save(animal);
            fila.setAnimal(animal);
            filaRepository.save(fila);
        }
        long activos = animalRepository.contarPorLoteEstado(lote.getId(), sesion.getEmpresaId(), FeedlotEstadoAnimal.ACTIVO);
        int cabezas = lote.getCabezasActuales() != null ? lote.getCabezasActuales() : 0;
        if (exigirAjusteSiExcede && activos > cabezas) {
            boolean ajustar = Boolean.TRUE.equals(sesion.getAjustarPlantel());
            if (!ajustar) {
                throw new ResourceConflictException(
                        "El enrolamiento supera el plantel actual (" + activos + " > " + cabezas
                                + "). Confirmá el ajuste de plantel.");
            }
            FeedlotAjustePlantelSolicitud ajuste = new FeedlotAjustePlantelSolicitud();
            ajuste.setFecha(sesion.getFecha());
            ajuste.setCabezasDespues((int) activos);
            ajuste.setMotivo("Ajuste por enrolamiento de sesión de manga " + sesion.getId());
            servicioLotes.registrarAjustePlantel(lote.getId(), ajuste, contexto.getUsuario());
        }
    }

    @Override
    public void revertir(FeedlotSesionManga sesion) {
        List<FeedlotSesionMangaFila> filas = filaRepository.listarPorSesionId(sesion.getId());
        for (FeedlotSesionMangaFila fila : filas) {
            FeedlotAnimal animal = fila.getAnimal();
            if (animal == null) {
                continue;
            }
            if (pesadaAnimalRepository.listarPorAnimalId(animal.getId()).stream()
                    .anyMatch(p -> p.getSesion() == null || !sesion.getId().equals(p.getSesion().getId()))) {
                throw new ResourceConflictException("No se puede anular: el animal ya tiene pesadas posteriores");
            }
            if (!eventoSanitarioRepository.listarPorAnimalId(animal.getId()).isEmpty()) {
                throw new ResourceConflictException("No se puede anular: el animal ya tiene eventos sanitarios");
            }
        }
        for (FeedlotSesionMangaFila fila : filas) {
            FeedlotAnimal animal = fila.getAnimal();
            if (animal == null) {
                continue;
            }
            fila.setAnimal(null);
            filaRepository.save(fila);
            animalRepository.delete(animal);
        }
    }

    private static String vacioANull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
