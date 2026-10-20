package com.agrocloud.feedlot.service.manga;

import com.agrocloud.feedlot.model.entity.FeedlotSesionManga;
import com.agrocloud.feedlot.model.entity.FeedlotSesionMangaFila;
import com.agrocloud.feedlot.model.enums.FeedlotTipoSesionManga;

import java.util.List;

public interface EstrategiaConfirmacionManga {

    FeedlotTipoSesionManga tipo();

    void confirmar(FeedlotSesionManga sesion, List<FeedlotSesionMangaFila> filasOk, ContextoConfirmacionManga contexto);

    void revertir(FeedlotSesionManga sesion);
}
