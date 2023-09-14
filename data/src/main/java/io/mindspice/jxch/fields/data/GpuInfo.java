package io.mindspice.jxch.fields.data;

import io.mindspice.jxch.fields.data.metrics.system.GpuMetrics;
import io.mindspice.jxch.fields.data.util.DataUtil;

import java.util.ArrayList;
import java.util.List;


public class GpuInfo {
    private final int deviceId;
    private final String deviceName;
    private final List<Integer> utilization = new ArrayList<>();
    private final List<Integer> memoryTotal = new ArrayList<>();
    private final List<Integer> memoryUsed = new ArrayList<>();
    private final List<Integer> temperature = new ArrayList<>();

    public GpuInfo(int deviceId, String deviceName) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public synchronized void addMetrics(int utilization, int memoryTotal, int memoryUsed, int temperature) {
        this.utilization.add(utilization);
        this.memoryTotal.add(memoryTotal);
        this.memoryUsed.add(memoryUsed);
        this.temperature.add(temperature);
    }

    public synchronized GpuMetrics getMetrics() {
        if (utilization.isEmpty()) { return new GpuMetrics(-1, "ERROR", 0, 0, 0, 0, 0, 0, 0, 0); }
        var utilizationSummary = utilization.stream().mapToInt(i -> (i)).summaryStatistics();
        var memoryTotalSummary = memoryTotal.stream().mapToInt(i -> (i)).summaryStatistics();
        var memoryUsedSummary = memoryUsed.stream().mapToInt(i -> (i)).summaryStatistics();
        var temperatureSummary = temperature.stream().mapToInt(i -> (i)).summaryStatistics();
        utilization.clear();
        memoryTotal.clear();
        memoryUsed.clear();
        temperature.clear();

        return new GpuMetrics(
                deviceId,
                deviceName,
                DataUtil.limitPrecision2D(utilizationSummary.getAverage()),
                DataUtil.limitPrecision2D(utilizationSummary.getMax()),
                DataUtil.limitPrecision2D(memoryTotalSummary.getAverage()),
                DataUtil.limitPrecision2D(memoryTotalSummary.getMax()),
                DataUtil.limitPrecision2D(memoryUsedSummary.getAverage()),
                DataUtil.limitPrecision2D(memoryUsedSummary.getMax()),
                DataUtil.limitPrecision2D(temperatureSummary.getAverage()),
                DataUtil.limitPrecision2D(temperatureSummary.getMax())
        );
    }
}
