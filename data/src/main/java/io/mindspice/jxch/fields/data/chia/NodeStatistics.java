package io.mindspice.jxch.fields.data.chia;

import io.mindspice.jxch.fields.data.enums.LookUpTime;
import io.mindspice.jxch.fields.data.enums.SpInterval;
import io.mindspice.jxch.fields.data.metrics.chia.FilterMetrics;
import io.mindspice.jxch.fields.data.structures.CircularBuffer;
import io.mindspice.jxch.fields.data.util.DataUtil;
import io.mindspice.jxch.fields.data.util.Pair;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class NodeStatistics {
    private final CircularBuffer<FilterMetrics> filterMetrics = new CircularBuffer<>(9217); // +1 to keep old sum to remove
    private final Map<LookUpTime, Integer> lookupTime = new EnumMap<>(LookUpTime.class);
    // Pairs indexes are (average, running sum)
    private final Map<SpInterval, Pair<Double, Long>> spIntervalAverages = new EnumMap<>(SpInterval.class);
    private Pair<Double, Double> avgLookupTime;
    private double maxLookupTime;
    private boolean doneInit = false;

    public FilterMetrics getLastFilterMetrics() {
        synchronized (filterMetrics) {
            return filterMetrics.getFromOldest(0);
        }
    }

    public List<FilterMetrics> getAllFilterMetrics() {
        synchronized (filterMetrics) {
            return filterMetrics.getAsList();
        }
    }

    public void addFilterMetric(FilterMetrics metrics) {
        synchronized (filterMetrics) {
            if (!doneInit) {
                initFirstEntry(metrics);
                return;
            }
            filterMetrics.add(metrics);
            updateSpAverages(metrics.epochMilli());
            incLookupTime(metrics.epochMilli());
        }
    }

    // Populate the keys and default values for the maps
    private void initFirstEntry(FilterMetrics metrics) {
        for (var key : SpInterval.values()) {
            spIntervalAverages.put(key, new Pair<>(DataUtil.limitPrecision3D(metrics.lookupTime()), metrics.epochMilli()));
        }
        for (var key : LookUpTime.values()) { lookupTime.put(key, 0); }
        incLookupTime(metrics.lookupTime());
        avgLookupTime = new Pair<>(metrics.lookupTime(), metrics.lookupTime());
    }

    // Loops through and subtracts the old sum (+1 index further than interval) from the running sum
    // Then adds the new metric to the running sum and recalculates average. Prevent having to loop
    // all values for each update. If not enough filterMetrics exist for a given interval then the
    // maximum interval that can be averaged is used for the remaining
    private void updateSpAverages(long newEpochMilli) {
        int filterCount = filterMetrics.size();
        for (var intvKey : SpInterval.values()) {
            // Skip the 2 keys with a value of Integer Max, these are used for other stats
            if (intvKey.value == Integer.MAX_VALUE) { continue; }
            if (filterCount > intvKey.value) {
                var oldMetric = filterMetrics.getFromNewest(intvKey.value);
                var currAvg = spIntervalAverages.get(intvKey);
                long newSum = currAvg.second() - oldMetric.epochMilli() + newEpochMilli;
                double newAvg = DataUtil.limitPrecision3D((double) newSum / intvKey.value);
                spIntervalAverages.put(intvKey, new Pair<>(newAvg, newSum));
            } else {
                var currAvg = spIntervalAverages.get(intvKey);
                long newSum = currAvg.second() + newEpochMilli;
                double newAvg = (double) newSum / filterMetrics.size();
                spIntervalAverages.put(intvKey, new Pair<>(newAvg, newSum));
            }
        }
    }

    // Decrement lookup time bucket with the oldest lookup that is being rotated out if needed
    // Increment the new bucket with the new time if needed
    private void updateLookUptimes(float lookTime) {
        decLookupTime(filterMetrics.getFromOldest(0).epochMilli());
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
        double newSum = filterMetrics.isFull()
                ? (oldAvg.second() - filterMetrics.getFromOldest(0).lookupTime() + newLookupTime)
                : oldAvg.second() + newLookupTime;
        double newAvg = newSum / filterMetrics.size() - 1;
        avgLookupTime = new Pair<>(newAvg, newSum);
    }

}


