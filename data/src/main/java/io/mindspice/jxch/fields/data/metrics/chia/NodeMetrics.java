package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record NodeMetrics(
        ChainMetrics chainMetrics,
        MempoolMetrics mempoolMetrics
)implements ClientMsg {

}
