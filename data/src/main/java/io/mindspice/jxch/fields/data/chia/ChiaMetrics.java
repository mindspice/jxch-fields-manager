package io.mindspice.jxch.fields.data.chia;

import io.mindspice.jxch.fields.data.metrics.chia.PoolMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.NodeMetrics;
import io.mindspice.jxch.rpc.schemas.farmer.HarvesterSummary;

import java.util.ArrayList;
import java.util.List;


public record ChiaMetrics(
        NodeMetrics nodeMetrics,
        List<PoolMetrics> poolMetrics,
        List<HarvesterSummary> harvesterMetrics
) { }
