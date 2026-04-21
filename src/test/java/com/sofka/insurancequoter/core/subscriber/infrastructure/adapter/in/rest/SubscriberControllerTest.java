package com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscriberDto;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.dto.SubscribersResponse;
import com.sofka.insurancequoter.core.subscriber.infrastructure.adapter.in.rest.mapper.SubscriberRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberControllerTest {

    @Mock
    private GetSubscribersUseCase getSubscribersUseCase;

    @Mock
    private SubscriberRestMapper subscriberRestMapper;

    @InjectMocks
    private SubscriberController controller;

    @Test
    void getSubscribers_returnsHttp200WithSubscriberList() {
        // GIVEN
        List<Subscriber> subscribers = List.of(new Subscriber("SUB-001", "Seguros Sofka"));
        SubscribersResponse response = new SubscribersResponse(List.of(new SubscriberDto("SUB-001", "Seguros Sofka")));
        when(getSubscribersUseCase.getAll()).thenReturn(subscribers);
        when(subscriberRestMapper.toResponse(subscribers)).thenReturn(response);

        // WHEN
        ResponseEntity<SubscribersResponse> result = controller.getSubscribers();

        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().subscribers()).hasSize(1);
        assertThat(result.getBody().subscribers().get(0).id()).isEqualTo("SUB-001");
    }

    @Test
    void getSubscribers_delegatesToUseCaseAndMapper() {
        // GIVEN
        List<Subscriber> subscribers = List.of();
        when(getSubscribersUseCase.getAll()).thenReturn(subscribers);
        when(subscriberRestMapper.toResponse(subscribers)).thenReturn(new SubscribersResponse(List.of()));

        // WHEN
        controller.getSubscribers();

        // THEN
        verify(getSubscribersUseCase, times(1)).getAll();
        verify(subscriberRestMapper, times(1)).toResponse(subscribers);
        verifyNoMoreInteractions(getSubscribersUseCase, subscriberRestMapper);
    }
}
