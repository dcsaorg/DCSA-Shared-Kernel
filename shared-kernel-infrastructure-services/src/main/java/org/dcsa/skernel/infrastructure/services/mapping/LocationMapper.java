package org.dcsa.skernel.infrastructure.services.mapping;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcsa.skernel.domain.persistence.entity.Facility;
import org.dcsa.skernel.domain.persistence.entity.Location;
import org.dcsa.skernel.infrastructure.transferobject.LocationTO;
import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class LocationMapper {
  private final AddressMapper addressMapper;

  public LocationTO toDTO(Location location) {
    if (location == null) {
      return null;
    }

    String facilityCode = null;
    FacilityCodeListProvider facilityCodeListProvider = null;
    if (location.getFacility() != null) {
      Facility facility = location.getFacility();
      if (facility.getFacilitySMDGCode() != null) {
        facilityCode = facility.getFacilitySMDGCode();
        facilityCodeListProvider = FacilityCodeListProvider.SMDG;
      } else if (facility.getFacilityBICCode() != null) {
        facilityCode = facility.getFacilityBICCode();
        facilityCodeListProvider = FacilityCodeListProvider.BIC;
      } else {
        throw new IllegalArgumentException("Facility '" + facility.getId() + "' has neither SMDG code nor BIC code");
      }
    }

    return LocationTO.builder()
      .locationName(location.getLocationName())
      .address(addressMapper.toDTO(location.getAddress()))
      .UNLocationCode(location.getUNLocationCode())
      .facilityCode(facilityCode)
      .facilityCodeListProvider(facilityCodeListProvider)
      .latitude(location.getLatitude())
      .longitude(location.getLongitude())
      .build();
  }
}
