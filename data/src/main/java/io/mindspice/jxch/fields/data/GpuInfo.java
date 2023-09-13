package io.mindspice.jxch.fields.data;

import io.mindspice.jxch.fields.data.system.GpuMetrics;
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
                DataUtil.limitPrecision(utilizationSummary.getAverage()),
                DataUtil.limitPrecision(utilizationSummary.getMax()),
                DataUtil.limitPrecision(memoryTotalSummary.getAverage()),
                DataUtil.limitPrecision(memoryTotalSummary.getMax()),
                DataUtil.limitPrecision(memoryUsedSummary.getAverage()),
                DataUtil.limitPrecision(memoryUsedSummary.getMax()),
                DataUtil.limitPrecision(temperatureSummary.getAverage()),
                DataUtil.limitPrecision(temperatureSummary.getMax())
        );
    }
}
