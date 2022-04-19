package org.dcsa.skernel.service;

import org.dcsa.core.service.QueryService;
import org.dcsa.skernel.model.Vessel;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VesselService extends QueryService<Vessel, UUID> {

    Mono<Vessel> create(Vessel vessel);

    Mono<Vessel> update(Vessel vessel);

    Mono<Vessel> findByVesselIMONumber(String vesselIMONumber);

    Mono<Vessel> findById(UUID vesselID);
}
