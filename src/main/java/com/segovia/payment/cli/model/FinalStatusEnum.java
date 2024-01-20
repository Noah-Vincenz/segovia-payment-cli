package com.segovia.payment.cli.model;

import lombok.Getter;

public enum FinalStatusEnum {
    SUCCEEDED("Succeeded"),
    FAILED("Failed"),
    UNKNOWN("Unknown");

    @Getter
    private final String value;

    FinalStatusEnum(String value) {
        this.value = value;
    }
}
