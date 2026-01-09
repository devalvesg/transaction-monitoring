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
public class BlacklistResponse {

    private Long id;
    private String address;
    private String reason;
    private Instant addedAt;
}
