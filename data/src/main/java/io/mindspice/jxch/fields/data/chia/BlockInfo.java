package io.mindspice.jxch.fields.data.chia;

public record BlockInfo(
        int signagePointIndex,
        boolean farmedBySelf,
        float validationTime,
        float responseTime,
        long cost,
        float pctFull
) {

    public BlockInfo(int signagePointIndex, boolean farmedBySelf, float validationTime, long cost) {
        this(signagePointIndex,farmedBySelf, validationTime, -1, cost, -1);
    }
}
