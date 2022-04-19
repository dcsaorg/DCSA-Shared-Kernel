package org.dcsa.skernel.service;

import org.dcsa.skernel.model.enums.CarrierCodeListProvider;
import org.dcsa.skernel.model.Carrier;
import org.dcsa.core.service.QueryService;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CarrierService extends QueryService<Carrier, UUID> {

    Mono<Carrier> findByCode(CarrierCodeListProvider carrierCodeListProvider, String carrierCode);
}
