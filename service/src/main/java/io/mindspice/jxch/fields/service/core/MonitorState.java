package io.mindspice.jxch.fields.service.core;

import io.mindspice.jxch.fields.data.chia.ChiaMetrics;
import io.mindspice.jxch.fields.data.chia.NodeStatistics;
import io.mindspice.jxch.fields.data.structures.CircularQueue;
import io.mindspice.jxch.fields.data.metrics.system.SystemMetrics;
import io.mindspice.jxch.fields.service.config.MonitorConfig;
import io.mindspice.jxch.rpc.NodeConfig;
import io.mindspice.jxch.rpc.enums.ChiaService;
import io.mindspice.jxch.rpc.http.*;
import io.mindspice.jxch.rpc.schemas.wallet.WalletBalance;
import io.mindspice.jxch.rpc.util.RPCException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class MonitorState {
    //Config
    private final MonitorConfig monitorConfig;
    private Map<ChiaService, ChiaAPI> chiaServices;

    // Metrics
    // Circular queue need synchronization
    private final CircularQueue<SystemMetrics> systemMetricsHistory;
    private final CircularQueue<ChiaMetrics> chiaMetricsHistory;
    private final NodeStatistics nodeStatistics;
//    private final CircularQueue<SubSlotInfo> = null;

    private final AtomicInteger lastHddCheck = new AtomicInteger(1);
    private final AtomicInteger lastHarvesterCheck = new AtomicInteger(1);
    private final AtomicInteger lastChiaMetricsUpdate = new AtomicInteger(1);
    private final Map<String, WalletBalance> walletStates = new ConcurrentHashMap<>();

    private volatile long activeFingerPrint = -1;
    private volatile int height;
    private volatile int difficulty;
    private volatile long weight;
    private LocalDateTime epochStartTime = LocalDateTime.MIN;

    public MonitorState(MonitorConfig config) throws IllegalStateException {
        monitorConfig = config;
        systemMetricsHistory = new CircularQueue<>(config.getMaxSystemHistorySize());
        chiaMetricsHistory = new CircularQueue<>(config.getMaxChiaHistorySize());
        nodeStatistics = new NodeStatistics();
    }

    private boolean init() {
        Map<ChiaService, ChiaAPI> serviceMap = new HashMap<>(4);
        NodeConfig nodeConfig = null;
        try {
            nodeConfig = NodeConfig.loadConfig(monitorConfig.getPathToNodeConfig());
        } catch (IOException e) {
            return false;
            // TODO Log/Print
        }
        var rpcClient = new RPCClient(nodeConfig);
        for (ChiaService service : rpcClient.getAvailableServices()) {
            ChiaAPI serviceApi = null;
            switch (service) {
                case FARMER -> serviceApi = new FarmerAPI(rpcClient);
                case FULL_NODE -> serviceApi = new FullNodeAPI(rpcClient);
                case HARVESTER -> serviceApi = new HarvesterAPI(rpcClient);
                case WALLET -> serviceApi = new WalletAPI(rpcClient);
            }
            if (serviceApi != null) { serviceMap.put(service, serviceApi); }
        }
        for (Map.Entry<ChiaService, ChiaAPI> entry : serviceMap.entrySet()) {
            try {
                if (!entry.getValue().healthz().success()) {
                    serviceMap.remove(entry.getKey());
                }
            } catch (RPCException e) {
                serviceMap.remove(entry.getKey());
                // TODO Log/Print
            }
        }
        chiaServices = Collections.unmodifiableMap(serviceMap);
        return !serviceMap.isEmpty();
    }

    public void setActiveFingerPrint(long fingerPrint) { activeFingerPrint = activeFingerPrint; }

    public Set<ChiaService> getChiaService() { return chiaServices.keySet(); }

    public ChiaAPI getChiaServiceAPI(ChiaService service) { return chiaServices.get(service); }

    public boolean doSystemMetrics() { return monitorConfig.isDoSystemMetrics(); }

    public boolean doHDDCheck() { return monitorConfig.isDoHDDCheck(); }

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

    public void addSystemMetrics(SystemMetrics metrics) {
        synchronized (systemMetricsHistory) {
            systemMetricsHistory.add(metrics);
        }
        // TODO notify client send
    }

    public void addChiaMetrics(ChiaMetrics chiaMetrics) {
        synchronized (chiaMetricsHistory) {
            chiaMetricsHistory.add(chiaMetrics);
        }
        // TODO notify client send
    }

    public void updateWalletState(WalletBalance newBalance) {
        String key = newBalance.fingerprint() + ":" + newBalance.walletId();
        if (walletStates.containsKey(key)) {
            WalletBalance existingBalance = walletStates.get(key);
            if (!existingBalance.equals(newBalance)) {
                walletStates.put(key, newBalance);
                // TODO notify client send
            }
        }
    }

    public void setPeakMetrics(int height, long weight, int difficulty) {
        if (this.height < height) { this.height = height; }
        if (this.weight < weight) { this.weight = weight; }
        if (this.difficulty < difficulty) { this.difficulty = difficulty; }
    }

}
