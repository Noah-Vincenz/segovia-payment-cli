package com.segovia.payment.cli.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.segovia.payment.cli.model.AccessToken;
import com.segovia.payment.cli.model.request.AuthRequest;
import com.segovia.payment.cli.model.request.PayRequest;

import static com.segovia.payment.cli.constants.Constants.DEFAULT_ACCOUNT;
import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;

public class PayRequestService {

    private static final ObjectMapper mapper = new ObjectMapper();

    private PayRequestService() {
    }

    public static Response sendPayRequest(PayRequest payRequest, AccessToken accessToken) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        if (accessToken.getExpiryDateTime().isBefore(LocalDateTime.now().minusSeconds(15))) { // access token is expiring soon
            accessToken.setToken(AuthRequestService.sendAuthRequest(AuthRequest.builder()
                                                                               .account(DEFAULT_ACCOUNT)
                                                                               .build())
                                                   .getToken());
            accessToken.setExpiryDateTime(LocalDateTime.now().plusMinutes(5));
        }
        Future<Response> payResponseFuture = ApiRequestExecutorService.sendPostRequest("/pay", Map.of(AUTHORIZATION, "Bearer " + accessToken.getToken()), mapper.writeValueAsString(payRequest));
        return payResponseFuture.get(5, TimeUnit.SECONDS);
    }
}
