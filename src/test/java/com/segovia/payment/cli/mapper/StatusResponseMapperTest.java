package com.segovia.payment.cli.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusResponseMapperTest {

    @ParameterizedTest
    @CsvSource({
            "0, Succeeded",
            "100, Unknown",
            "500, Failed",
            "-10, Failed",
            "1000, Failed",
            "20000, Failed",
            "20001, Failed",
            "20002, Failed",
            "20003, Failed",
            "20004, Failed",
            "20005, Failed",
            "20014, Failed",
            "30006, Failed",
    })
    void test_mapStatusResponse(int input, String expectedOutput) {
        assertEquals(expectedOutput, StatusResponseMapper.mapStatusResponse(input));
    }
}
