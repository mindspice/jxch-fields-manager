package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.rpc.schemas.farmer.HarvesterSummary;

import java.util.List;


public record HarvesterMetrics(
        List<HarvesterSummary> summaries
) implements ClientMsg { }
