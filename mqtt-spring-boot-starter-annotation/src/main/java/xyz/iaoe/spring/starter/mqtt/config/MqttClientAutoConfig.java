package xyz.iaoe.spring.starter.mqtt.config;

import cn.hutool.core.util.StrUtil;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;


@Component
@ComponentScan
@ComponentScan(value = "xyz.iaoe.spring.starter.mqtt.service")
public class MqttClientAutoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientAutoConfig.class);

    @Autowired
    private ConfigProperties.MqttConfig mqttConfig;

    @Autowired
    private MqttConnectOptions options;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Bean
    public MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(mqttConfig.isCleanSession());
        if (StrUtil.isNotBlank(mqttConfig.getUserName())) {
            options.setUserName(mqttConfig.getUserName());
            options.setPassword(mqttConfig.getPassword().toCharArray());
        }
        options.setConnectionTimeout(mqttConfig.getConnectionTimeout());
        options.setKeepAliveInterval(mqttConfig.getKeepAliveInterval());
        options.setAutomaticReconnect(mqttConfig.isReconnect());
        return options;
    }

    @Bean(name = "mqttClient")
    public MqttClient mqttClient() throws MqttException {
        MqttClient mqttClient = new MqttClient(
                mqttConfig.isSsl()
                        ? String.format("ssl://%s:%d", mqttConfig.getHost(), mqttConfig.getPort())
                        : String.format("tcp://%s:%d", mqttConfig.getHost(), mqttConfig.getPort()),
                mqttConfig.getClientId());

        mqttClient.connect(options);

        //订阅配置文件里的主题
        for (ConfigProperties.MqttConfig.SubTopicConfig subTopic : mqttConfig.getSubTopics()) {
            logger.info("初始化订阅Mqtt主题:{}, qos:{}", subTopic.getTopic(), subTopic.getQos());
            mqttClient.subscribe(subTopic.getTopic(), subTopic.getQos());
        }

        logger.info("连接Mqtt成功");

        return mqttClient;
    }
}
