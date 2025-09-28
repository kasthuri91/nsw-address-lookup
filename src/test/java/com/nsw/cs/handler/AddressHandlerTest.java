package com.nsw.cs.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.nsw.cs.Exception.HttpClientException;
import com.nsw.cs.Exception.HttpServerException;
import com.nsw.cs.client.AddressApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AddressHandler
 */
public class AddressHandlerTest {

    private AddressHandler handler;
    private AddressApiClient mockClient;

    @BeforeEach
    void setUp() throws Exception {
        handler = new AddressHandler();
        mockClient = Mockito.mock(AddressApiClient.class);

        // Inject mock into private final field AddressHandler.client
        Field f = AddressHandler.class.getDeclaredField("client");
        f.setAccessible(true);
        f.set(handler, mockClient);
    }

    // ---- helpers ----------------------------------------------------------

    private static APIGatewayV2HTTPEvent eventWithAddress(String address) {
        APIGatewayV2HTTPEvent e = new APIGatewayV2HTTPEvent();
        Map<String, String> qs = new HashMap<>();
        qs.put("address", address);
        e.setQueryStringParameters(qs);
        return e;
    }

    private static APIGatewayV2HTTPEvent eventWithoutQueryParams() {
        return new APIGatewayV2HTTPEvent(); // null query params
    }

    // ---- tests: success & validation --------------------------------------

    @Test
    void returns200_onSuccess() throws Exception {
        String address = "346 PANORAMA AVENUE BATHURST";
        String json = "{\"district\":\"BATHURST\"}";
        when(mockClient.getDistrictJson(address)).thenReturn(json);

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void returns400_whenQueryParamsMissing() {
        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithoutQueryParams(), null);
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    void returns400_whenAddressBlank() {
        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress("   "), null);
        assertEquals(400, resp.getStatusCode());
    }

    // ---- tests for each catch branch ----------------------------------------

    @Test
    void returns404_onNoSuchElementException() throws Exception {
        String address = "unknown address";
        when(mockClient.getDistrictJson(address)).thenThrow(new NoSuchElementException("Not found"));

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(404, resp.getStatusCode());
    }

    @Test
    void returns400_onHttpClientException_404() throws Exception {
        String address = "some address";
        when(mockClient.getDistrictJson(address))
                .thenThrow(new HttpClientException("Upstream 404", 400));

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(400, resp.getStatusCode());
    }

    @Test
    void returns502_onHttpServerException() throws Exception {
        String address = "any";
        when(mockClient.getDistrictJson(address))
                .thenThrow(new HttpServerException("Upstream 502", 502));

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(502, resp.getStatusCode());
    }

    @Test
    void returns504_onHttpTimeoutException() throws Exception {
        String address = "any";
        when(mockClient.getDistrictJson(address))
                .thenThrow(new HttpTimeoutException("timed out"));

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(504, resp.getStatusCode());
    }

    @Test
    void returns502_onConnectException() throws Exception {
        String address = "any";
        when(mockClient.getDistrictJson(address))
                .thenThrow(new ConnectException("connection refused"));

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(502, resp.getStatusCode());
    }

    @Test
    void returns500_onUnexpectedException() throws Exception {
        String address = "any";
        when(mockClient.getDistrictJson(address))
                .thenThrow(new RuntimeException("exception"));

        APIGatewayV2HTTPResponse resp = handler.handleRequest(eventWithAddress(address), null);

        assertEquals(500, resp.getStatusCode());
    }
}
