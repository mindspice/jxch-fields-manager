package io.mindspice.jxch.fields.data.structures;

import io.mindspice.jxch.fields.data.metrics.chia.SignagePointMetrics;

import java.time.LocalDateTime;


public class SignagePointInfo {
    private final int index;
    private final LocalDateTime dateTime;
    private SignagePointMetrics signagePointMetrics;

    public SignagePointInfo(int index, LocalDateTime dateTime) {
        this.index = index;
        this.dateTime = dateTime;
    }

    public SignagePointMetrics getAsMetrics(int eligiblePlots, int proofsFound, float lookupTime, int totalPlots) {
        return new SignagePointMetrics(
                index,
                dateTime,
                eligiblePlots,
                proofsFound,
                lookupTime,
                totalPlots
        );
    }


    public int getIndex() {
        return index;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

}
