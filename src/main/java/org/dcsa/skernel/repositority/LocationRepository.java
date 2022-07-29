package org.dcsa.skernel.repositority;

import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Facility;
import org.dcsa.skernel.model.Location;
import org.dcsa.skernel.model.transferobjects.LocationTO;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LocationRepository extends ReactiveCrudRepository<Location, UUID> {

  Mono<Location> findByAddressIDAndFacilityIDAndLocationNameAndLatitudeAndLongitudeAndUnLocationCode(
    UUID addressID,
    UUID facilityID,
    String locationName,
    String latitude,
    String longitude,
    String unLocationCode
  );

  default Mono<Location> findByContent(LocationTO locationTO) {
    Address address = locationTO.getAddress();
    Facility facility = locationTO.getFacility();
    UUID addressID = address != null ? address.getId() : null;
    UUID facilityID = facility != null ? facility.getFacilityID() : null;
    return findByAddressIDAndFacilityIDAndLocationNameAndLatitudeAndLongitudeAndUnLocationCode(
      addressID,
      facilityID,
      locationTO.getLocationName(),
      locationTO.getLatitude(),
      locationTO.getLongitude(),
      locationTO.getUnLocationCode()
    );
  }
}
