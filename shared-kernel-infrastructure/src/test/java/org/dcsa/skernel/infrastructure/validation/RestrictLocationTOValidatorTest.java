package org.dcsa.skernel.infrastructure.validation;

import org.dcsa.skernel.infrastructure.transferobject.AddressTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO.LocationType;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class RestrictLocationTOValidatorTest {
  record AddressLocationRestricted(
    @RestrictLocationTO(LocationType.ADDRESS) LocationTO location
  ) { }
  record FacilityLocationRestricted(
    @RestrictLocationTO(LocationType.FACILITY) LocationTO location
  ) { }
  record UNLocationLocationRestricted(
    @RestrictLocationTO(LocationType.UNLOCATION) LocationTO location
  ) { }
  record GeoLocationRestricted(
    @RestrictLocationTO(LocationType.GEO) LocationTO location
  ) { }

  // Facility and UNLocation are the annoying combo since they overlap
  record CombiLocationRestricted(
    @RestrictLocationTO({LocationType.FACILITY, LocationType.UNLOCATION}) LocationTO location
  ) { }

  private ValidatorFactory validatorFactory;

  @BeforeAll
  public void createValidatorFactory() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
  }

  @AfterAll
  public void closeValidatorFactory() {
    validatorFactory.close();
  }

  @Test
  public void testAddressRestriction() {
    assertValid(new AddressLocationRestricted(null));
    assertValid(new AddressLocationRestricted(addressLocationTO()));
    assertInvalid(new AddressLocationRestricted(facilityLocationTO(true)));
    assertInvalid(new AddressLocationRestricted(unLocationLocationTO()));
    assertInvalid(new AddressLocationRestricted(geoLocationTO()));
  }

  @Test
  public void testFacilityRestriction() {
    assertValid(new FacilityLocationRestricted(null));
    assertValid(new FacilityLocationRestricted(facilityLocationTO(true)));
    assertInvalid(new FacilityLocationRestricted(addressLocationTO()));
    assertInvalid(new FacilityLocationRestricted(unLocationLocationTO()));
    assertInvalid(new FacilityLocationRestricted(geoLocationTO()));
  }

  @Test
  public void testUnLocationRestriction() {
    assertValid(new UNLocationLocationRestricted(null));
    assertValid(new UNLocationLocationRestricted(unLocationLocationTO()));
    assertInvalid(new UNLocationLocationRestricted(addressLocationTO()));
    assertInvalid(new UNLocationLocationRestricted(facilityLocationTO(true)));
    assertInvalid(new UNLocationLocationRestricted(geoLocationTO()));
  }

  @Test
  public void testGeoRestriction() {
    assertValid(new GeoLocationRestricted(null));
    assertValid(new GeoLocationRestricted(geoLocationTO()));
    assertInvalid(new GeoLocationRestricted(addressLocationTO()));
    assertInvalid(new GeoLocationRestricted(facilityLocationTO(true)));
    assertInvalid(new GeoLocationRestricted(unLocationLocationTO()));
  }

  @Test
  public void testCombiRestriction() {
    assertValid(new CombiLocationRestricted(null));
    assertValid(new CombiLocationRestricted(facilityLocationTO(true)));
    assertValid(new CombiLocationRestricted(facilityLocationTO(false)));
    assertValid(new CombiLocationRestricted(unLocationLocationTO()));
    assertInvalid(new CombiLocationRestricted(addressLocationTO()));
    assertInvalid(new CombiLocationRestricted(geoLocationTO()));
  }

  @Test
  public void testPrettyMessage() {
    Set<ConstraintViolation<AddressLocationRestricted>> violations =
      validate(new AddressLocationRestricted(facilityLocationTO(true)));

    assertEquals(1, violations.size());
    assertEquals(
      "must be one or more of the following location types: [AddressLocation]",
      violations.iterator().next().getMessage()
    );
  }

  private <T> void assertValid(T value) {
    assertTrue(validate(value).isEmpty());
  }

  private <T> void assertInvalid(T value) {
    assertFalse(validate(value).isEmpty());
  }

  private <T> Set<ConstraintViolation<T>> validate(T value) {
    return validatorFactory.getValidator().validate(value);
  }

  private static LocationTO addressLocationTO() {
    return LocationTO.builder()
      .locationName("test address location")
      .address(AddressTO.builder().name("test").build())
      .build();
  }

  private static LocationTO unLocationLocationTO() {
    return LocationTO.builder()
      .locationName("test unlocation location")
      .UNLocationCode("NTLWS")
      .build();
  }

  private static LocationTO facilityLocationTO(boolean includeUN) {
    return LocationTO.builder()
      .locationName("test facility location")
      .UNLocationCode(includeUN ? "NTLWS" : null)
      .facilityCode("pling")
      .facilityCodeListProvider(FacilityCodeListProvider.SMDG)
      .build();
  }

  private static LocationTO geoLocationTO() {
    return LocationTO.builder()
      .locationName("test geo location")
      .latitude("5.432")
      .longitude("1234")
      .build();
  }
}
