package xyz.iaoe.spring.starter.mqtt.annotation;

public interface DecodeEncode<T> {

    byte[] encode(T dto);

    T decode(byte[] payload);


}
