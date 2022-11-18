package org.dcsa.skernel.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.dcsa.skernel.domain.persistence.entity.Address;
import org.dcsa.skernel.domain.persistence.repository.AddressRepository;
import org.dcsa.skernel.infrastructure.services.mapping.AddressMapper;
import org.dcsa.skernel.infrastructure.services.util.EnsureResolvable;
import org.dcsa.skernel.infrastructure.transferobject.AddressTO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class AddressService extends EnsureResolvable<AddressTO, Address> {
  private final AddressRepository addressRepository;
  private final AddressMapper addressMapper;

  private final ExampleMatcher exampleMatcher =
    ExampleMatcher.matchingAll().withIncludeNullValues().withIgnorePaths("id");

  /**
   * Ensures that an address is resolvable. Will create an Address if no matching Addresses are found.
   * If the input is null will return the result of calling the mapper with (null, false).
   */
  @Override
  @Transactional
  public <C> C ensureResolvable(AddressTO addressTO, BiFunction<Address, Boolean, C> mapper) {
    if (addressTO == null) {
      return mapper.apply(null, false);
    }

    Address mappedAddress = addressMapper.toDAO(addressTO);
    return ensureResolvable(
      addressRepository.findAll(Example.of(mappedAddress, exampleMatcher)),
      () -> addressRepository.save(mappedAddress),
      mapper
    );
  }
}
