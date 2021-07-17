package xyz.iaoe.spring.starter.mqtt.processor;

import cn.hutool.json.JSONObject;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.iaoe.spring.starter.mqtt.annotation.MqttProxyGen;
import xyz.iaoe.spring.starter.mqtt.annotation.Topic;
import xyz.iaoe.spring.starter.mqtt.service.MqttRespService;
import xyz.iaoe.spring.starter.mqtt.common.MqttReply;
import xyz.iaoe.spring.starter.mqtt.common.MqttRespMsg;
import xyz.iaoe.spring.starter.mqtt.utils.MqttXUtil;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author iaoe
 * @date 2021/6/23 15:47
 */
@AutoService(Processor.class)
public class MqttServiceGenProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.size() == 0) return true;
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(MqttProxyGen.class)) {
                if (element.getKind() != ElementKind.INTERFACE) {
                    error(element, "Only interface can be annotated with @%s",
                            MqttProxyGen.class.getSimpleName());
                    return true;
                } else {
                    TypeSpec.Builder mqttServiceProxyHandlerClass = TypeSpec.classBuilder(element.getSimpleName() + "Proxy")
                            .addField(FieldSpec.builder(MqttClient.class, "mqttClient").addAnnotation(Autowired.class).addModifiers(Modifier.PRIVATE).build())
                            .addField(FieldSpec.builder(MqttRespService.class, "mqttRespService").addAnnotation(Autowired.class).addModifiers(Modifier.PRIVATE).build())
                            .addAnnotation(Service.class)
                            .addSuperinterface(element.asType())
                            .addModifiers(Modifier.PUBLIC);
                    List<MethodSpec> methodSpecList = new ArrayList<>();
                    // 遍历AST
                    for (Element enclosed : element.getEnclosedElements()) {
                        if (enclosed.getKind() == ElementKind.METHOD) {
                            ExecutableElement methodElement = (ExecutableElement) enclosed;
                            Topic annotation = methodElement.getAnnotation(Topic.class);
                            if (annotation != null) {
                                //获取前n-1个参数
                                List<String> paramNames = methodElement.getParameters().subList(0, methodElement.getParameters().size() - 1)
                                        .stream().map(param -> param.getSimpleName().toString()).collect(Collectors.toList());

                                boolean throwException = methodElement.getThrownTypes().size() != 0;
                                if (throwException) {
                                    error(methodElement, "method can not throw Exception");
                                }
                                boolean hasFuture = methodElement.getReturnType().toString().indexOf(MqttReply.class.getCanonicalName()) == 0;

                                methodSpecList.add(MethodSpec.methodBuilder(methodElement.getSimpleName().toString())
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameters(
                                                // 转换原方法的参数到生成方法的参数列表
                                                methodElement.getParameters().stream()
                                                        .map(param -> ParameterSpec.builder(TypeName.get(param.asType()),
                                                                param.getSimpleName().toString()).build())
                                                        .collect(Collectors.toList()))
                                        .addExceptions(
                                                methodElement.getThrownTypes().stream()
                                                        .map(TypeName::get)
                                                        .collect(Collectors.toList())
                                        )
                                        .addCode(
                                                createMqttClientStatement(
                                                        annotation.topicPattern(),
                                                        paramNames,
                                                        annotation.sendQos(),
                                                        annotation.sendRetained(),
                                                        //获取最后一个参数的类型
                                                        methodElement.getParameters()
                                                                .get(methodElement.getParameters().size() - 1),
                                                        hasFuture))
                                        .returns(hasFuture ? TypeName.get(methodElement.getReturnType()) : TypeName.VOID)
                                        .build());
                            }
                        }
                    }
                    mqttServiceProxyHandlerClass.addMethods(methodSpecList);
                    JavaFile.builder(processingEnv.getElementUtils().getPackageOf(element).toString(),
                            mqttServiceProxyHandlerClass.build()).build().writeTo(filer);
                }
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            error(null, e.getMessage());
            return true;
        }
    }

    //创建Mqtt发消息语句
    private CodeBlock createMqttClientStatement(String topicPattern, List<String> paramNames, int qos, boolean retained, VariableElement msgBody, boolean hasReply) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        String bodyType = msgBody.asType().toString();
        codeBlock.beginControlFlow("try");
        if (JSONObject.class.getCanonicalName().equals(bodyType)
                || String.class.getCanonicalName().equals(bodyType)
                || Short.class.getCanonicalName().equals(bodyType)
                || Long.class.getCanonicalName().equals(bodyType)
                || Boolean.class.getCanonicalName().equals(bodyType)
                || Integer.class.getCanonicalName().equals(bodyType)
                || Double.class.getCanonicalName().equals(bodyType)
                || Byte.class.getCanonicalName().equals(bodyType)) {
            //TODO 不用toString实现
            codeBlock.addStatement("byte[] payload = " + msgBody.getSimpleName() + ".toString().getBytes()");
        } else if (byte[].class.getCanonicalName().equals(bodyType)) {
            codeBlock.addStatement("byte[] payload = " + msgBody.getSimpleName());
        } else if (Void.class.getCanonicalName().equals(bodyType)) {
            codeBlock.addStatement("byte[] payload = new byte[]{}");
        } else {
            codeBlock.addStatement("byte[] payload = new $T($L).toString().getBytes()", JSONObject.class, msgBody.getSimpleName());
        }
        if (hasReply) {
            codeBlock.addStatement("long serialNum = $T.getSerialNum()", MqttXUtil.class);
            codeBlock.addStatement("payload = new $T($T.getMyClientId(), System.currentTimeMillis(), serialNum, payload).encode()", MqttRespMsg.class, MqttXUtil.class);
        }
        codeBlock.addStatement("mqttClient.publish($L, payload, $L, $L)", createTopicStatement(topicPattern, paramNames), qos, retained);
        if (hasReply) {
            codeBlock.addStatement("MqttReply reply = MqttReply.reply()");
            codeBlock.addStatement("mqttRespService.addWaitReply(serialNum, reply)");
            codeBlock.addStatement("return reply");
        }
        codeBlock.nextControlFlow("catch ($T e)", Exception.class);
        if (hasReply) {
            codeBlock.addStatement("return MqttReply.failReply(e)");
        }
        codeBlock.endControlFlow();
        return codeBlock.build();
    }

    private static final String REGEX = "\\{([a-z_A-Z][a-z_A-Z0-9]*)}";
    private static final Pattern pattern = Pattern.compile(REGEX);

    //创建主题拼接语句
    private String createTopicStatement(String topicPattern, List<String> paramNames) {
        Matcher matcher = pattern.matcher(topicPattern);
        StringBuffer result = new StringBuffer();
        Iterator<String> iterator = paramNames.iterator();
        result.append("\"");
        while (matcher.find() && iterator.hasNext()) {
            matcher.appendReplacement(result, String.format("\"+ %s +\"", iterator.next()));
        }
        matcher.appendTail(result);
        result.append("\"");
        return result.toString();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new HashSet<>();
        supportedAnnotationTypes.add(MqttProxyGen.class.getCanonicalName());
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

}
