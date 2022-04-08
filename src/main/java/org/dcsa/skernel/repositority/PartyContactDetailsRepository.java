package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.PartyContactDetails;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface PartyContactDetailsRepository extends ReactiveCrudRepository<PartyContactDetails, UUID> {
  Flux<PartyContactDetails> findByPartyID(String partyID);
}
