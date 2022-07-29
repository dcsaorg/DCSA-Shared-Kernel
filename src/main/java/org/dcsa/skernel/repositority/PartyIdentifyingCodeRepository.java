package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.PartyIdentifyingCode;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface PartyIdentifyingCodeRepository extends ReactiveCrudRepository<PartyIdentifyingCode, UUID> {
    Flux<PartyIdentifyingCode> findAllByPartyID(UUID partyID);
}
