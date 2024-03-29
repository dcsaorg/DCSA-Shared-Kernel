package org.dcsa.skernel.infrastructure.services.datafactories;

import lombok.experimental.UtilityClass;
import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;

import java.util.UUID;

@UtilityClass
public class LocationDataFactory {
  public static LocationTO addressLocationTO() {
    return LocationTO.builder()
      .locationName("Asseco DK office")
      .address(AddressDataFactory.addressTO())
      .build();
  }

  public static LocationTO facilityLocationTO() {
    return LocationTO.builder()
      .locationName(FacilityDataFactory.NAME)
      .UNLocationCode(FacilityDataFactory.UNLOCATION_CODE)
      .facilityCode(FacilityDataFactory.SMDG_CODE)
      .facilityCodeListProvider(FacilityCodeListProvider.SMDG)
      .build();
  }

  public static LocationTO unLocationLocationTO() {
    return LocationTO.builder()
      .locationName("Rotterdam UNLocation")
      .UNLocationCode("NLRTM")
      .build();
  }

  public static Location addressLocationWithoutId() {
    return addressLocationBuilder().build();
  }

  public static Location addressLocationWithId() {
    return addressLocationBuilder()
      .id(UUID.fromString("ed4c0ba4-2a5e-4f06-b584-b0c255e64eca"))
      .build();
  }

  private static Location.LocationBuilder addressLocationBuilder() {
    return Location.builder()
      .locationName("Asseco DK office")
      .address(AddressDataFactory.addressWithId())
      ;
  }

  public static Location facilityLocationWithoutId() {
    return facilityLocationBuilder().build();
  }

  public static Location facilityLocationWithId() {
    return facilityLocationBuilder()
      .id(UUID.fromString("e52af3db-c853-4fcd-b23a-c04d0e3be96a"))
      .build();
  }

  private static Location.LocationBuilder facilityLocationBuilder() {
    return Location.builder()
      .locationName(FacilityDataFactory.NAME)
      .UNLocationCode(FacilityDataFactory.UNLOCATION_CODE)
      .facility(FacilityDataFactory.facility())
      ;
  }

  public static Location unLocationLocationWithoutId() {
    return unLocationLocationBuilder().build();
  }

  public static Location unLocationLocationWithId() {
    return unLocationLocationBuilder()
      .id(UUID.fromString("b93a351e-0ccb-4b99-bc0b-7e9439fdaea1"))
      .build();
  }

  private static Location.LocationBuilder unLocationLocationBuilder() {
    return Location.builder()
      .locationName("Rotterdam UNLocation")
      .UNLocationCode("NLRTM")
      ;
  }
}
