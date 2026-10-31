package com.giftplatform.monetization;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.server.ResponseStatusException;

class MonetizationCalculationTest {
    private MonetizationService.Settings settings(boolean master,boolean stream,String mode,long fixed,int bps,long cap,String bearer) {
        return new MonetizationService.Settings(7,master,List.of(new MonetizationService.Stream("CONTRIBUTION",stream,mode,fixed,bps,cap,bearer,true)));
    }
    @Test void switchesIndependentlyDisableFees() {
        for(var config:List.of(settings(false,true,"FIXED",100,0,0,"GUEST"),settings(true,false,"FIXED",100,0,0,"GUEST"))) {
            var q=MonetizationService.calculate(config,"CONTRIBUTION",50000);
            assertEquals(0,q.platformFeePaise());assertEquals(50000,q.totalPaise());assertEquals(50000,q.hostReceivesPaise());
        }
    }
    @Test void percentWithCapMatchesExamples() {
        var config=settings(true,true,"PERCENTAGE",0,200,7500,"GUEST");
        assertEquals(1000,MonetizationService.calculate(config,"CONTRIBUTION",50000).platformFeePaise());
        assertEquals(2000,MonetizationService.calculate(config,"CONTRIBUTION",100000).platformFeePaise());
        var q=MonetizationService.calculate(config,"CONTRIBUTION",500000);
        assertEquals(7500,q.platformFeePaise());assertEquals(507500,q.totalPaise());assertEquals(500000,q.hostReceivesPaise());
    }
    @Test void hostDeductionDoesNotChangeGuestTotal() {
        var q=MonetizationService.calculate(settings(true,true,"PERCENTAGE",0,200,0,"HOST"),"CONTRIBUTION",100000);
        assertEquals(100000,q.totalPaise());assertEquals(98000,q.hostReceivesPaise());assertEquals(2000,q.platformFeePaise());
    }
    @Test void fixedAndRoundingAreInIntegerPaise() {
        assertEquals(99,MonetizationService.calculate(settings(true,true,"FIXED",99,0,0,"GUEST"),"CONTRIBUTION",100).platformFeePaise());
        assertEquals(1,MonetizationService.calculate(settings(true,true,"PERCENTAGE",0,100,0,"GUEST"),"CONTRIBUTION",50).platformFeePaise());
    }
    @Test void rejectNonpositiveNetAndInvalidAmounts() {
        var config=settings(true,true,"FIXED",100,0,0,"HOST");
        for(long amount:List.of(0L,-1L,100L,100000001L)) assertThrows(ResponseStatusException.class,()->MonetizationService.calculate(config,"CONTRIBUTION",amount));
    }
}
