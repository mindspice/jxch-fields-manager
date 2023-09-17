package io.mindspice.jxch.fields.data.network;

import io.mindspice.jxch.fields.data.enums.MonitorMsgType;
import io.mindspice.jxch.fields.data.metrics.ClientMsg;


public record MonitorOutMsg<T>(
        MonitorMsgType msgType,
        T message
) implements ClientMsg{
}

