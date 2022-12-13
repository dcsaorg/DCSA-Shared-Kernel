package org.dcsa.skernel.domain.persistence.entity.base;

import lombok.*;
import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.domain.persistence.entity.enums.FacilityTypeCode;
import org.dcsa.skernel.domain.persistence.entity.enums.PortCallStatusCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import java.util.UUID;

@MappedSuperclass
@Table(name = "transport_call")
@EqualsAndHashCode
@Builder
@Setter(value = AccessLevel.PROTECTED)
@Getter
public class BaseTransportCall {
  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "transport_call_reference", length = 100, nullable = false)
  private String transportCallReference;

  @Column(name = "transport_call_sequence_number")
  private Integer transportCallSequenceNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "facility_type_code", length = 4, columnDefinition = "bpchar") // "bpchar" here is not a typing error
  private FacilityTypeCode facilityTypeCode;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "location_id")
  private Location location;

  @Column(name = "mode_of_transport_code", length = 3)
  private String modeOfTransportCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "port_call_status_type_code", length = 4, columnDefinition = "bpchar") // "bpchar" here is not a typing error
  private PortCallStatusCode portCallStatusCode;

  @Column(name="port_visit_reference", length = 50)
  private String portVisitReference;
}
