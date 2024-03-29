package org.dcsa.skernel.domain.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Entity
@Table(name = "un_location")
public class UnLocation {
  @Id
  @Column(name = "un_location_code", length = 5, columnDefinition = "bpchar")
  private String unLocationCode;

  @Column(name = "un_location_name", length = 100)
  private String unLocationName;

  @Column(name = "location_code", length = 3, columnDefinition = "bpchar")
  private String locationCode;

  @Column(name = "country_code", length = 2, columnDefinition = "bpchar")
  private String countryCode;
}
