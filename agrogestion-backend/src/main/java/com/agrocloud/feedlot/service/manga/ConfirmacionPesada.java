package com.agrocloud.feedlot.service.manga;

import com.agrocloud.feedlot.model.entity.*;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;
import com.agrocloud.feedlot.repository.FeedlotPesadaAnimalRepository;
import com.agrocloud.feedlot.repository.FeedlotPesadaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ConfirmacionPesada implements EstrategiaConfirmacionManga {

    private final FeedlotPesadaAnimalRepository pesadaAnimalRepository;
    private final FeedlotPesadaRepository pesadaRepository;

    public ConfirmacionPesada(
            FeedlotPesadaAnimalRepository pesadaAnimalRepository,
            FeedlotPesadaRepository pesadaRepository) {
        this.pesadaAnimalRepository = pesadaAnimalRepository;
        this.pesadaRepository = pesadaRepository;
    }

    @Override
    public FeedlotTipoSesionManga tipo() {
        return FeedlotTipoSesionManga.PESADA;
    }

    @Override
    public void confirmar(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filasOk, ContextoConfirmacionManga contexto) {
        BigDecimal suma = BigDecimal.ZERO;
        for (FeedlotSesionMangaFila fila : filasOk) {
            FeedlotPesadaAnimal p = new FeedlotPesadaAnimal();
            p.setEmpresaId(sesion.getEmpresaId());
            p.setAnimal(fila.getAnimal());
            p.setLote(sesion.getLote());
            p.setSesion(sesion);
            p.setFecha(sesion.getFecha());
            p.setPesoKg(fila.getPesoKg());
            pesadaAnimalRepository.save(p);
            suma = suma.add(fila.getPesoKg());
        }
        FeedlotPesada lotePesada = new FeedlotPesada();
        lotePesada.setLote(sesion.getLote());
        lotePesada.setEmpresaId(sesion.getEmpresaId());
        lotePesada.setFecha(sesion.getFecha());
        lotePesada.setPesoPromedioKg(suma.divide(BigDecimal.valueOf(filasOk.size()), 2, RoundingMode.HALF_UP));
        lotePesada.setCabezasMuestreadas(filasOk.size());
        lotePesada.setSesionId(sesion.getId());
        lotePesada.setObservaciones("Promedio de sesión de manga " + sesion.getId());
        pesadaRepository.save(lotePesada);
    }

    @Override
    public void revertir(FeedlotSesionManga sesion) {
        pesadaAnimalRepository.eliminarPorSesionId(sesion.getId());
        pesadaRepository.eliminarPorSesionId(sesion.getId());
    }
}
