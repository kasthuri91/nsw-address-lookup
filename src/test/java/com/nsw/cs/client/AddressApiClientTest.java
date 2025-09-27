package com.nsw.cs.client;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AddressApiClientTest {

    private static Method extractSuburb;

    @BeforeAll
    static void setUp() throws Exception {
        // Access the private static method: extractSuburb(String)
        extractSuburb = AddressApiClient.class.getDeclaredMethod("extractSuburb", String.class);
        extractSuburb.setAccessible(true);
    }

    private static String callExtractSuburb(String input) {
        try {
            // static method → target = null
            return (String) extractSuburb.invoke(null, input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void returnsNullForNullOrBlank() {
        assertNull(callExtractSuburb(null));
        assertNull(callExtractSuburb("   "));
    }

    @Test
    void extractsAfterFullStreetType() {
        assertEquals("BATHURST",
                callExtractSuburb("346 PANORAMA AVENUE BATHURST"));
    }

    @Test
    void caseInsensitiveAndWhitespaceNormalised() {
        assertEquals("Bathurst",
                callExtractSuburb("  346   Panorama    avenue   Bathurst  "));
    }

    @Test
    void whenNoStreetTypePresentReturnsOriginalTail() {
        assertEquals("BATHURST", callExtractSuburb("BATHURST"));
    }

}