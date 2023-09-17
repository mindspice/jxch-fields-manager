package io.mindspice.jxch.fields.service.networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.jxch.fields.data.metrics.ClientMsg;
import io.mindspice.jxch.fields.data.network.MonitorOutMsg;
import io.mindspice.jxch.fields.service.config.Config;
import io.mindspice.jxch.rpc.util.JsonUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class FieldsServer {
    public static FieldsServer INSTANCE = null;
    private final WsServerImpl wsServer;

    // Processing
    Executor exec = Executors.newSingleThreadExecutor();
    Thread procThread;
    private final AtomicBoolean doProcess = new AtomicBoolean(false);

    // message queue to broadcast
    private final LinkedBlockingQueue<ClientMsg> clientMsgQueue;

    private FieldsServer() {
        String addr = Config.GET().getBindAddress();
        int port = Config.GET().getBindPort();
        this.wsServer = new WsServerImpl(addr, port);
        wsServer.setTcpNoDelay(true);
        clientMsgQueue = new LinkedBlockingQueue<>();
    }

    public static FieldsServer GET() {
        if (INSTANCE == null) { INSTANCE = new FieldsServer(); }
        return INSTANCE;
    }

    public boolean start() {
        if (doProcess.get()) { return false; }
        procThread = new Thread(new process());
        exec.execute(procThread);
        doProcess.set(true);
        return true;
    }

    public void stop() {
        doProcess.set(false);
        if (procThread != null) { procThread.interrupt(); }
    }

    public boolean submitClientMsg(ClientMsg msg) {
        System.out.println("Message Received");
        System.out.println(msg);
//        if (!doProcess.get()) { return false; }
//        clientMsgQueue.add(msg);
        return true;
    }

    private class process implements Runnable {
        @Override
        public void run() {
            while (doProcess.get()) {
                return;
//                try {
//                    ClientMsg msg = clientMsgQueue.take();
//                    byte[] msgBytes = JsonUtils.writeBytes(msg);
//                    wsServer.broadcast(msgBytes);
//                } catch (InterruptedException e) {
//                    // Log stop
//                } catch (JsonProcessingException e) {
//                    // TODO log this
//                }
            }
        }
    }

}
