package io.mindspice.jxch.fields.service.core.tailer;

import io.mindspice.jxch.fields.data.chia.SignagePointMetrics;
import io.mindspice.jxch.fields.data.chia.node.BlockInfo;
import io.mindspice.jxch.fields.data.chia.node.MempoolMetrics;
import io.mindspice.jxch.fields.data.chia.node.SignagePointState;
import io.mindspice.jxch.fields.data.chia.node.SubSlotInfo;
import io.mindspice.jxch.fields.data.enums.OsType;

import io.mindspice.jxch.fields.data.enums.PoolErrorCode;
import io.mindspice.jxch.fields.service.core.MonitorState;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;


public class LogParser {
    private final MonitorState monitorState;
    private final Map<Integer, SignagePointState> subSlotMap = new HashMap<>(64);
    private Tailer tailer;
    private volatile SubSlotInfo currentSubSlot = new SubSlotInfo();

    public LogParser(MonitorState monitorState) { this.monitorState = monitorState; }

    public Tailer getTailer(String logfile) {
        File logFile = new File(logfile);
        LogTailer logTailer = new LogTailer();
        OsType osType = OsType.FromString(System.getProperty("os.name"));
        if (osType == OsType.WINDOWS) {

        }

        return Tailer.builder()
                .setTailerListener(logTailer)
                .setFile(logFile)
                .setDelayDuration(Duration.ofSeconds(2))
                .setStartThread(false)
                .get();
    }

    private class LogTailer extends TailerListenerAdapter {

        @Override
        public void handle(String line) {
            //TODO get epoch times

            if (line.contains("ERROR")) {
                parseError(line);
                return;
            }
            if (line.contains("WARNING")) {
                parseWarning(line);
                return;
            }
            if (line.contains("chia.full_node.full_node")) {
                parseNodeInfo(line);
                return;
            }
            if (line.contains("chia.harvester.harvester")) {
                parseHarvester(line);
                return;
            }
            if (line.contains("chia.full_node.mempool_manager")) {
                parseMemPool(line);
                return;
            }
            if (line.contains("chia.farmer.farmer")) {
                parseFarmer(line);
                return;
            }
        }

        private void parseNodeInfo(String line) {
            if (line.contains("Finished signage point")) {
                Matcher spIndex = LogPatterns.FINISHED_SIGNAGE_POINT.matcher(line);
                if (spIndex.find()) {
                    int index = Integer.parseInt(spIndex.group(1));
                    LocalDateTime dateTime = getDateTime(line);
                    subSlotMap.put(index, new SignagePointState(index, dateTime));
                }
                return;
            }

            if (line.contains("Updated peak")) {
                Matcher updatedPeak = LogPatterns.UPDATED_PEAK.matcher(line);
                if (updatedPeak.find()) {
                    int height = Integer.parseInt(updatedPeak.group(1));
                    long weight = Long.parseLong(updatedPeak.group(2));
                    int difficulty = Integer.parseInt(updatedPeak.group(3));
                    monitorState.setPeakMetrics(height, weight, difficulty);
                }
                return;
            }

            if (line.contains("Added unfinished_block")) {
                Matcher block = LogPatterns.NON_FARMED_BLOCK.matcher(line);
                if (block.find()) {
                    Integer idx = Integer.valueOf(block.group(1));
                    BlockInfo blockInfo = new BlockInfo(
                            false,
                            Float.parseFloat(block.group(2)),
                            Float.parseFloat(block.group(3)),
                            Long.parseLong(block.group(4)),
                            Float.parseFloat(block.group(5))
                    );
                    var spInfo = subSlotMap.get(idx);
                    // if (spInfo != null) { spInfo.addBlockInfo(blockInfo); }
                }
                return;
            }

            if (line.contains("Finished sub slot")) {
                Matcher subSlot = LogPatterns.FINISHED_SUB_SLOT.matcher(line);
                if (subSlot.find()) {
                    int deficit = Integer.parseInt(subSlot.group(1));
                }
                return;
            }

            if (line.contains("caching signage point")) {
                Matcher cachedSp = LogPatterns.CACHED_SIGNAGE_POINT.matcher(line);
                if (cachedSp.find()) {
                    int index = Integer.parseInt(cachedSp.group(1));
                }
                return;
            }

            if (line.contains("Farmed unfinished_block")) {
                Matcher farmedBlock = LogPatterns.FARMED_BLOCK.matcher(line);
                if (farmedBlock.find()) {
                    Integer idx = Integer.parseInt(farmedBlock.group(1));
                    BlockInfo blockInfo = new BlockInfo(
                            false,
                            Float.parseFloat(farmedBlock.group(2)),
                            Long.parseLong(farmedBlock.group(3))
                    );
                }
                return;
            }
        }

        private void parseMemPool(String line) {
            if (line.contains("Size of mempool")) {
                Matcher mempoolInfo = LogPatterns.MEMPOOL_INFO.matcher(line);
                if (mempoolInfo.find()) {
                    MempoolMetrics mpMetrics = new MempoolMetrics(
                            Integer.parseInt(mempoolInfo.group(1)),
                            Long.parseLong(mempoolInfo.group(2)),
                            -1,
                            -1,
                            Long.parseLong(mempoolInfo.group(3))
                    );
                }
            }

        }

        private void parseHarvester(String line) {
            if (line.contains("eligible for farming")) {
                Matcher eligiblePlots = LogPatterns.ELIGIBLE_PLOTS.matcher(line);
                if (eligiblePlots.find()) {
                    SignagePointState sp;
                    SignagePointMetrics spMetrics = sp.getAsMetrics(
                            Integer.parseInt(eligiblePlots.group(1)),
                            Integer.parseInt(eligiblePlots.group(2)),
                            Float.parseFloat(eligiblePlots.group(3)),
                            Integer.parseInt(eligiblePlots.group(4))
                    );

                }
            }

        }

        private void parseFarmer(String line) {
            if (line.contains("Error in pooling")) {
                Matcher poolError = LogPatterns.POOL_ERROR.matcher(line);
                if (poolError.find()) {
                    PoolErrorCode errorCode = PoolErrorCode.fromCode(Integer.parseInt(poolError.group(1)));
                }
            }

        }

        private void parseError(String line) {
            if (line.contains("Task exception was never retrieved")) {
                return; // Ignored. Only time I have seen this is attempting to send ping to disconnect WS connections
            }

            if (line.contains("Ex")) {
                System.out.println(line);
                String exception = line.substring(line.indexOf("Ex"));
            }
        }

        private void parseWarning(String line) {

        }

        @Override
        public void handle(Exception ex) {

        }

        @Override
        public void fileNotFound() {
            tailer.close();

        }

        private LocalDateTime getDateTime(String line) {
            Matcher dataTime = LogPatterns.DATE_TIME.matcher(line);
            if (dataTime.find()) {
                return LocalDateTime.parse(dataTime.group(1), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                return LocalDateTime.MIN;
            }
        }

    }

}
