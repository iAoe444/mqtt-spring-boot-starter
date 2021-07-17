package xyz.iaoe.spring.starter.mqtt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {

    //订阅主题
    String topicPattern();

    //是否log, 也就是打印请求响应情况
    boolean log() default true;

    //发送Qos
    int sendQos() default 0;

    //是否保留发送消息
    boolean sendRetained() default false;

}
