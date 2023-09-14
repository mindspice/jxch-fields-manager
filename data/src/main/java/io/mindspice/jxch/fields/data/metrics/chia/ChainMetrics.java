package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.util.DataUtil;

import java.math.BigInteger;


public record ChainMetrics(
        int height,
        int difficulty,
        long weight,
        double effectiveSpaceEiB,
        boolean synced,
        boolean isSyncing,
        int syncedHeight
) {
    public ChainMetrics(int height, int difficulty, long weight, BigInteger effectiveSpaceEiB, boolean synced,
            boolean isSyncing, int syncedHeight) {
        this(
                height,
                difficulty, weight,
                DataUtil.bytesToEiB(effectiveSpaceEiB),
                synced,
                isSyncing,
                syncedHeight
        );
    }
}


