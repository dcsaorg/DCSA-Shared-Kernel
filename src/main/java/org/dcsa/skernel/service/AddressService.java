package org.dcsa.skernel.service;

import org.dcsa.skernel.model.Address;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressService {
  Mono<Address> ensureResolvable(Address address);

  Mono<Address> findByIdOrEmpty(UUID id);

  Mono<Address> findById(UUID uuid);
}
