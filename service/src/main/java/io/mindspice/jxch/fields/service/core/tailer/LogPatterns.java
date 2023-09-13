package io.mindspice.jxch.fields.service.core.tailer;

import java.util.regex.Pattern;


public class LogPatterns {
    public static final Pattern DATE_TIME = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");

    // Extracts 2 groups from the index of a finished signage points ex. 12/64 = {12,64}
    public static final Pattern FINISHED_SIGNAGE_POINT = Pattern.compile(
            "(\\d+)" +  // Match/group <int>
                    "\\/" +     // Match '/'
                    "(\\d+)"    // Match/group <int>
    );

    // Extracts deficit and signals end of sub slot
    public static final Pattern FINISHED_SUB_SLOT = Pattern.compile("\\bDeficit\\s(\\d+)");

    // Get cached signage point index
    public static final Pattern CACHED_SIGNAGE_POINT = Pattern.compile("(\\bcaching signage point)\\s(\\d+)");

    // Extracts 3 groups from a finished block {SP<int>, validation time<float>, cost<long>}
    public static final Pattern FARMED_BLOCK = Pattern.compile(
            "\\bSP:\\s(\\d+)" +
                    ".*" +
                    "validation time:\\s+(\\d+\\.\\d+)" +
                    ".*" +
                    "cost:\\s(\\d+)"
    );

    // Extracts 3 groups from a finished block
    // {SP<int>, farmer response time <float>, validation time<float>, cost<long>, percent full <float>}
    public static final Pattern NON_FARMED_BLOCK = Pattern.compile(
            "\\bSP:\\s+(\\d+)" +
                    ".*" +
                    "farmer response time:\\s+(\\d+\\.\\d+)" +
                    ".*" +
                    "validation time:\\s+(\\d+\\.\\d+)" +
                    ".*" +
                    "cost:\\s+(\\d+)" +
                    ".*" +
                    "percent full:\\s+(\\d+\\.\\d+)"
    );

    // Extract 3 groups from new peak height {height<int>, weight<long>, difficulty<int>
    public static final Pattern UPDATED_PEAK = Pattern.compile(
            "\\bheight\\s(\\d+)" +
                    ".*" +
                    "weight\\s(\\d+)" +
                    ".*" +
                    "difficulty:\\s(\\d+)"

    );

    // Extract 3 groups from mempool info {size<int>, cost<long>, min 5M mojo fee<int>}
    public static final Pattern MEMPOOL_INFO = Pattern.compile(
            "\\bmempool:\\s(\\d+)" +
                    ".*" +
                    "cost:\\s(\\d+)" +
                    ".*" +
                    "tx:\\s(\\d+)"
    );

    // Extract 4 groups from eligible plots {eligible plots<int>, proofs found found <int>, lookup time <float>, total plots<int>}
    public static final Pattern ELIGIBLE_PLOTS = Pattern.compile(
            "(\\d+) plots were eligible " +
                    ".* Found " +
                    "(\\d+) proofs\\. Time: " +
                    "(\\d+\\.\\d+) s\\. Total " +
                    "(\\d+) plots"
    );

    //Extract pool error code
    public static final Pattern POOL_ERROR = Pattern.compile("Error in pooling: \\((\\d+),");


}
