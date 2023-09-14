package io.mindspice.jxch.fields.data.metrics.system;

import io.mindspice.jxch.fields.data.util.DataUtil;


public record DiskMetrics(
        String mount,
        String uuid,
        long totalSpace,
        long freeSpace
) { }
