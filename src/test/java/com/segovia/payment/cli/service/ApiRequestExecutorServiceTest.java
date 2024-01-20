package com.segovia.payment.cli.service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.Response;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.segovia.payment.cli.model.request.PayRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiRequestExecutorServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void test_sendPayPostRequest_no_token() throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Future<Response> futureResponse = ApiRequestExecutorService.sendPostRequest("/pay",
                                                                                    Collections.emptyMap(),
                                                                                    mapper.writeValueAsString(PayRequest.builder().build()));
        assertEquals(403, futureResponse.get(5, TimeUnit.SECONDS).getStatusCode());
    }

    @Test
    void test_sendStatusGetRequest_no_token() throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Future<Response> futureResponse = ApiRequestExecutorService.sendGetRequest("/status/12345",
                                                                                    Collections.emptyMap());
        assertEquals(403, futureResponse.get(5, TimeUnit.SECONDS).getStatusCode());
    }
}
