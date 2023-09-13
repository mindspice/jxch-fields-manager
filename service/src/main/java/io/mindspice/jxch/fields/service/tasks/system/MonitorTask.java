package io.mindspice.jxch.fields.service.tasks.system;

import io.mindspice.jxch.fields.data.chia.ChiaMetrics;
import io.mindspice.jxch.fields.data.chia.Wallet.WalletState;
import io.mindspice.jxch.fields.data.chia.farmer.PoolMetrics;
import io.mindspice.jxch.fields.data.chia.node.ChainMetrics;
import io.mindspice.jxch.fields.data.chia.node.MempoolMetrics;
import io.mindspice.jxch.fields.data.chia.node.NodeMetrics;
import io.mindspice.jxch.fields.data.system.*;
import io.mindspice.jxch.fields.service.core.MonitorState;
import io.mindspice.jxch.fields.service.core.SystemMonitor;
import io.mindspice.jxch.rpc.enums.ChiaService;
import io.mindspice.jxch.rpc.http.FarmerAPI;
import io.mindspice.jxch.rpc.http.FullNodeAPI;
import io.mindspice.jxch.rpc.http.WalletAPI;
import io.mindspice.jxch.rpc.schemas.farmer.PoolState;
import io.mindspice.jxch.rpc.schemas.fullnode.BlockChainState;
import io.mindspice.jxch.rpc.schemas.wallet.Wallet;
import io.mindspice.jxch.rpc.schemas.wallet.WalletBalance;
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

        ChiaMetrics chiaMetrics = new ChiaMetrics();
        for (var service : monitorState.getChiaService()) {
            try {
                switch (service) {
                    case FARMER -> {
                        FarmerAPI farmerApi = (FarmerAPI) monitorState.getChiaServiceAPI(service);

                        farmerApi.getPoolState().data().ifPresent(d -> d.forEach(p ->
                                chiaMetrics.addPoolMetrics(new PoolMetrics(
                                        p.poolConfig().launcherId(), p.poolConfig().poolUrl(),
                                        p.currentDifficulty(), p.plotCount(),
                                        p.pointsAcknowledged24h().size(), p.poolErrors24h().size())
                                ))
                        );
                        if (monitorState.needHarvesterCheck()) {
                            farmerApi.getHarvestersSummary().data().ifPresent(chiaMetrics::setHarvesterMetrics);
                        }
                    }
                    case FULL_NODE -> {
                        FullNodeAPI nodeApi = (FullNodeAPI) monitorState.getChiaServiceAPI(service);

                        nodeApi.getBlockChainState().data().ifPresent(d -> {
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
                            chiaMetrics.setNodeMetrics(new NodeMetrics(chainMetrics, mempoolMetrics));
                        });
                    }
                    case WALLET -> {
                        WalletAPI walletApi = (WalletAPI) monitorState.getChiaServiceAPI(service);
                        Optional<Wallets> wallets = walletApi.getWallets(true).data();
                        if (wallets.isEmpty()) { continue; }

                        long fingerprint = wallets.get().fingerprint();
                        List<Integer> ids = wallets.get().wallets().stream().map(Wallet::id).toList();
                        Map<Integer, WalletState> walletStates = monitorState.getWalletState(fingerprint);

                        List<WalletBalance> walletBalances = new ArrayList<>();
                        for (var id : ids) { walletApi.getWalletBalance(id).data().ifPresent(walletBalances::add); }
                        for (var wb : walletBalances) {
                            if (walletStates.containsKey(wb.walletId())) {
                                walletStates.get(wb.walletId()).updateBalance(wb);
                            } else {
                                walletStates.put(wb.walletId(), WalletState.fromRWalletBalance(wb));
                            }
                        }
                    }
                }
            } catch (RPCException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


