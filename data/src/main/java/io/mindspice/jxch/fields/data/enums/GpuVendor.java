package io.mindspice.jxch.fields.data.enums;

import java.util.Arrays;


public enum GpuVendor {
    NVIDIA,
    AMD,
    UNKNOWN;

    public static GpuVendor FromString(String vendorString) {
        return Arrays.stream(GpuVendor.values())
                .filter(e -> vendorString.toUpperCase().contains(e.name()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
