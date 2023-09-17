package io.mindspice.jxch.fields.service.core;

import io.mindspice.jxch.fields.data.structures.Pair;
import io.mindspice.jxch.fields.service.config.MonitorConfig;
import io.mindspice.jxch.fields.service.tasks.MonitorTask;
import io.mindspice.jxch.fields.service.tasks.tailer.LogParser;
import io.mindspice.jxch.rpc.NodeConfig;
import io.mindspice.jxch.rpc.enums.ChiaService;
import io.mindspice.jxch.rpc.enums.endpoints.FullNode;
import io.mindspice.jxch.rpc.http.*;
import io.mindspice.jxch.rpc.util.RPCException;
import org.apache.commons.io.input.Tailer;
import oshi.jna.platform.unix.SolarisLibc;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class MonitorService {
    private final MonitorConfig config;
    private final MonitorState monitorState;
    private LogParser logParser;
    private Tailer tailerInstance;
    private MonitorTask monitorTask;

    public MonitorService(MonitorConfig config) {
        this.config = config;
        this.monitorState = new MonitorState(config);
    }

    public Pair<Tailer, MonitorTask> init() {
        if (config.isDoLogParsing()) {
            logParser = new LogParser(monitorState);
            tailerInstance = logParser.getTailer(config.getPathToLog());
            if (tailerInstance.getFile() != null) {
                System.out.println("Monitor: " + config.getMonitorName() + " | Loaded Tailer");
            }
        }
        if (config.isDoRpcMetrics() || config.isDoSystemMetrics()) {
            monitorTask = new MonitorTask(monitorState);
            if (config.isDoRpcMetrics()) {
                var serviceMap = initChiaRpc();
                monitorTask.init(serviceMap);
                if (serviceMap.containsKey(ChiaService.FULL_NODE)) {
                    logParser.disableChainMetrics();
                }
            }
        }
        return new Pair<>(tailerInstance, monitorTask);
    }

    private Map<ChiaService, ChiaAPI> initChiaRpc() {
        Map<ChiaService, ChiaAPI> serviceMap = new HashMap<>(4);
        NodeConfig nodeConfig = null;
        try {
            nodeConfig = NodeConfig.loadConfig(config.getPathToNodeConfig());
        } catch (IOException e) {
            System.out.println("Failed to load node config for monitor: " + config.getMonitorName()
                    + " | " + e.getMessage() + " | Trace: ");
            Arrays.stream(e.getStackTrace()).forEach(System.out::println);
            return Map.of();
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

        List<ChiaService> failedServices = null;
        for (var entry : serviceMap.entrySet()) {
            try {
                if (!entry.getValue().healthz().success()) {
                    if (failedServices == null) { failedServices = new ArrayList<>(4); }
                    failedServices.add(entry.getKey());
                }
                System.out.println("Monitor: " + config.getMonitorName() + " | Loaded RPC Service: " + entry.getKey());
            } catch (RPCException e) {
                if (failedServices == null) { failedServices = new ArrayList<>(4); }
                failedServices.add(entry.getKey());
                System.out.println(e);
                System.out.println("Monitor: " + config.getMonitorName() + "Failed to load service: " + entry.getKey());
            }
        }
        if (failedServices != null) { failedServices.forEach(serviceMap::remove); }
        return Collections.unmodifiableMap(serviceMap);
    }
}


