package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record PoolInfo(
        String launcherId,
        String poolURL,
        int difficulty,
        int plotCount,
        int partialSuccessCount,
        int partialErrorCount
)implements ClientMsg { }
