module io.mindspice.jxch.fields.data {
    requires jxch.rpc.library;
    requires com.fasterxml.jackson.core;
    exports io.mindspice.jxch.fields.data;
    exports io.mindspice.jxch.fields.data.metrics.system;
    exports io.mindspice.jxch.fields.data.metrics.chia;
    exports io.mindspice.jxch.fields.data.metrics;
    exports io.mindspice.jxch.fields.data.enums;
    exports io.mindspice.jxch.fields.data.util;
    exports io.mindspice.jxch.fields.data.structures;
    exports io.mindspice.jxch.fields.data.network;
}