package io.mindspice.jxch.fields.service.core;

import io.mindspice.jxch.fields.data.*;
import io.mindspice.jxch.fields.data.chia.ChiaMetrics;
import io.mindspice.jxch.fields.data.chia.SignagePointMetrics;
import io.mindspice.jxch.fields.data.chia.node.SignagePointState;
import io.mindspice.jxch.fields.data.chia.Wallet.WalletState;
import io.mindspice.jxch.fields.data.system.SystemMetrics;
import io.mindspice.jxch.fields.service.config.MonitorConfig;
import io.mindspice.jxch.rpc.NodeConfig;
import io.mindspice.jxch.rpc.enums.ChiaService;
import io.mindspice.jxch.rpc.http.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class MonitorState {
    private final MonitorConfig monitorConfig;
    private final CircularQueue<SystemMetrics> systemMetrics;
    private final CircularQueue<ChiaMetrics> chiaMetrics;

    private final CircularQueue<SignagePointState> signagePointBuffer;
    private final Map<Integer, SignagePointMetrics> signagePointMetrics;
    private final Map<ChiaService, ChiaAPI> chiaServices;

    private final AtomicInteger lastHddCheck = new AtomicInteger(1);
    private final AtomicInteger lassHarvesterCheck = new AtomicInteger(1);

    private final Map<Long, Map<Integer, WalletState>> walletStates = new ConcurrentHashMap<>();
    private volatile long activeFingerPrint = -1;

    private volatile int height;
    private volatile int difficulty;
    private volatile long weight;
    private LocalDateTime epochStartTime = LocalDateTime.MIN;

    public MonitorState(MonitorConfig config) throws IllegalStateException {
        monitorConfig = config;
        systemMetrics = new CircularQueue<>(config.MaxHistorySize());
        chiaMetrics = new CircularQueue<>(64);
        signagePointBuffer = new CircularQueue<>(4608);
        signagePointMetrics = null;
        chiaServices = new HashMap<>(4);

        try {
            var nodeConfig = NodeConfig.loadConfig(config.PathToNodeConfig());
            var rpcClient = new RPCClient(nodeConfig);
            for (ChiaService service : rpcClient.getAvailableServices()) {
                ChiaAPI serviceApi = null;
                switch (service) {
                    case FARMER -> serviceApi = new FarmerAPI(rpcClient);
                    case FULL_NODE -> serviceApi = new FullNodeAPI(rpcClient);
                    case HARVESTER -> serviceApi = new HarvesterAPI(rpcClient);
                    case WALLET -> serviceApi = new WalletAPI(rpcClient);
                }
                if (serviceApi != null) { chiaServices.put(service, serviceApi); }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed To Initialize Monitor Service For Node: " + config.NodeName());
        }
    }

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

    public synchronized void addSystemMetrics(SystemMetrics metrics) {
        systemMetrics.add(metrics);
    }

    public Map<Integer, WalletState> getWalletState(long fingerPrint) {
        activeFingerPrint = fingerPrint;
        return walletStates.computeIfAbsent(fingerPrint, k -> new ConcurrentHashMap<>());
    }

    public void setPeakMetrics(int height, long weight, int difficulty) {
        if (this.height < height) { this.height = height; }
        if (this.weight < weight) { this.weight = weight; }
        if (this.difficulty < difficulty) { this.difficulty = difficulty; }
    }

}
