package io.mindspice.jxch.fields.data.chia;

import io.mindspice.jxch.fields.data.util.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public record SubSlotMetrics(
        LocalDateTime startTime,
        LocalDateTime endTime,
        int deficit,
        Map<Integer, SignagePointInfo> signagePoints,
        List<Pair<LocalDateTime, Integer>> cachedSignagePoints,
        List<Pair<LocalDateTime, Integer>> duplicateSignagePoints,
        List<BlockInfo> blocks
) { }


