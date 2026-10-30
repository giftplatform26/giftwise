package com.giftplatform.payment.report;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HostContributionReportExportTest {
 @Test void csvInrConvertsPaiseExactly() {
  assertEquals("1700.00", HostContributionReportService.csvInr(170000));
  assertEquals("1799.00", HostContributionReportService.csvInr(179900));
  assertEquals("0.01", HostContributionReportService.csvInr(1));
  assertEquals("0.00", HostContributionReportService.csvInr(0));
  assertEquals("-0.01", HostContributionReportService.csvInr(-1));
 }

 @Test void csvEscapesFormulasAndQuotes() {
  assertEquals("\"'=SUM(1,2)\"",HostContributionReportService.csvCell("=SUM(1,2)"));
  assertEquals("\"John \"\"Guest\"\"\"",HostContributionReportService.csvCell("John \"Guest\""));
  assertEquals("\"'+123\"",HostContributionReportService.csvCell("+123"));
 }
 @Test void dateBoundsAreInclusiveUtc() {
  var d=OffsetDateTime.parse("2026-10-22T23:30:00+05:30");
  assertTrue(HostContributionReportService.withinDates(d,LocalDate.parse("2026-10-22"),LocalDate.parse("2026-10-22")));
  assertFalse(HostContributionReportService.withinDates(d,LocalDate.parse("2026-10-23"),null));
 }
}
