package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.Party;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PartyRepository extends ReactiveCrudRepository<Party, UUID> {
  default Mono<Party> findByIdOrEmpty(UUID id) {
    return Mono.justOrEmpty(id)
      .flatMap(this::findById);
  }
}
