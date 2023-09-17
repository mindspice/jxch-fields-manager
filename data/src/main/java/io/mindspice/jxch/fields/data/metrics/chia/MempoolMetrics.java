package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record MempoolMetrics(
        long mempoolSize,
        long mempoolCost,
        long mempoolFees,
        long mempoolMaxCost,
        long minFee5e6
)implements ClientMsg { }
