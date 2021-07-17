package xyz.iaoe.spring.starter.mqtt.common;

/**
 * @author iaoe
 * @date 2021/6/25 16:53
 */
public interface Handler<T> {
    void handle(T e);
}

