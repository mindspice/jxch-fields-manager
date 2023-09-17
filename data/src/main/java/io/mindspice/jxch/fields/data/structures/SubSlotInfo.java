package io.mindspice.jxch.fields.data.structures;

import io.mindspice.jxch.fields.data.enums.SubSlotEndState;
import io.mindspice.jxch.fields.data.metrics.chia.BlockMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.SignagePointMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.SubSlotMetrics;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;


public class SubSlotInfo {
    private Map<Integer, SignagePointMetrics> signagePoints = new HashMap<>(64);
    private List<BlockMetrics> blocks = new ArrayList<>(36); // 32 per sub-slot avg + 10%
    private List<Pair<LocalDateTime, Integer>> cachedSignagePoints;
    private List<Pair<LocalDateTime, Integer>> duplicateSignagePoints;
    private LocalDateTime startTime;
    private int spHeight = 0;
    private int deficit;
    private LocalDateTime endTime;
    private boolean awaitingLastFilter;

    public SubSlotInfo() { }

    public void init(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public boolean awaitingInit() { return startTime == null; }

    public SubSlotMetrics getAsMetrics() {
        SubSlotEndState endState;
        int[] missedSignagePoints = null;
        if (signagePoints.size() == 64) {
            endState = SubSlotEndState.COMPLETE;
        } else {
            if (signagePoints.isEmpty()) {
                missedSignagePoints = new int[0];
            } else {
                missedSignagePoints = IntStream
                        .rangeClosed(Collections.min(signagePoints.keySet()), 64)
                        .filter(i -> !signagePoints.containsKey(i))
                        .toArray();
            }
            endState = missedSignagePoints.length == 0 ? SubSlotEndState.PARTIAL : SubSlotEndState.INCOMPLETE;
        }
        return new SubSlotMetrics(
                startTime,
                endTime,
                deficit,
                blocks,
                endState,
                signagePoints,
                cachedSignagePoints,
                duplicateSignagePoints,
                missedSignagePoints
        );
    }

    public void setEndOfSubSlot(LocalDateTime endTime, int deficit) {
        this.endTime = endTime;
        this.deficit = deficit;
        awaitingLastFilter = true;
    }

    public void addCachedSignagePoint(LocalDateTime dateTime, Integer sp) {
        if (cachedSignagePoints == null) { cachedSignagePoints = new ArrayList<>(4); }
        cachedSignagePoints.add(new Pair<>(dateTime, sp));
    }

    public void addSignagePoint(int index, SignagePointMetrics metrics) {
        if (spHeight < index) { spHeight = index; }
        if (signagePoints.containsKey(index)) {
            if (duplicateSignagePoints == null) { duplicateSignagePoints = new ArrayList<>(); }
            duplicateSignagePoints.add(new Pair<>(metrics.dateTime(), index));
        } else {
            signagePoints.put(index, metrics);
        }
    }

    public void addBlock(BlockMetrics block) { blocks.add(block); }

    public Map<Integer, SignagePointMetrics> getSignagePoints() { return signagePoints; }

    public List<Pair<LocalDateTime, Integer>> getCachedSignagePoints() { return cachedSignagePoints; }

    public List<Pair<LocalDateTime, Integer>> getDuplicateSignagePoints() { return duplicateSignagePoints; }

    public List<BlockMetrics> getBlocks() { return blocks; }

    public LocalDateTime getStartTime() { return startTime; }

    public int getSpHeight() { return spHeight; }

    public int getDeficit() { return deficit; }

    public LocalDateTime getEndTime() { return endTime; }

    public boolean isAwaitingLastFilter() { return awaitingLastFilter; }
}
