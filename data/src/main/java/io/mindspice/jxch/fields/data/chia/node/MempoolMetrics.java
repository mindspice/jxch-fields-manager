package io.mindspice.jxch.fields.data.chia.node;

public record MempoolMetrics(
        long mempoolSize,
        long mempoolCost,
        long mempoolFees,
        long mempoolMaxCost,
        long minFee5e6
) { }
