package io.mindspice.jxch.fields.data.metrics.system;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record GpuMetrics(
        int deviceId,
        String deviceName,
        double utilizationAvg,
        double utilizationMax,
        double memoryTotalAvg,
        double memoryTotalMax,
        double memoryUsedAvg,
        double memoryUsedMax,
        double temperatureAvg,
        double temperatureMax
)implements ClientMsg { }