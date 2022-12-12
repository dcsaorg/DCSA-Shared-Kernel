package org.dcsa.skernel.infrastructure.transferobject.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.dcsa.skernel.errors.exceptions.ConcreteRequestErrorMessageException;
import org.dcsa.skernel.infrastructure.transferobject.AddressTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;

import java.io.IOException;

/**
 * Need a custom deserializer for LocationTO since there is no discriminator and the fields
 * are not unique enough for Jackson to figure it out automagically (spelling intended).
 */
public class LocationTODeserializer extends StdDeserializer<LocationTO> {
  public LocationTODeserializer() {
    super(LocationTO.class);
  }

  @Override
  public LocationTO deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
      throw ConcreteRequestErrorMessageException.internalServerError(
        "Missing case for a new location type or CombinedLocation.isValidLocation() is wrong"
      );
  }

  private record CombinedLocation(
    String locationName,
    @JsonProperty("UNLocationCode") String UNLocationCode,
    String facilityCode,
    FacilityCodeListProvider facilityCodeListProvider,
    AddressTO address,
    String latitude,
    String longitude
  ) {
    boolean isAddress() { return address != null; }
    boolean isFacility() { return facilityCodeListProvider != null && facilityCode != null; }
    boolean isUNLocation() { return UNLocationCode != null && !isFacility(); }
    boolean isGeoLocation() { return longitude != null && latitude != null; }

    boolean isValidLocation() {
      int locationTypeCount = 0;
      locationTypeCount += isAddress() ? 1 : 0;
      locationTypeCount += isFacility() ? 1 : 0;
      locationTypeCount += isUNLocation() ? 1 : 0;
      locationTypeCount += isGeoLocation() ? 1 : 0;
      return locationTypeCount == 1;
    }
  }
}
