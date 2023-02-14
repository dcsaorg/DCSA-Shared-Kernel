package org.dcsa.skernel.dataloader.flywayloader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.dataloader.DataloaderConfig.DataloaderSource;
import org.dcsa.skernel.dataloader.TypedDataloader;
import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlywayDataloader implements TypedDataloader {
  private final DataSource dataSource;

  @Override
  public void loadData(DataloaderSource source) {
    if (!source.path().startsWith("classpath:")) {
      throw new FlywayDataloaderException("FlywayDataloader only supports classpath paths: " + source.path());
    }
    Flyway.configure()
      .dataSource(dataSource)
      .table(source.historyTable())
      .baselineOnMigrate(true)
      .baselineVersion("0")
      .locations(source.path())
      .load()
      .migrate();
  }
}
