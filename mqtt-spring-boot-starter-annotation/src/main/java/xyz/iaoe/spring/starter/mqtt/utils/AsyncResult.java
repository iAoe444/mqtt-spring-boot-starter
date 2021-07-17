package xyz.iaoe.spring.starter.mqtt.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.io.*;
import java.util.List;

public class AsyncResult<T> {

    private String errMsg;
    private T result;
    private boolean isError;

    private AsyncResult() {

    }

    public static <T> AsyncResult<T> errResult(String errMsg) {
        AsyncResult<T> ar = new AsyncResult<>();
        ar.errMsg = errMsg;
        ar.result = null;
        ar.isError = true;
        return ar;
    }

    public static <T> AsyncResult<T> errResult(Exception e) {
        AsyncResult<T> ar = new AsyncResult<>();
        ar.errMsg = e.getMessage();
        ar.result = null;
        ar.isError = true;
        return ar;
    }

    public static <T> AsyncResult<T> successResult(T result) {
        AsyncResult<T> ar = new AsyncResult<>();
        ar.errMsg = null;
        ar.result = result;
        ar.isError = false;
        return ar;
    }

    public boolean isSuccess() {
        return !isError;
    }

    public String errMsg() {
        if (!isError) throw new RuntimeException("result isn't error");
        return errMsg;
    }

    public T result() {
        if (isError) throw new RuntimeException("result isn't success");
        return result;
    }

    public static <T> AsyncResult<T> decode(byte[] payload) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(payload)) {
            DataInputStream dis = new DataInputStream(is);
            boolean isSuccess = dis.readBoolean();
            if (isSuccess) {
                if (payload.length == 1) {
                    AsyncResult<T> ar = new AsyncResult<>();
                    ar.result = null;
                    ar.isError = false;
                    return ar;
                }
                String resType = dis.readUTF();
                Object res;
                if (Long.class.getCanonicalName().equals(resType)) {
                    res = dis.readLong();
                } else if (Integer.class.getCanonicalName().equals(resType)) {
                    res = dis.readInt();
                } else if (Boolean.class.getCanonicalName().equals(resType)) {
                    res = dis.readBoolean();
                } else if (String.class.getCanonicalName().equals(resType)) {
                    res = dis.readUTF();
                } else if (byte[].class.getCanonicalName().equals(resType)) {
                    res = dis.read(new byte[dis.available()]);
                } else if (Short.class.getCanonicalName().equals(resType)) {
                    res = dis.readShort();
                } else if (Double.class.getCanonicalName().equals(resType)) {
                    res = dis.readDouble();
                } else if (Float.class.getCanonicalName().equals(resType)) {
                    res = dis.readFloat();
                } else if (Byte.class.getCanonicalName().equals(resType)) {
                    res = dis.readByte();
                } else if (JSONObject.class.getCanonicalName().equals(resType)) {
                    res = new JSONObject(dis.readUTF());
                } else if (List.class.getCanonicalName().equals(resType)) {
                    res = null;
                } else {
                    res = new JSONObject(dis.readUTF()).toBean(Class.forName(resType));
                }
                AsyncResult<T> ar = new AsyncResult<>();
                ar.result = (T) res;
                ar.isError = false;
                return ar;
            } else {
                if (payload.length == 1) {
                    AsyncResult<T> ar = new AsyncResult<>();
                    ar.errMsg = null;
                    ar.isError = true;
                    return ar;
                } else {
                    String errMsg = dis.readUTF();
                    return AsyncResult.errResult(errMsg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("decode error", e);
        }
    }

    public static <T> byte[] encode(AsyncResult<T> asyncResult) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            DataOutputStream dos = new DataOutputStream(os);
            if (asyncResult.isSuccess()) {
                dos.writeBoolean(true);
                T result = asyncResult.result();
                if (result != null) {
                    String resType = result.getClass().getCanonicalName();
                    dos.writeUTF(resType);
                    if (Long.class.getCanonicalName().equals(resType)) {
                        dos.writeLong((Long) result);
                    } else if (Integer.class.getCanonicalName().equals(resType)) {
                        dos.writeInt((Integer) result);
                    } else if (Boolean.class.getCanonicalName().equals(resType)) {
                        dos.writeBoolean((Boolean) result);
                    } else if (String.class.getCanonicalName().equals(resType)) {
                        dos.writeUTF((String) result);
                    } else if (byte[].class.getCanonicalName().equals(resType)) {
                        dos.write((byte[]) result);
                    } else if (Short.class.getCanonicalName().equals(resType)) {
                        dos.writeShort((Short) result);
                    } else if (Double.class.getCanonicalName().equals(resType)) {
                        dos.writeDouble((Double) result);
                    } else if (Float.class.getCanonicalName().equals(resType)) {
                        dos.writeFloat((Float) result);
                    } else if (Byte.class.getCanonicalName().equals(resType)) {
                        dos.writeFloat((Byte) result);
                    } else if (JSONObject.class.getCanonicalName().equals(resType)) {
                        dos.writeUTF(result.toString());
                    } else if (List.class.isAssignableFrom(Class.forName(resType))) {
                        dos.writeUTF(new JSONArray(result).toString());
                    } else {
                        dos.writeUTF(new JSONObject(result).toString());
                    }
                }
            } else {
                dos.writeBoolean(false);
                if (asyncResult.errMsg() != null) {
                    dos.writeUTF(asyncResult.errMsg());
                }
            }
            return os.toByteArray();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("encode error", e);
        }
    }

    @Override
    public String toString() {
        return "AsyncResult{" +
                "errMsg='" + errMsg + '\'' +
                ", result=" + result +
                ", isError=" + isError +
                '}';
    }
}

