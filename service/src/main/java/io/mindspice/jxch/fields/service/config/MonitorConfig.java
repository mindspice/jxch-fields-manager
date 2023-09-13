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
    private int maxHistorySize = 1440;
    private List<String> harvesterConfigs;
    private boolean doHDDCheck;

    public String NodeName() {
        return nodeName;
    }

    public boolean isHarvester() {
        return isHarvester;
    }

    public boolean isDoWebService() {
        return doWebService;
    }

    public String WebServiceApiKey() {
        return webServiceApiKey;
    }

    public boolean isDoSystemMetrics() {
        return doSystemMetrics;
    }

    public String PathToNodeConfig() {
        return pathToNodeConfig;
    }

    public boolean isDoLogParsing() {
        return doLogParsing;
    }

    public String PathToLog() {
        return pathToLog;
    }

    public int MaxHistorySize() {
        return maxHistorySize;
    }

    public List<String> HarvesterConfigs() {
        return harvesterConfigs;
    }

    public boolean isDoHDDCheck() {
        return doHDDCheck;
    }
}