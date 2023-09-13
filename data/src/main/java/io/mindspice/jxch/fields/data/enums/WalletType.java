package io.mindspice.jxch.fields.data.enums;

import java.util.Arrays;


public enum WalletType {
    STANDARD_WALLET(0),
    ATOMIC_SWAP(2),
    AUTHORIZED_PAYEE(3),
    MULTI_SIG(4),
    CUSTODY(5),
    CAT(6),
    RECOVERABLE(7),
    DECENTRALIZED_ID(8),
    POOLING_WALLET(9),
    NFT(10),
    DATA_LAYER(11),
    DATA_LAYER_OFFER(12),
    VC(13),
    UNKNOWN(-1);

    public final int code;

    WalletType(int code) { this.code = code; }

    public static WalletType fromCode(int code) {
        return Arrays.stream(WalletType.values())
                .filter(c -> c.code == code)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
