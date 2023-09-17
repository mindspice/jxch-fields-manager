package io.mindspice.jxch.fields.data.metrics.system;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record MemoryMetrics(
        double totalMemory,
        double usedMemory,
        double totalSwap,
        double usedSwap
)implements ClientMsg { }
