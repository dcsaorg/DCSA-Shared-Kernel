package org.dcsa.skernel.infrastructure.http.queryparams;

import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class QueryParameterParserTest {


  private static final String VALID_DATETIME_AS_STRING = "2021-04-01T14:12:56+01:00";
  private static final ZonedDateTime VALID_DATETIME = ZonedDateTime.parse(VALID_DATETIME_AS_STRING);
  private static final String QUERY_PARAM_ECDT = "eventCreatedDateTime";

  private final DCSAQueryParameterParser dcsaQueryParameterParser = new DCSAQueryParameterParser();

  @Test
  public void testQueryParameterParserSimple() {
    List<ParsedQueryParameter<ZonedDateTime>> results = dcsaQueryParameterParser.parseCustomQueryParameter(
      Map.of(QUERY_PARAM_ECDT, VALID_DATETIME_AS_STRING),
      QUERY_PARAM_ECDT, ZonedDateTime::parse);

    assertEquals(
      List.of(new ParsedQueryParameter<>(QUERY_PARAM_ECDT, ComparisonType.EQ, VALID_DATETIME)),
      results
    );

    results = dcsaQueryParameterParser.parseCustomQueryParameter(
      Map.of("RandomUnrelatedOption", VALID_DATETIME_AS_STRING),
      QUERY_PARAM_ECDT, ZonedDateTime::parse);
    assertEquals(Collections.emptyList(), results);
  }


  @Test
  public void testQueryParameterParserSimpleAtMostOnce() {
    Optional<ParsedQueryParameter<ZonedDateTime>> result = dcsaQueryParameterParser.parseCustomQueryParameterAtMostOnce(
      Map.of(QUERY_PARAM_ECDT, VALID_DATETIME_AS_STRING),
      QUERY_PARAM_ECDT, ZonedDateTime::parse);

    assertEquals(
      Optional.of(new ParsedQueryParameter<>(QUERY_PARAM_ECDT, ComparisonType.EQ, VALID_DATETIME)),
      result
    );

    result = dcsaQueryParameterParser.parseCustomQueryParameterAtMostOnce(
      Map.of("RandomUnrelatedOption", VALID_DATETIME_AS_STRING),
      QUERY_PARAM_ECDT, ZonedDateTime::parse);
    assertEquals(Optional.empty(), result);
  }


  @Test
  public void testQueryParameterParserWithAttributes() {
    for (ComparisonType comparisonType : ComparisonType.values()) {
      Optional<ParsedQueryParameter<ZonedDateTime>> result = dcsaQueryParameterParser.parseCustomQueryParameterAtMostOnce(
        Map.of(QUERY_PARAM_ECDT + ":" + comparisonType.name().toLowerCase(), VALID_DATETIME_AS_STRING),
        QUERY_PARAM_ECDT, ZonedDateTime::parse);

      assertEquals(
        Optional.of(new ParsedQueryParameter<>(QUERY_PARAM_ECDT, comparisonType, VALID_DATETIME)),
        result
      );

    }

    Optional<ParsedQueryParameter<ZonedDateTime>> result = dcsaQueryParameterParser.parseCustomQueryParameterAtMostOnce(
      Map.of("RandomUnrelatedOption:withAttribute", VALID_DATETIME_AS_STRING),
      QUERY_PARAM_ECDT, ZonedDateTime::parse);
    assertEquals(Optional.empty(), result);
  }

  @Test
  public void testQueryParameterParserWithMany() {
    List<ParsedQueryParameter<ZonedDateTime>> results = dcsaQueryParameterParser.parseCustomQueryParameter(
      new LinkedHashMap<>(){{
        put(QUERY_PARAM_ECDT + ":lt", VALID_DATETIME_AS_STRING);
        put(QUERY_PARAM_ECDT + ":gt", VALID_DATETIME_AS_STRING);
      }},
      QUERY_PARAM_ECDT,
      ZonedDateTime::parse
    );

    assertEquals(
      List.of(
        new ParsedQueryParameter<>(QUERY_PARAM_ECDT, ComparisonType.LT, VALID_DATETIME),
        new ParsedQueryParameter<>(QUERY_PARAM_ECDT, ComparisonType.GT, VALID_DATETIME)
      ),
      results
    );

    ConcreteRequestErrorMessageException exception = assertThrows(
      ConcreteRequestErrorMessageException.class,
      () -> dcsaQueryParameterParser.parseCustomQueryParameterAtMostOnce(
      new LinkedHashMap<>(){{
        put(QUERY_PARAM_ECDT + ":lt", VALID_DATETIME_AS_STRING);
        put(QUERY_PARAM_ECDT + ":gt", VALID_DATETIME_AS_STRING);
      }},
      QUERY_PARAM_ECDT,
      ZonedDateTime::parse
    ));
    assertTrue(exception.getMessage().contains("can only be used once"));
  }

  @Test
  public void testAssertThrows() {
    ConcreteRequestErrorMessageException exception = assertThrows(
      ConcreteRequestErrorMessageException.class,
      () -> dcsaQueryParameterParser.parseCustomQueryParameter(
        Map.of(QUERY_PARAM_ECDT + ":UNKNOWN", VALID_DATETIME_AS_STRING),
        QUERY_PARAM_ECDT, ZonedDateTime::parse)
    );
    assertTrue(exception.getMessage().contains("Unknown attribute / operator"));

    exception = assertThrows(
      ConcreteRequestErrorMessageException.class,
      () -> dcsaQueryParameterParser.parseCustomQueryParameter(
        Map.of(QUERY_PARAM_ECDT, VALID_DATETIME_AS_STRING),
        QUERY_PARAM_ECDT, s -> {throw new IllegalArgumentException("foo");})
    );
    assertTrue(exception.getMessage().contains("Invalid value"));
  }

}
