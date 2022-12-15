package org.dcsa.skernel.infrastructure.services;

import org.dcsa.skernel.domain.persistence.entity.Address;
import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.domain.persistence.entity.UnLocation;
import org.dcsa.skernel.domain.persistence.repository.FacilityRepository;
import org.dcsa.skernel.domain.persistence.repository.LocationRepository;
import org.dcsa.skernel.domain.persistence.repository.UnLocationRepository;
import org.dcsa.skernel.infrastructure.services.datafactories.AddressDataFactory;
import org.dcsa.skernel.infrastructure.services.datafactories.LocationDataFactory;
import org.dcsa.skernel.errors.exceptions.NotFoundException;
import org.dcsa.skernel.infrastructure.transferobject.AddressTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {
  @Mock private AddressService addressService;
  @Mock private LocationRepository locationRepository;
  @Mock private FacilityRepository facilityRepository;
  @Mock private UnLocationRepository unLocationRepository;

  @InjectMocks private LocationService locationService;

  @BeforeEach
  public void resetMocks() {
    reset(addressService, locationRepository, facilityRepository, unLocationRepository);
  }

  @Test
  public void testNull() {
    assertNull(locationService.ensureResolvable(null));
  }

  @Test
  public void testAddressLocation_AddressNew() {
    // Setup
    LocationTO locationTO = LocationDataFactory.addressLocationTO();
    Location location = LocationDataFactory.addressLocationWithId();

    when(addressService.ensureResolvable(any(AddressTO.class), any(BiFunction.class)))
      .thenAnswer(invocation -> {
        BiFunction<Address, Boolean, Location> mapper = invocation.getArgument(1);
        return mapper.apply(AddressDataFactory.addressWithId(), true);
      });
    when(locationRepository.save(any(Location.class))).thenReturn(location);

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(addressService).ensureResolvable(eq(locationTO.address()), any(BiFunction.class));
    verify(locationRepository, never()).findAll(any(Example.class));
    verify(locationRepository).save(LocationDataFactory.addressLocationWithoutId());
  }

  @Test
  public void testAddressLocation_AddressExisting_LocationNotFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.addressLocationTO();
    Location location = LocationDataFactory.addressLocationWithId();

    when(addressService.ensureResolvable(any(AddressTO.class), any(BiFunction.class)))
      .thenAnswer(invocation -> {
      BiFunction<Address, Boolean, Location> mapper = invocation.getArgument(1);
      return mapper.apply(AddressDataFactory.addressWithId(), false);
    });
    when(locationRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());
    when(locationRepository.save(any(Location.class))).thenReturn(location);

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(addressService).ensureResolvable(eq(locationTO.address()), any(BiFunction.class));
    verify(locationRepository).findAll(any(Example.class));
    verify(locationRepository).save(LocationDataFactory.addressLocationWithoutId());
  }

  @Test
  public void testAddressLocation_AddressExisting_LocationFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.addressLocationTO();
    Location location = LocationDataFactory.addressLocationWithId();

    when(addressService.ensureResolvable(any(AddressTO.class), any(BiFunction.class)))
      .thenAnswer(invocation -> {
      BiFunction<Address, Boolean, Location> mapper = invocation.getArgument(1);
      return mapper.apply(AddressDataFactory.addressWithId(), false);
    });
    when(locationRepository.findAll(any(Example.class))).thenReturn(List.of(location));

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(addressService).ensureResolvable(eq(locationTO.address()), any(BiFunction.class));
    verify(locationRepository).findAll(any(Example.class));
    verify(locationRepository, never()).save(any(Location.class));
  }

  @Test
  public void testUNLocation_UNLocationNotFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.unLocationLocationTO();
    when(unLocationRepository.findById(anyString())).thenReturn(Optional.empty());

    // Execute/Verify
    NotFoundException exception = assertThrows(NotFoundException.class, () -> locationService.ensureResolvable(locationTO));
    assertEquals("No UNLocation found for UNLocationCode = 'NLRTM'", exception.getMessage());
  }

  @Test
  public void testUNLocation_UNLocationFound_LocationNotFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.unLocationLocationTO();
    Location location = LocationDataFactory.unLocationLocationWithId();

    when(unLocationRepository.findById(anyString())).thenReturn(
      Optional.of(UnLocation.builder().unLocationCode(locationTO.UNLocationCode()).build()));
    when(locationRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());
    when(locationRepository.save(any(Location.class))).thenReturn(location);
    mockNullAddress();

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(unLocationRepository).findById(locationTO.UNLocationCode());
    verify(locationRepository).findAll(any(Example.class));
    verify(locationRepository).save(LocationDataFactory.unLocationLocationWithoutId());
  }

  @Test
  public void testUNLocation_UNLocationFound_LocationFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.unLocationLocationTO();
    Location location = LocationDataFactory.unLocationLocationWithId();

    when(unLocationRepository.findById(anyString())).thenReturn(
      Optional.of(UnLocation.builder().unLocationCode(locationTO.UNLocationCode()).build()));
    when(locationRepository.findAll(any(Example.class))).thenReturn(List.of(location));
    mockNullAddress();

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(unLocationRepository).findById(locationTO.UNLocationCode());
    verify(locationRepository).findAll(any(Example.class));
    verify(locationRepository, never()).save(any(Location.class));
  }

  @Test
  public void testFacilityLocation_FacilityNotFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.facilityLocationTO();
    when(facilityRepository.findByUNLocationCodeAndFacilitySMDGCode(anyString(), anyString())).thenReturn(Optional.empty());

    // Execute/Verify
    NotFoundException exception = assertThrows(NotFoundException.class, () -> locationService.ensureResolvable(locationTO));
    assertEquals("No facility found for UNLocationCode = 'AUSYD' and facilitySMDGCode = 'ASLPB'", exception.getMessage());
  }

  @Test
  public void testFacilityLocation_FacilityFound_LocationNotFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.facilityLocationTO();
    Location location = LocationDataFactory.facilityLocationWithId();

    when(facilityRepository.findByUNLocationCodeAndFacilitySMDGCode(anyString(), anyString())).thenReturn(Optional.of(location.getFacility()));
    when(locationRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());
    when(locationRepository.save(any(Location.class))).thenReturn(location);
    mockNullAddress();

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(facilityRepository).findByUNLocationCodeAndFacilitySMDGCode(locationTO.UNLocationCode(), locationTO.facilityCode());
    verify(locationRepository).findAll(any(Example.class));
    verify(locationRepository).save(LocationDataFactory.facilityLocationWithoutId());
  }

  @Test
  public void testFacilityLocation_FacilityFound_LocationFound() {
    // Setup
    LocationTO locationTO = LocationDataFactory.facilityLocationTO();
    Location location = LocationDataFactory.facilityLocationWithId();

    when(facilityRepository.findByUNLocationCodeAndFacilitySMDGCode(anyString(), anyString())).thenReturn(Optional.of(location.getFacility()));
    when(locationRepository.findAll(any(Example.class))).thenReturn(List.of(location));
    mockNullAddress();

    // Execute
    Location actual = locationService.ensureResolvable(locationTO);

    // Verify
    assertEquals(location, actual);
    verify(facilityRepository).findByUNLocationCodeAndFacilitySMDGCode(locationTO.UNLocationCode(), locationTO.facilityCode());
    verify(locationRepository).findAll(any(Example.class));
    verify(locationRepository, never()).save(any(Location.class));
  }

  private void mockNullAddress() {
    when(addressService.ensureResolvable(eq(null), any(BiFunction.class)))
      .thenAnswer(invocation -> {
        BiFunction<Address, Boolean, Location> mapper = invocation.getArgument(1);
        return mapper.apply(null, false);
      });

  }
}
