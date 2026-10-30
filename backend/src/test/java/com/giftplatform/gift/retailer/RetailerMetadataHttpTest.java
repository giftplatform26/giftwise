package com.giftplatform.gift.retailer;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RetailerMetadataHttpTest {
    @Test void lookupErrorsExposeActionableDetailWithoutGlobalErrorSettings() throws Exception {
        UUID event=UUID.randomUUID();String url="https://retailer.com/item";
        var service=mock(RetailerMetadataService.class);
        when(service.lookup(event,url)).thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY,
            "This retailer blocks automated lookups. You can still save its link and enter the gift details."));
        var mvc=MockMvcBuilders.standaloneSetup(new RetailerMetadataController(service)).build();
        mvc.perform(post("/api/v1/events/"+event+"/gifts/retailer-metadata")
            .contentType(MediaType.APPLICATION_JSON).content("{\"productUrl\":\""+url+"\"}"))
            .andExpect(status().isBadGateway()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("This retailer blocks automated lookups. You can still save its link and enter the gift details."))
            .andExpect(header().string("Cache-Control","no-store"));
    }
}
