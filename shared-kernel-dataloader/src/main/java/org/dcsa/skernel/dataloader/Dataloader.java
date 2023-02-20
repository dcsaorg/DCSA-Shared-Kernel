package org.dcsa.skernel.dataloader;

import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.dataloader.csvloader.CsvDataloader;
import org.dcsa.skernel.dataloader.flywayloader.FlywayDataloader;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class Dataloader {
  private final DataloaderAppConfig appConfig;
  private final DataloaderConfig config;
  private final Map<String, TypedDataloader> loaders;

  public Dataloader(DataloaderAppConfig appConfig, DataloaderConfig config, CsvDataloader csvDataloader, FlywayDataloader flywayDataloader) {
    this.appConfig = appConfig;
    this.config = config;
    loaders = Map.of(
      "csv", csvDataloader,
      "flyway", flywayDataloader
    );
    log.trace("appConfig={}", appConfig);
    log.trace("config={}", config);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void autoload() {
    loadData(appConfig.getAutoload());
    log.info("Autoload complete");
  }

  public void loadData(Set<String> requestedGroups) {
    Set<String> groups = new HashSet<>(requestedGroups);
    var groupsAdded = new Object(){ boolean value; };
    do {
      groupsAdded.value = false;
      config.getSources().forEach(source -> {
        if (!Collections.disjoint(groups, source.groups()) && source.dependsOn() != null && !source.dependsOn().isEmpty()) {
          if (groups.addAll(source.dependsOn())) {
            groupsAdded.value = true;
          }
        }
      });
    } while (groupsAdded.value);

    log.info("Loading data for groups {} -> {}", requestedGroups, groups);
    config.getSources().forEach(source -> {
      if (!Collections.disjoint(groups, source.groups())) {
        TypedDataloader dataloader = loaders.get(source.type());
        if (dataloader == null) {
          throw new IllegalArgumentException("No dataloaders for '" + source.type() + "': " + source);
        }
        log.debug("Loading {} source {} {}", source.type(), source.path(), source.groups());
        dataloader.loadData(source.withCsvDefaults(config.getDefaults().csv()));
      } else {
        log.debug("Skipping {} source {} {}", source.type(), source.path(), source.groups());
      }
    });
  }
}
