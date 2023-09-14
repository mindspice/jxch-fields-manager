package io.mindspice.jxch.fields.data.util;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;


public class DataUtil {
    public final static int HZ_DIV = 1000000000;
    public final static int BYTE_MIB_DIV = 1 << 20;
    public final static int BYTE_GIB_DIV = 1 << 30;
    public final static long BYTE_TIB_DIV = 1L << 40;
    public final static long BYTE_PIB_DIV = 1L << 50;
    public final static long BYTE_EIB_DIV = 1L << 60;
    public final static BigInteger BYTE_EIB_DIV_BI = BigInteger.valueOf(1L << 60);
    private final static DecimalFormat df = new DecimalFormat("#.##");
    public static DateTimeFormatter dtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");


    public static double[] hzToGhz(long[] cores) {
        var rtnArr = new double[cores.length];
        for (int i = 0; i < cores.length; ++i) {
            rtnArr[i] = limitPrecision2D(hzToGhz(cores[i]));
        }
        return rtnArr;
    }

    public static double hzToGhz(long hz) { return 1d * hz / HZ_DIV; }

    public static double hzToGhz(double hz) { return hz / HZ_DIV; }

    public static double bytesToMiB(long bytes) { return limitPrecision2D(1d * bytes / BYTE_MIB_DIV); }

    public static double bytesToGiB(long bytes) { return limitPrecision2D(1d * bytes / BYTE_GIB_DIV); }

    public static double bytesToTiB(long bytes) { return limitPrecision2D(1d * bytes / BYTE_TIB_DIV); }

    public static double bytesToaPiB(long bytes) { return limitPrecision2D(1d * bytes / BYTE_PIB_DIV); }

    public static double bytesToEiB(long bytes) { return limitPrecision2D(1d * bytes / BYTE_EIB_DIV); }

    public static double bytesToEiB(BigInteger bytes) {
        BigInteger[] results = bytes.divideAndRemainder(BYTE_EIB_DIV_BI);
        return limitPrecision2D(results[0].doubleValue() + (results[1].doubleValue() / BYTE_EIB_DIV));
    }

    public static DecimalFormat Formatter() { return df; }

    public static double limitPrecision2D(double d) { return Math.round(d * 100.0) / 100.0; }
    public static double limitPrecision3D(double d) { return Math.round(d * 1000.0) / 1000.0; }
    public static double toPercentage(double d) { return Math.round(d * 10000) / 100.0; }


}


