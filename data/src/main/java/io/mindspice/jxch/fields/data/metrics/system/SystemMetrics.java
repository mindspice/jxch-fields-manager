package io.mindspice.jxch.fields.data.metrics.system;

import java.util.List;


public record SystemMetrics(
        CpuMetrics cpuMetrics,
        MemoryMetrics memoryMetrics,
        List<GpuMetrics> gpuMetrics,
        List<DiskMetrics> diskMetrics
) { }
