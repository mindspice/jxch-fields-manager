package io.mindspice.jxch.fields.service;

import io.mindspice.jxch.fields.data.metrics.chia.PoolInfo;
import io.mindspice.jxch.fields.service.config.Config;
import io.mindspice.jxch.fields.service.core.ServiceManager;
import io.mindspice.jxch.rpc.util.JsonUtils;

import java.io.IOException;
import java.util.List;


public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        ServiceManager manager = new ServiceManager(Config.GET());
        manager.init();
    }
}
