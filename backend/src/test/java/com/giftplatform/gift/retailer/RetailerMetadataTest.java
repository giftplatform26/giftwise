package com.giftplatform.gift.retailer;

import java.net.InetAddress;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class RetailerMetadataTest {
    final RetailerMetadataService service=new RetailerMetadataService(null,null,null,new ObjectMapper());
    @Test void structuredProductWinsAndPreservesRawJson(){
        var data=service.parse("""
            <title>Store</title><meta property="og:title" content="Fallback">
            <script type="application/ld+json">{"@graph":[{"@type":"WebPage"},{"@type":"Product","name":"Mixer &amp; Bowl","description":"<b>Steel</b>","offers":{"price":"1999.95","priceCurrency":"INR"}}]}</script>
            ""","https://retailer.com/item");
        assertEquals("Mixer & Bowl",data.get("name"));assertEquals("Steel",data.get("description"));
        assertEquals(new BigDecimal("1999.95"),data.get("targetAmount"));
        assertTrue(data.get("jsonLd").toString().contains("1999.95"));
    }
    @Test void openGraphFallbackAndNonInrPriceDoesNotFillInrAmount(){
        var data=service.parse("""
            <meta property="og:title" content="Camera"><meta name="description" content="A camera">
            <meta property="product:price:amount" content="99.95"><meta property="product:price:currency" content="USD">
            <script type="application/ld+json">broken</script>
            ""","https://retailer.com/item");
        assertEquals("Camera",data.get("name"));assertNull(data.get("targetAmount"));assertEquals("USD",data.get("currency"));
    }
    @Test void amazonCurrentPriceWinsOverCrossedOutMrpAndOtherProducts(){
        var data=service.parse("""
            <title>Amazon.in : washing machine</title>
            <meta name="description" content="Generic store description">
            <span id="productTitle">LG 7 Kg Smart Inverter Washing Machine</span>
            <div id="corePriceDisplay_desktop_feature_div">
              <span class="a-price a-text-price"><span class="a-offscreen">₹27,490</span></span>
              <span class="a-price"><span class="a-price-symbol">₹</span><span class="a-price-whole">18,990</span><span class="a-price-fraction">00</span></span>
            </div>
            <div id="feature-bullets"><ul><li><span class="a-list-item">7 kg capacity</span></li><li><span class="a-list-item">Smart inverter motor</span></li></ul></div>
            <div id="recommendations"><span class="a-price"><span class="a-offscreen">₹999</span></span></div>
            ""","https://www.amazon.in/LG-Washing-Machine/dp/B0DPHT2HZL/ref=sr_1_1?psc=1");
        assertEquals("LG 7 Kg Smart Inverter Washing Machine",data.get("name"));
        assertEquals(new BigDecimal("18990.00"),data.get("targetAmount"));
        assertEquals("INR",data.get("currency"));
        assertEquals("7 kg capacity Smart inverter motor",data.get("description"));
        assertTrue(data.get("retailerFields").toString().contains("18990.00"));
    }
    @Test void amazonChallengeIsNotSavedAsAGift(){
        assertThrows(IllegalArgumentException.class,()->service.parse("<title>Amazon.com</title><form action='/errors/validateCaptcha'><input id='captchacharacters'></form>","https://www.amazon.in/dp/B0DPHT2HZL"));
    }
    @Test void amazonMrpAloneDoesNotBecomeTheTargetPrice(){
        var data=service.parse("<span id='productTitle'>LG Washer</span><div id='corePriceDisplay_desktop_feature_div'><span class='a-price a-text-price'><span class='a-offscreen'>₹27,490</span></span></div>","https://www.amazon.in/dp/B0DPHT2HZL");
        assertNull(data.get("targetAmount"));
    }
    @Test void homeAndSearchPagesAreRejectedEvenWithATitle(){
        assertThrows(IllegalArgumentException.class,()->service.parse("<title>Shop everything</title><meta property='og:title' content='Store home'>","https://retailer.com/"));
        assertThrows(IllegalArgumentException.class,()->service.parse("<title>Search</title><span id='productTitle'>Search results</span>","https://www.amazon.in/s?k=washer"));
    }
    @Test void searchResultProductsAreNotMistakenForTheMainProduct(){
        assertThrows(IllegalArgumentException.class,()->service.parse("<title>Search results</title><script type='application/ld+json'>{\"@type\":\"ItemList\",\"itemListElement\":[{\"@type\":\"Product\",\"name\":\"Camera\"}]}</script>","https://retailer.com/search?q=camera"));
    }
    @Test void productImageIsPickedAndUnsafeImageLinksAreDiscarded(){
        var data=service.parse("<span id='productTitle'>Washer</span><img id='landingImage' src='https://m.media-amazon.com/images/I/washer.jpg'>","https://www.amazon.in/dp/B0DPHT2HZL");
        assertEquals("https://m.media-amazon.com/images/I/washer.jpg",data.get("imageUrl"));
        var invalid=service.parse("<meta property='og:type' content='product'><title>Gift</title><meta property='og:image' content='http://localhost/private'>","https://retailer.com/item");
        assertEquals("",invalid.get("imageUrl"));
    }
    @Test void httpsOnlyAndNoLocalOrCredentialedUrls(){
        for(String url:new String[]{"http://retailer.com/item","https://localhost/item","https://127.0.0.1/","https://[::1]/","https://a.local/","https://user:pass@retailer.com/item","https://retailer.com:8443/"})
            assertThrows(IllegalArgumentException.class,()->RetailerPageClient.validateUrl(url));
        assertEquals("retailer.com",RetailerPageClient.validateUrl("https://retailer.com/item").getHost());
    }
    @Test void dnsResolverBlocksPrivateAndMetadataDestinations() throws Exception {
        for(String address:new String[]{"127.0.0.1","10.0.0.1","192.168.1.1","172.16.0.1","169.254.169.254","100.64.0.1","0.0.0.0","::1","fc00::1","fe80::1"})
            assertFalse(RetailerPageClient.publicAddress(InetAddress.getByName(address)),address);
        assertTrue(RetailerPageClient.publicAddress(InetAddress.getByName("8.8.8.8")));
    }
}
