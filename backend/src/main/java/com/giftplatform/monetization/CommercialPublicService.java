package com.giftplatform.monetization;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommercialPublicService {
    private final JdbcTemplate db;private final MonetizationService pricing;
    public CommercialPublicService(JdbcTemplate db,MonetizationService pricing){this.db=db;this.pricing=pricing;}
    @Transactional public List<Map<String,Object>> placements(String stream,String occasion){
        if(!Set.of("VENDOR_LISTING","SPONSORED_GIFT").contains(stream==null?"":stream))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if(!pricing.enabled(stream))return List.of();
        return db.queryForList("""
            SELECT p.id,p.title,p.description,p.category,p.occasion,p.stream,
            '/backend/api/public/commerce/placements/'||p.id||'/visit' AS url
            FROM commercial_placements p WHERE p.stream=? AND p.moderation='APPROVED'
            AND (p.occasion='ALL' OR ?='' OR p.occasion=?) AND EXISTS(
              SELECT 1 FROM commercial_entitlements e WHERE e.buyer_kind='HOST' AND e.buyer_id=p.owner_id
              AND e.stream=p.stream AND e.resource_id=p.id AND e.starts_at<=now() AND e.expires_at>now())
            ORDER BY p.created_at DESC LIMIT 50
            """,stream,occasion==null?"":occasion,occasion==null?"":occasion);
    }
    @Transactional public String visit(UUID id){
        var rows=db.queryForList("SELECT stream,url,owner_id FROM commercial_placements WHERE id=? AND moderation='APPROVED'",id);
        if(rows.size()!=1)throw notFound();var row=rows.getFirst();
        if(!pricing.enabled((String)row.get("stream"))||!Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM commercial_entitlements WHERE buyer_kind='HOST' AND buyer_id=? AND stream=? AND resource_id=? AND starts_at<=now() AND expires_at>now())",Boolean.class,row.get("owner_id"),row.get("stream"),id)))throw notFound();
        db.update("INSERT INTO commercial_placement_clicks(id,placement_id) VALUES(?,?)",UUID.randomUUID(),id);return (String)row.get("url");
    }
    @Transactional public String affiliate(UUID token,UUID gift){
        var rows=db.queryForList("""
            SELECT g.product_url,g.event_id FROM gifts g JOIN events e ON e.id=g.event_id
            WHERE e.public_token=? AND e.status='PUBLISHED' AND g.id=? AND g.status='ACTIVE'
              AND g.visibility='PUBLIC' AND g.product_url IS NOT NULL
            """,token,gift);
        if(rows.size()!=1)throw notFound();var row=rows.getFirst();String source=(String)row.get("product_url");
        URI uri=CommercialSetupService.https(source);
        if(!pricing.enabled("AFFILIATE"))return source;
        var partners=db.queryForList("SELECT * FROM affiliate_partners WHERE enabled ORDER BY length(product_domain) DESC");
        for(var partner:partners){
            String domain=(String)partner.get("product_domain"),host=uri.getHost().toLowerCase(Locale.ROOT);
            if(!host.equals(domain)&&!host.endsWith("."+domain))continue;
            UUID click=UUID.randomUUID();String param=(String)partner.get("parameter_name"),subid=(String)partner.get("click_parameter");
            List<String> query=new ArrayList<>();
            if(uri.getRawQuery()!=null)for(String part:uri.getRawQuery().split("&")){
                String key=URLDecoder.decode(part.split("=",2)[0],StandardCharsets.UTF_8);
                if(!key.equals(param)&&!key.equals(subid))query.add(part);
            }
            query.add(encode(param)+"="+encode((String)partner.get("parameter_value")));
            if(subid!=null)query.add(encode(subid)+"="+click);
            String target="https://"+uri.getRawAuthority()+(uri.getRawPath()==null?"":uri.getRawPath())+"?"+String.join("&",query)+(uri.getRawFragment()==null?"":"#"+uri.getRawFragment());
            String redirect=(String)partner.get("redirect_template");
            if(redirect!=null)target=redirect.replace("{url}",encode(target)).replace("{click}",click.toString());
            db.update("INSERT INTO affiliate_clicks(id,partner_id,event_id,gift_id,destination_url) VALUES(?,?,?,?,?)",click,partner.get("id"),row.get("event_id"),gift,target);
            return target;
        }
        return source;
    }
    private static String encode(String s){return URLEncoder.encode(s,StandardCharsets.UTF_8);}
    private static ResponseStatusException notFound(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Offer is unavailable");}
}
