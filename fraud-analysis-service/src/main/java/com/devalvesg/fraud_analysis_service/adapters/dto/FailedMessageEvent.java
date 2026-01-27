package com.devalvesg.fraud_analysis_service.adapters.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedMessageEvent {

    private String originalTopic;
    private Integer originalPartition;
    private Long originalOffset;
    private String messageKey;
    private Object originalPayload;
    private String errorMessage;
    private String exceptionClass;
    private String stackTrace;
    private Instant failedAt;
    private Integer retryAttempts;
}
