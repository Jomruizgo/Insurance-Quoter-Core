package com.sofka.insurancequoter.core.subscriber.domain.port.in;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;

import java.util.List;

public interface GetSubscribersUseCase {
    List<Subscriber> getAll();
}
