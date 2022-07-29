package org.dcsa.skernel.service;

import org.dcsa.skernel.model.transferobjects.LocationTO;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

public interface LocationService {

  Mono<LocationTO> ensureResolvable(LocationTO locationTO);

  Mono<LocationTO> findTOById(UUID locationID);

  Mono<LocationTO> fetchLocationByID(UUID id);

  Mono<LocationTO> fetchLocationDeepObjByID(UUID id);

  Mono<LocationTO> createLocationByTO(
      LocationTO locationTO, Function<UUID, Mono<Boolean>> updateEDocumentationCallback);

  Mono<LocationTO> resolveLocationByTO(
      UUID currentLocationIDInEDocumentation,
      LocationTO locationTO,
      Function<UUID, Mono<Boolean>> updateEDocumentationCallback);
}
