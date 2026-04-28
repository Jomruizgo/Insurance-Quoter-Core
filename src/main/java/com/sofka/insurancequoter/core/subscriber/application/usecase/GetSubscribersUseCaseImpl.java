package com.sofka.insurancequoter.core.subscriber.application.usecase;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import com.sofka.insurancequoter.core.subscriber.domain.port.out.SubscriberRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @Observed creates a trace span per invocation via ObservedAspect (registered in ObservabilityConfig).
 */
@Observed(name = "subscriber.get-all", contextualName = "get-subscribers")
@RequiredArgsConstructor
public class GetSubscribersUseCaseImpl implements GetSubscribersUseCase {

    private final SubscriberRepository subscriberRepository;

    @Override
    public List<Subscriber> getAll() {
        return subscriberRepository.findAll();
    }
}
