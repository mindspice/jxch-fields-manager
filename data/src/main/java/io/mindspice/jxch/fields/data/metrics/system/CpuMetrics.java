package io.mindspice.jxch.fields.data.metrics.system;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.fields.data.util.DataUtil;

import java.util.Arrays;


public record CpuMetrics(
        double[] avgCoreUsage,
        double[] maxCoreUsage,
        double[] coreFrequencies,
        double avgTotalUsage,
        double maxTotalUsage,
        double avgFreq,
        int temperature,
        int threadCount,
        int processCount
) implements ClientMsg {
    public CpuMetrics(double[] avgCoreUsage, double[] maxCoreUsage, double[] coreFrequencies,
            int temperature, int threadCount, int processCount) {
        this(
                avgCoreUsage,
                maxCoreUsage,
                coreFrequencies,
                DataUtil.limitPrecision2D(Arrays.stream(avgCoreUsage).average().orElse(0.00)),
                DataUtil.limitPrecision2D(Arrays.stream(maxCoreUsage).average().orElse(0.00)),
                DataUtil.limitPrecision2D(Arrays.stream(coreFrequencies).average().orElse(0.00)),
                temperature,
                threadCount,
                processCount
        );
    }
}

