package xyz.iaoe.spring.starter.mqtt.utils;

import java.util.Optional;

/**
 * Future工具类
 *
 * @author iaoe
 * @date 2021/6/25 13:32
 */
public class Future<T> {


    private volatile Handler<AsyncResult<T>> completeHandler;
//    private volatile Handler<String> failHandler;
//    private volatile Handler<T> successHandler;

    private AsyncResult<T> asyncResult;
    private volatile boolean isHandle;

    public static <T> Future<T> successFuture(T result) {
        Future<T> future = new Future<>();
        future.asyncResult = AsyncResult.successResult(result);
        return future;
    }

    public static <T> Future<T> failFuture(Exception e) {
        Future<T> future = new Future<>();
        future.asyncResult = AsyncResult.errResult(e);
        return future;
    }

    public static <T> Future<T> failFuture(String errMsg) {
        Future<T> future = new Future<>();
        future.asyncResult = AsyncResult.errResult(errMsg);
        return future;
    }

    public static <T> Future<T> future() {
        return new Future<>();
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

    public void completeHandle(Handler<AsyncResult<T>> handler) {
        this.completeHandler = handler;
        tryComplete();
    }


    private synchronized void tryComplete() {
        if (asyncResult != null) {
            if (asyncResult.isSuccess()) {
                trySuccess(asyncResult.result());
            } else {
                tryFail(asyncResult.errMsg());
            }
        }
    }
}
