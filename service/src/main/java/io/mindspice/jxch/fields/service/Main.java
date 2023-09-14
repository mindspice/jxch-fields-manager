package io.mindspice.jxch.fields.service;

import io.mindspice.jxch.fields.data.chia.NodeStatistics;
import io.mindspice.jxch.fields.data.metrics.chia.FilterMetrics;
import io.mindspice.jxch.fields.data.metrics.chia.SignagePointMetrics;
import io.mindspice.jxch.fields.data.util.DataUtil;
import oshi.jna.platform.unix.SolarisLibc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


public class Main {

    public static void main(String[] args) throws InterruptedException {

        var ns = new NodeStatistics();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 10000; i++) {
            var m = new FilterMetrics(
                    1,
                    LocalDateTime.ofEpochSecond(rng.nextLong(-365243219162L, 365241780471L), rng.nextInt(0, 100000), ZoneOffset.UTC),
                    rng.nextInt(0, 200),
                    10,
                    rng.nextFloat(0, 10),
                    10000
            );
            var time = System.nanoTime();
            ns.addFilterMetric(m);
            System.out.println(System.nanoTime() - time);

        }

        var fm = ns.getAllFilterMetrics();

    }


}
//
//        System.out.println(s);
//        Tailer tailer = new LogParser(null).getTailer("/home/mindspice/code/Java/Thow-Aways/chia-ws-test/src/main/log/debug.log");
//        new Thread(tailer).start();
//        while(true) {
//            Thread.sleep(1000);
//        }

