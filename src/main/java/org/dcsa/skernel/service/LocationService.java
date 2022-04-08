package org.dcsa.skernel.service;

import org.dcsa.skernel.model.transferobjects.LocationTO;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface LocationService {

  Mono<LocationTO> ensureResolvable(LocationTO locationTO);

  Mono<LocationTO> findPaymentLocationByShippingInstructionReference(String shippingInstructionReference);

  Mono<LocationTO> findTOById(String locationID);

  Mono<LocationTO> fetchLocationByID(String id);

  Mono<LocationTO> fetchLocationDeepObjByID(String id);

  Mono<LocationTO> createLocationByTO(
      LocationTO locationTO, Function<String, Mono<Boolean>> updateEDocumentationCallback);

  Mono<LocationTO> resolveLocationByTO(
      String currentLocationIDInEDocumentation,
      LocationTO locationTO,
      Function<String, Mono<Boolean>> updateEDocumentationCallback);
}
