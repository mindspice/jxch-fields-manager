package io.mindspice.jxch.fields.service.core;

import io.mindspice.jxch.fields.data.enums.*;
import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.fields.data.metrics.chia.*;
import io.mindspice.jxch.fields.data.network.MonitorOutMsg;
import io.mindspice.jxch.fields.data.metrics.system.SystemMetrics;
import io.mindspice.jxch.fields.data.structures.*;
import io.mindspice.jxch.fields.service.config.MonitorConfig;
import io.mindspice.jxch.fields.service.networking.FieldsServer;
import io.mindspice.jxch.rpc.enums.ChiaService;
import io.mindspice.jxch.rpc.http.*;
import io.mindspice.jxch.rpc.schemas.farmer.HarvesterSummary;
import io.mindspice.jxch.rpc.schemas.wallet.WalletBalance;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class MonitorState {
    //Config
    private final MonitorConfig monitorConfig;
    private final String monitorName;

    private final ConcurrentCircularBuffer<SystemMetrics> systemMetricsHistory;
    private final ConcurrentCircularBuffer<SubSlotMetrics> subSlotHistory;
    private final ConcurrentCircularBuffer<Pair<String, String>> errorsAndWarnings;
    private final ConcurrentCircularBuffer<ChainMetrics> chainMetricsHistory;
    private final ConcurrentCircularBuffer<MempoolMetrics> mempoolMetricsHistory;

    private final NodeStatistics nodeStatistics;

    // Key track of checks/metrics that don't need to ran or added every iteration
    private final AtomicInteger lastHddCheck = new AtomicInteger(1);
    private final AtomicInteger lastHarvesterCheck = new AtomicInteger(1);
    private final AtomicInteger lastChiaMetricsUpdate = new AtomicInteger(1);
    private final AtomicInteger finalizeSubSlotSend = new AtomicInteger(0);
    private final AtomicLong lastMemPoolMetricsTime = new AtomicLong(0);
    private final AtomicLong lastChainMetricsTime = new AtomicLong(0);

    // Wallet State
    private final Map<String, WalletBalance> walletStates = new ConcurrentHashMap<>();

    private volatile List<PoolInfo> poolMetrics;
    private volatile List<HarvesterSummary> harvesterSummaries;
    private volatile long activeFingerPrint = -1;
    private volatile int height;

    // Current active subSlot, is mutated and reference changes hence the lock
    private SubSlotInfo currentSubSlot = null;
    private final ReentrantReadWriteLock currSubSlotLock = new ReentrantReadWriteLock();

    public MonitorState(MonitorConfig config) throws IllegalStateException {
        monitorConfig = config;
        monitorName = monitorConfig.getMonitorName();
        systemMetricsHistory = new ConcurrentCircularBuffer<>(config.getMaxSystemHistorySize());
        chainMetricsHistory = new ConcurrentCircularBuffer<>(config.getMaxChiaHistorySize());
        mempoolMetricsHistory = new ConcurrentCircularBuffer<>(config.getMaxChiaHistorySize());
        nodeStatistics = new NodeStatistics();
        subSlotHistory = new ConcurrentCircularBuffer<>(config.getMaxSubSlotHistorySize());
        errorsAndWarnings = new ConcurrentCircularBuffer<>(config.getMaxErrorWarnHistorySize());
    }

    public void setActiveFingerPrint(long fingerPrint) { activeFingerPrint = fingerPrint; }

    public boolean doSystemMetrics() { return monitorConfig.isDoSystemMetrics(); }

    public boolean doHDDCheck() { return monitorConfig.isDoHDDCheck(); }

    public MonitorConfig getConfig() { return monitorConfig; }

    public boolean needHDDCheck() {
        if (lastHddCheck.decrementAndGet() == 0) {
            lastHddCheck.set(10);
            return true;
        }
        return false;
    }

    public boolean needHarvesterCheck() {
        if (lastHddCheck.decrementAndGet() == 0) {
            lastHddCheck.set(10);
            return true;
        }
        return false;
    }

    public void updateHeight(int height) {
        if (this.height < height) { this.height = height; }
    }

    public void addSystemMetrics(SystemMetrics metrics) {
        systemMetricsHistory.add(metrics);
        broadcastMessage(MonitorMsgType.SYSTEM_METRICS, metrics); // NOTE Message Broadcast
    }

    public void updateWalletState(WalletBalance newBalance) {
        String key = newBalance.fingerprint() + ":" + newBalance.walletId();
        if (walletStates.containsKey(key)) {
            WalletBalance existingBalance = walletStates.get(key);
            if (!existingBalance.equals(newBalance)) {
                broadcastMessage(MonitorMsgType.BALANCE_UPDATE, newBalance); // NOTE Message Broadcast

            }
        }
    }

    public void updatePoolMetrics(List<PoolInfo> poolInfo) {
        this.poolMetrics = poolInfo;
        broadcastMessage(MonitorMsgType.POOL_METRICS, new PoolMetrics(poolInfo)); // NOTE Message Broadcast
    }

    public void updateHarvesterSummaries(List<HarvesterSummary> harvesterSummaries) {
        this.harvesterSummaries = harvesterSummaries;
        broadcastMessage(MonitorMsgType.HARVESTER_METRICS, new HarvesterMetrics(harvesterSummaries)); // NOTE Message Broadcast

    }

    public void addChainMetrics(ChainMetrics chainMetrics) {
        chainMetricsHistory.add(chainMetrics);
        if (chainMetrics.height() > height) { height = chainMetrics.height(); }
        if (chainMetrics.effectiveSpaceEiB() > 0) { nodeStatistics.updateNetSpace(chainMetrics.effectiveSpaceEiB()); }
        broadcastMessage(MonitorMsgType.CHAIN_METRICS, chainMetrics); // NOTE Message Broadcast
    }

    public void addMempoolMetrics(MempoolMetrics mempoolMetrics) {
        if (Instant.now().getEpochSecond() - lastMemPoolMetricsTime.get() >= 59) {
            mempoolMetricsHistory.add(mempoolMetrics);
        }
        broadcastMessage(MonitorMsgType.CHAIN_METRICS, mempoolMetrics); // NOTE Message Broadcast
    }

    public void addSignagePointMetrics(SignagePointMetrics metrics) {
        currSubSlotLock.writeLock().lock();
        try {
            if (currentSubSlot == null) { currentSubSlot = new SubSlotInfo(); }
            if (currentSubSlot.awaitingInit()) { currentSubSlot.init(metrics.dateTime()); }
            currentSubSlot.addSignagePoint(metrics.index(), metrics);

            if (currentSubSlot.isAwaitingLastFilter()) {
                synchronized (subSlotHistory) {
                    subSlotHistory.add(currentSubSlot.getAsMetrics());
                }
                currentSubSlot = new SubSlotInfo();
                finalizeSubSlotSend.set(4); // Prepare sub slot send after 4 more sps to account for late blocks
            }
        } finally {
            currSubSlotLock.writeLock().unlock();
        }
        if (finalizeSubSlotSend.decrementAndGet() == 0) { // send last sub slot after 4 sps of the new one have passed
            broadcastMessage(MonitorMsgType.SUB_SLOT_METRICS, subSlotHistory.getFromNewest(0));// NOTE Message Broadcast
        }
    }

    public void addCachedSignagePoint(LocalDateTime dateTime, int spIndex) {
        currSubSlotLock.writeLock().lock();
        try {
            if (currentSubSlot == null) { return; }
            currentSubSlot.addCachedSignagePoint(dateTime, spIndex);
        } finally {
            currSubSlotLock.writeLock().unlock();
        }
    }

    public void addBlock(BlockMetrics blockMetrics) {
        currSubSlotLock.writeLock().lock();
        try {
            if (currentSubSlot == null) { return; } // return if there is no sub slot inited yet (start of tail
            if (currentSubSlot.getSpHeight() >= blockMetrics.signagePointIndex()) {
                currentSubSlot.addBlock(blockMetrics);
                if (blockMetrics.farmedBySelf()) {
                    broadcastMessage(MonitorMsgType.FARMED_BLOCK, blockMetrics);// NOTE Message Broadcast
                }
                return;
            }
        } finally {
            currSubSlotLock.writeLock().unlock();
        }
        // Add late block, may result in stale view
        if (subSlotHistory.isEmpty()) { return; } // Return if there is not history to add to (start of tailing)
        subSlotHistory.getFromOldest(0).blocks().add(blockMetrics);
        if (blockMetrics.farmedBySelf()) {
            broadcastMessage(MonitorMsgType.FARMED_BLOCK, blockMetrics);// NOTE Message Broadcast
        }
    }

    public void addErrorOrWarning(LocalDateTime dateTime, String errorWarning) {
        var warningPair = new Pair<>(dateTime.toString(), errorWarning);
        errorsAndWarnings.add(warningPair);
        broadcastMessage(MonitorMsgType.ERROR_WARNING, warningPair);// NOTE Message Broadcast bro

    }

    public void finalizeSubSlot(LocalDateTime endTime, int deficit) {
        currSubSlotLock.writeLock().lock();
        try {
            currentSubSlot.setEndOfSubSlot(endTime, deficit);
        } finally {
            currSubSlotLock.writeLock().unlock();
        }
    }

    private <T> void broadcastMessage(MonitorMsgType msgType, T obj) {
        MonitorOutMsg<T> msg = new MonitorOutMsg<>(msgType, obj);
        FieldsServer.GET().submitClientMsg(msg);
    }
}
