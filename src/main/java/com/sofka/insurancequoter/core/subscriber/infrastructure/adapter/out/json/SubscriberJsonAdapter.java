package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.out.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.domain.port.out.SubscriberRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class SubscriberJsonAdapter implements SubscriberRepository {

    private final List<Subscriber> subscribers;

    public SubscriberJsonAdapter(ObjectMapper objectMapper, Resource resource) {
        try {
            this.subscribers = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load subscribers from " + resource.getDescription(), e);
        }
    }

    @Override
    public List<Subscriber> findAll() {
        return List.copyOf(subscribers);
    }
}
