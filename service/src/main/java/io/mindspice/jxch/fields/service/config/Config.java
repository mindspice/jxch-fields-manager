package io.mindspice.jxch.fields.service.config;

import java.util.List;


public class Config {
    private static Config INSTANCE;

    // Client Settings
    private boolean makeClientConnection;
    private String clientAddress;
    private int clientPort;

    // Monitored Systems
    private List<MonitorConfig> monitoredSystems;

    public static Config GET() {
        return INSTANCE;
    }

    public boolean isMakeClientConnection() { return makeClientConnection; }

    public String ClientAddress() { return clientAddress; }

    public int ClientPort() { return clientPort; }

    public List<MonitorConfig> Monitored() { return monitoredSystems; }
}
