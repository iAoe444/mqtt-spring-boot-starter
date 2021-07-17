package xyz.iaoe.spring.starter.mqtt.annotation;

public @interface EmqSubscribe {

    String value();

    int qos() default 0;



}
