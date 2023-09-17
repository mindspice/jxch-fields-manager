package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record FarmingMetrics(
        SignagePointMetrics signagePointMetrics,
        PlotMetrics plotMetrics
) implements ClientMsg { }
