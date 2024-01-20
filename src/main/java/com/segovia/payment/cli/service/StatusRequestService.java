package com.segovia.payment.cli.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.segovia.payment.cli.model.AccessToken;
import com.segovia.payment.cli.model.request.AuthRequest;

import static com.segovia.payment.cli.constants.Constants.DEFAULT_ACCOUNT;
import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;

public class StatusRequestService {

    private StatusRequestService() {
    }

    public static Response sendStatusRequest(String conversationId, AccessToken accessToken) throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
        if (accessToken.getExpiryDateTime().isBefore(LocalDateTime.now().minusSeconds(15))) { // access token is expiring soon
            accessToken.setToken(AuthRequestService.sendAuthRequest(AuthRequest.builder()
                                                                               .account(DEFAULT_ACCOUNT)
                                                                               .build())
                                                   .getToken());
            accessToken.setExpiryDateTime(LocalDateTime.now().plusMinutes(5));
        }
        Future<Response> statusResponseFuture = ApiRequestExecutorService.sendGetRequest("/status/" + conversationId, Map.of(AUTHORIZATION, "Bearer " + accessToken.getToken()));
        return statusResponseFuture.get(5, TimeUnit.SECONDS);
    }
}
