package com.omnicharge.operator.service;

import com.omnicharge.operator.dto.OperatorDtos.*;
import com.omnicharge.operator.exception.ResourceNotFoundException;
import com.omnicharge.operator.model.Operator;
import com.omnicharge.operator.model.Plan;
import com.omnicharge.operator.repository.OperatorRepository;
import com.omnicharge.operator.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperatorServiceTest {

    @Mock private OperatorRepository operatorRepository;
    @Mock private PlanRepository planRepository;
    @InjectMocks private OperatorService operatorService;

    private Operator sampleOperator;

    @BeforeEach
    void setUp() {
        sampleOperator = Operator.builder()
                .id(1L).name("Airtel").code("AT")
                .description("Airtel Telecom").active(true).build();
    }

    @Test
    void getAllActiveOperators_returnsList() {
        when(operatorRepository.findByActiveTrue()).thenReturn(List.of(sampleOperator));
        List<OperatorResponse> result = operatorService.getAllActiveOperators();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Airtel");
    }

    @Test
    void getOperatorById_notFound_throws() {
        when(operatorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> operatorService.getOperatorById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createOperator_success() {
        OperatorRequest req = new OperatorRequest("Jio", "JO", "Reliance Jio");
        when(operatorRepository.existsByName("Jio")).thenReturn(false);
        when(operatorRepository.save(any())).thenReturn(
                Operator.builder().id(2L).name("Jio").code("JO").active(true).build());

        OperatorResponse result = operatorService.createOperator(req);
        assertThat(result.getName()).isEqualTo("Jio");
    }

    @Test
    void createOperator_duplicate_throws() {
        OperatorRequest req = new OperatorRequest("Airtel", "AT", "desc");
        when(operatorRepository.existsByName("Airtel")).thenReturn(true);
        assertThatThrownBy(() -> operatorService.createOperator(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getPlansByOperator_success() {
        Plan plan = Plan.builder().id(1L).name("299 Plan").price(new BigDecimal("299"))
                .validityDays(28).type(Plan.PlanType.PREPAID).active(true)
                .operator(sampleOperator).build();
        when(operatorRepository.existsById(1L)).thenReturn(true);
        when(planRepository.findByOperatorIdAndActiveTrue(1L)).thenReturn(List.of(plan));

        List<PlanResponse> result = operatorService.getPlansByOperator(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("299 Plan");
    }

    @Test
    void getPlanById_notFound_throws() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> operatorService.getPlanById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
