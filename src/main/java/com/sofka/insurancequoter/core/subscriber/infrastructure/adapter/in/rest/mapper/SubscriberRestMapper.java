package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.mapper;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscriberDto;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscribersResponse;

import java.util.List;

public class SubscriberRestMapper {

    public SubscribersResponse toResponse(List<Subscriber> subscribers) {
        List<SubscriberDto> dtos = subscribers.stream()
                .map(s -> new SubscriberDto(s.id(), s.name()))
                .toList();
        return new SubscribersResponse(dtos);
    }
}
