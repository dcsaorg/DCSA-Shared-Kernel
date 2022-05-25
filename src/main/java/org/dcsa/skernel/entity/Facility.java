package org.dcsa.skernel.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@Entity
@Table(name = "facility")
public class Facility {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID facilityID;

  @Column(name = "facility_name", nullable = false, length = 100)
  private String facilityName;

  @Column(name = "un_location_code", length = 5)
  private String unLocationCode;

  @Column(name = "facility_bic_code", length = 4)
  private String facilityBICCode;

  @Size(max = 6)
  @Column(name = "facility_smdg_code", length = 6)
  private String facilitySMDGCode;

  @Column(name = "location_id")
  private String locationID;
}
