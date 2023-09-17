package io.mindspice.jxch.fields.data.metrics.chia;

import io.mindspice.jxch.fields.data.enums.SubSlotEndState;
import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.fields.data.structures.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public record SubSlotMetrics(
        LocalDateTime startTime,
        LocalDateTime endTime,
        int deficit,
        List<BlockMetrics> blocks,
        SubSlotEndState endState,
        Map<Integer, SignagePointMetrics> signagePoints,
        List<Pair<LocalDateTime, Integer>> cachedSignagePoints,
        List<Pair<LocalDateTime, Integer>> duplicateSignagePoints,
        int[] missedSignagePoints
)implements ClientMsg { }


