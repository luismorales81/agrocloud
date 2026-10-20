package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotSubtipoBaja;

import java.util.ArrayList;
import java.util.List;

public class FeedlotGenerarBajaFaltantesSolicitud {

    private List<Long> animalIds = new ArrayList<>();
    private FeedlotSubtipoBaja subtipoBaja;

    public List<Long> getAnimalIds() { return animalIds; }
    public void setAnimalIds(List<Long> animalIds) { this.animalIds = animalIds; }
    public FeedlotSubtipoBaja getSubtipoBaja() { return subtipoBaja; }
    public void setSubtipoBaja(FeedlotSubtipoBaja subtipoBaja) { this.subtipoBaja = subtipoBaja; }
}
