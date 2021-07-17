package xyz.iaoe.spring.starter.mqtt.utils;

import xyz.iaoe.spring.starter.mqtt.annotation.EnableMqttAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.*;

/**
 * Spring源码抄过来的
 *
 * @author iaoe
 * @date 2021/7/5 14:43
 */
public class EnableMqttAutoConfigurationParser {

    private static final Logger log = LoggerFactory.getLogger(EnableMqttAutoConfigurationParser.class);

    private final BeanNameGenerator serviceScanBeanNameGenerator = new AnnotationBeanNameGenerator();

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    private final ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
    private final Environment environment;
    private final BeanDefinitionRegistry registry;

    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private final List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();
    private final BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();


    public EnableMqttAutoConfigurationParser(Environment environment, BeanDefinitionRegistry registry) {
        this.environment = environment;
        this.registry = registry;
    }


    public void parse(Set<BeanDefinitionHolder> configCandidates) {
        for (BeanDefinitionHolder holder : configCandidates) {
            BeanDefinition bd = holder.getBeanDefinition();
            String className = bd.getBeanClassName();
            MetadataReader metadataReader = ConfigurationUtils.getMetadataReader(className);
            AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

            Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(EnableMqttAutoConfiguration.class.getName(), true);
            AnnotationAttributes enableMqttAutoConfiguration = AnnotationAttributes.fromMap(annotationAttributes);

            if (enableMqttAutoConfiguration != null) {
                parse(enableMqttAutoConfiguration, className);
            }
        }
    }

    public Set<BeanDefinitionHolder> parse(AnnotationAttributes enableMqttAutoConfiguration, String className) {
        String[] basePackages = enableMqttAutoConfiguration.getStringArray("services");

        includeFilters.add(new AnnotationTypeFilter(Service.class));
        Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
        Set<BeanDefinition> candidates = new HashSet<>();
        for (String basePackage : basePackages) {
            candidates.addAll(findCandidateComponents(basePackage));
        }

        for (BeanDefinition candidate : candidates) {
            String generateBeanName = serviceScanBeanNameGenerator.generateBeanName(candidate, registry);

            if (candidate instanceof AbstractBeanDefinition) {
                ((AbstractBeanDefinition) candidate).applyDefaults(this.beanDefinitionDefaults);
            }

            if (candidate instanceof AnnotatedBeanDefinition) {
                AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
            }

            boolean b = checkCandidate(generateBeanName, candidate);
            if (b) {
                beanDefinitions.add(new BeanDefinitionHolder(candidate, generateBeanName));
            }
        }


        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitions) {
            registry.registerBeanDefinition(beanDefinitionHolder.getBeanName(), beanDefinitionHolder.getBeanDefinition());
        }

        return beanDefinitions;
    }

    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if (!this.registry.containsBeanDefinition(beanName)) {
            return true;
        }
        return false;
    }

    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + "/" + DEFAULT_RESOURCE_PATTERN;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {

                if (!resource.isReadable()) {
                    continue;
                }

                MetadataReader metadataReader = ConfigurationUtils.getMetadataReader(resource);
                if (isCandidateComponent(metadataReader)) {
                    ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                    sbd.setResource(resource);
                    sbd.setSource(resource);
                    candidates.add(sbd);
                } else {
                    log.info("Ignored because not matching any filter: " + resource);
                }
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }

        return candidates;
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(environment.resolveRequiredPlaceholders(basePackage));
    }


    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, ConfigurationUtils.getMetadataReaderFactory())) {
                return true;
            }
        }
        return false;
    }

}
