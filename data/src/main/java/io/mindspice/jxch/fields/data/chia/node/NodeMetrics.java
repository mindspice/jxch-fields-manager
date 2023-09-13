package io.mindspice.jxch.fields.data.chia.node;

public class NodeMetrics {
    private final ChainMetrics chainMetrics;
    private final MempoolMetrics mempoolMetrics;

    public NodeMetrics(ChainMetrics chainMetrics, MempoolMetrics mempoolMetrics) {
        this.chainMetrics = chainMetrics;
        this.mempoolMetrics = mempoolMetrics;
    }

    public ChainMetrics getChainMetrics() { return chainMetrics; }

    public MempoolMetrics getMempoolMetrics() { return mempoolMetrics; }
}
