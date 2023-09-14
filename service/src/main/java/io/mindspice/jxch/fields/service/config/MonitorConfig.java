package io.mindspice.jxch.fields.service.config;

import java.util.List;


public class MonitorConfig {
    private String nodeName;
    private boolean isHarvester;
    private boolean doWebService = false;
    private String webServiceApiKey = null;
    private boolean doSystemMetrics = true;
    private String pathToNodeConfig = "";
    private boolean doLogParsing = true;
    private String pathToLog = "";
    private int maxChiaHistorySize = 1440;
    private int maxSystemHistorySize = 1440;
    private List<String> harvesterConfigs;
    private boolean doHDDCheck;

    public String getNodeName() {
        return nodeName;
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
}