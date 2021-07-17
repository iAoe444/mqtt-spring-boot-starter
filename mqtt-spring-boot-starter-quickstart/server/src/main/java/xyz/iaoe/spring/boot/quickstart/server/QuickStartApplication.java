package xyz.iaoe.spring.boot.quickstart.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xyz.iaoe.mqtt.spring.starter.quickstart.service.MqttUserService;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.LockSupport;

@SpringBootApplication
public class QuickStartApplication {

    @Autowired
    private MqttUserService mqttUserService;

    @PostConstruct
    public void sendHelloWorld() {
        //send mqtt msg to topic:/user/12 payload:hello world
        mqttUserService.sendMsg("12", "hello world")
                //receive mqtt reply
                .replyHandle(ar -> {
                    //if mqtt reply is success
                    if (ar.success()) {
                        System.out.println("send success");
                    } else {
                        System.out.println("send fail, errMsg:" + ar.errMsg());
                    }
                });
        LockSupport.park();
    }

    public static void main(String[] args) {
        SpringApplication.run(QuickStartApplication.class, args);
    }

}
