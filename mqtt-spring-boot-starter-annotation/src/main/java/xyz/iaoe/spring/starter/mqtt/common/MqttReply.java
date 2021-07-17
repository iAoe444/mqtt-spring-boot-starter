package xyz.iaoe.spring.starter.mqtt.common;

import java.util.Optional;

/**
 * MqttReply工具类
 *
 * @author iaoe
 * @date 2021/6/25 13:32
 */
public class MqttReply<T> {


    private volatile Handler<AsyncResult<T>> completeHandler;
//    private volatile Handler<String> failHandler;
//    private volatile Handler<T> successHandler;

    private AsyncResult<T> asyncResult;
    private volatile boolean isHandle;

    public static MqttReply<Void> successReply() {
        MqttReply<Void> mqttReply = new MqttReply<>();
        mqttReply.asyncResult = AsyncResult.successResult(null);
        return mqttReply;
    }

    public static <T> MqttReply<T> successReply(T result) {
        MqttReply<T> mqttReply = new MqttReply<>();
        mqttReply.asyncResult = AsyncResult.successResult(result);
        return mqttReply;
    }

    public static <T> MqttReply<T> failReply(Exception e) {
        MqttReply<T> mqttReply = new MqttReply<>();
        mqttReply.asyncResult = AsyncResult.errResult(e);
        return mqttReply;
    }

    public static <T> MqttReply<T> failReply(String errMsg) {
        MqttReply<T> mqttReply = new MqttReply<>();
        mqttReply.asyncResult = AsyncResult.errResult(errMsg);
        return mqttReply;
    }

    public static <T> MqttReply<T> reply() {
        return new MqttReply<>();
    }

    public synchronized boolean trySuccess(T result) {
        if (isHandle) {
            return false;
        } else {
            Optional.ofNullable(completeHandler)
                    .ifPresent(handler -> {
                        handler.handle(AsyncResult.successResult(result));
                    });
//            Optional.ofNullable(successHandler)
//                    .ifPresent(handler -> {
//                        handler.handle(result);
//                    });
            isHandle = true;
            return true;
        }
    }

    public synchronized boolean tryFail(Exception e) {
        if (isHandle) {
            return false;
        } else {
            Optional.ofNullable(completeHandler)
                    .ifPresent(handler -> {
                        handler.handle(AsyncResult.errResult(e));
                    });
//            Optional.ofNullable(failHandler)
//                    .ifPresent(handler -> {
//                        handler.handle(e.getMessage());
//                    });
            isHandle = true;
            return true;
        }
    }

    public synchronized boolean tryFail(String errMsg) {
        if (isHandle) {
            return false;
        } else {
            Optional.ofNullable(completeHandler)
                    .ifPresent(handler -> {
                        handler.handle(AsyncResult.errResult(errMsg));
                    });
//            Optional.ofNullable(failHandler)
//                    .ifPresent(handler -> {
//                        handler.handle(errMsg);
//                    });
            isHandle = true;
            return true;
        }
    }

    public synchronized boolean tryComplete(AsyncResult<T> asyncResult) {
        if (isHandle) {
            return false;
        } else {
            Optional.ofNullable(completeHandler)
                    .ifPresent(handler -> {
                        handler.handle(asyncResult);
                    });
            isHandle = true;
            return true;
        }
    }

    public void replyHandle(Handler<AsyncResult<T>> handler) {
        this.completeHandler = handler;
        tryComplete();
    }


    private synchronized void tryComplete() {
        if (asyncResult != null) {
            if (asyncResult.success()) {
                trySuccess(asyncResult.result());
            } else {
                tryFail(asyncResult.errMsg());
            }
        }
    }
}
