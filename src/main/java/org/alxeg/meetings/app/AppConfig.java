package org.alxeg.meetings.app;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.alxeg.meetings.services.RoomService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan("org.alxeg.meetings")
public class AppConfig extends WebMvcConfigurerAdapter {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JodaModule());
        objectMapper.setDateFormat(new StdDateFormat().getISO8601Format(TimeZone.getDefault(), Locale.getDefault()));
        return objectMapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter() {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(objectMapper());
        messageConverter.setSupportedMediaTypes(MediaType.parseMediaTypes(
                  "application/json,"+
                  "application/vnd.spring-boot.actuator.v1+json;q=0.8"));
        return messageConverter;
    }

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        StringHttpMessageConverter messageConverter = new StringHttpMessageConverter();
        messageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
        return messageConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonMessageConverter());
        converters.add(stringHttpMessageConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/**")) {
            registry
                .addResourceHandler("/**")
                .addResourceLocations("classpath:/webapp/");
        }
    }

    @Bean
    public RoomService room1() {
        return new RoomService();
    }
    @Bean
    public RoomService room2() {
        return new RoomService();
    }
    @Bean
    public RoomService room3() {
        return new RoomService();
    }
    @Bean
    public RoomService room4() {
        return new RoomService();
    }
    @Bean
    public RoomService room5() {
        return new RoomService();
    }

}

