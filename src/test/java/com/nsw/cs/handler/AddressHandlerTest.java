package com.nsw.cs.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AddressHandlerTest {

    @Test
    void testWithAddressQueryParam() {
        var event = new APIGatewayV2HTTPEvent();
        event.setQueryStringParameters(Map.of("address", "346 PANORAMA AVENUE BATHURST"));
        var ctx = mock(Context.class);
        var resp = new AddressHandler().handleRequest(event, ctx);
        assertEquals(200, resp.getStatusCode());
    }

    @Test
    void testWithoutAddressQueryParam() {
        var event = new APIGatewayV2HTTPEvent();
        var ctx = mock(Context.class);
        var resp = new AddressHandler().handleRequest(event, ctx);
        assertEquals(400, resp.getStatusCode());
    }

    @Test
    void testWithInvalidAddressQueryParam() {
        var event = new APIGatewayV2HTTPEvent();
        event.setQueryStringParameters(Map.of("address", "Test BATHURST"));

        var ctx = mock(Context.class);
        var resp = new AddressHandler().handleRequest(event, ctx);
        assertEquals(404, resp.getStatusCode());
    }
    @Test
    void testWithEmptyAddressQueryParam() {
        var event = new APIGatewayV2HTTPEvent();
        event.setQueryStringParameters(Map.of("address", ""));
        var ctx = mock(Context.class);
        var resp = new AddressHandler().handleRequest(event, ctx);
        assertEquals(400, resp.getStatusCode());
    }

}
