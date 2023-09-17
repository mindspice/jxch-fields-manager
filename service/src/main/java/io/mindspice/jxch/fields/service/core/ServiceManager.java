package io.mindspice.jxch.fields.service.core;

import io.mindspice.jxch.fields.data.structures.Pair;
import io.mindspice.jxch.fields.service.config.Config;
import io.mindspice.jxch.fields.service.tasks.MonitorTask;
import org.apache.commons.io.input.Tailer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ServiceManager {
    private final Config config;
    private ScheduledExecutorService serviceExec;
    Map<String, MonitorService> monitorServices;

    public ServiceManager(Config config) {
        this.config = config;
    }

    public boolean init() {
        monitorServices = new HashMap<>(config.getMonitoredSystems().size());

        List<Pair<Tailer, MonitorTask>> taskList = new ArrayList<>();
        int taskCount = 0;
        for (var monitor : config.getMonitoredSystems()) {
            var monitorService = new MonitorService(monitor);
            var tasks = monitorService.init();
            taskCount += (tasks.first() != null ? 1 : 0) + (tasks.second() != null ? 1 : 0);
            taskList.add(tasks);
            monitorServices.put(monitor.getMonitorName(), monitorService);
        }
        serviceExec = Executors.newScheduledThreadPool(taskCount);
        for (var task : taskList) {
            System.out.println(task);
            if (task.first() != null) { serviceExec.submit(task.first()); }
            if (task.second() != null) {
                serviceExec.scheduleAtFixedRate(task.second(), 0, 5, TimeUnit.SECONDS);
            }
        }
        System.out.println(((ThreadPoolExecutor) serviceExec).getActiveCount());
        return taskCount != 0;
    }
    // TODO add method so client can request data from services
}
