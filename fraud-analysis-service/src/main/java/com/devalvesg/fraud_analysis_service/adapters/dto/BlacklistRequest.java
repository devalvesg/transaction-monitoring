package com.devalvesg.fraud_analysis_service.adapters.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistRequest {

    @NotBlank(message = "Address is required")
    @Size(max = 256, message = "Address must not exceed 256 characters")
    private String address;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
