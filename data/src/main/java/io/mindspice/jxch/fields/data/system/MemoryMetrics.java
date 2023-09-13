package io.mindspice.jxch.fields.data.system;

public record MemoryMetrics(
        double totalMemory,
        double usedMemory,
        double totalSwap,
        double usedSwap
) { }
