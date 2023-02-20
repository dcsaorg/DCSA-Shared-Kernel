package org.dcsa.skernel.dataloader;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "${dcsa.dataloader.sources:classpath:db/dataloader-sources.yml}", factory = YamlPropertySourceFactory.class)
public class DataloaderConfig {
  private DataloaderDefaults defaults;
  private List<DataloaderSource> sources;

  public enum OnChecksumError { REFRESH, FAIL }
  public record DataloaderCsvDefaults(OnChecksumError onChecksumError) {}
  public record DataloaderDefaults(DataloaderCsvDefaults csv) { }
  public record DataloaderSource(
    String path,
    String type,
    Set<String> groups,
    OnChecksumError onChecksumError,
    Set<String> includes,
    Set<String> excludes,
    String historyTable,
    Set<String> dependsOn
  ) {
    @Builder
    public DataloaderSource {}

    public DataloaderSource withCsvDefaults(DataloaderCsvDefaults defaults) {
      return DataloaderSource.builder()
        .path(path)
        .type(type)
        .groups(groups)
        .onChecksumError(onChecksumError != null ? onChecksumError : defaults.onChecksumError())
        .includes(includes != null ? includes : Collections.emptySet())
        .excludes(excludes != null ? excludes : Collections.emptySet())
        .historyTable(historyTable)
        .dependsOn(dependsOn)
        .build();
    }
  }
}
