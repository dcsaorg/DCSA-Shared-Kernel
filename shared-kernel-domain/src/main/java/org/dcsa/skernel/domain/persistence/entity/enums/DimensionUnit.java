package org.dcsa.skernel.domain.persistence.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DimensionUnit {
  MTR("Meter"),
  FOT("Foot");

  private final String name;
}
