package org.dcsa.skernel.service;

import org.dcsa.core.service.QueryService;
import org.dcsa.skernel.model.Facility;
import org.dcsa.skernel.model.enums.FacilityCodeListProvider;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FacilityService extends QueryService<Facility, UUID> {

  Mono<Facility> findByUNLocationCodeAndFacilityCode(
      String unLocationCode,
      FacilityCodeListProvider facilityCodeListProvider,
      String facilityCode);

  Mono<Facility> findByIdOrEmpty(UUID id);
  Mono<Facility> findById(UUID uuid);
}
