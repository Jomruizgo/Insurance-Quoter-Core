package com.sofka.insurancequoter.core.subscriber.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.subscriber.application.usecase.GetSubscribersUseCaseImpl;
import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.mapper.SubscriberRestMapper;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.out.json.SubscriberJsonAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class SubscriberConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public SubscriberJsonAdapter subscriberJsonAdapter(ObjectMapper objectMapper) {
        return new SubscriberJsonAdapter(objectMapper, new ClassPathResource("fixtures/subscribers.json"));
    }

    @Bean
    public GetSubscribersUseCase getSubscribersUseCase(SubscriberJsonAdapter subscriberJsonAdapter) {
        return new GetSubscribersUseCaseImpl(subscriberJsonAdapter);
    }

    @Bean
    public SubscriberRestMapper subscriberRestMapper() {
        return new SubscriberRestMapper();
    }
}
