package com.giftplatform.localization;

import org.springframework.boot.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Component
public class LocalizationSourceBootstrap implements ApplicationRunner {
 private final JdbcTemplate db;private final ObjectMapper mapper;
 public LocalizationSourceBootstrap(JdbcTemplate db,ObjectMapper mapper){this.db=db;this.mapper=mapper;}
 @SuppressWarnings("unchecked") private <T> T read(String path) throws Exception {try(var input=new ClassPathResource(path).getInputStream()){return (T)mapper.readValue(input,Map.class);}}
 @Override @Transactional
 public void run(ApplicationArguments arguments) throws Exception {
  Map<String,String> ui=read("localization/en.json"),communications=read("localization/communications.json");
  insert(ui,"UI");insert(communications,"EMAIL");
  Map<String,String> all=new LinkedHashMap<>(ui);all.putAll(communications);
  Map<String,Map<String,String>> starter=read("localization/starter-translations.json");
  List<Object[]> translations=new ArrayList<>();
  for(var language:starter.entrySet())for(var source:all.entrySet()){
   String stripped=source.getValue().strip();String lookup=stripped;
   String value=language.getValue().get(lookup);
   if(value==null&&source.getKey().startsWith("ui.")){
    lookup=stripped.replaceFirst("[ \t]*[→←*:]+$", "").stripTrailing();String wanted=lookup;
    value=language.getValue().entrySet().stream().filter(entry->entry.getKey().equalsIgnoreCase(wanted)).map(Map.Entry::getValue).findFirst().orElse(null);
   }
   if(value==null)continue;
   int first=source.getValue().indexOf(lookup);String prefix=source.getValue().substring(0,first),suffix=source.getValue().substring(first+lookup.length());
   if(language.getKey().equals("ar")){prefix=mirror(prefix);suffix=mirror(suffix);}value=prefix+value+suffix;
   if(!LocalizationService.placeholders(source.getValue()).equals(LocalizationService.placeholders(value)))throw new IllegalStateException("Invalid starter translation placeholders: "+source.getKey());
   translations.add(new Object[]{language.getKey(),source.getKey(),value});
  }
  db.batchUpdate("INSERT INTO localization_translations(language_code,message_key,translated_text,published,version) VALUES(?,?,?,TRUE,1) ON CONFLICT(language_code,message_key) DO NOTHING",translations);
 }
 private String mirror(String text){StringBuilder result=new StringBuilder();for(char c:text.toCharArray())result.append(c=='→'?'←':c=='←'?'→':c);return result.toString();}
 private void insert(Map<String,String> messages,String category){
  db.batchUpdate("INSERT INTO localization_sources(message_key,source_text,category) VALUES(?,?,?) ON CONFLICT(message_key) DO NOTHING",new ArrayList<>(messages.entrySet()),500,(statement,entry)->{statement.setString(1,entry.getKey());statement.setString(2,entry.getValue());statement.setString(3,entry.getKey().startsWith("sms.")?"SMS":entry.getKey().startsWith("notification.")?"NOTIFICATION":category);});
 }
}
