package io.mindspice.jxch.fields.data.metrics.system;

public record MemoryMetrics(
        double totalMemory,
        double usedMemory,
        double totalSwap,
        double usedSwap
) { }