package org.example;

import com.nsw.cs.client.AddressApiClient;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {

        AddressApiClient client=new AddressApiClient();
        client.getDistrictJson("346 PANORAMA AVENUE BATHURST");
        System.out.println( "Hello World!" );

       // System.out.println(AddressApiClient.extractSuburb("346 PANORAMA CLOSE NORTH BATHURST"));
    }
}
