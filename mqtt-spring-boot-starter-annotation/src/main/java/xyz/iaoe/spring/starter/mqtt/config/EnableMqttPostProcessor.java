package xyz.iaoe.spring.starter.mqtt.config;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import xyz.iaoe.spring.starter.mqtt.annotation.EnableMqttAutoConfiguration;
import xyz.iaoe.spring.starter.mqtt.utils.ConfigurationUtils;
import xyz.iaoe.spring.starter.mqtt.utils.EnableMqttAutoConfigurationParser;

import java.util.Collections;

/**
 * EnableMqttAutoConfiguration生效的配置
 *
 * @author iaoe
 * @date 2021/7/5 12:47
 */
@Component
public class EnableMqttPostProcessor implements BeanDefinitionRegistryPostProcessor, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware {

    private ClassLoader classLoader;

    private Environment environment;

    private ResourceLoader resourceLoader;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            //找到是否由BeanName注解
            if (ConfigurationUtils.checkAnnotationCandidate(beanDef, EnableMqttAutoConfiguration.class)) {
                BeanDefinitionHolder bdh = new BeanDefinitionHolder(beanDef, beanName);
                EnableMqttAutoConfigurationParser parser = new EnableMqttAutoConfigurationParser(environment, registry);
                parser.parse(Collections.singleton(bdh));
                return;
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
