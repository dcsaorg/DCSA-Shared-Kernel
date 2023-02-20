package org.dcsa.skernel.dataloader;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "dcsa.dataloader")
public class DataloaderAppConfig {
  private Set<String> autoload;
}
