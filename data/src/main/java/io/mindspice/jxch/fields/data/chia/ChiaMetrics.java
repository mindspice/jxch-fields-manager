package io.mindspice.jxch.fields.data.chia;

import io.mindspice.jxch.fields.data.chia.farmer.PoolMetrics;
import io.mindspice.jxch.fields.data.chia.node.NodeMetrics;
import io.mindspice.jxch.rpc.schemas.farmer.HarvesterSummary;

import java.util.ArrayList;
import java.util.List;


public class ChiaMetrics {
    private NodeMetrics nodeMetrics;
    private List<PoolMetrics> poolMetrics;
    private List<HarvesterSummary> harvesterMetrics;

    public NodeMetrics getNodeMetrics() {
        return nodeMetrics;
    }

    public void setNodeMetrics(NodeMetrics nodeMetrics) {
        this.nodeMetrics = nodeMetrics;
    }

    public List<PoolMetrics> getPoolMetrics() {
        return poolMetrics;
    }

    public void addPoolMetrics(PoolMetrics metrics) {
        if (poolMetrics == null) { poolMetrics = new ArrayList<>(); }
        poolMetrics.add(metrics);
    }

    public void setHarvesterMetrics(List<HarvesterSummary> harvesterSummaries) {
        harvesterMetrics = harvesterSummaries;
    }
}
