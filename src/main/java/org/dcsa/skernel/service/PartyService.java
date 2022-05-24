package org.dcsa.skernel.service;

import org.dcsa.skernel.model.transferobjects.PartyTO;
import reactor.core.publisher.Mono;

public interface PartyService {
  Mono<PartyTO> createPartyByTO(PartyTO partyTO);
  Mono<PartyTO> findTOById(String publisherID);
}
