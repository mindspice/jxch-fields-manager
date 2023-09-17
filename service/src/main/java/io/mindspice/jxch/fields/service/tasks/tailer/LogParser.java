package io.mindspice.jxch.fields.service.tasks.tailer;

import io.mindspice.jxch.fields.data.metrics.chia.*;
import io.mindspice.jxch.fields.data.structures.SignagePointInfo;
import io.mindspice.jxch.fields.data.enums.OsType;

import io.mindspice.jxch.fields.data.enums.PoolErrorCode;
import io.mindspice.jxch.fields.service.core.MonitorState;
import io.mindspice.jxch.rpc.enums.ChiaService;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;


public class LogParser {
    private final MonitorState monitorState;
    private boolean firstTail;
    private boolean getChainMetrics = true;

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

    public void disableChainMetrics() {
        getChainMetrics = false;
    }

    private class LogTailer extends TailerListenerAdapter {
        private SignagePointInfo currentSignagePoint = null;

        @Override
        public void handle(String line) {
            if (line.contains("ERROR")) {
                try {
                    parseError(line);
                } catch (Exception e) {
                    System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                            + " | Unhandled Exception parsing error info: " + e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                }
                return;
            }
            if (line.contains("WARNING")) {
                try {
                    parseWarning(line);
                    return;
                } catch (Exception e) {
                    System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                            + " | Unhandled Exception parsing warning info: " + e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                }
            }
            if (line.contains("chia.full_node.full_node")) {
                try {
                    parseNodeInfo(line);
                    return;
                } catch (Exception e) {
                    System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                            + " | Unhandled Exception parsing node info: " + e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                }
            }
            if (line.contains("chia.harvester.harvester")) {
                try {
                    parseHarvester(line);
                    return;
                } catch (Exception e) {
                    System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                            + " | Unhandled Exception parsing harvester info: " + e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                }
            }
            if (line.contains("chia.full_node.mempool_manager")) {
                try {
                    parseMemPool(line);
                    return;
                } catch (Exception e) {
                    System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                            + " | Unhandled Exception parsing mempool info: " + e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                }
            }
            if (line.contains("chia.farmer.farmer")) {
                try {
                    parseFarmer(line);
                    return;
                } catch (Exception e) {
                    System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                            + " | Unhandled Exception parsing farmer info: " + e.getMessage());
                    Arrays.stream(e.getStackTrace()).forEach(System.out::println);
                }
            }
        }

        private void parseNodeInfo(String line) {
            if (line.contains("Finished signage point")) {
                Matcher spIndex = LogPatterns.FINISHED_SIGNAGE_POINT.matcher(line);
                if (spIndex.find()) {
                    int index = Integer.parseInt(spIndex.group(1));
                    LocalDateTime dateTime = getDateTime(line);
                    currentSignagePoint = new SignagePointInfo(index, dateTime);
                }
                return;

            }

            if (line.contains("Updated peak")) {
                Matcher updatedPeak = LogPatterns.UPDATED_PEAK.matcher(line);
                if (updatedPeak.find()) { // Only update height if full metrics are gotten by node
                    if (!getChainMetrics) {
                        monitorState.updateHeight(Integer.parseInt(updatedPeak.group(1)));
                        return;
                    }
                    monitorState.addChainMetrics(
                            new ChainMetrics(
                                    Integer.parseInt(updatedPeak.group(1)),
                                    Integer.parseInt(updatedPeak.group(3)),
                                    Long.parseLong(updatedPeak.group(2)),
                                    -1,
                                    true,
                                    false,
                                    Integer.parseInt(updatedPeak.group(1)))
                    );
                }
                return;
            }

            if (line.contains("Added unfinished_block")) {
                Matcher block = LogPatterns.NON_FARMED_BLOCK.matcher(line);
                if (block.find()) {
                    int idx = Integer.parseInt(block.group(1));
                    BlockMetrics blockMetrics = new BlockMetrics(
                            idx,
                            false,
                            Float.parseFloat(block.group(2)),
                            Float.parseFloat(block.group(3)),
                            Long.parseLong(block.group(4)),
                            Float.parseFloat(block.group(5))
                    );
                    monitorState.addBlock(blockMetrics);
                }
                return;
            }

            if (line.contains("Finished sub slot")) {
                Matcher subSlot = LogPatterns.FINISHED_SUB_SLOT.matcher(line);
                if (subSlot.find()) {
                    int deficit = Integer.parseInt(subSlot.group(1));
                    monitorState.finalizeSubSlot(getDateTime(line), deficit);
                }
                return;
            }

            if (line.contains("caching signage point")) {
                Matcher cachedSp = LogPatterns.CACHED_SIGNAGE_POINT.matcher(line);
                if (cachedSp.find()) {
                    int index = Integer.parseInt(cachedSp.group(1));
                    monitorState.addCachedSignagePoint(getDateTime(line), index);
                }
                return;
            }

            if (line.contains("Farmed unfinished_block")) {
                Matcher farmedBlock = LogPatterns.FARMED_BLOCK.matcher(line);
                if (farmedBlock.find()) {
                    int idx = Integer.parseInt(farmedBlock.group(1));
                    BlockMetrics blockInfo = new BlockMetrics(
                            idx,
                            false,
                            Float.parseFloat(farmedBlock.group(2)),
                            Long.parseLong(farmedBlock.group(3))
                    );
                    monitorState.addBlock(blockInfo);
                }
                return;
            }
        }

        private void parseMemPool(String line) {
            if (line.contains("Size of mempool")) {
                if (!getChainMetrics) { return; } // skip if gotten by node
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
                return;
            }
        }

        private void parseHarvester(String line) {
            if (line.contains("eligible for farming")) {
                if (currentSignagePoint == null) { return; }
                Matcher eligiblePlots = LogPatterns.ELIGIBLE_PLOTS.matcher(line);
                if (eligiblePlots.find()) {
                    SignagePointMetrics spMetrics = currentSignagePoint.getAsMetrics(
                            Integer.parseInt(eligiblePlots.group(1)),
                            Integer.parseInt(eligiblePlots.group(2)),
                            Float.parseFloat(eligiblePlots.group(3)),
                            Integer.parseInt(eligiblePlots.group(4))
                    );
                    monitorState.addSignagePointMetrics(spMetrics);
                }
                return;
            }
        }

        private void parseFarmer(String line) {
            if (line.contains("Error in pooling")) {
                Matcher poolError = LogPatterns.POOL_ERROR.matcher(line);
                if (poolError.find()) {
                    PoolErrorCode errorCode = PoolErrorCode.fromCode(Integer.parseInt(poolError.group(1)));
                    monitorState.addErrorOrWarning(getDateTime(line), "Pooling Error: " + errorCode);
                }
                return;
            }

        }

        private void parseError(String line) {
            if (line.contains("Task exception was never retrieved")) {
                return; // Ignored. Only time I have seen this is attempting to send ping to disconnect WS connections
            }

            if (line.contains("Ex")) {
                String exception = line.substring(line.indexOf("Ex"));
                monitorState.addErrorOrWarning(getDateTime(line), exception);
                return;
            }
        }

        private void parseWarning(String line) {
        }

        @Override
        public void handle(Exception ex) {
            System.out.println("Monitor: " + monitorState.getConfig().getMonitorName()
                    + " | Tailer Exception: " + ex.getMessage() + "| Trace: " + Arrays.toString(ex.getStackTrace()));
        }

        @Override
        public void fileNotFound() {
            System.out.println("Monitor:" + monitorState.getConfig().getMonitorName()
                    + " | Log File Not Found: " + monitorState.getConfig().getPathToNodeConfig());
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
