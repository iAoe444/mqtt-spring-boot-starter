package xyz.iaoe.spring.starter.mqtt;

import xyz.iaoe.spring.starter.mqtt.utils.ThreadUtil;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * mqtt回调管理器
 *
 * @author iaoe
 * @date 2021/6/2 10:17
 */
public class MqttCallBackManager<M> {

    private final MqttTopicTreeNode<M> mqttTopicTreeNode = new MqttTopicTreeNode<>(MqttTopicTreeNode.NodeTypeEnum.HEAD);
    public static final Pattern GENERAL_MATCHING_PATTERN = Pattern.compile("^\\{.*?}$");
    private static final ThreadPoolExecutor threadPool = ThreadUtil.getIoIntenseTargetThreadPool();

    /**
     * 主题订阅树
     */
    private static class MqttTopicTreeNode<M> {


        private static final String GENERAL_MATCHING_STRING = "*";
        //通配节点
        private static final MqttTopicTreeNode generalMatchingNode = new MqttTopicTreeNode<>(MqttTopicTreeNode.NodeTypeEnum.GENERAL_MATCHING);

        //节点类型
        public enum NodeTypeEnum {
            //通配节点
            GENERAL_MATCHING,
            //普通节点
            NORMAL,
            //头节点
            HEAD
        }

        private String nodeName;
        private NodeTypeEnum nodeType = NodeTypeEnum.NORMAL;
        //孩子节点
        private final Map<String, MqttTopicTreeNode<M>> mqttTopicChildNodes = new HashMap<>();
        private final List<BiConsumer<String[], M>> callbacks = new ArrayList<>();

        public MqttTopicTreeNode(String nodeName) {
            this.nodeName = nodeName;
        }

        private MqttTopicTreeNode(NodeTypeEnum nodeType) {
            this.nodeType = nodeType;
        }

        /**
         * 消除尾递归法创建订阅树型结构
         *
         * @param topicUnitIterator 主题元素迭代器
         */
        private void addSubscribeCallBack(Iterator<String> topicUnitIterator, BiConsumer<String[], M> callBack) {
            if (topicUnitIterator.hasNext()) {
                String topicUnit = topicUnitIterator.next();
                MqttTopicTreeNode<M> mqttTopicTreeNode = mqttTopicChildNodes.get(topicUnit);
                // 如果这个节点为空的话
                if (mqttTopicTreeNode == null) {
                    if (GENERAL_MATCHING_PATTERN.matcher(topicUnit).find()) {
                        mqttTopicTreeNode = generalMatchingNode;
                        topicUnit = GENERAL_MATCHING_STRING;
                    } else {
                        mqttTopicTreeNode = new MqttTopicTreeNode<>(topicUnit);
                    }
                    mqttTopicChildNodes.put(topicUnit, mqttTopicTreeNode);
                }
                //如果节点不为空，从这个节点向下遍历
                mqttTopicTreeNode.addSubscribeCallBack(topicUnitIterator, callBack);
            } else {
                //走到节点的末尾, 所有的都添加完毕, 在最后添加回调
                callbacks.add(callBack);
            }
        }


        private void sendMqttMsg(final List<String> topicUnits, String[] topicMatchArray, int topicUnitIndex, M body) {
            //如果走到了末尾
            if (topicUnitIndex == topicUnits.size()) {
                if (topicUnits.size() > 0 && topicUnits.get(0).equals("/")) topicUnits.set(0, "");
                callbacks.forEach(callback -> {
                    //这里使用线程池的来执行IO密集型代码
                    threadPool.execute(() -> {
                        callback.accept(topicMatchArray, body);
                    });
                });
            } else {
                String topicUnit = topicUnits.get(topicUnitIndex++);
                final int finalTopicUnitIndex = topicUnitIndex;
                Optional.ofNullable(mqttTopicChildNodes.get(topicUnit))
                        .ifPresent(node -> {
                            node.sendMqttMsg(topicUnits, topicMatchArray, finalTopicUnitIndex, body);
                        });
                Optional.ofNullable(mqttTopicChildNodes.get(GENERAL_MATCHING_STRING))
                        .ifPresent(node -> {
                            String[] newTopicMatchArray = new String[topicMatchArray.length + 1];
                            System.arraycopy(newTopicMatchArray, 0, newTopicMatchArray, 0, topicMatchArray.length + 1);
                            newTopicMatchArray[topicMatchArray.length] = topicUnit;
                            node.sendMqttMsg(topicUnits, newTopicMatchArray, finalTopicUnitIndex, body);
                        });
            }
        }
    }

    /**
     * @param topic    /test/{abc}
     * @param callBack 回调函数
     */
    public void addSubsCallBack(String topic, BiConsumer<String[], M> callBack) {
        List<String> topicUnits = Arrays.asList(topic.split("/"));
        if (topicUnits.size() > 0 && topicUnits.get(0).equals("")) {
            topicUnits.set(0, "/");
        }
        mqttTopicTreeNode.addSubscribeCallBack(topicUnits.iterator(), callBack);
    }

    /**
     * 发送新消息
     *
     * @param topic 主题，消息
     * @param body  消息体
     */
    public void pushNewMsg(String topic, M body) {
        List<String> topicUnits = Arrays.asList(topic.split("/"));
        //这个是为了防止 "/abc/ad" 分成  ["", "abc", "ad"]这种情况
        if (topicUnits.size() > 0 && topicUnits.get(0).equals("")) {
            topicUnits.set(0, "/");
        }
        mqttTopicTreeNode.sendMqttMsg(topicUnits, new String[]{}, 0, body);
    }

}
