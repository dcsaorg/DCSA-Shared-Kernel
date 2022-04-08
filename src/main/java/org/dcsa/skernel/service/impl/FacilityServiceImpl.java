package org.dcsa.skernel.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.core.service.impl.QueryServiceImpl;
import org.dcsa.skernel.model.Facility;
import org.dcsa.skernel.model.enums.FacilityCodeListProvider;
import org.dcsa.skernel.repositority.FacilityRepository;
import org.dcsa.skernel.service.FacilityService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FacilityServiceImpl extends QueryServiceImpl<FacilityRepository, Facility, UUID>
    implements FacilityService {

  private final FacilityRepository facilityRepository;

  @Override
  public FacilityRepository getRepository() {
    return facilityRepository;
  }

  @Override
  public Mono<Facility> findByUNLocationCodeAndFacilityCode(
      String unLocationCode,
      FacilityCodeListProvider facilityCodeListProvider,
      String facilityCode) {

    if (unLocationCode == null)
      return Mono.error(
          ConcreteRequestErrorMessageException.invalidParameter(
              "The attribute unLocationCode cannot be null"));
    if (facilityCode == null)
      return Mono.error(
          ConcreteRequestErrorMessageException.invalidParameter(
              "The attribute facilityCode cannot be null"));

    return Mono.just(facilityCodeListProvider)
        .flatMap(
            facilityCodeListProvider1 -> {
              if (facilityCodeListProvider1 == FacilityCodeListProvider.SMDG) {
                return facilityRepository.findByUnLocationCodeAndFacilitySMDGCode(
                    unLocationCode, facilityCode);
              } else if (facilityCodeListProvider1 == FacilityCodeListProvider.BIC) {
                return facilityRepository.findByUnLocationCodeAndFacilityBICCode(
                    unLocationCode, facilityCode);
              } else {
                return Mono.error(
                    ConcreteRequestErrorMessageException.invalidParameter(
                        "Unsupported facility code list provider: " + facilityCodeListProvider1));
              }
            })
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.invalidParameter(
                    "Cannot find any facility with UNLocationCode + Facility code: "
                        + unLocationCode
                        + ", "
                        + facilityCode)));
  }

  @Override
  public Mono<Facility> findByIdOrEmpty(UUID id) {
    return facilityRepository.findByIdOrEmpty(id);
  }

  @Override
  public Mono<Facility> findById(UUID uuid) {
    return facilityRepository.findById(uuid)
             .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.notFound("Facility with id " + uuid + " missing")));
  }
}
