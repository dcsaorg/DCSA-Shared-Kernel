package org.dcsa.skernel.infrastructure.validation;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;

public class PseudoEnumValidator implements ConstraintValidator<PseudoEnum, String> {

  private static final Map<String, Set<String>> CACHE = new ConcurrentHashMap<>();

  private Set<String> allowedValues;

  @Override
  public void initialize(PseudoEnum constraintAnnotation) {
    var name = constraintAnnotation.value();
    var columnName = constraintAnnotation.column();
    allowedValues = loadData(name, columnName);
  }


  @SneakyThrows(IOException.class)
  private Set<String> loadData(String datasetName, String columnName) {
    var cacheKey = datasetName + "!" + columnName;
    Set<String> values = CACHE.get(cacheKey);
    if (values != null) {
      return values;
    }

    InputStream in = this.getClass().getClassLoader().getResourceAsStream("validations/" + datasetName);
    if (in == null) {
      throw new IllegalArgumentException("No data set called validations/" + datasetName);
    }
    var mapper = new CsvMapper();
    var objectReader = mapper.readerForListOf(String.class)
      .with(CsvParser.Feature.WRAP_AS_ARRAY)
      .with(CsvParser.Feature.EMPTY_STRING_AS_NULL);
    // Ordering is important to ensure stable error messages because we are not sorting during
    // the generation of the error message.
    values = new LinkedHashSet<>();
    try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      var contents = objectReader.<List<String>>readValues(r)) {
      ensureHasNext(contents, datasetName);
      var headers = contents.next();
      var columnIndex = 0;
      if (!columnName.isBlank()) {
        columnIndex = headers.indexOf(columnName);
        if (columnIndex < 0) {
          throw new IllegalArgumentException("Cannot find the column with header \"" + columnName
            + "\" in the dataset validations/" + datasetName);
        }
      }
      ensureHasNext(contents, datasetName);
      do {
        var row = contents.next();
        if (columnIndex >= row.size()) {
          throw new IllegalArgumentException("The dataset in validations/" + datasetName + " had rows that did not have at least " + (columnIndex + 1) + " column(s).");
        }
        var value = row.get(columnIndex);
        values.add(value);
      } while (contents.hasNext());
    }

    CACHE.put(cacheKey, Collections.unmodifiableSet(values));
    return values;
  }

  private void ensureHasNext(Iterator<?> it, String dataset) {
    if (!it.hasNext()) {
      throw new IllegalArgumentException("The dataset validations/" + dataset + " must have at least two lines");
    }
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
    if (null == value || this.allowedValues.contains(value)) {
      return true;
    }
    constraintValidatorContext.disableDefaultConstraintViolation();
    String values = String.join(", ", this.allowedValues);
    constraintValidatorContext.buildConstraintViolationWithTemplate(
      "The value should have been one of: " + values
    ).addConstraintViolation();

    return false;
  }
}
