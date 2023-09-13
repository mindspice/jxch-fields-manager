package io.mindspice.jxch.fields.data.enums;

import java.util.Arrays;


public enum OsType {
    WINDOWS,
    LINUX,
    UNKNOWN;

    public static OsType FromString(String osString) {
        return Arrays.stream(OsType.values())
                .filter(e -> osString.toUpperCase().contains(e.name()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}

