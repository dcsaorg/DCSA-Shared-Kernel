package org.dcsa.skernel.model.mapper;

import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Facility;
import org.dcsa.skernel.model.Location;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
  LocationTO locationToDTO(Location location);

  @Mapping(source = "location.id", target = "id")
  @Mapping(source = "location.unLocationCode", target = "unLocationCode")
  @Mapping(source = "location.facilityID", target = "facilityID")
  LocationTO locationToDTO(Location location, Address address, Facility facility);

  Location dtoToLocation(LocationTO locationTO);
}
