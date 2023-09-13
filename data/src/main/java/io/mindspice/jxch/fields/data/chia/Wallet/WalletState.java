package io.mindspice.jxch.fields.data.chia.Wallet;

import io.mindspice.jxch.fields.data.enums.WalletType;
import io.mindspice.jxch.rpc.schemas.wallet.WalletBalance;

import java.util.Arrays;


public class WalletState {
    private final WalletType type;
    private final long fingerprint;
    private final String assetId;
    private final int walletId;
    private volatile long confirmedBalance = 0;
    private volatile long unconfirmedBalance;
    private volatile long pendingChange = 0;
    private volatile long spendableBalance = 0;
    private volatile long maxSendAmount = 0;
    private volatile int unspentCoinCount;
    private volatile int pendingCoinRemovalCount;

    public WalletState(int type, long fingerprint, String assetId, int walletId, long confirmedBalance,
            long unconfirmedBalance, long pendingChange, long spendableBalance, long maxSendAmount,
            int unspentCoinCount, int pendingCoinRemovalCount) {
        this.type = WalletType.fromCode(type);
        this.fingerprint = fingerprint;
        this.assetId = assetId;
        this.walletId = walletId;
        this.confirmedBalance = confirmedBalance;
        this.unconfirmedBalance = unconfirmedBalance;
        this.pendingChange = pendingChange;
        this.spendableBalance = spendableBalance;
        this.maxSendAmount = maxSendAmount;
        this.unspentCoinCount = unspentCoinCount;
        this.pendingCoinRemovalCount = pendingCoinRemovalCount;
    }

    public static WalletState fromRWalletBalance(WalletBalance balance) {
        var type = Arrays.stream(WalletType.values())
                .filter(t -> t.code == balance.walletType())
                .findFirst().orElse(WalletType.STANDARD_WALLET);
        return new WalletState(
                balance.walletType(),
                balance.fingerprint(),
                balance.assetId(),
                balance.walletId(),
                balance.confirmedWalletBalance(),
                balance.unconfirmedWalletBalance(),
                balance.pendingChange(),
                balance.spendableBalance(),
                balance.maxSendAmount(),
                balance.unspentCoinCount(),
                balance.pendingCoinRemovalCount()
        );
    }

    public void updateBalance(WalletBalance balance) {
        confirmedBalance = balance.confirmedWalletBalance();
        unconfirmedBalance = balance.unconfirmedWalletBalance();
        pendingChange = balance.pendingChange();
        spendableBalance = balance.spendableBalance();
        maxSendAmount = balance.maxSendAmount();
        unspentCoinCount = balance.unspentCoinCount();
        pendingCoinRemovalCount = balance.pendingCoinRemovalCount();
    }

    public WalletType getType() { return type; }

    public long getFingerprint() { return fingerprint; }

    public String getAssetId() { return assetId; }

    public int getWalletId() { return walletId; }

    public long getConfirmedBalance() { return confirmedBalance; }

    public long getUnconfirmedBalance() { return unconfirmedBalance; }

    public long getPendingChange() { return pendingChange; }

    public long getSpendableBalance() { return spendableBalance; }

    public long getMaxSendAmount() { return maxSendAmount; }

    public int getUnspentCoinCount() { return unspentCoinCount; }

    public int getPendingCoinRemovalCount() { return pendingCoinRemovalCount; }
}


