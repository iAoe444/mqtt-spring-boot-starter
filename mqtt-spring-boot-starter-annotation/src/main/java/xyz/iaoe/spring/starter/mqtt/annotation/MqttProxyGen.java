package xyz.iaoe.spring.starter.mqtt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author iaoe
 * @date 2021/6/22 14:27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MqttProxyGen {
}
