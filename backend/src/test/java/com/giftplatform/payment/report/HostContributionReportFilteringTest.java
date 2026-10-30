package com.giftplatform.payment.report;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HostContributionReportFilteringTest {
  private static HostContributionReportService.Entry row(UUID gift,long gp,long wp,String status) {
    return new HostContributionReportService.Entry(UUID.randomUUID(),"MANUAL","Guest",gift,"Table",
      gift==null?"WALLET":"GIFT","UPI",gp+wp,gp,wp,status,null);
  }
  @Test void excessWalletIsVisibleUnderBothGiftAndWalletViews() {
    UUID gift=UUID.randomUUID(); var r=row(gift,1000000,100000,"VERIFICATION_PENDING");
    assertTrue(HostContributionReportService.matches(r,"ALL","ALL",gift));
    assertTrue(HostContributionReportService.matches(r,"GIFT","ALL",gift));
    assertTrue(HostContributionReportService.matches(r,"WALLET","ALL",gift));
    assertFalse(HostContributionReportService.matches(r,"ALL","VERIFIED",gift));
    assertFalse(HostContributionReportService.matches(r,"ALL","ALL",UUID.randomUUID()));
  }
  @Test void directWalletDoesNotMatchGiftFilter() {
    var r=row(null,0,50000,"VERIFICATION_PENDING");
    assertTrue(HostContributionReportService.matches(r,"WALLET","ALL",null));
    assertFalse(HostContributionReportService.matches(r,"GIFT","ALL",null));
  }
  @Test void statusFiltersRespectEvidence() {
    var r=row(UUID.randomUUID(),10000,0,"VERIFIED");
    assertTrue(HostContributionReportService.matches(r,"GIFT","VERIFIED",r.giftId()));
    assertFalse(HostContributionReportService.matches(r,"GIFT","VERIFICATION_PENDING",r.giftId()));
  }
}
