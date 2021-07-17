package xyz.iaoe.spring.starter.mqtt.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

/**
 * @author iaoe
 * @date 2021/7/5 14:03
 */
public class ConfigurationUtils {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationUtils.class);

    public static MetadataReaderFactory getMetadataReaderFactory() {
        return metadataReaderFactory;
    }

    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();


    public static boolean checkAnnotationCandidate(BeanDefinition beanDef, Class<?> annotation) {
        AnnotationMetadata metadata = null;
        if (beanDef instanceof AbstractBeanDefinition &&
                ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
            Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
            metadata = new StandardAnnotationMetadata(beanClass, true);
        } else {
            String className = beanDef.getBeanClassName();
            if (className != null) {
                try {
                    //根据className，获取元数据reader,里面用了asm框架,visitor模式
                    MetadataReader metadataReader =
                            metadataReaderFactory.getMetadataReader(className);
                    metadata = metadataReader.getAnnotationMetadata();
                } catch (IOException ex) {
                    log.debug("Could not find class file for introspecting factory methods: " + className, ex);
                    return false;
                }
            }
        }

        if (metadata != null) {
            return metadata.isAnnotated(annotation.getName());
        }

        return false;
    }

    public static MetadataReader getMetadataReader(String className) {
        MetadataReaderFactory metadataReaderFactory = getMetadataReaderFactory();
        MetadataReader metadataReader;
        try {
            metadataReader = metadataReaderFactory.getMetadataReader(className);
        } catch (IOException e) {
            log.error("error", e);
            throw new RuntimeException();
        }
        return metadataReader;
    }


    public static MetadataReader getMetadataReader(Resource resource) {
        MetadataReaderFactory metadataReaderFactory = getMetadataReaderFactory();
        MetadataReader metadataReader = null;
        try {
            metadataReader = metadataReaderFactory.getMetadataReader(resource);
        } catch (IOException e) {
            log.error("error", e);
            throw new RuntimeException();
        }
        return metadataReader;
    }


}
