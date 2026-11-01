package com.giftplatform.group;

import com.giftplatform.gift.retailer.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/public/groups/{token}")
public class IndependentGroupGiftController {
    private final JdbcTemplate db;
    private final GroupContributionController groups;
    private final RetailerPageClient client;
    private final RetailerMetadataService metadata;
    public IndependentGroupGiftController(JdbcTemplate db,GroupContributionController groups,RetailerPageClient client,RetailerMetadataService metadata){
        this.db=db;this.groups=groups;this.client=client;this.metadata=metadata;
    }
    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<org.springframework.http.ProblemDetail> failure(ResponseStatusException error){
        return org.springframework.http.ResponseEntity.status(error.getStatusCode()).body(org.springframework.http.ProblemDetail.forStatusAndDetail(error.getStatusCode(),error.getReason()==null?"Unable to update the gift plan":error.getReason()));
    }
    public record Link(@NotBlank @Size(max=1000) String productUrl) {}
    @PostMapping("/gift-metadata")
    public Map<String,Object> lookup(@PathVariable UUID token,@Valid @RequestBody Link input,HttpServletRequest request){
        UUID creator=groups.sessionAdmin(groups.event(token),request);
        var session=request.getSession();
        synchronized(session){
            String key="gift.group.metadata."+creator;
            @SuppressWarnings("unchecked") var attempts=(ArrayDeque<Long>)session.getAttribute(key);
            if(attempts==null){attempts=new ArrayDeque<>();session.setAttribute(key,attempts);}
            long now=System.currentTimeMillis();while(!attempts.isEmpty()&&now-attempts.peekFirst()>60000)attempts.removeFirst();
            if(attempts.size()>=10)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Please wait before fetching another gift link");
            attempts.addLast(now);
        }
        try{
            String source=RetailerPageClient.validateUrl(input.productUrl()).toString();
            var parsed=metadata.parse(client.fetch(source),source);
            var out=new LinkedHashMap<String,Object>();
            for(String key:List.of("name","description","targetAmount","currency"))out.put(key,parsed.get(key));
            out.put("productUrl",source);return out;
        }catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,ex.getMessage());}
        catch(java.io.IOException ex){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Could not read this product page. You can enter the gift details manually.");}
    }
    public record Plan(@Size(max=160) String giftName,@Size(max=500) String giftDescription,
        @Size(max=1000) @Pattern(regexp="https://[^\\s]+") String giftLink,@Positive @Max(100000000000L) Long targetPaise) {}
    @PostMapping("/gift-plan/{groupId}")
    @Transactional
    public Map<String,String> plan(@PathVariable UUID token,@PathVariable UUID groupId,@Valid @RequestBody Plan input,HttpServletRequest request){
        UUID creator=groups.requireGroupCreator(token,groupId,request);
        var row=db.queryForMap("SELECT independent_gift,gift_published_at FROM gift_groups WHERE id=? AND admin_id=? FOR UPDATE",groupId,creator);
        if(!Boolean.TRUE.equals(row.get("independent_gift")))throw new ResponseStatusException(HttpStatus.CONFLICT,"This group uses a host wishlist gift");
        if(row.get("gift_published_at")!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Published gift plans cannot be changed");
        String name=clean(input.giftName()),description=clean(input.giftDescription()),link=clean(input.giftLink());
        if(name==null&&(description!=null||link!=null))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter the gift name");
        db.update("UPDATE gift_groups SET gift_name=?,gift_description=?,gift_link=?,target_paise=? WHERE id=?",name,description,link,input.targetPaise(),groupId);
        return Map.of("message","Your group gift plan is saved. All contributions and history are preserved.");
    }
    private String clean(String value){return value==null||value.isBlank()?null:value.strip();}
}
