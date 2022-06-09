package org.dcsa.skernel.infrastructure.http.queryparams;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class DCSAQueryParameterParser {

  /**
   * Parse DCSA's "foo:gt" query parameter accepting repeated versions.
   *
   * Note if you only support at most once instance of the parameter, then please use
   * {@link #parseCustomQueryParameterAtMostOnce(Map, String, Function)} instead.
   *
   * Example usage:
   * <pre>{@code
   *     @Autowired
   *     DCSAQueryParameterParser queryParameterParser;
   *
   *     @GetMapping
   *     public List<Event> getEvents(@RequestParam Map<String, String> queryParams) {
   *         List<ParsedQueryParameter<ZonedDateTime>> parsedQueryParams = queryParameterParser.parseCustomQueryParameter(
   *           queryParams,
   *           "eventCreatedDateTime",
   *           ZonedDateTime::parse
   *         );
   *         if (!parsedQueryParams.isEmpty()) {
   *             // Use parsedQueryParams as a part of the filter.
   *             ...
   *         } else {
   *             ...
   *         }
   *         return ...;
   *     }
   * }</pre>
   *
   * @param allQueryParameters All query parameters (typically obtained from {@code @RequestParam Map<String, String> queryParams})
   * @param basename The basename of the query parameter.  E.g., {@code "eventCreatedDateTime"}
   *                 If you need to parse multiple parameters ("eventCreatedDateTime" AND "eventDateTime"),
   *                 call this method twice with distinct base names.
   * @param valueParser A {@link Function} that translates the String into the desired value type or throws an exception on error. E.g., {@link java.time.ZonedDateTime#parse(CharSequence)}
   * @return A (possibly empty) list of the query parameter parsed along with its values and operators.
   * @param <T> The desired value type.
   * @throws ConcreteRequestErrorMessageException If the parameters are present but not valid, the error is communicated
   *  via ConcreteRequestErrorMessageException exceptions (typically, the
   *  {@link org.dcsa.skernel.errors.infrastructure.GlobalExceptionHandler} will provide a decent result for these).
   */
  public <T> List<ParsedQueryParameter<T>> parseCustomQueryParameter(Map<String, String> allQueryParameters, String basename, Function<String, T> valueParser) {
    return allQueryParameters.entrySet().stream()
      .filter(e -> e.getKey().equals(basename) || e.getKey().startsWith(basename + ":"))
      .map(entry -> {
        String fullName = entry.getKey();
        String[] parts = fullName.split(":", 2);
        ComparisonType comparisonType;
        T value;
        if (parts.length < 2) {
          assert parts[0].equals(basename);
          // default if not defined.
          comparisonType = ComparisonType.EQ;
        } else {
          comparisonType = parseComparisonType(fullName, parts[1]);
        }
        try {
          value = valueParser.apply(entry.getValue());
        } catch (RuntimeException e) {
          throw ConcreteRequestErrorMessageException.invalidQuery(fullName, "Invalid value for " + fullName, e);
        }

        return new ParsedQueryParameter<>(basename, comparisonType, value);
      }).toList();
  }

  /**
   * Parse DCSA's "foo:gt" query parameter accepting at most one version.
   *
   * Example usage:
   * <pre>{@code
   *     @Autowired
   *     DCSAQueryParameterParser queryParameterParser;
   *
   *     @GetMapping
   *     public List<Event> getEvents(@RequestParam Map<String, String> queryParams) {
   *         Optional<ParsedQueryParameter<ZonedDateTime>> parsedQueryParam = queryParameterParser.parseCustomQueryParameterAtMostOnce(
   *           queryParams,
   *           "eventCreatedDateTime",
   *           ZonedDateTime::parse
   *         );
   *         if (parsedQueryParam.isPresent()) {
   *             // Use parsedQueryParams as a part of the filter.
   *             ...
   *         } else {
   *             ...
   *         }
   *         return ...;
   *     }
   * }</pre>
   *
   * @param allQueryParameters All query parameters (typically obtained from {@code @RequestParam Map<String, String> queryParams})
   * @param basename The basename of the query parameter.  E.g., {@code "eventCreatedDateTime"}
   *                 If you need to parse multiple parameters ("eventCreatedDateTime" AND "eventDateTime"),
   *                 call this method twice with distinct base names.
   * @param valueParser A {@link Function} that translates the String into the desired value type or throws an exception on error. E.g., {@link java.time.ZonedDateTime#parse(CharSequence)}
   * @return An instance of the parsed query parameter along with its value and comparison type or {@link Optional#empty()}.
   * @param <T> The desired value type.
   * @throws ConcreteRequestErrorMessageException If the parameters are present but not valid, the error is communicated
   *  via ConcreteRequestErrorMessageException exceptions (typically, the
   *  {@link org.dcsa.skernel.errors.infrastructure.GlobalExceptionHandler} will provide a decent result for these).
   */
  public <T> Optional<ParsedQueryParameter<T>> parseCustomQueryParameterAtMostOnce(Map<String, String> allQueryParameters, String basename, Function<String, T> valueParser) {
    List<ParsedQueryParameter<T>> matches = parseCustomQueryParameter(allQueryParameters, basename, valueParser);
    if (matches.size() > 1) {
      throw ConcreteRequestErrorMessageException.invalidQuery(basename, "The query parameter " + basename
        + " can only be used once, but request provided it multiple times (with different attributes/operators).");
    }
    return matches.parallelStream().findFirst();
  }

  private ComparisonType parseComparisonType(String fullname, String comparisonTypeStr) {
    try {
      return ComparisonType.valueOf(comparisonTypeStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw ConcreteRequestErrorMessageException.invalidQuery(fullname, "Unknown attribute / operator: " + comparisonTypeStr);
    }
  }

}
