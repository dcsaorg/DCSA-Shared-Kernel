package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.Address;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressRepository extends ReactiveCrudRepository<Address, UUID> {

  Mono<Address> findByNameAndStreetAndStreetNumberAndFloorAndPostalCodeAndCityAndStateRegionAndCountry(
    String name,
    String street,
    String streetNumber,
    String floor,
    String postalCode,
    String city,
    String stateRegion,
    String country
  );

  default Mono<Address> findByContent(Address address) {
    if (address.getId() != null) {
      return findById(address.getId());
    }
    return findByNameAndStreetAndStreetNumberAndFloorAndPostalCodeAndCityAndStateRegionAndCountry(
      address.getName(),
      address.getStreet(),
      address.getStreetNumber(),
      address.getFloor(),
      address.getPostalCode(),
      address.getCity(),
      address.getStateRegion(),
      address.getCountry()
    );
  }

  default Mono<Address> findByIdOrEmpty(UUID id) {
    return Mono.justOrEmpty(id)
      .flatMap(this::findById);
  }
}
