package com.sofka.insurancequoter.core.agent.application.usecase;

import com.sofka.insurancequoter.core.agent.domain.model.Agent;
import com.sofka.insurancequoter.core.agent.domain.port.out.AgentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAgentsUseCaseImplTest {

    @Mock
    private AgentRepository agentRepository;

    @InjectMocks
    private GetAgentsUseCaseImpl useCase;

    @Test
    void getAll_returnsListFromRepository() {
        // GIVEN
        List<Agent> expected = List.of(
                new Agent("AGT-123", "Juan Pérez", "SUB-001"),
                new Agent("AGT-124", "María López", "SUB-001")
        );
        when(agentRepository.findAll()).thenReturn(expected);

        // WHEN
        List<Agent> result = useCase.getAll();

        // THEN
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAll_delegatesToRepositoryExactlyOnce() {
        // GIVEN
        when(agentRepository.findAll()).thenReturn(List.of());

        // WHEN
        useCase.getAll();

        // THEN
        verify(agentRepository, times(1)).findAll();
        verifyNoMoreInteractions(agentRepository);
    }

    @Test
    void getAll_returnsEmptyListWhenRepositoryIsEmpty() {
        // GIVEN
        when(agentRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<Agent> result = useCase.getAll();

        // THEN
        assertThat(result).isEmpty();
    }
}
