package io.mindspice.jxch.fields.data.metrics.chia;

public record NodeMetrics(
        ChainMetrics chainMetrics,
        MempoolMetrics mempoolMetrics
) {

}
