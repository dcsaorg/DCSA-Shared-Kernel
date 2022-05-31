package org.dcsa.skernel.infrastructure.http.queryparams;

public record ParsedQueryParameter<T>(
  String queryParameterBasename,
  ComparisonType comparisonType,
  T value
  ) {
}
