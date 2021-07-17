package xyz.iaoe.mqtt.spring.starter.quickstart.service;

import xyz.iaoe.spring.starter.mqtt.annotation.MqttProxyGen;
import xyz.iaoe.spring.starter.mqtt.annotation.Topic;
import xyz.iaoe.spring.starter.mqtt.common.MqttReply;

@MqttProxyGen
public interface MqttUserService {

    @Topic(topicPattern = "/user/{userId}")
    MqttReply<Void> sendMsg(String userId, String msg);

}
