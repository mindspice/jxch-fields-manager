package io.mindspice.jxch.fields.service.tasks;

import io.mindspice.jxch.fields.data.metrics.chia.PoolInfo;
import io.mindspice.jxch.fields.data.metrics.chia.ChainMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.MempoolMetrics;
import io.mindspice.jxch.fields.data.metrics.system.*;
import io.mindspice.jxch.fields.service.core.MonitorState;
import io.mindspice.jxch.fields.service.core.SystemMonitor;
import io.mindspice.jxch.rpc.enums.ChiaService;
import io.mindspice.jxch.rpc.http.ChiaAPI;
import io.mindspice.jxch.rpc.http.FarmerAPI;
import io.mindspice.jxch.rpc.http.FullNodeAPI;
import io.mindspice.jxch.rpc.http.WalletAPI;
import io.mindspice.jxch.rpc.schemas.wallet.Wallet;
import io.mindspice.jxch.rpc.schemas.wallet.Wallets;
import io.mindspice.jxch.rpc.util.RPCException;

import java.util.*;

import static io.mindspice.jxch.rpc.enums.ChiaService.*;


public class MonitorTask implements Runnable {
    private final MonitorState monitorState;
    private Map<ChiaService, ChiaAPI> chiaServices;

    public MonitorTask(MonitorState monitorState) {
        this.monitorState = monitorState;
    }

    public void init(Map<ChiaService, ChiaAPI> chiaServices) {
        this.chiaServices = chiaServices;
    }

    @Override
    public void run() {
        if (monitorState.doSystemMetrics()) {
            try {
                CpuMetrics cpuMetrics = SystemMonitor.GET().getCpuMetrics();
                MemoryMetrics memoryMetrics = SystemMonitor.GET().getMemoryMetrics();
                List<GpuMetrics> gpuMetrics = SystemMonitor.GET().getGpuMetrics();
                List<DiskMetrics> diskMetrics = (monitorState.doHDDCheck() && monitorState.needHDDCheck())
                        ? SystemMonitor.GET().getDiskMetrics()
                        : List.of();
                monitorState.addSystemMetrics(new SystemMetrics(cpuMetrics, memoryMetrics, gpuMetrics, diskMetrics));
            } catch (Exception e) {
                System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                        + " | Unhandled Exception gathering system metrics" + e.getMessage());
                Arrays.stream(e.getStackTrace()).forEach(System.out::println);
            }
        }

        for (var service : chiaServices.keySet()) {
            try {
                switch (service) {
                    case FARMER -> {
                        FarmerAPI farmerApi = (FarmerAPI) chiaServices.get(service);
                        farmerApi.getPoolState().data().ifPresent(d -> {
                                    var poolMetrics = d.stream().map(p ->
                                            new PoolInfo(
                                                    p.poolConfig().launcherId(), p.poolConfig().poolUrl(),
                                                    p.currentDifficulty(), p.plotCount(),
                                                    p.pointsAcknowledged24h().size(), p.poolErrors24h().size())
                                    ).toList();
                                    monitorState.updatePoolMetrics(poolMetrics);
                                }
                        );
                        if (monitorState.needHarvesterCheck()) {
                            farmerApi.getHarvestersSummary().data().ifPresent(monitorState::updateHarvesterSummaries);
                        }
                    }
                    case FULL_NODE -> {
                        FullNodeAPI nodeApi = (FullNodeAPI) chiaServices.get(service);
                        nodeApi.getBlockChainState().data().ifPresent(d -> {
                            ChainMetrics chainMetrics = new ChainMetrics(
                                    d.peak().height(), d.difficulty(),
                                    d.peak().weight(), d.space(),
                                    d.sync().synced(), d.sync().syncMode(),
                                    d.sync().syncProgressHeight()
                            );
                            monitorState.addChainMetrics(chainMetrics);

                            MempoolMetrics mempoolMetrics = new MempoolMetrics(
                                    d.mempoolSize(), d.mempoolCost(),
                                    d.mempoolFees(), d.mempoolMaxTotalCost(),
                                    d.mempoolMinFees().cost_5000000()
                            );
                            monitorState.addMempoolMetrics(mempoolMetrics);
                        });
                    }
                    case WALLET -> {
                        WalletAPI walletApi = (WalletAPI) chiaServices.get(service);
                        Optional<Wallets> wallets = walletApi.getWallets(true).data();
                        if (wallets.isEmpty()) { continue; }

                        monitorState.setActiveFingerPrint(wallets.get().fingerprint());
                        List<Integer> walletIds = wallets.get().wallets().stream().map(Wallet::id).toList();
                        for (var id : walletIds) {
                            walletApi.getWalletBalance(id).data().ifPresent(monitorState::updateWalletState);
                        }
                    }
                }
            } catch (RPCException e) {
                System.out.println("Monitor: " + monitorState.getConfig().getMonitorName() + " | Rpc Exception: "
                        + e.getMessage() + "| Trace: " + Arrays.toString(e.getStackTrace()));
            } catch (Exception e) {
                System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                        + " | Unhandled Exception during RPC call:" + e.getMessage());
                Arrays.stream(e.getStackTrace()).forEach(System.out::println);
            }
        }
    }
}


