package com.segovia.payment.cli.mapper;

import com.segovia.payment.cli.model.FinalStatusEnum;

public class StatusResponseMapper {

    private StatusResponseMapper() {
    }

    public static String mapStatusResponse(int statusToBeMapped) {
        return switch (statusToBeMapped) {
            case 0 -> FinalStatusEnum.SUCCEEDED.getValue();
            case 100 -> FinalStatusEnum.UNKNOWN.getValue();
            default -> FinalStatusEnum.FAILED.getValue();
        };
    }
}
