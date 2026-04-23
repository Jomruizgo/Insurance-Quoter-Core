package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscribersResponse;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.mapper.SubscriberRestMapper;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.swaggerdocs.SubscriberApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubscriberController implements SubscriberApi {

    private final GetSubscribersUseCase getSubscribersUseCase;
    private final SubscriberRestMapper subscriberRestMapper;

    @Override
    public ResponseEntity<SubscribersResponse> getSubscribers() {
        return ResponseEntity.ok(subscriberRestMapper.toResponse(getSubscribersUseCase.getAll()));
    }
}
