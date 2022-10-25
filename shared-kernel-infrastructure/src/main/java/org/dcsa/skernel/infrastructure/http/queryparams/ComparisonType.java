package org.dcsa.skernel.infrastructure.http.queryparams;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ComparisonType {
  GT,
  GTE,
  EQ,
  LTE,
  LT,
  ;

  public final static Set<String> valueSet = Arrays.stream(values())
    .map(Enum::name)
    .collect(Collectors.toSet());
}
