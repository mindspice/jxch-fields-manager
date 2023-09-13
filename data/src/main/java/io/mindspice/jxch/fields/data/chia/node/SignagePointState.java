package io.mindspice.jxch.fields.data.chia.node;

import java.time.LocalDateTime;


public class SignagePointState {
    private final int index;
    private final LocalDateTime dateTime;
    private FilterMetrics filterMetrics;

    public SignagePointState(int index, LocalDateTime dateTime) {
        this.index = index;
        this.dateTime = dateTime;
    }

    public FilterMetrics getAsMetrics(int eligiblePlots, int proofsFound, float lookupTime, int totalPlots) {
        return new FilterMetrics(
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
