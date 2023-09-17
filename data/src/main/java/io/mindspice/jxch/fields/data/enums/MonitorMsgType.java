package io.mindspice.jxch.fields.data.enums;

import io.mindspice.jxch.fields.data.metrics.chia.*;
import io.mindspice.jxch.fields.data.metrics.system.SystemMetrics;
import io.mindspice.jxch.fields.data.structures.Pair;
import io.mindspice.jxch.rpc.schemas.wallet.WalletBalance;

import java.time.LocalDateTime;
import java.util.List;


public enum MonitorMsgType {
    FARMED_BLOCK(BlockMetrics.class),
    ERROR_WARNING(Pair.class),
    BALANCE_UPDATE(WalletBalance.class),
    SYSTEM_METRICS(SystemMetrics.class),
    NEW_PEAK(HeightMetrics.class),
    HEIGHT_METRICS(HeightMetrics.class),
    CHAIN_METRICS(ChainMetrics.class),
    MEMPOOL_METRICS(MempoolMetrics.class),
    POOL_METRICS(List.class),
    HARVESTER_METRICS(List.class), // FIXME Need type ref
    SUB_SLOT_METRICS(SubSlotMetrics.class),
    DISCONNECTED_DISKS(DisconnectedDisks.class);
    private final Class<?> clazz;

    MonitorMsgType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getJsonClass() {
        return clazz;
    }
}
