package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record PlotMetrics(
        int avgEligiblePlots,
        int effectiveSpaceTiB,
        double estimatedWinTime
) implements ClientMsg { }
