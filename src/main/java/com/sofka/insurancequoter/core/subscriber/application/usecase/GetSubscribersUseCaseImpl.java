package com.sofka.insurancequoter.core.subscriber.application.usecase;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import com.sofka.insurancequoter.core.subscriber.domain.port.out.SubscriberRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetSubscribersUseCaseImpl implements GetSubscribersUseCase {

    private final SubscriberRepository subscriberRepository;

    @Override
    public List<Subscriber> getAll() {
        return subscriberRepository.findAll();
    }
}
