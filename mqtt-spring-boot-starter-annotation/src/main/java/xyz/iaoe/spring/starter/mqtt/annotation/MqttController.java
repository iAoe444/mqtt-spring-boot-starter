package xyz.iaoe.spring.starter.mqtt.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mqtt控制器
 *
 * @author iaoe
 * @date 2021/6/1 19:25
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MqttController {

    //是否打印所有的log日志
    boolean log() default false;

}
