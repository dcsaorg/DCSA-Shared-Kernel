package org.dcsa.skernel.domain.persistence.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@MappedSuperclass
@EqualsAndHashCode
@Setter(value = AccessLevel.PROTECTED)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vessel")
public class BaseVessel {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  protected UUID id;

  @Column(name = "vessel_imo_number", length = 7, unique = true)
  protected String vesselIMONumber;

  @Column(name = "vessel_name", length = 35)
  protected String vesselName;

  @Column(name = "vessel_flag", length = 2, columnDefinition = "bpchar")
  protected String vesselFlag;

  @Column(name = "vessel_call_sign", length = 18)
  protected String vesselCallSign;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vessel_operator_carrier_id")
  protected Carrier vesselOperatorCarrier;

  @Column(name = "is_dummy")
  protected Boolean isDummyVessel;

  @Column(name = "length", columnDefinition = "numeric")
  protected Float vesselLength;

  @Column(name = "width", columnDefinition = "numeric")
  protected Float vesselWidth;

  @Column(name = "dimension_unit", length = 3)
  protected String dimensionUnit;
}
