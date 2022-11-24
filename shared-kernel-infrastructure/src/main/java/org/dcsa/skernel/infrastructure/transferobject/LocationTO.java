package org.dcsa.skernel.infrastructure.transferobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO.AddressLocationTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO.FacilityLocationTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO.GeoLocationTO;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO.UNLocationLocationTO;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;
import org.dcsa.skernel.infrastructure.transferobject.jackson.LocationTODeserializer;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonDeserialize(using = LocationTODeserializer.class)
public sealed interface LocationTO permits AddressLocationTO, FacilityLocationTO, UNLocationLocationTO, GeoLocationTO {
  @Size(max = 100)
  String locationName();

  /**
   * Shortcut for creating a builder for a LocationTO of type AddressLocationTO.
   */
  static AddressLocationTO.AddressLocationTOBuilder addressLocationBuilder() {
    return AddressLocationTO.builder();
  }

  /**
   * Shortcut for creating a builder for a LocationTO of type FacilityLocationTO.
   */
  static FacilityLocationTO.FacilityLocationTOBuilder facilityLocationBuilder() {
    return FacilityLocationTO.builder();
  }

  /**
   * Shortcut for creating a builder for a LocationTO of type UNLocationLocationTO.
   */
  static UNLocationLocationTO.UNLocationLocationTOBuilder unLocationLocationBuilder() {
    return UNLocationLocationTO.builder();
  }


  /**
   * Shortcut for creating a builder for a LocationTO of type GeoLocationTO.
   */
  static GeoLocationTO.GeoLocationTOBuilder geoLocationBuilder() {
    return GeoLocationTO.builder();
  }

  record AddressLocationTO(
    @Size(max = 100)
    String locationName,

    @Valid @NotNull
    AddressTO address
  ) implements LocationTO {
    @Builder(toBuilder = true)
    public AddressLocationTO { }
  }

  record UNLocationLocationTO(
    @Size(max = 100)
    String locationName,

    @NotBlank @Size(max = 5)
    @JsonProperty("UNLocationCode")
    String UNLocationCode
  ) implements LocationTO {
    @Builder(toBuilder = true)
    public UNLocationLocationTO { }
  }

  record FacilityLocationTO(
    @Size(max = 100)
    String locationName,

    @Size(max = 5)
    @JsonProperty("UNLocationCode")
    String UNLocationCode,

    @NotBlank @Size(max = 6)
    String facilityCode,

    @NotNull
    FacilityCodeListProvider facilityCodeListProvider
  ) implements LocationTO {
    @Builder(toBuilder = true)
    public FacilityLocationTO { }
  }


  record GeoLocationTO(
    @Size(max = 100)
    String locationName,

    @NotBlank @Size(max = 11)
    String latitude,

    @NotBlank @Size(max = 11)
    String longitude
  ) implements LocationTO {
    @Builder(toBuilder = true)
    public GeoLocationTO { }
  }

}
