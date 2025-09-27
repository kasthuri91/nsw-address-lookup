package com.nsw.cs.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsw.cs.util.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST API client for querying NSW Spatial Services.
 */
public class AddressApiClient {

    //Shared http client
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_2)
            .build();

    //Shared Jackson mapper
    private static final ObjectMapper MAPPER = new ObjectMapper();
    //Initialize timeout duration for rest calls
    private static final Duration REQ_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Resolve district and coordinates for the given address and return a compact JSON string.
     *
     * @param address sent in query param
     * @return JSON string containing suburb, coordinates, and district
     * @throws Exception
     */
    public String getDistrictJson(String address) throws Exception {
        System.out.println("Inside getDistrictJson");

        //Build the address-theme URI and call NSW Spatial (address theme).
        URI addressUri = buildAddressLookupUri(address);
        String addressBody = send(addressUri);

        //Parse the root JSON and extract the features array.
        JsonNode adderRoot = MAPPER.readTree(addressBody);
        JsonNode features = adderRoot.path("features");
        if (!features.isArray() || features.isEmpty()) {
            System.out.println("Address features not found");
            throw new NoSuchElementException("No features");
        }

        // Take the first feature and read its coordinates from geometry.
        JsonNode firstFeature = features.get(0);
        JsonNode coordinates = firstFeature.path("geometry").path("coordinates");
        if (!coordinates.isArray() || coordinates.size() < 2) {
            System.out.println("No coordinates found");
            throw new NoSuchElementException("No coordinates");
        }

        double x = coordinates.get(0).asDouble();
        double y = coordinates.get(1).asDouble();

        //Build the boundaries URI and call NSW Spatial (boundaries layer).
        URI boundariesUri = buildBoundariesLookupUri(x, y);
        //Send boundaries request to the NSW spatial boundaries api
        String boundariesBody = send(boundariesUri);

        JsonNode disRoot = MAPPER.readTree(boundariesBody);
        JsonNode disFeatures = disRoot.path("features");
        if (!disFeatures.isArray() || disFeatures.isEmpty()) {
            System.out.println("No boundaries features");
            throw new NoSuchElementException("No features");
        }

        // Extract the district name from the first boundary feature.
        JsonNode properties = disFeatures.get(0).path("properties");
        String district = properties.hasNonNull("districtname")
                ? properties.get("districtname").asText()
                : null;
        if (district == null || district.isBlank()) {
            System.out.println("District not found");
            throw new NoSuchElementException("No district");
        }

        // Extract the suburb from the original address string.
        String suburb = extractSuburb(address);

        //Build the final payload (latitude = y, longitude = x).
        Map<String, Object> payload = Map.of(
                "suburb", suburb,
                "coordinates", Map.of("latitude", y, "longitude", x),
                "district", district
        );
        System.out.println("Response : " + payload);
        return MAPPER.writeValueAsString(payload);
    }

    /**
     * Build the NSW Spatial address-theme lookup URI for a given address.
     *
     * @param address
     * @return URI for the address theme endpoint
     */

    private static URI buildAddressLookupUri(String address) {
        String where = "address = '" + address.replace("'", "''") + "'";
        String query = "where=" + URLEncoder.encode(where, StandardCharsets.UTF_8) +
                "&outFields=*" +
                "&f=geojson";
        return URI.create(Constants.ADDRESS_URL + "?" + query);
    }

    /**
     * Build the NSW Spatial boundaries lookup URI using longitude/latitude.
     *
     * @param lon
     * @param lat
     * @return URI for the boundaries endpoint
     */

    private static URI buildBoundariesLookupUri(double lon, double lat) {
        String coords = lon + "," + lat;
        String query = "geometry=" + URLEncoder.encode(coords, StandardCharsets.UTF_8) +
                "&geometryType=esriGeometryPoint" +
                "&inSR=4326" +
                "&spatialRel=esriSpatialRelIntersects" +
                "&outFields=*" +
                "&returnGeometry=false" +
                "&f=geojson";
        return URI.create(Constants.BOUNDARIES_URL + "?" + query);
    }

    /**
     * Send a GET request to the given URI and return the response body as text.
     *
     * @param uri endpoint to call
     * @return body as a string
     * @throws IOException
     * @throws InterruptedException
     * @throws HttpTimeoutException
     */
    private static String send(URI uri) throws IOException, InterruptedException, HttpTimeoutException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(REQ_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        System.out.println("Before sending request to NSW spatial service");
        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("After calling request to NSW spatial service , response : " + resp.body());

        int code = resp.statusCode();
        if (code < 200 || code >= 300) {
            System.out.println("IllegalStateException code:" + code);
            throw new IllegalStateException("HTTP " + code + " from " + uri);
        }
        return resp.body();
    }

    /**
     * Extract the suburb/locality from an Australian-style address string.
     * <p>Assumptions:</p>
     * <ul>
     *    <li>The street type (e.g., STREET, ROAD) appears before the suburb.</li>
     *    <li>The address ends with the suburb (optionally followed by state/postcode, which this method ignores).</li>
     *  </ul>
     *
     * @param address
     * @return suburb
     */
    private static String extractSuburb(String address) {

        // Common full street-type words;
        final String STREET_TYPES =
                "(?:STREET|ROAD|AVENUE|DRIVE|COURT|CRESCENT|LANE|HIGHWAY|PARADE|PLACE|BOULEVARD|TERRACE|WAY|CLOSE|ESPLANADE)";

        if (address == null || address.isBlank()) {
            return null;
        }

        // Normalise whitespace and commas, then remove everything up to the last street type.
        String sub = address.trim().replace(",", " ").replaceAll("\\s+", " ");
        sub = sub.replaceFirst("(?i).*\\b" + STREET_TYPES + "\\b\\s+", "");

        return sub.trim();
    }


}
