package io.mindspice.jxch.fields.data.chia.node;

import io.mindspice.jxch.fields.data.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SubSlotInfo {
    private int deficit;
    private Map<Integer, SignagePointState> signagePoints = new HashMap<>(63);
    private List<Pair<Integer, LocalDateTime>> cachedSignagePoints = new ArrayList<>(4);
    private List<Pair<Integer, LocalDateTime>> duplicateSignagePoints;

    public void finalize(int deficit) {
        this.deficit = deficit;
        this.signagePoints = signagePoints;
    }

    public void addCachedSignagePoint(LocalDateTime dateTime, Integer sp) {
        cachedSignagePoints.add(new Pair<>(sp, dateTime));
    }

    public void addSignagePoint(Integer index, SignagePointState signagePoint) {
        if (signagePoints.containsKey(index)) {
            if (duplicateSignagePoints.isEmpty()) { duplicateSignagePoints = new ArrayList<>(); }
            duplicateSignagePoints.add(new Pair<>(index, signagePoint.getDateTime()));
        }
    }

    public void getSignagePoint() {

    }

}
