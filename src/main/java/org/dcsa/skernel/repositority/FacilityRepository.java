package org.dcsa.skernel.repositority;

import org.dcsa.core.repository.ExtendedRepository;
import org.dcsa.skernel.model.Facility;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FacilityRepository extends ExtendedRepository<Facility, UUID> {

  Mono<Facility> findByUnLocationCodeAndFacilitySMDGCode(
      String uNLocationCode, String facilitySMDGCode);

  Mono<Facility> findByUnLocationCodeAndFacilityBICCode(
      String uNLocationCode, String facilityBICCode);

  default Mono<Facility> findByIdOrEmpty(UUID id) {
    return Mono.justOrEmpty(id)
      .flatMap(this::findById);
  }
}
