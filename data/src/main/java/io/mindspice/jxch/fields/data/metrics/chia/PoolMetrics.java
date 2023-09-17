package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;

import java.util.List;


public record PoolMetrics(
        List<PoolInfo> poolInfo
) implements ClientMsg {
}
