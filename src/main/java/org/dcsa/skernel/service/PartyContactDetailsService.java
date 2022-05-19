package org.dcsa.skernel.service;

import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PartyContactDetailsService {
  Flux<PartyContactDetailsTO> findTOByPartyID(String partyID);
  Mono<PartyContactDetailsTO> ensureResolvable(PartyContactDetailsTO partyContactDetailsTO, String partyId);
}
