package com.sofka.insurancequoter.core.subscriber.application.usecase;

import com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber;
import com.sofka.insurancequoter.core.subscriber.domain.port.out.SubscriberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSubscribersUseCaseImplTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private GetSubscribersUseCaseImpl useCase;

    @Test
    void getAll_returnsListFromRepository() {
        // GIVEN
        List<Subscriber> expected = List.of(
                new Subscriber("SUB-001", "Seguros Sofka"),
                new Subscriber("SUB-002", "Aseguradora Norte")
        );
        when(subscriberRepository.findAll()).thenReturn(expected);

        // WHEN
        List<Subscriber> result = useCase.getAll();

        // THEN
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAll_delegatesToRepositoryExactlyOnce() {
        // GIVEN
        when(subscriberRepository.findAll()).thenReturn(List.of());

        // WHEN
        useCase.getAll();

        // THEN
        verify(subscriberRepository, times(1)).findAll();
        verifyNoMoreInteractions(subscriberRepository);
    }

    @Test
    void getAll_returnsEmptyListWhenRepositoryIsEmpty() {
        // GIVEN
        when(subscriberRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<Subscriber> result = useCase.getAll();

        // THEN
        assertThat(result).isEmpty();
    }
}
