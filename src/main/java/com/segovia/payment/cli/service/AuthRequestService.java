package com.segovia.payment.cli.service;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.segovia.payment.cli.model.request.AuthRequest;
import com.segovia.payment.cli.model.response.AuthResponse;

public class AuthRequestService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DEFAULT_API_KEY = "0aVp83wuFp6wjvQ3";

    private AuthRequestService() {
    }

    public static AuthResponse sendAuthRequest(AuthRequest authRequest) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Future<Response> authResponseFuture = ApiRequestExecutorService.sendPostRequest("/auth", Map.of("Api-Key", DEFAULT_API_KEY), mapper.writeValueAsString(authRequest));
        return mapper.readValue(authResponseFuture.get(5, TimeUnit.SECONDS).getResponseBody(), AuthResponse.class);
    }
}
