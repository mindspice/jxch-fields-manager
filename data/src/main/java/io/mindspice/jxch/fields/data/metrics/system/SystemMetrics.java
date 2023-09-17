package io.mindspice.jxch.fields.data.metrics.system;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;

import java.util.List;


public record SystemMetrics(
        CpuMetrics cpuMetrics,
        MemoryMetrics memoryMetrics,
        List<GpuMetrics> gpuMetrics,
        List<DiskMetrics> diskMetrics
) implements ClientMsg { }
