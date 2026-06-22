package com.agrocloud.feedlot.model.dto;

import com.agrocloud.feedlot.model.enums.FeedlotMetodoCloseout;

public class FeedlotConfiguracionCloseoutSolicitud {

    private FeedlotMetodoCloseout metodoCloseout;

    public FeedlotMetodoCloseout getMetodoCloseout() {
        return metodoCloseout;
    }

    public void setMetodoCloseout(FeedlotMetodoCloseout metodoCloseout) {
        this.metodoCloseout = metodoCloseout;
    }
}
