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

  // Taken from https://semver.org/
  static final Pattern SEM_VER_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

  static final String UNOFFICIAL = "THIS-CONTEXT-PATH-IS-AN-UNOFFICIAL-API";
  static final Pattern CONTEXT_PREFIX_PATTERN = Pattern.compile(
    // Match with up to 5 leading contexts (to avoid a run-away regex).
    "^((?:/+[^/\\s]+){0,5}/+v\\d+)(?:/.*)?$"
  );

  private String version;
  // We use a list here as Spring "eats" the "/" in property keys.
  private List<PrefixSpecificationRule> byPrefix;

  private Map<String, String> byPrefixCache = Collections.emptyMap();

  public void setVersion(String version) {
    this.version = normalizedVersion(version);
  }

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
      String normalizedVersion;
      try {
        normalizedVersion = normalizedVersion(rule.version);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid version for prefix " + rule.prefix);
      }
      String existing = byPrefixCache.put(rule.prefix, normalizedVersion);
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
    return version;
  }

  private static String normalizedVersion(String version) {
    // We have used N/A historically for unofficial APIs (e.g., UI-Support).
    // We might as well catch that and map it to UNOFFICIAL.
    if (version == null || "N/A".equals(version)) {
      return UNOFFICIAL;
    }
    if (!SEM_VER_PATTERN.matcher(version).matches()) {
      // DDT-1438
      throw new IllegalArgumentException("The version \"" + version
        + "\" does not match the semver specification.  Alternatively, use \"N/A\" if it is an unofficial API.");
    }
    return version;
  }

  @Data
  public static class PrefixSpecificationRule {
    String prefix;
    String version;
  }
}
