package org.dcsa.skernel.infrastructure.http.headers.api.version;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.regex.Pattern;

@Configuration
@ConfigurationProperties("dcsa.specification")
@Setter
@Getter
public class DcsaSpecificationConfiguration {

  static final String UNOFFICIAL = "THIS-CONTEXT-PATH-IS-AN-UNOFFICIAL-API";
  static final Pattern CONTEXT_PREFIX_PATTERN = Pattern.compile(
    // Match with up to 5 leading contexts (to avoid a run-away regex).
    "^((?:/+[^/\\s]+){0,5}/+v\\d+)(?:/.*)?$"
  );

  private String version;
  // We use a list here as Spring "eats" the "/" in property keys.
  private List<PrefixSpecificationRule> byPrefix;

  private Map<String, String> byPrefixCache = Collections.emptyMap();

  public void setByPrefix(List<PrefixSpecificationRule> byPrefix) {
    this.byPrefix = byPrefix;
    this.rebuildCache();
  }

  private void rebuildCache() {
    if (byPrefix == null || byPrefix.isEmpty()) {
      byPrefixCache = Collections.emptyMap();
      return;
    }
    if (version != null && !version.equals("N/A")) {
      throw new IllegalArgumentException("Invalid configuration in dcsa.specification: Cannot use both version"
        + " and by-prefix at the same time");
    }
    byPrefixCache = new HashMap<>(byPrefix.size());
    for (PrefixSpecificationRule rule : byPrefix) {
      if (!CONTEXT_PREFIX_PATTERN.matcher(rule.prefix).matches()) {
        throw new IllegalArgumentException("Invalid configuration in dcsa.specification.by-prefix: All prefixes"
          + " must follow the pattern /vX or /foo/vX (only major versions allowed!).  Offending prefix was \""
          + rule.prefix + "\"");
      }
      String existing = byPrefixCache.put(rule.prefix, normalizedVersion(rule.version));
      if (existing != null) {
        throw new IllegalArgumentException("Invalid configuration in dcsa.specification.by-prefix: Prefix \""
          + rule.prefix + "\" was used twice!");
      }
    }
  }

  public boolean isRejectUnversionedContexts() {
    return !this.byPrefixCache.isEmpty();
  }

  public String getSpecificationVersion(String dcsaPathVersionPrefix) {
    Objects.requireNonNull(dcsaPathVersionPrefix, "dcsaPathVersionPrefix must be not null");
    if (!byPrefixCache.isEmpty()) {
     return byPrefixCache.get(dcsaPathVersionPrefix);
    }
    return normalizedVersion(version);
  }

  private static String normalizedVersion(String version) {
    // We have used N/A historically for unofficial APIs (e.g., UI-Support).
    // We might as well catch that and map it to UNOFFICIAL.
    if (version == null || "N/A".equals(version)) {
      return UNOFFICIAL;
    }
    return version;
  }

  @Data
  public static class PrefixSpecificationRule {
    String prefix;
    String version;
  }
}
