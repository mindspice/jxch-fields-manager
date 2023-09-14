package io.mindspice.jxch.fields.data.chia;

import io.mindspice.jxch.fields.data.metrics.chia.FilterMetrics;

import java.time.LocalDateTime;
import java.util.ArrayList;


public class SignagePointInfo {
    private final int index;
    private final LocalDateTime dateTime;
    private FilterMetrics filterMetrics;

    public SignagePointInfo(int index, LocalDateTime dateTime) {
        this.index = index;
        this.dateTime = dateTime;
    }

    public FilterMetrics getAsMetrics(int eligiblePlots, int proofsFound, float lookupTime, int totalPlots) {
        filterMetrics = filterMetrics;
        return new FilterMetrics(
                index,
                dateTime,
                eligiblePlots,
                proofsFound,
                lookupTime,
                totalPlots
        );
    }

//    public void addBlock(BlockInfo block) {
//        if (blocks == null) { blocks = new ArrayList<>(3); }
//        blocks.add(block);
//    }

    public int getIndex() {
        return index;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


}
