package com.giftplatform.event.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventDeliveryAddressTest {
    @Test void sameAddressTracksChangesToVenue(){
        var e=new Event("Party",OccasionType.BIRTHDAY);
        e.setEventContact("First street","City","State","123456","India","Host","1234567890",null);
        assertEquals("First street",e.getDeliveryAddress().addressLine1());
        e.setEventContact("New venue","City","State","123456","India","Host","1234567890",null);
        assertEquals("New venue",e.getDeliveryAddress().addressLine1());
    }
    @Test void separateDeliveryAddressCanBeResetToVenue(){
        var e=new Event("Party",OccasionType.BIRTHDAY);
        var delivery=new DeliveryAddress("Home","City","State","123456","India");
        e.setDeliveryAddress(false,delivery);
        assertEquals(delivery,e.getDeliveryAddress());
        assertFalse(e.isDeliverySameAsEvent());
        assertThrows(IllegalArgumentException.class,()->e.setDeliveryAddress(false,null));
        e.setDeliveryAddress(true,null);
        assertTrue(e.isDeliverySameAsEvent());
    }
}
