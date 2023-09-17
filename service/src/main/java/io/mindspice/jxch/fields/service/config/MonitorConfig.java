package io.mindspice.jxch.fields.service.config;

import java.util.List;


public class MonitorConfig {
    private String monitorName;
    private boolean isHarvester;
    private boolean doWebService = false;
    private boolean doRpcMetrics;
    private String webServiceApiKey = null;
    private boolean doSystemMetrics = true;
    private String pathToNodeConfig;
    private boolean doLogParsing = true;
    private String pathToLog = "";
    private int maxChiaHistorySize = 1440;
    private int maxSystemHistorySize = 1440;
    private int maxSubSlotHistorySize = 144;
    private int maxErrorWarnHistorySize;
    private List<String> harvesterConfigs;
    private boolean doHDDCheck;

    public String getMonitorName() {
        return monitorName;
    }

    public boolean isHarvester() {
        return isHarvester;
    }

    public boolean isDoWebService() {
        return doWebService;
    }

    public String getWebServiceApiKey() {
        return webServiceApiKey;
    }

    public boolean isDoSystemMetrics() {
        return doSystemMetrics;
    }

    public String getPathToNodeConfig() {
        return pathToNodeConfig;
    }

    public boolean isDoLogParsing() {
        return doLogParsing;
    }

    public String getPathToLog() {
        return pathToLog;
    }

    public int getMaxChiaHistorySize() {
        return maxChiaHistorySize;
    }

    public int getMaxSystemHistorySize() {
        return maxSystemHistorySize;
    }

    public List<String> getHarvesterConfigs() {
        return harvesterConfigs;
    }

    public boolean isDoHDDCheck() {
        return doHDDCheck;
    }

    public int getMaxSubSlotHistorySize() {
        return maxSubSlotHistorySize;
    }

    public boolean isDoRpcMetrics() {
        return doRpcMetrics;
    }

    public int getMaxErrorWarnHistorySize() {
        return maxErrorWarnHistorySize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitorConfig: ");
        sb.append("\n  monitorName: \"").append(monitorName).append('\"');
        sb.append(",\n  isHarvester: ").append(isHarvester);
        sb.append(",\n  doWebService: ").append(doWebService);
        sb.append(",\n  doRpcMetrics: ").append(doRpcMetrics);
        sb.append(",\n  webServiceApiKey: \"").append(webServiceApiKey).append('\"');
        sb.append(",\n  doSystemMetrics: ").append(doSystemMetrics);
        sb.append(",\n  pathToNodeConfig: \"").append(pathToNodeConfig).append('\"');
        sb.append(",\n  doLogParsing: ").append(doLogParsing);
        sb.append(",\n  pathToLog: \"").append(pathToLog).append('\"');
        sb.append(",\n  maxChiaHistorySize: ").append(maxChiaHistorySize);
        sb.append(",\n  maxSystemHistorySize: ").append(maxSystemHistorySize);
        sb.append(",\n  maxSubSlotHistorySize: ").append(maxSubSlotHistorySize);
        sb.append(",\n  maxErrorWarnHistorySize: ").append(maxErrorWarnHistorySize);
        sb.append(",\n  harvesterConfigs: ").append(harvesterConfigs);
        sb.append(",\n  doHDDCheck: ").append(doHDDCheck);
        sb.append("\n");
        return sb.toString();
    }
}