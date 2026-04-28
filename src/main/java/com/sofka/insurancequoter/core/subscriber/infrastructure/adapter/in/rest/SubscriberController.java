package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.shared.infrastructure.metrics.SubscriberMetrics;
import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscribersResponse;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.mapper.SubscriberRestMapper;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.swaggerdocs.SubscriberApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Subscribers resource.
 * Records subscriber_queries_total metric on every successful response.
 */
@RestController
@RequiredArgsConstructor
public class SubscriberController implements SubscriberApi {

    private final GetSubscribersUseCase getSubscribersUseCase;
    private final SubscriberRestMapper subscriberRestMapper;
    private final SubscriberMetrics subscriberMetrics;

    @Override
    public ResponseEntity<SubscribersResponse> getSubscribers() {
        ResponseEntity<SubscribersResponse> response = ResponseEntity.ok(
                subscriberRestMapper.toResponse(getSubscribersUseCase.getAll()));
        subscriberMetrics.recordQuery();
        return response;
    }
}
