package xyz.iaoe.spring.starter.mqtt.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.iaoe.spring.starter.mqtt.annotation.MqttController;
import xyz.iaoe.spring.starter.mqtt.annotation.Topic;
import xyz.iaoe.spring.starter.mqtt.config.ConfigProperties;
import xyz.iaoe.spring.starter.mqtt.common.AsyncResult;
import xyz.iaoe.spring.starter.mqtt.common.MqttReply;
import xyz.iaoe.spring.starter.mqtt.common.MqttRespMsg;
import xyz.iaoe.spring.starter.mqtt.utils.MqttXUtil;

import javax.annotation.PostConstruct;

/**
 * @author iaoe
 * @date 2021/6/27 15:35
 */
@MqttController
public class MqttRespService {
    private static final Logger logger = LoggerFactory.getLogger(MqttRespService.class);
    private TimedCache<Long, MqttReply<?>> msgFlight;
    @Autowired
    private ConfigProperties.MqttConfig mqttConfig;
    @Autowired
    private MqttClient mqttClient;

    @PostConstruct
    private void init() throws MqttException {
        if (mqttConfig.isResp()) {
            logger.info("开启MQTT回复总线, 等待超时时间{}", mqttConfig.getRespTimeout());
            msgFlight = CacheUtil.newTimedCache(mqttConfig.getRespTimeout());
            msgFlight.schedulePrune(10);
            mqttClient.subscribe(MqttXUtil.genRespBusTopic(MqttXUtil.getMyClientId()), 1);
        } else {
            logger.info("关闭MQTT回复总线");
        }
    }

    @Topic(topicPattern = MqttXUtil.RESP_BUS_PATTERN)
    public void respBus(Long clientId, MqttMessage mqttMessage) {
        try {
            MqttRespMsg mqttRespMsg = new MqttRespMsg(mqttMessage.getPayload());
            MqttReply<?> mqttReply;
            synchronized (this) {
                mqttReply = msgFlight.get(mqttRespMsg.getSerialNum());
                msgFlight.remove(mqttRespMsg.getSerialNum());
            }
            if (mqttReply == null) {
                logger.error("Msg Duplicate");
            } else if (System.currentTimeMillis() - mqttRespMsg.getTimestamp() > mqttConfig.getRespTimeout()) {
                mqttReply.tryFail("TIMEOUT");
            } else {
                mqttReply.tryComplete(AsyncResult.decode(mqttRespMsg.getPayload()));
            }
        } catch (MqttRespMsg.CodecException e) {
            logger.error("mqtt respMsg decode error", e);
        }
    }

    public void addWaitReply(long serialNum, MqttReply<?> mqttReply) {
        if (mqttConfig.isResp()) {
            msgFlight.put(serialNum, mqttReply);
        }
    }

}
