package org.dcsa.skernel.infrastructure.transferobject;

import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

sealed public interface GeoLocationTO permits LocationTO {
  @Size(max = 100)
  String locationName();

  @NotBlank @Size(max = 11)
  String latitude();

  @NotBlank @Size(max = 11)
  String longitude();

  default GeoLocationTO geoLocationTO() {
    return this;
  }
  default LocationTO.LocationTOBuilder geoLocationBuilder() {
    return this.geoLocationBuilder();
  }

}
