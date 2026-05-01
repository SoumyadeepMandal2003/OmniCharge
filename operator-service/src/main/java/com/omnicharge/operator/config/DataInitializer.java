package com.omnicharge.operator.config;

import com.omnicharge.operator.model.Operator;
import com.omnicharge.operator.model.Plan;
import com.omnicharge.operator.repository.OperatorRepository;
import com.omnicharge.operator.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with sample operators and plans on first startup.
 * Checks for existing data before inserting to remain idempotent.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final OperatorRepository operatorRepository;
    private final PlanRepository planRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (operatorRepository.count() > 0) {
                log.info("Operators already seeded — skipping data initialization.");
                return;
            }

            log.info("Seeding initial operators and plans...");

            // ── Operators ────────────────────────────────────────────────────
            Operator jio = operatorRepository.save(Operator.builder()
                    .name("Jio").code("JIO").description("Reliance Jio Infocomm").active(true).build());

            Operator airtel = operatorRepository.save(Operator.builder()
                    .name("Airtel").code("AT").description("Bharti Airtel").active(true).build());

            Operator vi = operatorRepository.save(Operator.builder()
                    .name("Vi").code("VI").description("Vodafone Idea").active(true).build());

            Operator bsnl = operatorRepository.save(Operator.builder()
                    .name("BSNL").code("BSNL").description("Bharat Sanchar Nigam Limited").active(true).build());

            // ── Jio Plans ────────────────────────────────────────────────────
            planRepository.saveAll(List.of(
                    Plan.builder().name("Jio 149").price(new BigDecimal("149.00"))
                            .validityDays(24).data("1GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(jio).build(),

                    Plan.builder().name("Jio 299").price(new BigDecimal("299.00"))
                            .validityDays(28).data("2GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(jio).build(),

                    Plan.builder().name("Jio 599").price(new BigDecimal("599.00"))
                            .validityDays(84).data("2GB/day").calls("Unlimited")
                            .sms("100/day").description("3-month plan").type(Plan.PlanType.PREPAID)
                            .active(true).operator(jio).build(),

                    Plan.builder().name("Jio 5G 999").price(new BigDecimal("999.00"))
                            .validityDays(84).data("Unlimited 5G").calls("Unlimited")
                            .sms("100/day").description("5G unlimited plan").type(Plan.PlanType.PREPAID)
                            .active(true).operator(jio).build()
            ));

            // ── Airtel Plans ─────────────────────────────────────────────────
            planRepository.saveAll(List.of(
                    Plan.builder().name("Airtel 179").price(new BigDecimal("179.00"))
                            .validityDays(28).data("1.5GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(airtel).build(),

                    Plan.builder().name("Airtel 299").price(new BigDecimal("299.00"))
                            .validityDays(28).data("2GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(airtel).build(),

                    Plan.builder().name("Airtel 719").price(new BigDecimal("719.00"))
                            .validityDays(84).data("2GB/day").calls("Unlimited")
                            .sms("100/day").description("3-month plan").type(Plan.PlanType.PREPAID)
                            .active(true).operator(airtel).build()
            ));

            // ── Vi Plans ─────────────────────────────────────────────────────
            planRepository.saveAll(List.of(
                    Plan.builder().name("Vi 199").price(new BigDecimal("199.00"))
                            .validityDays(28).data("1.5GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(vi).build(),

                    Plan.builder().name("Vi 449").price(new BigDecimal("449.00"))
                            .validityDays(56).data("2GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(vi).build()
            ));

            // ── BSNL Plans ───────────────────────────────────────────────────
            planRepository.saveAll(List.of(
                    Plan.builder().name("BSNL 107").price(new BigDecimal("107.00"))
                            .validityDays(30).data("1GB/day").calls("Unlimited")
                            .sms("100/day").type(Plan.PlanType.PREPAID).active(true).operator(bsnl).build(),

                    Plan.builder().name("BSNL 397").price(new BigDecimal("397.00"))
                            .validityDays(90).data("2GB/day").calls("Unlimited")
                            .sms("100/day").description("3-month plan").type(Plan.PlanType.PREPAID)
                            .active(true).operator(bsnl).build()
            ));

            log.info("Data seeding complete: 4 operators, 11 plans created.");
        };
    }
}
