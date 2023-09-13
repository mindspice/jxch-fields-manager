package io.mindspice.jxch.fields.service.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ServiceManager {
    private final ScheduledExecutorService serviceExec;

    public ServiceManager(int monitoredCount) {
        serviceExec = Executors.newScheduledThreadPool(monitoredCount);
    }

    public void init() {

    }
}
