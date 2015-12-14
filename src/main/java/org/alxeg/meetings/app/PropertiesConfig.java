package org.alxeg.meetings.app;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;

@Configuration
public class PropertiesConfig {

    @Bean
    public static Resource[] propertiesLocations() throws MalformedURLException {
        return new Resource[] {
                new ClassPathResource("application.properties"),
                new UrlResource("file:application.properties"),
        };
    }

    @Bean(name = "appProperties")
    public PropertiesFactoryBean props() throws MalformedURLException {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setIgnoreResourceNotFound(true);
        bean.setLocations(propertiesLocations());
        bean.setFileEncoding("UTF-8");
        bean.setSingleton(false);
        return bean;
    }

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() throws IOException {
        PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        propertyPlaceholderConfigurer.setLocations(propertiesLocations());
        return propertyPlaceholderConfigurer;
    }
}
