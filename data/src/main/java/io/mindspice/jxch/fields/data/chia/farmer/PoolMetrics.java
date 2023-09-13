package io.mindspice.jxch.fields.data.chia.farmer;

public record PoolMetrics(
        String launcherId,
        String poolURL,
        int difficulty,
        int plotCount,
        int partialSuccessCount,
        int partialErrorCount
) { }
