package com.sofka.insurancequoter.core.subscriber.domain.port.out;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;

import java.util.List;

public interface SubscriberRepository {
    List<Subscriber> findAll();
}
