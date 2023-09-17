package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.fields.data.metrics.system.DiskMetrics;

import java.util.List;


public record DisconnectedDisks(
        List<DiskMetrics> diskMetrics
) implements ClientMsg { }