package org.dcsa.skernel.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.dcsa.skernel.domain.persistence.entity.Address;
import org.dcsa.skernel.domain.persistence.entity.Facility;
import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.domain.persistence.entity.UnLocation;
import org.dcsa.skernel.domain.persistence.repository.FacilityRepository;
import org.dcsa.skernel.domain.persistence.repository.LocationRepository;
import org.dcsa.skernel.domain.persistence.repository.UnLocationRepository;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.services.util.EnsureResolvable;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class LocationService extends EnsureResolvable<LocationTO, Location> {
  private final AddressService addressService;
  private final LocationRepository locationRepository;
  private final FacilityRepository facilityRepository;
  private final UnLocationRepository unLocationRepository;

  private final ExampleMatcher exampleMatcher =
    ExampleMatcher.matchingAll().withIncludeNullValues().withIgnorePaths("id");

  /**
   * Ensures that a location is resolvable. Will create Locations and Addresses if no matching Locations
   * or Addresses are found. If a location is not resolvable (i.e. for unknown Facilities or UNLocations) throws
   * an Exception.
   * If the input is null will return the result of calling the mapper with (null, false).
   */
  @Override
  @Transactional
  public <C> C ensureResolvable(LocationTO locationTO, BiFunction<Location, Boolean, C> mapper) {
    if (locationTO == null) {
      return mapper.apply(null, false);
    }

    Facility facility = findFacility(locationTO);
    String UNLocationCode = findUNLocationCode(locationTO.UNLocationCode(), facility);

    return addressService.ensureResolvable(locationTO.address(), (Address address, Boolean isAddressNew) -> {
      Location mappedLocation = Location.builder()
        .locationName(locationTO.locationName())
        .facility(facility)
        .UNLocationCode(UNLocationCode)
        .latitude(locationTO.latitude())
        .longitude(locationTO.longitude())
        .address(address)
        .build();

      if (isAddressNew) {
        return mapper.apply(locationRepository.save(mappedLocation), true);
      } else {
        return ensureResolvable(
          locationRepository.findAll(Example.of(mappedLocation, exampleMatcher)),
          () -> locationRepository.save(mappedLocation),
          mapper
        );
      }
    });
  }

  private Facility findFacility(LocationTO locationTO) {
    return locationTO.facilityCode() == null ? null : (switch (locationTO.facilityCodeListProvider()) {
      case SMDG -> facilityRepository.findByUNLocationCodeAndFacilitySMDGCode(locationTO.UNLocationCode(), locationTO.facilityCode());
      case BIC -> facilityRepository.findByUNLocationCodeAndFacilityBICCode(locationTO.UNLocationCode(), locationTO.facilityCode());
    })
      .orElseThrow(() -> ConcreteRequestErrorMessageException.notFound(
        "No facility found for UNLocationCode = '" + locationTO.UNLocationCode()
          + "' and facility" + locationTO.facilityCodeListProvider() + "Code = '" + locationTO.facilityCode() + "'"));
  }

  private String findUNLocationCode(String UNLocationCode, Facility facility) {
    // If facility is non-null then we know that UNLocationCode is valid so we don't need to check
    return (UNLocationCode == null || facility != null) ? UNLocationCode : unLocationRepository.findById(UNLocationCode)
      .map(UnLocation::getUnLocationCode)
      .orElseThrow(() -> ConcreteRequestErrorMessageException.notFound(
        "No UNLocation found for UNLocationCode = '" + UNLocationCode + "'"));
  }
}
