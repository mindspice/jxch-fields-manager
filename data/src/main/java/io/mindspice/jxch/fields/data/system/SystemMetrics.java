package io.mindspice.jxch.fields.data.system;

import java.util.List;


public record SystemMetrics(
        CpuMetrics cpuMetrics,
        MemoryMetrics memoryMetrics,
        List<GpuMetrics> gpuMetrics,
        List<DiskMetrics> diskMetrics
) { }
