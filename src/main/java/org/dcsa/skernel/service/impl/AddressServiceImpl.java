package org.dcsa.skernel.service.impl;

import lombok.RequiredArgsConstructor;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.repositority.AddressRepository;
import org.dcsa.skernel.service.AddressService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AddressServiceImpl implements AddressService {
  private final AddressRepository addressRepository;

  @Override
  public Mono<Address> ensureResolvable(Address address) {
    return addressRepository.findByContent(address)
      .switchIfEmpty(Mono.defer(() -> addressRepository.save(address)));
  }

  @Override
  public Mono<Address> findByIdOrEmpty(UUID id) {
    return addressRepository.findByIdOrEmpty(id);
  }

  @Override
  public Mono<Address> findById(UUID uuid) {
    return addressRepository.findById(uuid)
      .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.notFound("Address with id " + uuid + " missing")));
  }

}
