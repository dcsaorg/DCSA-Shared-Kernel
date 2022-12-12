package org.dcsa.skernel.infrastructure.transferobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//@JsonDeserialize(using = LocationTODeserializer.class)
public record LocationTO(
   String locationName,

   @JsonProperty("UNLocationCode") String UNLocationCode,

    String facilityCode,

    FacilityCodeListProvider facilityCodeListProvider,

    AddressTO address,

    String latitude,
    String longitude
)
implements UNLocationLocationTO, AddressLocationTO, FacilityLocationTO, GeoLocationTO {

  @Builder(toBuilder = true)
  public LocationTO {}

  public boolean isAddress() { return address != null; }
  public boolean isFacility() { return facilityCodeListProvider != null && facilityCode != null;}
  public boolean isUNLocation() { return UNLocationCode != null && !isFacility(); }
  public boolean isGeoLocation() { return longitude != null && latitude != null; }
}
