package org.dcsa.skernel.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Party;
import org.dcsa.skernel.model.PartyIdentifyingCode;
import org.dcsa.skernel.model.enums.DCSAResponsibleAgencyCode;
import org.dcsa.skernel.model.mapper.PartyMapper;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.model.transferobjects.PartyTO;
import org.dcsa.skernel.repositority.PartyIdentifyingCodeRepository;
import org.dcsa.skernel.repositority.PartyRepository;
import org.dcsa.skernel.service.AddressService;
import org.dcsa.skernel.service.PartyContactDetailsService;
import org.dcsa.skernel.service.PartyService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class PartyServiceImpl implements PartyService {
  private final AddressService addressService;
  private final PartyRepository partyRepository;
  private final PartyIdentifyingCodeRepository partyCodeListResponsibleAgencyRepository;
  private final PartyContactDetailsService partyContactDetailsService;
  private final PartyMapper partyMapper;

  @Override
  public Mono<PartyTO> createPartyByTO(PartyTO partyTO) {
    Party p = partyMapper.dtoToParty(partyTO);
    if (p.getId() == null) {
      // Work around for DDT-1013
      p.setId(UUID.randomUUID());
      p.setNew(true);
    }
    return partyRepository.save(p)
        .flatMap(
            party ->
                Mono.when(
                        Mono.justOrEmpty(partyTO.getAddress())
                            .flatMap(addressService::ensureResolvable)
                            .doOnNext(partyTO::setAddress),
                        ensureResolvablePartyContactDetailsForParty(
                                partyTO.getPartyContactDetails(), party.getId())
                            .doOnNext(partyTO::setPartyContactDetails),
                        savePartyIdentifyingCodesForParty(
                                partyTO.getIdentifyingCodes(), party.getId())
                            .doOnNext(partyTO::setIdentifyingCodes))
                    .thenReturn(partyTO)
                  .doOnNext(partyTOResult-> partyTOResult.setId(party.getId())));
  }

  private Mono<List<PartyTO.IdentifyingCode>> savePartyIdentifyingCodesForParty(
      List<PartyTO.IdentifyingCode> partyIdentifyingCodes, UUID partyId) {
    return Flux.fromIterable(partyIdentifyingCodes)
        .map(identifyingCode -> mapIdcCodeToPartyIdc.apply(partyId, identifyingCode))
        .flatMap(partyCodeListResponsibleAgencyRepository::save)
        .map(this::partyIdentifyingCodeToIdentifyingCode)
        .collectList();
  }

  private Mono<List<PartyContactDetailsTO>> ensureResolvablePartyContactDetailsForParty(
      List<PartyContactDetailsTO> partyContactDetailsTOList, UUID partyId) {
    return Flux.fromIterable(partyContactDetailsTOList)
        .flatMap(
            partyContactDetailsTO ->
                partyContactDetailsService.ensureResolvable(partyContactDetailsTO, partyId))
        .collectList();
  }

  private final BiFunction<UUID, PartyTO.IdentifyingCode, PartyIdentifyingCode>
      mapIdcCodeToPartyIdc =
          (partyId, idc) -> {
            PartyIdentifyingCode partyCodeListResponsibleAgency = new PartyIdentifyingCode();
            partyCodeListResponsibleAgency.setPartyID(partyId);
            DCSAResponsibleAgencyCode dcsaCode = idc.getDcsaResponsibleAgencyCode();
            if (dcsaCode == null) {
              if (idc.getCodeListResponsibleAgencyCode() == null) {
                throw ConcreteRequestErrorMessageException.invalidInput(
                    "Either DCSAResponsibleAgencyCode or codeListResponsibleAgencyCode must be provided",
                    null);
              }
              try {
                dcsaCode =
                    DCSAResponsibleAgencyCode.legacyCode2DCSACode(
                        idc.getCodeListResponsibleAgencyCode());
              } catch (IllegalArgumentException e) {
                throw ConcreteRequestErrorMessageException.invalidInput(
                    "Unknown codeListResponsibleAgencyCode!", null);
              }
            } else if (idc.getCodeListResponsibleAgencyCode() != null) {
              if (!dcsaCode.getLegacyAgencyCode().equals(idc.getCodeListResponsibleAgencyCode())) {
                throw ConcreteRequestErrorMessageException.invalidInput(
                    "DCSAResponsibleAgencyCode and codeListResponsibleAgencyCode do not match. "
                        + dcsaCode
                        + " ("
                        + dcsaCode.getLegacyAgencyCode()
                        + ") vs. "
                        + idc.getCodeListResponsibleAgencyCode(),
                    null);
              }
            }
            partyCodeListResponsibleAgency.setDcsaResponsibleAgencyCode(dcsaCode);
            partyCodeListResponsibleAgency.setPartyCode(idc.getPartyCode());
            partyCodeListResponsibleAgency.setCodeListName(idc.getCodeListName());
            return partyCodeListResponsibleAgency;
          };

  @Override
  public Mono<PartyTO> findTOById(UUID partyID) {
    return partyRepository
        .findById(partyID)
        .switchIfEmpty(
            Mono.error(
                ConcreteRequestErrorMessageException.notFound(
                    "Cannot find party with ID: " + partyID)))
        .flatMap(this::loadRelatedEntities);
  }

  private Mono<PartyTO> loadRelatedEntities(Party party) {
    assert party.getId() != null : "Loading a party that has not been stored in the DB!?";
    // We leave the address without a default (i.e., no .switchIfEmpty) as otherwise we end
    // up with "address: { <all-null-fields> }" instead of "address: null".
    Mono<Address> addressMono =
        Mono.justOrEmpty(party.getAddressID())
            .flatMap(addressService::findById);

    Mono<List<PartyTO.IdentifyingCode>> sartIdentifyingCodes =
        partyCodeListResponsibleAgencyRepository
            .findAllByPartyID(party.getId())
            .map(this::partyIdentifyingCodeToIdentifyingCode)
            .collectList();

    Mono<List<PartyContactDetailsTO>> partyContactDetailsMono =
      partyContactDetailsService.findTOByPartyID(party.getId())
            .collectList();

    return Mono.just(partyMapper.partyToDTO(party))
        .flatMap(
            partyTO ->
                Mono.when(
                        addressMono.doOnNext(partyTO::setAddress),
                        sartIdentifyingCodes
                            .doOnNext(partyTO::setIdentifyingCodes)
                            .flatMap(this::findNmftaCode)
                            .doOnNext(partyTO::setNmftaCode),
                        partyContactDetailsMono.doOnNext(partyTO::setPartyContactDetails))
                    .thenReturn(partyTO));
  }

  private PartyTO.IdentifyingCode partyIdentifyingCodeToIdentifyingCode(
      PartyIdentifyingCode partyIdentifyingCode) {
    return PartyTO.IdentifyingCode.builder()
        .partyCode(partyIdentifyingCode.getPartyCode())
        .dcsaResponsibleAgencyCode(partyIdentifyingCode.getDcsaResponsibleAgencyCode())
        .codeListResponsibleAgencyCode(
            partyIdentifyingCode.getDcsaResponsibleAgencyCode().getLegacyAgencyCode())
        .codeListName(partyIdentifyingCode.getCodeListName())
        .build();
  }

  private Mono<String> findNmftaCode(List<PartyTO.IdentifyingCode> identifyingCodes) {
    if (null == identifyingCodes || identifyingCodes.isEmpty()) {
      return Mono.empty();
    }
    return Mono.justOrEmpty(identifyingCodes.stream()
      .filter(idc ->
        DCSAResponsibleAgencyCode.SCAC.getLegacyAgencyCode().equals(idc.getCodeListResponsibleAgencyCode())
          || DCSAResponsibleAgencyCode.SCAC == idc.getDcsaResponsibleAgencyCode()
      )
      .map(PartyTO.IdentifyingCode::getPartyCode)
      .findFirst().orElse(null));
  }
}
