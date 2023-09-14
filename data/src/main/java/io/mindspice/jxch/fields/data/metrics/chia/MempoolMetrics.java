package io.mindspice.jxch.fields.data.metrics.chia;

public record MempoolMetrics(
        long mempoolSize,
        long mempoolCost,
        long mempoolFees,
        long mempoolMaxCost,
        long minFee5e6
) { }
