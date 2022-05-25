package org.dcsa.skernel.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "location")
public class Location {

  @Id
  @GeneratedValue
  private String id;

  @Column(name = "location_name", length = 100)
  private String locationName;

  @Column(name = "latitude", length = 10)
  private String latitude;

  @Column(name = "longitude", length = 11)
  private String longitude;

  @Column(name = "un_location_code", length = 5)
  private String unLocationCode;

  @Column(name = "address_id")
  private UUID addressID;

  @Column(name = "facility_id")
  private UUID facilityID;
}
