package io.mindspice.jxch.fields.service.tasks.system;

import io.mindspice.jxch.fields.data.chia.ChiaMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.PoolMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.ChainMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.MempoolMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.NodeMetrics;
import io.mindspice.jxch.fields.data.metrics.system.*;
import io.mindspice.jxch.fields.service.core.MonitorState;
import io.mindspice.jxch.fields.service.core.SystemMonitor;
import io.mindspice.jxch.rpc.http.FarmerAPI;
import io.mindspice.jxch.rpc.http.FullNodeAPI;
import io.mindspice.jxch.rpc.http.WalletAPI;
import io.mindspice.jxch.rpc.schemas.ApiResponse;
import io.mindspice.jxch.rpc.schemas.farmer.HarvesterSummary;
import io.mindspice.jxch.rpc.schemas.fullnode.BlockChainState;
import io.mindspice.jxch.rpc.schemas.wallet.Wallet;
import io.mindspice.jxch.rpc.schemas.wallet.Wallets;
import io.mindspice.jxch.rpc.util.RPCException;

import java.util.*;


public class MonitorTask implements Runnable {
    private final MonitorState monitorState;

    public MonitorTask(MonitorState monitorState) {
        this.monitorState = monitorState;
    }

    @Override
    public void run() {
        if (monitorState.doSystemMetrics()) {
            CpuMetrics cpuMetrics = SystemMonitor.get().getCpuMetrics();
            MemoryMetrics memoryMetrics = SystemMonitor.get().getMemoryMetrics();
            List<GpuMetrics> gpuMetrics = SystemMonitor.get().getGpuMetrics();
            List<DiskMetrics> diskMetrics = (monitorState.doHDDCheck() && monitorState.needHDDCheck())
                    ? SystemMonitor.get().getDiskMetrics()
                    : List.of();
            monitorState.addSystemMetrics(new SystemMetrics(cpuMetrics, memoryMetrics, gpuMetrics, diskMetrics));
        }

        List<PoolMetrics> poolMetrics = new ArrayList<>();
        List<HarvesterSummary> harvesterSummaries = new ArrayList<>();
        NodeMetrics nodeMetrics = null;

        for (var service : monitorState.getChiaService()) {
            try {
                switch (service) {
                    case FARMER -> {
                        FarmerAPI farmerApi = (FarmerAPI) monitorState.getChiaServiceAPI(service);

                        farmerApi.getPoolState().data().ifPresent(d -> d.forEach(p ->
                                poolMetrics.add(new PoolMetrics(
                                        p.poolConfig().launcherId(), p.poolConfig().poolUrl(),
                                        p.currentDifficulty(), p.plotCount(),
                                        p.pointsAcknowledged24h().size(), p.poolErrors24h().size())
                                ))
                        );
                        if (monitorState.needHarvesterCheck()) {
                            farmerApi.getHarvestersSummary().data().ifPresent(harvesterSummaries::addAll);
                        }
                    }
                    case FULL_NODE -> {
                        FullNodeAPI nodeApi = (FullNodeAPI) monitorState.getChiaServiceAPI(service);
                        ApiResponse<BlockChainState> blockChainState = nodeApi.getBlockChainState();
                        if (blockChainState.data().isPresent()) {
                            nodeMetrics = blockChainState.data().stream().map(d -> {
                                ChainMetrics chainMetrics = new ChainMetrics(
                                        d.peak().height(), d.difficulty(),
                                        d.peak().weight(), d.space(),
                                        d.sync().synced(), d.sync().syncMode(),
                                        d.sync().syncProgressHeight()
                                );
                                MempoolMetrics mempoolMetrics = new MempoolMetrics(
                                        d.mempoolSize(), d.mempoolCost(),
                                        d.mempoolFees(), d.mempoolMaxTotalCost(),
                                        d.mempoolMinFees().cost_5000000()
                                );
                                return new NodeMetrics(chainMetrics, mempoolMetrics);
                            }).findFirst().orElse(null);
                        }

                    }
                    case WALLET -> {
                        WalletAPI walletApi = (WalletAPI) monitorState.getChiaServiceAPI(service);
                        Optional<Wallets> wallets = walletApi.getWallets(true).data();
                        if (wallets.isEmpty()) { continue; }

                        monitorState.setActiveFingerPrint(wallets.get().fingerprint());
                        List<Integer> walletIds = wallets.get().wallets().stream().map(Wallet::id).toList();
                        for (var id : walletIds) {
                            walletApi.getWalletBalance(id).data().ifPresent(monitorState::updateWalletState);
                        }
                    }
                }
                ChiaMetrics chiaMetrics = new ChiaMetrics(nodeMetrics, poolMetrics, harvesterSummaries);
            } catch (RPCException e) {
                // TODO log/print
            }
        }
    }
}


