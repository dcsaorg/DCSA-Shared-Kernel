package org.dcsa.skernel.infrastructure.services.mapping;

import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.infrastructure.services.datafactories.LocationDataFactory;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LocationMapperTest {
  @Spy private AddressMapper addressMapper = Mappers.getMapper(AddressMapper.class);
  @InjectMocks private LocationMapper locationMapper;

  @Test
  public void testNull() {
    assertNull(locationMapper.toDTO(null));
  }

  @Test
  public void testAddressLocationMapping() {
    // Setup
    Location location = LocationDataFactory.addressLocationWithId();

    // Execute
    LocationTO actual = locationMapper.toDTO(location);

    // Verify
    assertEquals(LocationDataFactory.addressLocationTO(), actual);
  }

  @Test
  public void testFacilityLocationMapping() {
    // Setup
    Location location = LocationDataFactory.facilityLocationWithId();

    // Execute
    LocationTO actual = locationMapper.toDTO(location);

    // Verify
    assertEquals(LocationDataFactory.facilityLocationTO(), actual);
  }

  @Test
  public void testUNLocationLocationMapping() {
    // Setup
    Location location = LocationDataFactory.unLocationLocationWithId();

    // Execute
    LocationTO actual = locationMapper.toDTO(location);

    // Verify
    assertEquals(LocationDataFactory.unLocationLocationTO(), actual);
  }
}
