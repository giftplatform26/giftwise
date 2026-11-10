package com.giftplatform.localization;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Service
public class LocalizationService {
    private final JdbcTemplate db;
    public LocalizationService(JdbcTemplate db) { this.db=db; }
    public record Language(String code,String nativeName,String englishName,String direction,boolean enabled,long version) {}
    public record Settings(boolean enabled,String defaultLanguage,String fallbackLanguage,long version) {}
    public record Catalog(boolean enabled,String defaultLanguage,String fallbackLanguage,String selectedLanguage,
                          String direction,List<Language> languages,Map<String,Map<String,String>> resources,Map<String,String> sources,long version) {}
    public Settings settings() {
        Settings settings=db.queryForObject("SELECT * FROM platform_localization_settings WHERE id=1",(rs,i)->
            new Settings(rs.getBoolean("enabled"),rs.getString("default_language"),rs.getString("fallback_language"),rs.getLong("version")));
        return settings==null?new Settings(false,"en","en",0):settings;
    }
    public List<Language> languages(boolean enabledOnly) {
        return db.query("SELECT * FROM platform_languages"+(enabledOnly?" WHERE enabled":"")+" ORDER BY english_name",(rs,i)->
            new Language(rs.getString("code"),rs.getString("native_name"),rs.getString("english_name"),rs.getString("direction"),rs.getBoolean("enabled"),rs.getLong("version")));
    }
    public boolean available(UUID host,UUID event) {
        if(!settings().enabled()) return false;
        return Boolean.TRUE.equals(db.queryForObject("SELECT COALESCE((SELECT enabled FROM localization_host_controls WHERE host_id=?),TRUE) AND COALESCE((SELECT enabled FROM localization_event_controls WHERE event_id=?),TRUE)",Boolean.class,host,event));
    }
    public String preference(UUID user) {
        if(user==null) return null;
        return db.query("SELECT language_code FROM user_language_preferences WHERE user_id=?",(rs,i)->rs.getString(1),user).stream().findFirst().orElse(null);
    }
    public UUID[] publicEvent(UUID token) {
        if(token==null) return new UUID[]{null,null};
        return db.query("SELECT owner_id,id FROM events WHERE public_token=? AND status='PUBLISHED'",(rs,i)->new UUID[]{rs.getObject(1,UUID.class),rs.getObject(2,UUID.class)},token)
            .stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Invitation unavailable"));
    }
    public String resolve(UUID host,UUID event,String requested,boolean platformAdministrator) {
        Settings s=settings(); boolean enabled=platformAdministrator?s.enabled():available(host,event);
        Set<String> active=new HashSet<>(); for(Language language:languages(true))active.add(language.code());
        if(!enabled)return s.defaultLanguage();
        if(requested!=null&&active.contains(requested))return requested;
        String preferred=preference(host); return active.contains(preferred)?preferred:s.defaultLanguage();
    }
    public Catalog catalog(UUID host,UUID event,String requested,boolean platformAdministrator) {
        Settings s=settings();List<Language> active=languages(true);String selected=resolve(host,event,requested,platformAdministrator);
        LinkedHashSet<String> chain=new LinkedHashSet<>(List.of(selected,s.defaultLanguage(),s.fallbackLanguage(),"en"));
        Map<String,Map<String,String>> resources=new LinkedHashMap<>();
        Map<String,String> source=new LinkedHashMap<>(); db.query("SELECT message_key,source_text FROM localization_sources ORDER BY message_key",(org.springframework.jdbc.core.RowCallbackHandler)rs->source.put(rs.getString(1),rs.getString(2)));
        for(String code:chain) {
            Map<String,String> messages=new LinkedHashMap<>();
            db.query("SELECT message_key,translated_text FROM localization_translations WHERE language_code=? AND published",(org.springframework.jdbc.core.RowCallbackHandler)rs->messages.put(rs.getString(1),rs.getString(2)),code);
            resources.put(code,Map.copyOf(messages));
        }
        String direction=active.stream().filter(l->l.code().equals(selected)).map(Language::direction).findFirst().orElse("LTR");
        return new Catalog(platformAdministrator?s.enabled():available(host,event),s.defaultLanguage(),s.fallbackLanguage(),selected,direction,active,resources,Map.copyOf(source),s.version());
    }
    public static String sourceKey(String text) {
        try{return "ui."+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))).substring(0,16);}
        catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
    }
    public String render(String key,Map<String,String> values,String requestedLanguage) {
        Settings settings=settings();String code=settings.enabled()?requestedLanguage:settings.defaultLanguage();
        LinkedHashSet<String> chain=new LinkedHashSet<>();if(code!=null)chain.add(code);chain.add(settings.defaultLanguage());chain.add(settings.fallbackLanguage());
        String text=null;
        for(String language:chain){var rows=db.query("SELECT t.translated_text FROM localization_translations t JOIN platform_languages l ON l.code=t.language_code WHERE t.message_key=? AND t.language_code=? AND t.published AND l.enabled",(rs,i)->rs.getString(1),key,language);if(!rows.isEmpty()){text=rows.get(0);break;}}
        if(text==null)text=db.query("SELECT source_text FROM localization_sources WHERE message_key=?",(rs,i)->rs.getString(1),key).stream().findFirst().orElse(key);
        Matcher matcher=Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9_]*)}}").matcher(text);StringBuffer result=new StringBuffer();
        while(matcher.find())matcher.appendReplacement(result,Matcher.quoteReplacement(values.getOrDefault(matcher.group(1),matcher.group())));matcher.appendTail(result);return result.toString();
    }

    /** Match only code-owned communication templates. Customer-authored content is never registered publicly. */
    public String localizeRendered(String message,String language) {
        if(message==null||message.length()>20000)return message;
        var templates=db.query("SELECT message_key,source_text FROM localization_sources WHERE category IN ('EMAIL','SMS','NOTIFICATION') ORDER BY message_key",(rs,i)->new String[]{rs.getString(1),rs.getString(2)});
        for(String[] template:templates){
            if(!template[1].contains("{{")){if(template[1].equals(message))return render(template[0],Map.of(),language);continue;}
            if(template[0].startsWith("notification."))continue; // Financial/customer variables must be passed structurally, never inferred from text.
            Matcher tokens=Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9_]*)}}").matcher(template[1]);StringBuilder regex=new StringBuilder("^");List<String> names=new ArrayList<>();int end=0;
            while(tokens.find()){regex.append(Pattern.quote(template[1].substring(end,tokens.start()))).append("([\\s\\S]*?)");names.add(tokens.group(1));end=tokens.end();}
            regex.append(Pattern.quote(template[1].substring(end))).append('$');Matcher matched=Pattern.compile(regex.toString()).matcher(message);
            if(matched.matches()){Map<String,String> values=new LinkedHashMap<>();for(int i=0;i<names.size();i++)values.put(names.get(i),matched.group(i+1));return render(template[0],values,language);}
        }
        if(message.contains("\n\n"))return Arrays.stream(message.split("\n\n",-1)).map(part->localizeRendered(part,language)).collect(java.util.stream.Collectors.joining("\n\n"));
        return message;
    }
    public String eventLanguage(UUID eventId){UUID owner=db.queryForObject("SELECT owner_id FROM events WHERE id=?",UUID.class,eventId);return resolve(owner,eventId,null,false);}

    public String smsTemplate(String requested,String configuredDefault){
        Settings s=settings();LinkedHashSet<String> chain=new LinkedHashSet<>();if(s.enabled()&&requested!=null)chain.add(requested);chain.add(s.defaultLanguage());chain.add(s.fallbackLanguage());
        for(String code:chain){var templates=db.query("SELECT t.template_id FROM language_delivery_templates t JOIN platform_languages l ON l.code=t.language_code WHERE t.provider='MSG91' AND t.language_code=? AND l.enabled",(rs,i)->rs.getString(1),code);if(!templates.isEmpty())return templates.get(0);}
        return configuredDefault;
    }
    public static Set<String> placeholders(String text) {
        Set<String> result=new TreeSet<>();Matcher matcher=Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9_]*)}}").matcher(text);
        String remainder=matcher.replaceAll(match->{result.add(match.group(1));return "";});
        if(remainder.contains("{{")||remainder.contains("}}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use named {{placeholder}} values without formatting expressions");return result;
    }
}
