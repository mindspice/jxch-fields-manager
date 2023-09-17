package io.mindspice.jxch.fields.data.structures;

import io.mindspice.jxch.fields.data.enums.LookUpTime;
import io.mindspice.jxch.fields.data.enums.SpInterval;
import io.mindspice.jxch.fields.data.metrics.chia.FarmingMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.PlotMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.SignagePointMetrics;
import io.mindspice.jxch.fields.data.util.DataUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class NodeStatistics {
    private final CircularBuffer<FarmingMetrics> signagePointMetrics = new CircularBuffer<>(9217); // +1 to keep old sum to remove
    private final Map<LookUpTime, Integer> lookupTime = new EnumMap<>(LookUpTime.class);
    // Pairs index are (average, running sum)
    private final Map<SpInterval, Pair<Double, Long>> spIntervalAverages = new EnumMap<>(SpInterval.class);
    private Pair<Double, Double> avgLookupTime;
    private Pair<Integer, Long> avgEligiblePlots;
    private double maxLookupTime;
    private boolean doneInit = false;
    private volatile double netSpaceEiB = -1;
    private final Object mutex = new Object();

    public FarmingMetrics getLastSpMetrics() {
        synchronized (mutex) {
            return signagePointMetrics.getFromOldest(0);
        }
    }

    public List<FarmingMetrics> getAllSpMetrics() {
        synchronized (mutex) {
            return signagePointMetrics.getAsList();
        }
    }

    public void addFilterMetric(SignagePointMetrics metrics) {
        synchronized (mutex) {
            if (!doneInit) {
                initFirstEntry(metrics);
                return;
            }
            updateSpAverages(metrics.epochMilli());
            incLookupTime(metrics.epochMilli());
            var plotMetrics = calcPlotMetrics(metrics.eligiblePlots());
            signagePointMetrics.add(new FarmingMetrics(metrics, plotMetrics));
        }
    }

    public void updateNetSpace(double netSpace) {
        netSpace = netSpaceEiB;
    }

    // Populate the keys and default values for the maps
    private void initFirstEntry(SignagePointMetrics metrics) {
        for (var key : SpInterval.values()) {
            spIntervalAverages.put(key, new Pair<>(DataUtil.limitPrecision3D(metrics.lookupTime()), metrics.epochMilli()));
        }
        for (var key : LookUpTime.values()) { lookupTime.put(key, 0); }
        incLookupTime(metrics.lookupTime());
        avgLookupTime = new Pair<>(metrics.lookupTime(), metrics.lookupTime());
        avgEligiblePlots = new Pair<>(metrics.eligiblePlots(), (long) metrics.eligiblePlots());
    }

    // Loops through and subtracts the old sum (+1 index further than interval) from the running sum
    // Then adds the new metric to the running sum and recalculates average. Prevent having to loop
    // all values for each update. If not enough filterMetrics exist for a given interval then the
    // maximum interval that can be averaged is used for the remaining
    private void updateSpAverages(long newEpochMilli) {
        int spCount = signagePointMetrics.size();
        for (var intvKey : SpInterval.values()) {
            // Skip the 2 keys with a value of Integer Max, these are used for other stats
            if (intvKey.value == Integer.MAX_VALUE) { continue; }
            if (spCount > intvKey.value) {
                var oldMetric = signagePointMetrics.getFromNewest(intvKey.value);
                var currAvg = spIntervalAverages.get(intvKey);
                long newSum = currAvg.second() - oldMetric.signagePointMetrics().epochMilli() + newEpochMilli;
                double newAvg = DataUtil.limitPrecision3D((double) newSum / intvKey.value);
                spIntervalAverages.put(intvKey, new Pair<>(newAvg, newSum));
            } else {
                var currAvg = spIntervalAverages.get(intvKey);
                long newSum = currAvg.second() + newEpochMilli;
                double newAvg = (double) newSum / signagePointMetrics.size();
                spIntervalAverages.put(intvKey, new Pair<>(newAvg, newSum));
            }
        }
    }

    // Decrement lookup time bucket with the oldest lookup that is being rotated out if needed
    // Increment the new bucket with the new time if needed
    private void updateLookUptimes(float lookTime) {
        decLookupTime(signagePointMetrics.getFromOldest(0).signagePointMetrics().epochMilli());
        incLookupTime(lookTime);
    }

    // Can use compute since value will always be present
    private void incLookupTime(double lookUpTime) {
        LookUpTime luKey = LookUpTime.getFromTime(lookUpTime);
        if (luKey != LookUpTime._IGNORE) {
            lookupTime.compute(luKey, (k, v) -> v + 1);
        }
    }

    private void decLookupTime(double lookUpTime) {
        LookUpTime luKey = LookUpTime.getFromTime(lookUpTime);
        if (luKey != LookUpTime._IGNORE) {
            lookupTime.compute(luKey, (k, v) -> v > 0 ? v - 1 : v);
        }
    }

    private void calMaxAndAverageLookups(double newLookupTime) {
        if (newLookupTime > maxLookupTime) { maxLookupTime = newLookupTime; }
        var oldAvg = avgLookupTime;
        double newSum = signagePointMetrics.isFull()
                ? (oldAvg.second() - signagePointMetrics.getFromOldest(0).signagePointMetrics().lookupTime() + newLookupTime)
                : oldAvg.second() + newLookupTime;
        double newAvg = newSum / signagePointMetrics.size() - 1;
        avgLookupTime = new Pair<>(newAvg, newSum);
    }

    private PlotMetrics calcPlotMetrics(int newEligible) {
        var oldAvg = avgEligiblePlots;
        long newSum = signagePointMetrics.isFull()
                ? (oldAvg.second() - signagePointMetrics.getFromOldest(0).signagePointMetrics().eligiblePlots() + newEligible)
                : oldAvg.second() + newEligible;
        int newAvg = Math.round((int) (newSum / (signagePointMetrics.size() - 1)));

        avgEligiblePlots = new Pair<>(newAvg, newSum);
        int effectiveSpace = (int) (avgEligiblePlots.first() * 512 * 101.4) / 1024;
        double dailyPct = 1.0 - Math.pow((1.0 - effectiveSpace / netSpaceEiB), 4608);
        double etw = DataUtil.limitPrecision2D(1 / dailyPct);
        return new PlotMetrics(
                avgEligiblePlots.first(),
                effectiveSpace,
                netSpaceEiB == -1 ? -1 : etw
        );
    }
}


