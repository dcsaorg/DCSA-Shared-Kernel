package org.dcsa.skernel.infrastructure.transferobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;
import org.dcsa.skernel.infrastructure.validation.AllOrNone;
import org.dcsa.skernel.infrastructure.validation.AtLeast;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.function.Predicate;

@AtLeast(
  nonNullsRequired = 1,
  fields = {"address", "UNLocationCode", "facilityCode", "latitude"}
)
@AllOrNone({"facilityCode", "facilityCodeListProvider"})
@AllOrNone({"latitude", "longitude"})
@Builder(toBuilder = true)
public record LocationTO(
  @Size(max = 100)
  String locationName,

  @Valid
  AddressTO address,

  @Size(max = 5)
  @JsonProperty("UNLocationCode")
  String UNLocationCode,

  @Size(max = 6)
  String facilityCode,

  FacilityCodeListProvider facilityCodeListProvider,

  @Size(max = 11)
  String latitude,

  @Size(max = 11)
  String longitude
) {
  @Getter
  @RequiredArgsConstructor
  public enum LocationType {
    ADDRESS("AddressLocation", to -> to.address != null, to -> to.address == null),
    UNLOCATION("UNLocationLocation", to -> to.UNLocationCode != null, to -> to.UNLocationCode == null || to.facilityCode != null),
    FACILITY("FacilityLocation", to -> to.facilityCode != null, to -> to.facilityCode == null),
    GEO("GeoLocation", to -> to.latitude != null, to -> to.latitude == null)
    ;

    private final String prettyName;
    private final Predicate<LocationTO> isType;
    private final Predicate<LocationTO> mightNotBeType;
  }
}
