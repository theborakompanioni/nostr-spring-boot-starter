package org.tbk.nostr.example.agentic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import org.tbk.jackson.datatype.nostr.NostrModule;

import java.io.IOException;
import java.math.BigDecimal;

@EnableWebMvc
@Configuration(proxyBeanMethods = false)
class NostrAgenticExampleApplicationWebMvcConfigurer implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/",
            "classpath:/resources/",
            "classpath:/static/",
            "classpath:/public/"
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "index.html");
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new BufferedImageHttpMessageConverter());
        builder.withJsonConverter(new MappingJackson2HttpMessageConverter(configureObjectMapper()));
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

        SimpleModule internalModule = new SimpleModule("AppInternal")
                .addSerializer(new BigDecimalToStringSerializer());

        return objectMapper
                .registerModule(internalModule)
                .registerModule(new NostrModule())
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static final class BigDecimalToStringSerializer extends JsonSerializer<BigDecimal> {

        @Override
        public Class<BigDecimal> handledType() {
            return BigDecimal.class;
        }

        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toPlainString());
        }
    }
}
