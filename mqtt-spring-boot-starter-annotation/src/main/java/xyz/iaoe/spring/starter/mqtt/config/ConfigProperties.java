package xyz.iaoe.spring.starter.mqtt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author iaoe
 * @date 2021/3/29 13:53
 */
@Configuration
@EnableConfigurationProperties(ConfigProperties.MqttConfig.class)
public class ConfigProperties {

    @ConfigurationProperties(prefix = "mqtt")
    public static class MqttConfig {
        private String host;
        private int port;
        private String clientId;
        private String userName;
        private String password;
        // 是否开启回复
        private boolean resp = true;
        // 回复消息超时
        private long respTimeout = 30 * 1000;
        private boolean ssl = false;
        private boolean cleanSession = true;
        //设置超时时间
        private int connectionTimeout = 10;
        //设置会话心跳时间
        private int keepAliveInterval = 20;
        private List<SubTopicConfig> subTopics = new ArrayList<>();
        private boolean reconnect = false;
        private WillTopicConfig willTopic;

        public static class SubTopicConfig {
            private String topic;
            private int qos;

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }

            public int getQos() {
                return qos;
            }

            public void setQos(int qos) {
                this.qos = qos;
            }
        }


        public boolean isResp() {
            return resp;
        }

        public void setResp(boolean resp) {
            this.resp = resp;
        }

        public static class WillTopicConfig {
            private String topic;
            private int qos = 1;
            private String willMsg;

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }

            public int getQos() {
                return qos;
            }

            public void setQos(int qos) {
                this.qos = qos;
            }

            public String getWillMsg() {
                return willMsg;
            }

            public void setWillMsg(String willMsg) {
                this.willMsg = willMsg;
            }
        }

        public void setRespTimeout(long respTimeout) {
            this.respTimeout = respTimeout;
        }

        public boolean isReconnect() {
            return reconnect;
        }

        public void setReconnect(boolean reconnect) {
            this.reconnect = reconnect;
        }

        public Long getRespTimeout() {
            return respTimeout;
        }

        public void setRespTimeout(Long respTimeout) {
            this.respTimeout = respTimeout;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public boolean isCleanSession() {
            return cleanSession;
        }

        public void setCleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getKeepAliveInterval() {
            return keepAliveInterval;
        }

        public void setKeepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
        }

        public List<SubTopicConfig> getSubTopics() {
            return subTopics;
        }

        public void setSubTopics(List<SubTopicConfig> subTopics) {
            this.subTopics = subTopics;
        }

        public WillTopicConfig getWillTopic() {
            return willTopic;
        }

        public void setWillTopic(WillTopicConfig willTopic) {
            this.willTopic = willTopic;
        }
    }

}
