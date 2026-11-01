package com.giftplatform.admin;

import com.giftplatform.security.ActorContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("/api/admin/console/audit")
public class PlatformAdminAuditController {
    private static final int PAGE_SIZE=20;
    private final JdbcTemplate db;
    private final ActorContext actors;
    public PlatformAdminAuditController(JdbcTemplate db,ActorContext actors){this.db=db;this.actors=actors;}
    public record Page(List<Map<String,Object>> entries,long total,int page,int pageSize,long totalPages,List<String> actions){}

    @GetMapping("/page")
    @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Page page(@RequestParam(defaultValue="0") int page,
                     @RequestParam(defaultValue="") String search,
                     @RequestParam(defaultValue="") String action,
                     @RequestParam(defaultValue="") String module,
                     @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
                     @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to){
        if(!actors.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Platform admin only");
        actors.requireAuthenticatedUserId();
        if(page<0||page>1000000||search.length()>200||action.length()>60||module.length()>40
            ||(from!=null&&(from.getYear()<1||from.getYear()>9999))
            ||(to!=null&&(to.getYear()<1||to.getYear()>9999))
            ||(from!=null&&to!=null&&from.isAfter(to)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid audit filters");
        StringBuilder where=new StringBuilder(" WHERE TRUE");
        List<Object> params=new ArrayList<>();
        if(!search.isBlank()){
            where.append(" AND strpos(lower(concat_ws(' ',a.action,a.module_code,a.actor_id::text,a.target_id::text,u.username,u.email)),lower(?))>0");
            params.add(search.strip());
        }
        if(!action.isBlank()){where.append(" AND a.action=?");params.add(action.strip());}
        if(!module.isBlank()){where.append(" AND a.module_code=?");params.add(module.strip());}
        if(from!=null){where.append(" AND a.created_at>=?");params.add(Timestamp.from(from.atStartOfDay(ZoneOffset.UTC).toInstant()));}
        if(to!=null){where.append(" AND a.created_at<?");params.add(Timestamp.from(to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));}
        String source=" FROM platform_admin_action_audit a LEFT JOIN users u ON u.id=a.actor_id"+where;
        Long total=db.queryForObject("SELECT count(*)"+source,Long.class,params.toArray());
        long count=total==null?0:total;
        long pages=(count+PAGE_SIZE-1)/PAGE_SIZE;
        int current=(int)Math.min(page,Math.max(0,pages-1));
        List<Object> pageParams=new ArrayList<>(params);pageParams.add(PAGE_SIZE);pageParams.add(current*PAGE_SIZE);
        var entries=db.queryForList("SELECT a.id,a.actor_id,a.action,a.target_id,a.module_code,a.created_at,COALESCE(u.username,u.email,a.actor_id::text) AS actor_name"+source+" ORDER BY a.created_at DESC,a.id DESC LIMIT ? OFFSET ?",pageParams.toArray());
        var actions=db.queryForList("SELECT DISTINCT action FROM platform_admin_action_audit ORDER BY action",String.class);
        return new Page(entries,count,current,PAGE_SIZE,pages,actions);
    }
}
