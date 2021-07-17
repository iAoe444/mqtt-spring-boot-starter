package xyz.iaoe.spring.starter.mqtt.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author iaoe
 * @date 2021/6/27 15:24
 */
public class MqttXUtil {

    private static final String RESP_BUS = "/mqtt/respBus/";
    public static final String RESP_BUS_PATTERN = RESP_BUS + "{clientId}";
    private static final Long clientId;
    private static final AtomicLong serialNum = new AtomicLong();

    static {
        clientId = System.currentTimeMillis();
    }

    public static String genRespBusTopic(Long clientId) {
        return RESP_BUS + clientId;
    }

    public static long getMyClientId() {
        return clientId;
    }

    public static long getSerialNum() {
        return serialNum.incrementAndGet();
    }

}