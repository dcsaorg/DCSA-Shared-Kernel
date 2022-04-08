package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.Party;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PartyRepository extends ReactiveCrudRepository<Party, String> {
  default Mono<Party> findByIdOrEmpty(String id) {
    return Mono.justOrEmpty(id)
      .flatMap(this::findById);
  }
}
