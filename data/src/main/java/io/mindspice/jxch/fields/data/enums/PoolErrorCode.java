package io.mindspice.jxch.fields.data.enums;

import java.util.Arrays;


public enum PoolErrorCode {
    REVERTED_SIGNAGE_POINT(1),
    TOO_LATE(2),
    NOT_FOUND(3),
    INVALID_PROOF(4),
    PROOF_NOT_GOOD_ENOUGH(5),
    INVALID_DIFFICULTY(6),
    INVALID_SIGNATURE(7),
    SERVER_EXCEPTION(8),
    INVALID_P2_SINGLETON_PUZZLE_HASH(9),
    FARMER_NOT_KNOWN(10),
    FARMER_ALREADY_KNOWN(11),
    INVALID_AUTHENTICATION_TOKEN(12),
    INVALID_PAYOUT_INSTRUCTIONS(13),
    INVALID_SINGLETON(14),
    DELAY_TIME_TOO_SHORT(15),
    REQUEST_FAILED(16),
    UNKNOWN(-1);

    PoolErrorCode(int code) { this.code = code; }

    public final int code;

    public static PoolErrorCode fromCode(int code){
       return Arrays.stream(PoolErrorCode.values())
               .filter(e -> e.code == code)
               .findFirst()
               .orElse(UNKNOWN);
    }
}
