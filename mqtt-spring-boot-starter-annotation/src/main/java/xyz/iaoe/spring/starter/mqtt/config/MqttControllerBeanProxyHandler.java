package xyz.iaoe.spring.starter.mqtt.config;

import cn.hutool.json.JSONObject;;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import xyz.iaoe.spring.starter.mqtt.MqttCallBackManager;
import xyz.iaoe.spring.starter.mqtt.annotation.MqttController;
import xyz.iaoe.spring.starter.mqtt.annotation.Subscribe;
import xyz.iaoe.spring.starter.mqtt.constant.MqttServiceException;
import xyz.iaoe.spring.starter.mqtt.utils.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 解析mqttController的proxy
 *
 * @author iaoe
 * @date 2021/6/2 20:43
 */
@Component
public class MqttControllerBeanProxyHandler implements ApplicationContextAware {

    @Autowired
    private MqttClient mqttClient;
    private final static Logger log = LoggerFactory.getLogger(MqttControllerBeanProxyHandler.class);

    @Autowired
    private MqttConnectOptions options;

    private final MqttCallBackManager<MqttMsg> mqttCallBackManager = new MqttCallBackManager<>();
    public static final Pattern GENERAL_UNIT_MATCHING_PATTERN = Pattern.compile("\\{(.*?)}");

    public static class MqttMsg {
        private MqttMessage mqttMessage;
        private String topic;

        public MqttMsg(String topic, MqttMessage mqttMessage) {
            this.mqttMessage = mqttMessage;
            this.topic = topic;
        }

        public MqttMessage getMqttMessage() {
            return mqttMessage;
        }

        public void setMqttMessage(MqttMessage mqttMessage) {
            this.mqttMessage = mqttMessage;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        mqttClient.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                while (!mqttClient.isConnected()) {
                    try {
                        mqttClient.connect(options);
                        Thread.sleep(1000);
                    } catch (MqttException | InterruptedException e) {
                        log.error("reconnect fail", e);
                    }
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                mqttCallBackManager.pushNewMsg(topic, new MqttMsg(topic, message));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(MqttController.class);
        for (Object mqttController : beansWithAnnotation.values()) {
            List<Method> subscribeHandleMethodList = Arrays.stream(mqttController.getClass().getDeclaredMethods()).
                    filter(method -> method.isAnnotationPresent(Subscribe.class))
                    .collect(Collectors.toList());
            //TODO 清除相同方法
            //寻找父类接口的方法
            for (Class<?> anInterface : mqttController.getClass().getInterfaces()) {
                for (Method method : anInterface.getMethods()) {
                    if (method.isAnnotationPresent(Subscribe.class)) {
                        subscribeHandleMethodList.add(method);
                    }
                }
            }

            for (Method method : subscribeHandleMethodList) {
                Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                //处理Subscribe注解
                //TODO topic的校验
                mqttCallBackManager.addSubsCallBack(subscribeAnnotation.topic(), (topicMatchArray, msg) -> {
                    //找到topic的元素，同时填入方法里面
                    MqttMessage mqttMsg = msg.getMqttMessage();
                    MqttRespMsg mqttRespMsg = null;
                    try {
                        //打印MQTT消息
                        if (mqttController.getClass().getAnnotation(MqttController.class).log() || subscribeAnnotation.log()) {
                            log.info("[mqttMsg] topic:{}, qos:{}, msg:{}",
                                    msg.getTopic(), mqttMsg.getQos(), new String(mqttMsg.getPayload()));
                            log.info("[method] {}.{} invoked, matchTopic:{}, matchArray:{}",
                                    mqttController.getClass().getSimpleName(), method.getName(), subscribeAnnotation.topic(), topicMatchArray);
                        }
                        byte[] payload;
                        if (Future.class.isAssignableFrom(method.getReturnType())) {
                            mqttRespMsg = new MqttRespMsg(mqttMsg.getPayload());
                            payload = mqttRespMsg.getPayload();
                        } else {
                            payload = mqttMsg.getPayload();
                        }
                        //TODO 参数数量的校验，参数名称的校验
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        List<Object> params = paramCheckAndTrans(parameterTypes, topicMatchArray);
                        Class<?> mqttBodyType = parameterTypes[parameterTypes.length - 1];
                        //添加末尾参数的校验
                        //TODO 不用toString方法
                        if (MqttMessage.class.isAssignableFrom(mqttBodyType)) {
                            params.add(mqttMsg);
                        } else if (JSONObject.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            JSONObject body = new JSONObject(msgBody);
                            params.add(body);
                        } else if (Integer.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Integer body = Integer.valueOf(msgBody);
                            params.add(body);
                        } else if (Long.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Long body = Long.valueOf(msgBody);
                            params.add(body);
                        } else if (Double.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Double body = Double.valueOf(msgBody);
                            params.add(body);
                        } else if (Float.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Float body = Float.valueOf(msgBody);
                            params.add(body);
                        } else if (Boolean.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Boolean body = Boolean.valueOf(msgBody);
                            params.add(body);
                        } else if (Byte.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Byte body = Byte.valueOf(msgBody);
                            params.add(body);
                        } else if (Short.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            Short body = Short.valueOf(msgBody);
                            params.add(body);
                        } else if (byte[].class.isAssignableFrom(mqttBodyType)) {
                            params.add(payload);
                        } else if (Void.class.isAssignableFrom(mqttBodyType)) {
                            params.add(null);
                        } else if (String.class.isAssignableFrom(mqttBodyType)) {
                            String msgBody = new String(payload);
                            params.add(msgBody);
                        } else {
                            String msgBody = new String(payload);
                            params.add(new JSONObject(msgBody).toBean(mqttBodyType));
                        }
                        Object result = MethodInvokeUtil.invoke(method, mqttController, params);
                        // 看是否需要进行返回, 那就发到回复总线里面
                        if (mqttRespMsg != null) {
                            MqttRespMsg finalMqttRespMsg = mqttRespMsg;
                            ((Future<?>) result).completeHandle(ar -> {
                                try {
                                    byte[] respPayload = new MqttRespMsg(MqttXUtil.getMyClientId(), System.currentTimeMillis(),
                                            finalMqttRespMsg.getSerialNum(), AsyncResult.encode(ar)).encode();
                                    mqttClient.publish(MqttXUtil.genRespBusTopic(finalMqttRespMsg.getClientId()), respPayload, 1, false);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    } catch (MqttServiceException e) {
                        // 如果发生错误了, 那就把错误信息发到回复消息总线里面
                        if (mqttRespMsg != null) {
                            try {
                                byte[] respPayload = new MqttRespMsg(MqttXUtil.getMyClientId(), System.currentTimeMillis(),
                                        mqttRespMsg.getSerialNum(), AsyncResult.encode(AsyncResult.errResult(e))).encode();
                                mqttClient.publish(MqttXUtil.genRespBusTopic(mqttRespMsg.getClientId()), respPayload, 1, false);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Throwable e) {
                        log.error("invoked method {}.{} error", mqttController.getClass().getSimpleName(), method.getName(), e);
                    }
                });
            }
        }
    }

    /**
     * 参数校验
     *
     * @param parameterTypes  参数类型数组
     * @param topicMatchArray 主题单元匹配数组
     * @return
     */
    public static List<Object> paramCheckAndTrans(Class<?>[] parameterTypes, String[] topicMatchArray) {
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < topicMatchArray.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Object param = null;
            if (Double.class.isAssignableFrom(parameterType)) {
                param = Double.parseDouble(topicMatchArray[i]);
            } else if (Integer.class.isAssignableFrom(parameterType)) {
                param = Integer.parseInt(topicMatchArray[i]);
            } else if (Float.class.isAssignableFrom(parameterType)) {
                param = Float.parseFloat(topicMatchArray[i]);
            } else if (Long.class.isAssignableFrom(parameterType)) {
                param = Long.parseLong(topicMatchArray[i]);
            } else if (String.class.isAssignableFrom(parameterType)) {
                params.add(topicMatchArray[i]);
            } else if (Short.class.isAssignableFrom(parameterType)) {
                param = Short.parseShort(topicMatchArray[i]);
            } else {
                throw new RuntimeException("unknown param type");
            }
            params.add(param);
        }
        return params;
    }

}
