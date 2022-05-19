package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.PartyContactDetails;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PartyContactDetailsRepository
    extends ReactiveCrudRepository<PartyContactDetails, UUID> {
  Flux<PartyContactDetails> findByPartyID(String partyID);

  Mono<PartyContactDetails> findByNameAndEmailAndAndPhoneAndUrl(
      String name, String email, String phone, String url);

  default Mono<PartyContactDetails> findByContent(PartyContactDetails partyContactDetails) {
    if (partyContactDetails.getId() != null) {
      return findById(partyContactDetails.getId());
    }
    return findByNameAndEmailAndAndPhoneAndUrl(
        partyContactDetails.getName(),
        partyContactDetails.getEmail(),
        partyContactDetails.getPhone(),
        partyContactDetails.getUrl());
  }
}
