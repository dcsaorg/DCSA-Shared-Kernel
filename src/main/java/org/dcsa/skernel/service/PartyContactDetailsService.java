package org.dcsa.skernel.service;

import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PartyContactDetailsService {
  Flux<PartyContactDetailsTO> findTOByPartyID(UUID partyID);
  Mono<PartyContactDetailsTO> ensureResolvable(PartyContactDetailsTO partyContactDetailsTO, UUID partyId);
}
