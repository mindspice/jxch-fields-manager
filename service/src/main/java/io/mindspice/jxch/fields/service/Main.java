package io.mindspice.jxch.fields.service;

import io.mindspice.jxch.fields.data.system.DiskMetrics;
import io.mindspice.jxch.fields.service.core.SystemMonitor;
import io.mindspice.jxch.fields.service.core.tailer.LogParser;
import org.apache.commons.io.input.Tailer;


public class Main {

    public static void main(String[] args) throws InterruptedException {
        String s = """
                \\\\bSP:\\\\s(\\\\d+)\
                .*\
                validation time:\\\\s(\\\\d+\\\\.\\\\d+)\
                .*\
                cost:\\\\s(\\\\d+)\
                """;
        System.out.println(s);
        Tailer tailer = new LogParser(null).getTailer("/home/mindspice/code/Java/Thow-Aways/chia-ws-test/src/main/log/debug.log");
        new Thread(tailer).start();
        while(true) {
            Thread.sleep(1000);
        }
    }

}

