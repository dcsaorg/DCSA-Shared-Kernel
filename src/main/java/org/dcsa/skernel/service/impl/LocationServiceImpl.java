package org.dcsa.skernel.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dcsa.core.exception.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Facility;
import org.dcsa.skernel.model.Location;
import org.dcsa.skernel.model.mapper.LocationMapper;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.dcsa.skernel.repositority.LocationRepository;
import org.dcsa.skernel.repositority.UnLocationRepository;
import org.dcsa.skernel.service.AddressService;
import org.dcsa.skernel.service.FacilityService;
import org.dcsa.skernel.service.LocationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class LocationServiceImpl implements LocationService {

  private final FacilityService facilityService;
  private final AddressService addressService;

  private final LocationRepository locationRepository;
  private final UnLocationRepository unLocationRepository;

  private final LocationMapper locationMapper;


  public Mono<LocationTO> findPaymentLocationByShippingInstructionReference(String shippingInstructionReference) {
    return locationRepository
      .findPaymentLocationByShippingInstructionReference(shippingInstructionReference)
      .flatMap(this::getLocationTO);
  }

  @Override
  public Mono<LocationTO> ensureResolvable(LocationTO locationTO) {
    Mono<LocationTO> locationTOMono = ensureUnLocationResolvable(locationTO);

    Address address = locationTO.getAddress();
    if (address != null) {
      locationTOMono =
        locationTOMono
          .flatMap(loc ->
            addressService
              .ensureResolvable(address)
              .doOnNext(locationTO::setAddress)
              .thenReturn(locationTO)
          );
    }
    if (locationTO.getFacilityCode() != null) {
      locationTOMono =
        locationTOMono
          .flatMap(
            loc ->
              facilityService.findByUNLocationCodeAndFacilityCode(
                loc.getUnLocationCode(),
                loc.getFacilityCodeListProvider(),
                loc.getFacilityCode()))
          .doOnNext(locationTO::setFacility)
          .thenReturn(locationTO);
    }

    return locationTOMono
      .flatMap(locationRepository::findByContent)
      .switchIfEmpty(
        Mono.defer(() -> locationRepository.save(locationMapper.dtoToLocation(locationTO))))
      .doOnNext(loc -> locationTO.setId(loc.getId()))
      .map(
        location ->
          locationMapper.locationToDTO(
            location, locationTO.getAddress(), locationTO.getFacility()));
  }

  @Override
  public Mono<LocationTO> findTOById(String locationID) {
    return locationRepository.findById(locationID)
      .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.notFound("Cannot find location with ID: " + locationID)))
      .flatMap(this::getLocationTO);
  }

  @Override
  public Mono<LocationTO> fetchLocationByID(String id) {
    return Mono.justOrEmpty(id)
      .flatMap(locationRepository::findById)
      .flatMap(this::getLocationTO);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public Mono<LocationTO> fetchLocationDeepObjByID(String id) {
    if (id == null) return Mono.empty();
    return locationRepository
      .findById(id)
      .flatMap(
        location ->
          Mono.zip(
              addressService
                .findByIdOrEmpty(location.getAddressID())
                .map(Optional::of)
                .defaultIfEmpty(Optional.of(new Address())),
              facilityService
                .findByIdOrEmpty(location.getFacilityID())
                .map(Optional::of)
                .defaultIfEmpty(Optional.of(new Facility())))
            .flatMap(
              t2 ->
                Mono.just(
                  locationMapper.locationToDTO(
                    location, t2.getT1().get(), t2.getT2().get()))))
      .onErrorReturn(new LocationTO());
  }

  @Override
  public Mono<LocationTO> createLocationByTO(
    LocationTO locationTO, Function<String, Mono<Boolean>> updateEDocumentation) {

    if (Objects.isNull(locationTO)) {
      return Mono.empty();
    }

    Location location = locationMapper.dtoToLocation(locationTO);
    Mono<LocationTO> mono = ensureUnLocationResolvable(locationTO);

    if (Objects.isNull(locationTO.getAddress())) {
      return mono.flatMap(loc ->
        locationRepository
          .findByContent(locationTO)
          .switchIfEmpty(Mono.defer(() -> locationRepository.save(location)))
          .flatMap(l -> updateEDocumentation.apply(l.getId()).thenReturn(l))
          .map(locationMapper::locationToDTO));
    } else {
      return mono.flatMap(loc ->
        addressService
          .ensureResolvable(locationTO.getAddress())
          .flatMap(
            a -> {
              location.setAddressID(a.getId());
              locationTO.setAddress(a);
              return locationRepository
                .findByContent(locationTO)
                .switchIfEmpty(Mono.defer(() -> locationRepository.save(location)))
                .flatMap(l -> updateEDocumentation.apply(l.getId()).thenReturn(l))
                .map(l -> locationMapper.locationToDTO(l, a, null));
            }));
    }
  }

  @Override
  public Mono<LocationTO> resolveLocationByTO(
    String currentLocationIDInEDocumentation,
    LocationTO locationTO,
    Function<String, Mono<Boolean>> updateEDocumentationCallback) {

    // locationTO is the location received from the update eDocumentation request
    if (Objects.isNull(locationTO)) {
      if (StringUtils.isEmpty(currentLocationIDInEDocumentation)) {
        // it's possible that there may be no location linked to eDocumentation
        return Mono.empty();
      } else {
        return locationRepository.deleteById(currentLocationIDInEDocumentation).then(Mono.empty());
      }
    } else {
      return this.ensureResolvable(locationTO)
        .flatMap(lTO -> updateEDocumentationCallback.apply(lTO.getId()).thenReturn(lTO));
    }
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private Mono<LocationTO> getLocationTO(Location location) {
    return Mono.zip(
        addressService
          .findByIdOrEmpty(location.getAddressID())
          .map(Optional::of)
          .defaultIfEmpty(Optional.empty()),
        facilityService
          .findByIdOrEmpty(location.getFacilityID())
          .map(Optional::of)
          .defaultIfEmpty(Optional.empty()))
      .flatMap(
        t2 ->
          Mono.just(
            locationMapper.locationToDTO(
              location,
              t2.getT1().isPresent()
                ? t2.getT1().get()
                : null, // if optional is empty no value is retrieved hence in that case
              // we need to pass null
              t2.getT2().isPresent() ? t2.getT2().get() : null)));
  }

  private Mono<LocationTO> ensureUnLocationResolvable(LocationTO locationTO) {
    if (locationTO.getUnLocationCode() != null) {
      return unLocationRepository.findById(locationTO.getUnLocationCode())
        .switchIfEmpty(Mono.error(ConcreteRequestErrorMessageException.invalidParameter(
          "UNLocation with UNLocationCode "
            + locationTO.getUnLocationCode() + " not part of reference implementation data set")))
        .thenReturn(locationTO);
    }
    return Mono.just(locationTO);
  }
}
