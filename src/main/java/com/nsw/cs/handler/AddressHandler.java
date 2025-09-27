package com.nsw.cs.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.nsw.cs.client.AddressApiClient;

import java.net.http.HttpTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Lambda handler for address lookups.
 *
 * <p>Expects an HTTP API v2 / Function URL event with a query parameter
 * {@code address}. On success, returns a JSON payload containing the
 * district, suburb and coordinates for the supplied address.</p>
 */
public class AddressHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    /**
     * Default response headers.
     */
    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
            "Content-Type", "application/json"
    );

    /**
     * Single, reusable API client instance (final to avoid reassignment).
     */
    private final AddressApiClient client = new AddressApiClient();

    /**
     * Handles the incoming request.
     *
     * <p>Validates the {@code address} query parameter, calls the upstream
     * address service, and returns a JSON response.</p>
     *
     * @param event   HTTP API v2 event from the Function URL
     * @param context Lambda execution context
     * @return JSON response with status code and body
     */
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setHeaders(new HashMap<>(DEFAULT_HEADERS));

        System.out.println("Inside handle request : event " + event);
        try {
            // Ensure the event and query string map exist.
            if (event == null || event.getQueryStringParameters() == null) {
                response.setStatusCode(400);
                response.setBody("{\"error\":\"'address' query parameter is required\"}");
                return response;
            }

            System.out.println("Extract Address");
            // Extract and validate the 'address' query parameter.
            String address = event.getQueryStringParameters().get("address");
            if (address == null || address.isEmpty()) {
                response.setStatusCode(400);
                response.setBody("{\"error\":\"'address' query parameter is required\"}");
                return response;
            }

            // Call the client service to get district/coordinates JSON.
            String json = client.getDistrictJson(address);
            //Success, set response status 200 and return the json which contains district,coordinates and address
            response.setStatusCode(200);
            response.setBody(json);
            System.out.println("Response : " + json);
            return response;

        } catch (NoSuchElementException notFound) {
            // Upstream could not find coordinates or district for the address.
            System.out.println("Error : not found " + notFound);
            response.setStatusCode(404);
            response.setBody("{\"error\":\""+notFound.getLocalizedMessage()+"\"}");
            return response;

        } catch (HttpTimeoutException timeout) {
            // Upstream call timed out.
            System.out.println("Error : timeout " + timeout);
            response.setStatusCode(504);
            response.setBody("{\"error\":\"Upstream timed out\"}");
            return response;

        } catch (Exception e) {
            // Unexpected upstream/processing error.
            System.out.println("Error :  " + e);
            response.setStatusCode(502);
            response.setBody("{\"error\":\"Unexpected error\"}");
            return response;
        }
    }
}
