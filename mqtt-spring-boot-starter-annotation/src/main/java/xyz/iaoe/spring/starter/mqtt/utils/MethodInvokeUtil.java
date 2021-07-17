package xyz.iaoe.spring.starter.mqtt.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author iaoe
 * @date 2021/6/2 21:04
 */
public class MethodInvokeUtil {

    public static Object invoke(Method method, Object object, List<Object> args) throws InvocationTargetException, IllegalAccessException {
        switch (args.size()) {
            case 0:
                return method.invoke(object);
            case 1:
                return method.invoke(object, args.get(0));
            case 2:
                return method.invoke(object, args.get(0), args.get(1));
            case 3:
                return method.invoke(object, args.get(0), args.get(1), args.get(2));
            case 4:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3));
            case 5:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4));
            case 6:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5));
            case 7:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6));
            case 8:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6), args.get(7));
            case 9:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6), args.get(7), args.get(8));
            case 10:
                return method.invoke(object, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5), args.get(6), args.get(7), args.get(8), args.get(9));
            default:
                throw new RuntimeException("args size longer than 10");
        }
    }


}
