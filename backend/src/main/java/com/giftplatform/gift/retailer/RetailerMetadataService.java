package com.giftplatform.gift.retailer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.giftplatform.security.ActorContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class RetailerMetadataService {
    private final JdbcTemplate db;
    private final ActorContext actors;
    private final RetailerPageClient client;
    private final ObjectMapper mapper;
    private final com.giftplatform.gift.photo.GiftPhotoService photos;
    @org.springframework.beans.factory.annotation.Autowired
    public RetailerMetadataService(JdbcTemplate db,ActorContext actors,RetailerPageClient client,ObjectMapper mapper,com.giftplatform.gift.photo.GiftPhotoService photos){
        this.db=db;this.actors=actors;this.client=client;this.mapper=mapper;this.photos=photos;
    }
    public RetailerMetadataService(JdbcTemplate db,ActorContext actors,RetailerPageClient client,ObjectMapper mapper){
        this.db=db;this.actors=actors;this.client=client;this.mapper=mapper;this.photos=null;
    }
    public record Preview(UUID lookupId,String sourceUrl,String name,String description,BigDecimal targetAmount,String currency,String imageKey,String imageWarning){}
    public Preview lookup(UUID eventId,String url) {
        UUID actor=actors.requireAuthenticatedUserId();
        Integer owns=db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=? AND status IN ('DRAFT','PUBLISHED')",Integer.class,eventId,actor);
        if(owns==null||owns!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Editable event not found");
        try {
            String source=RetailerPageClient.validateUrl(url).toString();
            Map<String,Object> parsed=parse(client.fetch(source),source);
            String imageKey=null,imageWarning=null;
            String imageUrl=(String)parsed.get("imageUrl");
            if(photos!=null && imageUrl!=null && !imageUrl.isBlank()) {
                try{imageKey=photos.fromRetailer(eventId,actor,imageUrl).imageKey();}
                catch(java.io.IOException|ResponseStatusException|IllegalArgumentException e){imageWarning="Retailer photo could not be validated. You can upload a photo instead.";}
            }
            parsed.put("imageKey",imageKey);parsed.put("imageWarning",imageWarning);
            UUID lookup=UUID.randomUUID();
            db.update("INSERT INTO gift_retailer_metadata_audit(id,event_id,actor_id,source_url,metadata) VALUES (?,?,?,?,?::jsonb)",
                lookup,eventId,actor,source,mapper.writeValueAsString(parsed));
            return new Preview(lookup,source,(String)parsed.get("name"),(String)parsed.get("description"),
                (BigDecimal)parsed.get("targetAmount"),(String)parsed.get("currency"),imageKey,imageWarning);
        } catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());}
          catch(java.net.SocketTimeoutException e){throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,"The retailer took too long to respond. Try again or enter the details manually.");}
          catch(java.net.UnknownHostException e){throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"The retailer address could not be reached. Check the product link.");}
          catch(java.io.IOException e){
              String detail=e.getMessage();
              // Only our own, user-facing messages are returned; TLS/socket internals stay private.
              boolean safe=detail!=null && (detail.startsWith("This retailer ")||detail.startsWith("The retailer ")||detail.startsWith("Retailer "));
              throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,safe?detail:"Could not connect securely to the retailer. Try again or enter the details manually.");
          }
    }
    public void attach(UUID lookupId,UUID eventId,UUID giftId,UUID actor,String sourceUrl) {
        if(lookupId==null)return;
        int changed=db.update("""
            UPDATE gift_retailer_metadata_audit SET gift_id=?,saved_at=CURRENT_TIMESTAMP
            WHERE id=? AND event_id=? AND actor_id=? AND source_url=?
              AND fetched_at>CURRENT_TIMESTAMP-INTERVAL '24 hours'
              AND (gift_id IS NULL OR gift_id=?)
            """,giftId,lookupId,eventId,actor,sourceUrl,giftId);
        if(changed!=1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Retailer lookup expired or does not match this gift and URL. Fetch it again.");
    }
    public Map<String,Object> parse(String html,String source) {
        Document doc=Jsoup.parse(html,source);
        String host=java.net.URI.create(source).getHost();
        boolean amazonIndia="amazon.in".equalsIgnoreCase(host)||"www.amazon.in".equalsIgnoreCase(host);
        if(amazonIndia && (!doc.select("form[action*=validateCaptcha], #captchacharacters").isEmpty() ||
            doc.title().toLowerCase(Locale.ROOT).contains("robot check")))
            throw new IllegalArgumentException("Amazon requires a browser verification for this request. Open the retailer link and enter the gift details manually.");
        Map<String,String> retailerFields=new LinkedHashMap<>();
        if(amazonIndia) {
            retailerFields.put("name",doc.select("#productTitle").text());
            retailerFields.put("description",String.join("\n",doc.select("#feature-bullets li .a-list-item").eachText()));
            // Restrict prices to the main product panel and exclude crossed-out MRP.
            for(var price:doc.select("#corePriceDisplay_desktop_feature_div .a-price:not(.a-text-price), #corePrice_feature_div .a-price:not(.a-text-price), #apex_desktop .priceToPay")) {
                String whole=price.select(".a-price-whole").text().replace(",","").replaceAll("[^0-9]","");
                String fraction=price.select(".a-price-fraction").text().replaceAll("[^0-9]","");
                if(!whole.isBlank()) {
                    retailerFields.put("price",whole+(fraction.isBlank()?"":"."+fraction));
                    retailerFields.put("currency","INR");break;
                }
                String visible=price.select(".a-offscreen").text();
                if(visible.contains("₹")) {
                    retailerFields.put("price",visible.replace(",","").replaceAll("[^0-9.]",""));
                    retailerFields.put("currency","INR");break;
                }
            }
        }
        Map<String,String> tags=new LinkedHashMap<>();
        doc.select("meta[content]").forEach(e->{String key=e.hasAttr("property")?e.attr("property"):e.attr("name");if(!key.isBlank())tags.put(key.toLowerCase(Locale.ROOT),e.attr("content"));});
        List<JsonNode> structured=new ArrayList<>();
        for(var script:doc.select("script[type=application/ld+json]")) {
            try{structured.add(mapper.readTree(script.data()));}catch(Exception ignored){} // Some stores include malformed JSON-LD.
        }
        JsonNode product=null;
        for(JsonNode node:structured){product=findProduct(node);if(product!=null)break;}
        boolean isProduct=product!=null || "product".equalsIgnoreCase(tags.get("og:type")) ||
            tags.containsKey("product:price:amount") || doc.select("[itemtype]").stream().anyMatch(e->e.attr("itemtype").matches("(?i)https?://schema.org/Product/?"));
        if(amazonIndia)isProduct=!retailerFields.getOrDefault("name","").isBlank() && java.net.URI.create(source).getPath().matches("(?i).*/(?:dp|gp/product)/[A-Z0-9]{10}(?:/.*)?");
        if(!isProduct)throw new IllegalArgumentException("This link is not a recognizable product page. Use the individual product link, not a store home page, category, or search page.");
        JsonNode image=product==null?null:product.path("image");
        if(image!=null&&image.isArray())image=image.isEmpty()?null:image.get(0);
        String structuredImage=image==null?"":image.isObject()?text(image,"url"):image.asText("");
        String imageUrl=first(amazonIndia?doc.select("#landingImage, #imgBlkFront").attr("src"):"",structuredImage,tags.get("og:image"),tags.get("twitter:image"));
        try{imageUrl=imageUrl.isBlank()?"":RetailerPageClient.validateUrl(java.net.URI.create(source).resolve(imageUrl).toString()).toString();}
        catch(IllegalArgumentException e){imageUrl="";}
        String name=first(retailerFields.get("name"),text(product,"name"),tags.get("og:title"),tags.get("twitter:title"),doc.title());
        String description=first(retailerFields.get("description"),text(product,"description"),tags.get("og:description"),tags.get("description"));
        JsonNode offer=product==null?null:product.path("offers");
        if(offer!=null&&offer.isArray())offer=offer.isEmpty()?null:offer.get(0);
        String currency=first(retailerFields.get("currency"),text(offer,"priceCurrency"),tags.get("product:price:currency"),tags.get("og:price:currency"));
        String price=first(retailerFields.get("price"),text(offer,"price"),text(offer,"lowPrice"),tags.get("product:price:amount"),tags.get("og:price:amount"));
        BigDecimal amount=null;
        try{if("INR".equalsIgnoreCase(currency)){var n=new BigDecimal(price.replace(",",""));if(n.signum()>0&&n.compareTo(new BigDecimal("10000000"))<=0)amount=n.setScale(2,java.math.RoundingMode.HALF_UP);}}catch(Exception ignored){}
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("sourceUrl",source);result.put("fetchedAt",Instant.now().toString());
        result.put("name",limit(Jsoup.parse(name).text(),160));result.put("description",limit(Jsoup.parse(description).text(),2000));
        result.put("imageUrl",imageUrl);result.put("targetAmount",amount);result.put("currency",currency);result.put("retailerFields",retailerFields);result.put("metaTags",tags);result.put("jsonLd",structured);
        if(name.isBlank()&&description.isBlank())throw new IllegalArgumentException("No retailer details found. Enter gift details manually.");
        return result;
    }
    private static JsonNode findProduct(JsonNode node) {
        if(node==null)return null;
        if(node.isObject()) {
            JsonNode type=node.path("@type");
            if("Product".equalsIgnoreCase(type.asText()))return node;
            if(type.isArray())for(JsonNode value:type)if("Product".equalsIgnoreCase(value.asText()))return node;
        }
        if(node.isArray())for(JsonNode value:node){JsonNode found=findProduct(value);if(found!=null)return found;}
        if(node.isObject()) {
            JsonNode graph=findProduct(node.path("@graph"));if(graph!=null)return graph;
            return findProduct(node.path("mainEntity"));
        }
        return null;
    }
    private static String text(JsonNode node,String key){return node==null?"":node.path(key).asText("");}
    private static String first(String...values){for(String value:values)if(value!=null&&!value.isBlank())return value.trim();return "";}
    private static String limit(String value,int max){return value.length()>max?value.substring(0,max):value;}
}
