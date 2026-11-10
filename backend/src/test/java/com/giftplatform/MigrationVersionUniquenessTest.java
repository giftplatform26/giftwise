package com.giftplatform;

import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.assertThat;

/** Catch SQL/Java version collisions before Docker-dependent migration tests run. */
class MigrationVersionUniquenessTest {
 @Test void sqlAndJavaMigrationsHaveUniqueVersions()throws Exception{
  var pattern=Pattern.compile("^V([0-9]+(?:[._][0-9]+)*)__.*\\.(?:sql|java)$");
  Map<MigrationVersion,Path> seen=new HashMap<>();List<String> collisions=new ArrayList<>();
  for(Path root:List.of(Path.of("src/main/resources/db/migration"),Path.of("src/main/java/db/migration"))){
   if(!Files.exists(root))continue;
   try(var paths=Files.walk(root)){
    for(Path path:paths.filter(Files::isRegularFile).sorted().toList()){
     var match=pattern.matcher(path.getFileName().toString());if(!match.matches())continue;
     var version=MigrationVersion.fromVersion(match.group(1));Path previous=seen.putIfAbsent(version,path);
     if(previous!=null)collisions.add(version+": "+previous+" and "+path);
    }
   }
  }
  assertThat(collisions).as("SQL and Java migrations share one Flyway version namespace").isEmpty();
 }
}
