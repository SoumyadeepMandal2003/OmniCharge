package com.omnicharge.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RechargeEvent {
    private Long id;
    private String rechargeId;
    private String mobileNumber;
    private String operatorName;
    private String planName;
    private BigDecimal amount;
    private Integer validityDays;
    private String status;
    private String transactionId;

    // Accept any ISO LocalDateTime format — with or without microseconds/nanoseconds
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * Handles all LocalDateTime formats sent by recharge-service:
     *   - "2026-05-03T13:27:26.54722"   (ISO with microseconds, no offset)
     *   - "2026-05-03 13:27:26"          (space-separated)
     *   - "2026-05-03T13:27:26"          (ISO basic)
     */
    public static class FlexibleLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
        private static final DateTimeFormatter FLEXIBLE = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .optionalStart().appendLiteral('T').optionalEnd()
                .optionalStart().appendLiteral(' ').optionalEnd()
                .appendPattern("HH:mm:ss")
                .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
                .toFormatter();

        public FlexibleLocalDateTimeDeserializer() {
            super(FLEXIBLE);
        }
    }
}
