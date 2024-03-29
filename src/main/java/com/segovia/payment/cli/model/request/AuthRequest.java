package com.segovia.payment.cli.model.request;

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
public class AuthRequest {
    String account;
}
