package xyz.iaoe.spring.starter.mqtt.common;

import java.io.*;
import java.util.Arrays;

/**
 * @author iaoe
 * @date 2021/6/25 19:32
 */
public class MqttRespMsg {
    public long clientId;
    public long timestamp;
    public byte[] payload;
    public long serialNum;

    public MqttRespMsg(long clientId, long timestamp, long serialNum, byte[] payload) {
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.payload = payload;
        this.serialNum = serialNum;
    }

    public byte[] getPayload() {
        return payload;
    }

    public long getClientId() {
        return clientId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSerialNum() {
        return serialNum;
    }

    public static class CodecException extends Exception {
        public CodecException(String errMsg) {
            super(errMsg);
        }

        public CodecException(String errMsg, Exception e) {
            super(errMsg, e);
        }

        public CodecException() {
            super();
        }

        public CodecException(Exception e) {
            super(e);
        }
    }

    public byte[] encode() throws CodecException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            DataOutputStream dos = new DataOutputStream(outputStream);
            dos.writeLong(this.getClientId());
            dos.writeLong(this.getTimestamp());
            dos.writeLong(this.getSerialNum());
            dos.write(this.getPayload());
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CodecException(e);
        }
    }

    public MqttRespMsg(byte[] respPayload) throws CodecException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(respPayload)) {
            DataInputStream dis = new DataInputStream(inputStream);
            this.clientId = dis.readLong();
            this.timestamp = dis.readLong();
            this.serialNum = dis.readLong();
            this.payload = Arrays.copyOfRange(respPayload, 24, respPayload.length);
        } catch (IOException e) {
            throw new CodecException(e);
        }
    }

}
