package io.mindspice.jxch.fields.service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.mindspice.jxch.rpc.NodeConfig;
import io.mindspice.jxch.rpc.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class Config {
    private static Config INSTANCE;

    static {
        YAMLMapper mapper = new YAMLMapper();
        try {
            INSTANCE = JsonUtils.getMapper().readValue(new File("config.json"), Config.class);
            System.out.println(INSTANCE);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.yaml", e);
        }
    }

    // Client Settings
    private String bindAddress;
    private int bindPort;

    // Monitored Systems
    private List<MonitorConfig> monitoredSystems;

    public static Config GET() { return INSTANCE; }

    public String getBindAddress() { return bindAddress; }

    public int getBindPort() { return bindPort; }

    public List<MonitorConfig> getMonitoredSystems() { return monitoredSystems; }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Config: ");
        sb.append("\n  bindAddress: \"").append(bindAddress).append('\"');
        sb.append(",\n  bindPort: ").append(bindPort);
        sb.append(",\n  monitoredSystems: ").append(monitoredSystems);
        sb.append("\n");
        return sb.toString();
    }
}
