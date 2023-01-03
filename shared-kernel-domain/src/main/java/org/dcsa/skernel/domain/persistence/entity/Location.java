package org.dcsa.skernel.domain.persistence.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Entity
@Table(name = "location")
public class Location {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "location_name", length = 100)
  private String locationName;

  @Column(name = "latitude", length = 10)
  private String latitude;

  @Column(name = "longitude", length = 11)
  private String longitude;

  @Column(name = "un_location_code", length = 5, columnDefinition = "bpchar")
  private String UNLocationCode; /* REFERENCES dcsa_im_v3_0.un_location (un_location_code) */

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_id")
  private Address address;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "facility_id")
  private Facility facility;
}
