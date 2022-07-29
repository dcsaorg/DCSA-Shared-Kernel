package org.dcsa.skernel.service;

import org.dcsa.skernel.model.transferobjects.PartyTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PartyService {
  Mono<PartyTO> createPartyByTO(PartyTO partyTO);
  Mono<PartyTO> findTOById(UUID publisherID);
}
