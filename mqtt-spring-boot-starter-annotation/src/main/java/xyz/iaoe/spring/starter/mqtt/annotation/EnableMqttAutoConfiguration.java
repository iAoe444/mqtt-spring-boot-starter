package xyz.iaoe.spring.starter.mqtt.annotation;

import org.springframework.context.annotation.Import;
import xyz.iaoe.spring.starter.mqtt.config.MqttClientAutoConfig;

import java.lang.annotation.*;

/**
 * 开启Mqtt自动配置
 *
 * @author iaoe
 * @date 2021/7/4 18:07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(MqttClientAutoConfig.class)
public @interface EnableMqttAutoConfiguration {

    String[] services() default {};

}
