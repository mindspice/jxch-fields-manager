package io.mindspice.jxch.fields.data.metrics.chia;

public record PoolMetrics(
        String launcherId,
        String poolURL,
        int difficulty,
        int plotCount,
        int partialSuccessCount,
        int partialErrorCount
) { }
