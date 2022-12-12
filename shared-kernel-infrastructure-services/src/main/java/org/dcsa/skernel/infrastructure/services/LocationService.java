package org.dcsa.skernel.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.dcsa.skernel.domain.persistence.entity.Address;
import org.dcsa.skernel.domain.persistence.entity.Facility;
import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.domain.persistence.repository.FacilityRepository;
import org.dcsa.skernel.domain.persistence.repository.LocationRepository;
import org.dcsa.skernel.domain.persistence.repository.UnLocationRepository;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.services.util.EnsureResolvable;
import org.dcsa.skernel.infrastructure.transferobject.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class LocationService extends EnsureResolvable<LocationTO, Location> {
  private final AddressService addressService;
  private final LocationRepository locationRepository;
  private final FacilityRepository facilityRepository;
  private final UnLocationRepository unLocationRepository;

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
    } else if (locationTO.isAddress() && locationTO.isUNLocation()) {
      return ensureResolvableUNLocationAndAddress( locationTO, mapper);
    } else if (locationTO.isAddress()) {
      return ensureResolvable((AddressLocationTO) locationTO, mapper);
    } else if (locationTO.isUNLocation()) {
      return ensureResolvable((UNLocationLocationTO) locationTO, mapper);
    } else if (locationTO.isFacility()) {
      return ensureResolvable((FacilityLocationTO) locationTO, mapper);
    } else if (locationTO.isGeoLocation()) {
      return ensureResolvable((GeoLocationTO) locationTO, mapper);
    } else {
      throw ConcreteRequestErrorMessageException.internalServerError("Unable to resolve location of type "
        + locationTO.getClass().getSimpleName());
    }
  }

  private <C> C ensureResolvable(AddressLocationTO locationTO, BiFunction<Location, Boolean, C> mapper) {
    Function<Address, Location> creator = address -> locationRepository.save(Location.builder()
      .locationName(locationTO.locationName())
      .address(address)
      .build());

    return addressService.ensureResolvable(locationTO.address(), (Address address, Boolean isAddressNew) -> {
      if (isAddressNew) {
        return mapper.apply(creator.apply(address), true);
      } else {
        return ensureResolvable(
          locationRepository.findByLocationNameAndAddress(locationTO.locationName(), address),
          () -> creator.apply(address),
          mapper
        );
      }
    });
  }

  protected <C> C ensureResolvable(GeoLocationTO geoLocationTO, BiFunction<Location, Boolean, C> mapper) {
    return ensureResolvable(
      locationRepository.findByLocationNameAndLatitudeAndLongitude(
        geoLocationTO.locationName(),
        geoLocationTO.latitude(),
        geoLocationTO.longitude()
      ),
      () -> locationRepository.save(Location.builder()
        .locationName(geoLocationTO.locationName())
        .latitude(geoLocationTO.latitude())
        .longitude(geoLocationTO.longitude())
        .build()),
      mapper
    );
  }

  private <C> C ensureResolvable(UNLocationLocationTO locationTO, BiFunction<Location, Boolean, C> mapper) {
    resolveUNLocation(locationTO);

    return ensureResolvable(
      locationRepository.findByLocationNameAndUNLocationCode(locationTO.locationName(), locationTO.UNLocationCode()),
      () -> locationRepository.save(Location.builder()
        .locationName(locationTO.locationName())
        .UNLocationCode(locationTO.UNLocationCode())
        .build()),
      mapper);
  }

  private void resolveUNLocation(UNLocationLocationTO locationTO) {
    unLocationRepository.findById(locationTO.UNLocationCode())
      .orElseThrow(() -> ConcreteRequestErrorMessageException.notFound(
        "No UNLocation found for UNLocationCode = '" + locationTO.UNLocationCode() + "'"));
  }

  private <C> C ensureResolvable(FacilityLocationTO locationTO, BiFunction<Location, Boolean, C> mapper) {
    Facility facility = (switch (locationTO.facilityCodeListProvider()) {
      case SMDG -> facilityRepository.findByUNLocationCodeAndFacilitySMDGCode(locationTO.UNLocationCode(), locationTO.facilityCode());
      case BIC -> facilityRepository.findByUNLocationCodeAndFacilityBICCode(locationTO.UNLocationCode(), locationTO.facilityCode());
    })
      .orElseThrow(() -> ConcreteRequestErrorMessageException.notFound(
        "No facility found for UNLocationCode = '" + locationTO.UNLocationCode()
          + "' and facility" + locationTO.facilityCodeListProvider() + "Code = '" + locationTO.facilityCode() + "'"));

    return ensureResolvable(
      locationRepository.findByLocationNameAndFacilityAndUNLocationCode(locationTO.locationName(), facility, locationTO.UNLocationCode()),
      () -> locationRepository.save(Location.builder()
        .locationName(locationTO.locationName())
        .facility(facility)
        .UNLocationCode(locationTO.UNLocationCode())
        .build()),
      mapper
      );
  }

  private <C> C ensureResolvableUNLocationAndAddress(LocationTO locationTO, BiFunction<Location, Boolean, C> mapper) {
    resolveUNLocation(locationTO);
    Function<Address, Location> creator =
        address ->
            locationRepository.save(
                Location.builder()
                    .locationName(locationTO.locationName())
                    .address(address)
                    .UNLocationCode(locationTO.UNLocationCode())
                    .build());

    return addressService.ensureResolvable(
        locationTO.address(),
        (Address address, Boolean isAddressNew) -> {
          if (isAddressNew) {
            return mapper.apply(creator.apply(address), true);
          } else {
            return ensureResolvable(
                locationRepository.findByLocationNameAndAddressAndUNLocationCode(
                    locationTO.locationName(), address, locationTO.UNLocationCode()),
                () -> creator.apply(address),
                mapper);
          }
        });
  }
}
