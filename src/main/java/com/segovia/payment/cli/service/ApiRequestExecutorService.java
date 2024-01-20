package com.segovia.payment.cli.service;

import java.util.Map;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.get;
import static org.asynchttpclient.Dsl.post;

public class ApiRequestExecutorService {

    private static final AsyncHttpClient webClient = asyncHttpClient();
    private static final String BASE_URL = "http://127.0.0.1:7902";
    private static final String CONTENT_TYPE_HEADER_VALUE_APPLICATION_JSON = "application/json";

    private ApiRequestExecutorService() {
    }

    public static Future<Response> sendGetRequest(String path, Map<String, String> headers) {
        RequestBuilder requestBuilder = get(BASE_URL + path);
        headers.forEach(requestBuilder::setHeader);
        return webClient.executeRequest(requestBuilder.build());
    }

    public static Future<Response> sendPostRequest(String path, Map<String, String> headers, String body) {
        RequestBuilder requestBuilder = post(BASE_URL + path)
                .setHeader(CONTENT_TYPE, CONTENT_TYPE_HEADER_VALUE_APPLICATION_JSON);
        headers.forEach(requestBuilder::setHeader);
        requestBuilder.setBody(body);
        return webClient.executeRequest(requestBuilder.build());
    }
}
