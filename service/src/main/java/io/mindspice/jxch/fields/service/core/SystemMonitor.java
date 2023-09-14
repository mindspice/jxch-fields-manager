package io.mindspice.jxch.fields.service.core;

import io.mindspice.jxch.fields.data.GpuInfo;
import io.mindspice.jxch.fields.data.enums.GpuVendor;
import io.mindspice.jxch.fields.data.enums.OsType;
import io.mindspice.jxch.fields.data.metrics.system.CpuMetrics;
import io.mindspice.jxch.fields.data.metrics.system.DiskMetrics;
import io.mindspice.jxch.fields.data.metrics.system.GpuMetrics;
import io.mindspice.jxch.fields.data.metrics.system.MemoryMetrics;
import io.mindspice.jxch.fields.data.util.DataUtil;
import io.mindspice.jxch.fields.data.util.Pair;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;


public class SystemMonitor {
    private static volatile SystemMonitor INSTANCE;
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hwLayer = systemInfo.getHardware();
    private final CentralProcessor cpu = hwLayer.getProcessor();
    private final GlobalMemory memory = hwLayer.getMemory();

    private final Set<GpuVendor> gpuVendors;
    private final OsType osType;
    private final String osInfo = systemInfo.getOperatingSystem().getFamily() + " | " +
            systemInfo.getOperatingSystem().getVersionInfo().toString();

    private final List<double[]> cpuUsage = new ArrayList<>(75);
    private final Map<String, GpuInfo> gpuInfoMap;

    public static SystemMonitor get() {
        if (INSTANCE == null) { INSTANCE = new SystemMonitor(); }
        return INSTANCE;
    }

    private SystemMonitor() {
        osType = OsType.FromString(System.getProperty("os.name"));
        gpuVendors = systemInfo.getHardware().getGraphicsCards()
                .stream()
                .map(g -> GpuVendor.FromString(g.getVendor()))
                .collect(Collectors.toSet());

        List<Pair<String, GpuInfo>> gpuInfos = getGpuInfos();
        gpuInfoMap = gpuInfos.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Pair::first, Pair::second),
                        Collections::unmodifiableMap));
        init();
    }

    private void init() {
        new Thread(() -> {
            while (true) {
                try {
                    if (!gpuInfoMap.isEmpty()) { new Thread((this::updateGpuInfos)).start(); }
                    double[] cpuLoad = cpu.getProcessorCpuLoad(1000);
                    synchronized (cpuUsage) { cpuUsage.add(cpuLoad); }
                } catch (Exception ignored) {/* Ignored, should never happen.*/ }
            }
        }).start();
    }

    public CpuMetrics getCpuMetrics() {
        double[][] coreUsage = getCpuCoreUsage();
        if (coreUsage.length == 0) { return null; }
        return new CpuMetrics(
                coreUsage[0],
                coreUsage[1],
                getCoreFrequencies(),
                getCpuTemp(),
                getThreadCount(),
                getProcessCount()
        );
    }

    private double[][] getCpuCoreUsage() {
        synchronized (cpuUsage) {
            if (cpuUsage.size() == 0) { return new double[0][0]; }

            double[] avgUsage = new double[cpuUsage.get(0).length];
            double[] maxUsage = new double[cpuUsage.get(0).length];
            for (double[] sample : cpuUsage) {
                for (int i = 0; i < sample.length; ++i) {
                    double currVal = DataUtil.toPercentage(sample[i]);
                    avgUsage[i] += currVal;
                    maxUsage[i] = (Math.max(maxUsage[i], currVal));
                }
            }

            for (int i = 0; i < avgUsage.length; ++i) {
                avgUsage[i] = DataUtil.limitPrecision2D(avgUsage[i] / cpuUsage.size());
            }
            cpuUsage.clear();
            return new double[][]{avgUsage, maxUsage};
        }
    }

    public int getCpuTemp() { return (int) hwLayer.getSensors().getCpuTemperature(); }

    public int getProcessCount() { return systemInfo.getOperatingSystem().getThreadCount(); }

    public int getThreadCount() { return systemInfo.getOperatingSystem().getProcessCount(); }

    public double[] getCoreFrequencies() { return DataUtil.hzToGhz(cpu.getCurrentFreq()); }

    public MemoryMetrics getMemoryMetrics() {
        double totalMemory = DataUtil.limitPrecision2D(DataUtil.bytesToGiB(memory.getTotal()));
        double freeMemory = DataUtil.limitPrecision2D(DataUtil.bytesToGiB(memory.getAvailable()));
        double totalSwap = DataUtil.limitPrecision2D(DataUtil.bytesToGiB(memory.getVirtualMemory().getSwapTotal()));
        double usedSwap = DataUtil.limitPrecision2D(DataUtil.bytesToGiB(memory.getVirtualMemory().getSwapUsed()));
        return new MemoryMetrics(
                totalMemory,
                DataUtil.limitPrecision2D(totalMemory - freeMemory),
                totalSwap,
                usedSwap
        );
    }

    public boolean hasGpuMetrics() { return !gpuInfoMap.isEmpty(); }

    public List<GpuMetrics> getGpuMetrics() {
        if (gpuInfoMap.isEmpty()) { return List.of(); }
        return gpuInfoMap.values().stream().map(GpuInfo::getMetrics).toList();
    }

    private List<Pair<String, GpuInfo>> getGpuInfos() {
        List<Pair<String, GpuInfo>> gpuInfos = new ArrayList<>();
        for (var vendor : gpuVendors) {
            if (osType == OsType.LINUX) {
                if (vendor == GpuVendor.NVIDIA) { gpuInfos.addAll(getNvidiaLinuxGpuInfo()); }
            }
        }
        return gpuInfos;
    }

    private void updateGpuInfos() {
        for (var vendor : gpuVendors) {
            if (osType == OsType.LINUX) {
                if (vendor == GpuVendor.NVIDIA) { updateNvidiaLinuxGpuInfo(); }
            }
        }
    }

    private List<Pair<String, GpuInfo>> getNvidiaLinuxGpuInfo() {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=index,name",
                "--format=csv,noheader,nounits");
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                List<Pair<String, GpuInfo>> gpuInfos = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(", ");
                    if (values.length != 2) { continue; }

                    String id = GpuVendor.NVIDIA + values[0];
                    GpuInfo gpuInfo = new GpuInfo(Integer.parseInt(values[0]), values[1]);
                    gpuInfos.add(new Pair<>(id, gpuInfo));
                }
                return gpuInfos;
            } catch (IOException e) {
                System.out.println("Failed To Read GPU Info | Vendor: Nvidia | System: Linux");
            }
        } catch (IOException e) {
            System.out.println("Failed To Read GPU Info | Vendor: Nvidia | System: Linux");
        }
        return List.of();
    }

    private void updateNvidiaLinuxGpuInfo() {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=index,utilization.gpu,memory.total,memory.used,temperature.gpu"
                , "--format=csv,noheader,nounits"
        );
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(", ");
                    if (values.length != 5) { continue; }

                    String id = GpuVendor.NVIDIA + values[0];
                    GpuInfo gpuInfo = gpuInfoMap.get(id);
                    if (gpuInfo == null) { continue; }
                    gpuInfo.addMetrics(
                            Integer.parseInt(values[1]),
                            Integer.parseInt(values[2]),
                            Integer.parseInt(values[3]),
                            Integer.parseInt(values[4]));
                }
            } catch (IOException e) {
                System.out.println("Failed To Read GPU Info | Vendor: Nvidia | System: Linux");
            }
        } catch (IOException e) {
            System.out.println("Failed To Read GPU Info | Vendor: Nvidia | System: Linux");
        }
    }

    public List<DiskMetrics> getDiskMetrics() {
        return systemInfo.getOperatingSystem().getFileSystem().getFileStores().stream()
                .map(fs -> new DiskMetrics(fs.getMount(), fs.getUUID(), fs.getTotalSpace(), fs.getFreeSpace()))
                .toList();
    }
}


