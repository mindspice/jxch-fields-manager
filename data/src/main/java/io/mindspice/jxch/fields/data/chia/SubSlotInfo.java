package io.mindspice.jxch.fields.data.chia;

import io.mindspice.jxch.fields.data.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SubSlotInfo {
    private int deficit;
    private Map<Integer, SignagePointInfo> signagePoints = new HashMap<>(63);
    private List<Pair<LocalDateTime, Integer>> cachedSignagePoints = new ArrayList<>(4);
    private List<Pair<LocalDateTime, Integer>> duplicateSignagePoints;


    public void addDeficit(int deficit) { this.deficit = deficit; }

    public void addCachedSignagePoint(LocalDateTime dateTime, Integer sp) {
        cachedSignagePoints.add(new Pair<>(dateTime, sp));
    }

    public void addSignagePoint(Integer index, SignagePointInfo signagePoint) {
        if (signagePoints.containsKey(index)) {
            if (duplicateSignagePoints.isEmpty()) { duplicateSignagePoints = new ArrayList<>(); }
            duplicateSignagePoints.add(new Pair<>(signagePoint.getDateTime(), index));
        }
    }

    public int getDeficit() { return deficit; }

    public Map<Integer, SignagePointInfo> getSignagePoints() { return signagePoints; }

    public List<Pair<LocalDateTime, Integer>> getCachedSignagePoints() { return cachedSignagePoints; }

    public List<Pair<LocalDateTime, Integer>> getDuplicateSignagePoints() { return duplicateSignagePoints; }
}
