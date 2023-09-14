package io.mindspice.jxch.fields.data.metrics.chia;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;


public record FilterMetrics(
        int index,
        LocalDateTime dateTime,
        int eligiblePlots,
        int proofsFound,
        double lookupTime,
        int totalPlots,
        long epochMilli
) {
    public FilterMetrics(int index, LocalDateTime dateTime, int eligiblePlots, int proofsFound,
            float lookupTime, int totalPlots) {
        this(
                index,
                dateTime,
                eligiblePlots,
                proofsFound,
                lookupTime,
                totalPlots,
                dateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli()
        );
    }
}
