package org.dcsa.skernel.dataloader.csvloader;

import lombok.Builder;

import java.util.List;

public record CsvDataloaderConfig(
  CsvDataloaderDefaults defaults,
  List<CsvDataloaderEntry> tables
) {
  public record CsvDataloaderDefaults(
    boolean csvheader,
    boolean emptyIsNull,
    List<String> otherNullValues
  ) { }

  public record CsvDataloaderEntry(
    String table,
    String file,
    Boolean csvheader,
    Boolean emptyIsNull,
    List<String> otherNullValues,
    List<String> columns,
    String primaryKey
  ) {
    @Builder
    public CsvDataloaderEntry {}

    public CsvDataloaderEntry withDefaults(CsvDataloaderDefaults defaults) {
      return CsvDataloaderEntry.builder()
        .table(table)
        .file(file)
        .csvheader(csvheader != null ? csvheader : defaults.csvheader())
        .emptyIsNull(emptyIsNull != null ? emptyIsNull : defaults.emptyIsNull())
        .otherNullValues(otherNullValues != null ? otherNullValues : defaults.otherNullValues())
        .columns(columns)
        .primaryKey(primaryKey)
        .build();
    }
  }
}
