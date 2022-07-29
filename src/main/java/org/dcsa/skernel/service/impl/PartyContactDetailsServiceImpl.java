package org.dcsa.skernel.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.skernel.model.PartyContactDetails;
import org.dcsa.skernel.model.mapper.PartyContactDetailsMapper;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.repositority.PartyContactDetailsRepository;
import org.dcsa.skernel.service.PartyContactDetailsService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartyContactDetailsServiceImpl implements PartyContactDetailsService {

  private final PartyContactDetailsRepository partyContactDetailsRepository;
  private final PartyContactDetailsMapper partyContactDetailsMapper;

  @Override
  public Flux<PartyContactDetailsTO> findTOByPartyID(UUID partyID) {
    return partyContactDetailsRepository
        .findByPartyID(partyID)
        .map(partyContactDetailsMapper::partyContactDetailsToDTO);
  }

  @Override
  public Mono<PartyContactDetailsTO> ensureResolvable(
      PartyContactDetailsTO partyContactDetailsTO, UUID partyId) {
    PartyContactDetails partyContactDetails =
        partyContactDetailsMapper.dtoToPartyContactDetails(partyContactDetailsTO, partyId);
    return partyContactDetailsRepository
        .findByContent(partyContactDetails)
        .switchIfEmpty(Mono.defer(() -> saveNewPartyContactDetails(partyContactDetails, partyId)))
        .map(partyContactDetailsMapper::partyContactDetailsToDTO);
  }

  private Mono<PartyContactDetails> saveNewPartyContactDetails(
      PartyContactDetails partyContactDetails, UUID partyId) {
    partyContactDetails.setPartyID(partyId);
    return partyContactDetailsRepository.save(partyContactDetails);
  }
}
