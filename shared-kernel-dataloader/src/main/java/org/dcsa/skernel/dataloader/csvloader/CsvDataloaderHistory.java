package org.dcsa.skernel.dataloader.csvloader;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.dataloader.DataloaderConfig.DataloaderSource;
import org.dcsa.skernel.dataloader.csvloader.CsvDataloaderConfig.CsvDataloaderEntry;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Scanner;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
@Component
public class CsvDataloaderHistory {
  private final JdbcTemplate jdbcTemplate;
  private boolean initialized = false;

  private final static String[] CREATE_HISTORY = {"""
    create table csvloader_history (
      table_name varchar(100) not null,
      source text not null,
      checksum varchar(64) not null,
      rows_affected integer not null,
      created timestamp with time zone not null
    )""",
    "create index table_name_idx on csvloader_history (table_name)",
    "create index created_idx on csvloader_history (created)"
  };
  private final static String SELECT_HISTORY = """
    select * from csvloader_history where table_name = ? order by created desc limit 1
    """;
  private final static String INSERT_HISTORY = """
    insert into csvloader_history (table_name, source, checksum, rows_affected, created) values (?,?,?,?,?)
    """;

  private record HistoryEntry(
    String table,
    String source,
    String checksum,
    int rowsAffected,
    Timestamp created
  ) {
    @Builder(toBuilder = true)
    public HistoryEntry {}

    public void insert(JdbcTemplate jdbcTemplate) {
      jdbcTemplate.update(INSERT_HISTORY, table, source, checksum, rowsAffected, created);
    }
  }

  private final static ResultSetExtractor<HistoryEntry> historyEntryExtractor = (ResultSet rs) -> rs.next() ? HistoryEntry.builder()
      .table(rs.getString("table_name"))
      .source(rs.getString("source"))
      .checksum(rs.getString("checksum"))
      .created(rs.getTimestamp("created"))
      .build() : null;

  public boolean performForEntry(DataloaderSource source, CsvDataloaderEntry entry, BiFunction<StringReader, Boolean, Integer> consumer) {
    HistoryEntry historyEntry = loadEntry(entry.table());
    String url = source.path() + "/" + entry.file();
    String fileContents = readUrl(url);
    String checksum = checksum(fileContents);

    int rowsAffected;
    if (historyEntry != null) {
      if (checksum.equals(historyEntry.checksum)) {
        log.trace("Checksum match for {}", url);
        return false;
      }
      rowsAffected = switch (source.onChecksumError()) {
        case FAIL -> throw new CsvDataloaderException("Checksum validation failed for " + url, null);
        case REFRESH -> consumer.apply(new StringReader(fileContents), true);
      };
    } else {
      rowsAffected = consumer.apply(new StringReader(fileContents), false);
    }
    HistoryEntry.builder()
      .table(entry.table())
      .source(url)
      .checksum(checksum)
      .rowsAffected(rowsAffected)
      .created(Timestamp.from(OffsetDateTime.now().toInstant()))
      .build()
      .insert(jdbcTemplate);
    return true;
  }

  @Synchronized
  private HistoryEntry loadEntry(String table) {
    if (!initialized) {
      try {
        return jdbcTemplate.query(SELECT_HISTORY, historyEntryExtractor, table);
      } catch (BadSqlGrammarException e) {
        log.info("Creating history table csvloader_history");
        jdbcTemplate.batchUpdate(CREATE_HISTORY);
        initialized = true;
        return null;
      }
    } else {
      return jdbcTemplate.query(SELECT_HISTORY, historyEntryExtractor, table);
    }
  }

  @SneakyThrows
  private String checksum(String str) {
    byte[] sha = MessageDigest.getInstance("SHA3-256").digest(str.getBytes(StandardCharsets.UTF_8));
    return new String(Hex.encode(sha));
  }

  @SneakyThrows
  private String readUrl(String url) {
    try (Scanner scanner = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8)) {
      scanner.useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }
  }
}
