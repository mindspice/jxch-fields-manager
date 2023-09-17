package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;


public record SignagePointMetrics(
        int index,
        LocalDateTime dateTime,
        int eligiblePlots,
        int proofsFound,
        double lookupTime,
        int totalPlots,
        long epochMilli
) implements ClientMsg {
    public SignagePointMetrics(int index, LocalDateTime dateTime, int eligiblePlots, int proofsFound,
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

    public String getDateTime() { return dateTime.toString(); }
}
