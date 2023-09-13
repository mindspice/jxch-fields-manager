package io.mindspice.jxch.fields.data.chia.node;

import java.time.LocalDateTime;


public record FilterMetrics(
        int index,
        LocalDateTime dateTime,
        int eligiblePlots,
        int proofsFound,
        float lookupTime,
        int totalPlots
) { }
