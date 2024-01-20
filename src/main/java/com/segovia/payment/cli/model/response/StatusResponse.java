package com.segovia.payment.cli.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusResponse {
    int status;
    String timestamp;
    String reference;
    String message;
    String customerReference;
    String fee;
}
