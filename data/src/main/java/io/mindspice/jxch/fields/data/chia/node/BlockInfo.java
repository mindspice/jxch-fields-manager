package io.mindspice.jxch.fields.data.chia.node;

public record BlockInfo(
        boolean farmedBySelf,
        float validationTime,
        float responseTime,
        long cost,
        float pctFull
) {

    public BlockInfo(boolean farmedBySelf, float validationTime, long cost) {
        this(farmedBySelf, validationTime, -1, cost, -1);
    }
}
