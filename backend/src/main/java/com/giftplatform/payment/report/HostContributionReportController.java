package com.giftplatform.payment.report;

import java.util.UUID;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/contributions")
public class HostContributionReportController {
  private final HostContributionReportService service;
  public HostContributionReportController(HostContributionReportService service){this.service=service;}
  @GetMapping
  public ResponseEntity<HostContributionReportService.Report> report(@PathVariable UUID eventId,
      @RequestParam(defaultValue="20") int limit,@RequestParam(defaultValue="0") int offset,
      @RequestParam(defaultValue="ALL") String type,@RequestParam(defaultValue="ALL") String status,
      @RequestParam(required=false) UUID giftId,
      @RequestParam(required=false) String search,
      @RequestParam(required=false) LocalDate from, @RequestParam(required=false) LocalDate to){
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
      .body(search==null?service.report(eventId,limit,offset,type,status,giftId,from,to):service.report(eventId,limit,offset,type,status,giftId,from,to,search));
  }
  @GetMapping(value="/export.csv", produces="text/csv")
  public ResponseEntity<String> export(@PathVariable UUID eventId,
      @RequestParam(defaultValue="ALL") String type,@RequestParam(defaultValue="ALL") String status,
      @RequestParam(required=false) UUID giftId,
      @RequestParam(required=false) String search,
      @RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to) {
    String csv=search==null?service.exportCsv(eventId,type,status,giftId,from,to):service.exportCsv(eventId,type,status,giftId,from,to,search);
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
      .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=contributions.csv")
      .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(csv);
  }

}
