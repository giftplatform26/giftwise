package com.giftplatform.operations;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@Component @Profile({"production","prod"})
public class ProductionStartupGuard implements ApplicationRunner {
 private final Environment environment;
 public ProductionStartupGuard(Environment environment){this.environment=environment;}
 @Override public void run(ApplicationArguments arguments){var errors=ProductionConfigurationPolicy.violations(environment);if(!errors.isEmpty())throw new IllegalStateException("Production configuration rejected: "+String.join("; ",errors));}
}
