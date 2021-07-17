package xyz.iaoe.spring.boot.quickstarter.client;


import xyz.iaoe.mqtt.spring.starter.quickstart.service.MqttUserService;
import xyz.iaoe.spring.starter.mqtt.annotation.EmqSubscribe;
import xyz.iaoe.spring.starter.mqtt.annotation.MqttController;
import xyz.iaoe.spring.starter.mqtt.common.MqttReply;

@MqttController(log = true)
public class MqttUserController implements MqttUserService {

    //subscribe mqtt topic:/user/12
    @Override
    @EmqSubscribe("userId=${user.id}")
    //when receive msg from topic:/user/12, this method will be invoked, and msg is payload
    public MqttReply<Void> sendMsg(String userId, String msg) {
        if (msg == null || msg.length() == 0) {
            return MqttReply.failReply("msg can not be null");
        } else {
            System.out.println("user[" + userId + "] receive msg: " + msg);
            return MqttReply.successReply();
        }
    }

}

