package xyz.iaoe.spring.starter.mqtt.constant;

/**
 * @author iaoe
 * @date 2021/6/20 14:23
 */
public class MqttServiceException extends RuntimeException {

    public MqttServiceException() {
        super();
    }

    public MqttServiceException(String msg) {
        super(msg);
    }

}
