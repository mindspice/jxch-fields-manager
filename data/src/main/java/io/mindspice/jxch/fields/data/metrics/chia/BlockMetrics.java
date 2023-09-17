package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record BlockMetrics(
        int signagePointIndex,
        boolean farmedBySelf,
        double validationTime,
        double responseTime,
        long cost,
        float pctFull
) implements ClientMsg {

    public BlockMetrics(int signagePointIndex, boolean farmedBySelf, float validationTime, long cost) {
        this(signagePointIndex,farmedBySelf, validationTime, -1, cost, -1);
    }
}
