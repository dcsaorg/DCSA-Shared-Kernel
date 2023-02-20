package org.dcsa.skernel.dataloader.csvloader;


import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser.Feature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.dataloader.DataloaderConfig.DataloaderSource;
import org.dcsa.skernel.dataloader.TypedDataloader;
import org.dcsa.skernel.dataloader.csvloader.CsvDataloaderConfig.CsvDataloaderEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class CsvDataloader implements TypedDataloader {
  private final JdbcTemplate jdbcTemplate;
  private final TransactionTemplate transactionTemplate;
  private final CsvMapper csvMapper = new CsvMapper();
  private final CsvDataloaderHistory history;

  private record Column(String name, Function<String,Object> parser) {}

  @Override
  public void loadData(DataloaderSource source) {
    CsvDataloaderConfig config = loadConfig(source.path());
    var count = new Object(){ int populated = 0; int total = 0; };
    config.tables().forEach(entry -> {
      Set<String> excludes = source.excludes();
      Set<String> includes = source.includes();
      count.total++;
      if (excludes != null && (excludes.contains(entry.table()) || excludes.contains(entry.file()))) {
        log.trace("Skipping {} ({}/{}) - excluded", entry.table(), source.path(), entry.file());
      } else if (includes != null && !includes.isEmpty() && !includes.contains(entry.table()) && !includes.contains(entry.file())) {
        log.trace("Skipping {} ({}/{}) - not included", entry.table(), source.path(), entry.file());
      } else {
        if (history.performForEntry(source, entry, (StringReader reader, Boolean doAsUpdate) ->
          readEntry(source, entry.withDefaults(config.defaults()), reader, doAsUpdate))) {
          count.populated++;
        }
      }
    });
    log.info("{}/{} tables populated from {} source {}", count.populated, count.total, source.type(), source.path());
  }

  @SneakyThrows
  private int readEntry(DataloaderSource source, CsvDataloaderEntry entry, StringReader reader, Boolean refresh) {
    String path = source.path();
    log.trace("Populating {} from {}/{}", entry.table(), path, entry.file());

    try (MappingIterator<List<String>> contents = getReader(entry).readValues(reader)) {
      if (!contents.hasNext()) {
        log.warn("{}/{} was empty", path, entry.file());
        return 0;
      }

      var lineNo = new Object(){ int value = 0; };
      List<Column> columns;
      if (entry.csvheader()) {
        List<String> firstLine = contents.next();
        columns = parseColumns(entry.columns() != null ? entry.columns() : firstLine);
        lineNo.value++;
      } else {
        columns = entry.columns() != null ? parseColumns(entry.columns()) : null;
      }
      if (columns == null || columns.isEmpty()) {
        throw new IllegalArgumentException("Unable to determine column names for " + entry.table());
      }

      String sql = "insert into " + entry.table() +
        " (" + columns.stream().map(Column::name).collect(Collectors.joining(", ")) +
        ") values (" +
        IntStream.range(0, columns.size()).mapToObj(i -> "?").collect(Collectors.joining(", ")) +
        ")";
      if (refresh) {
        if (columns.size() <= 1) {
          sql = sql + " on conflict do nothing";
        } else {
          String primaryKey = entry.primaryKey() != null ? entry.primaryKey() : columns.get(0).name();
          // PostgreSQL specific way to do "UPSERT"
          sql = sql + " on conflict (" + primaryKey + ") do update set "
            + columns.stream()
            .filter(column -> !primaryKey.equals(column.name()))
            .map(Column::name)
            .map(n -> n + "=EXCLUDED." + n)
            .collect(Collectors.joining(", "));
        }
      }
      log.trace("sql for {} = {}", entry.table(), sql);

      final String finalSql = sql;
      var rows = new Object(){ int value = 0; };
      transactionTemplate.executeWithoutResult(transaction -> {
        while (contents.hasNext()) {
          try {
            List<String> line = contents.next();
            jdbcTemplate.update(finalSql, toArgs(entry, lineNo.value++, columns, line));
            rows.value++;
          } catch (Exception e) {
            throw new CsvDataloaderException("Error in " + entry.file() + ":" + lineNo.value + " - " + e.getMessage(), e);
          }
        }
      });
      return rows.value;
    }
  }

  private ObjectReader getReader(CsvDataloaderEntry entry) {
    ObjectReader reader = csvMapper.readerForListOf(String.class).with(Feature.WRAP_AS_ARRAY);
    return entry.emptyIsNull() ? reader.with(Feature.EMPTY_STRING_AS_NULL) : reader;
  }

  private List<Column> parseColumns(List<String> names) {
    return names.stream().map(this::parseColumn).toList();
  }

  private Column parseColumn(String name) {
    int index = name.indexOf(':');
    if (index == -1) {
      return new Column(cleanColumnName(name), s -> s);
    } else {
      String type = name.substring(index + 1);
      Function<String, Object> parser = switch (type) {
        case "int", "integer" -> Integer::parseInt;
        case "long" -> Long::parseLong;
        case "bool", "boolean" -> Boolean::parseBoolean;
        case "real", "float" -> Float::parseFloat;
        case "double" -> Double::parseDouble;
        case "str", "string" -> s -> s;
        default -> throw new IllegalArgumentException("Unknown column type: " + type + " (" + name + ")");
      };
      return new Column(cleanColumnName(name.substring(0, index)), parser);
    }
  }

  private String cleanColumnName(String s) {
    return s
      .trim()
      .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
      .replaceAll("[ -]", "_")
      .toLowerCase(Locale.ROOT);
  }

  private Object[] toArgs(CsvDataloaderEntry entry, int lineNo, List<Column> columns, List<String> data) {
    if (columns.size() != data.size()) {
      throw new IllegalArgumentException(
        "Error in " + entry.file() + ":" + lineNo +" - expected " + columns.size() + " columns but was " + data.size());
    }
    return IntStream.range(0, columns.size()).mapToObj(i -> {
      String arg = data.get(i);
      if (arg == null || (entry.otherNullValues() != null && entry.otherNullValues().contains(arg))) {
        return null;
      } else {
        return columns.get(i).parser.apply(arg);
      }
    }).toArray();
  }

  @SneakyThrows
  private CsvDataloaderConfig loadConfig(String path) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    URL configFile = new URL(path + "/csvloader.yml");
    return mapper.readValue(configFile, CsvDataloaderConfig.class);
  }
}
