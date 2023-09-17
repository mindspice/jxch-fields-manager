package io.mindspice.jxch.fields.data.metrics.system;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.fields.data.structures.Pair;
import io.mindspice.jxch.fields.data.util.DataUtil;

import java.util.List;


public record DiskMetrics(
        String mount,
        String uuid,
        long totalSpace,
        long freeSpace
) implements ClientMsg { }
