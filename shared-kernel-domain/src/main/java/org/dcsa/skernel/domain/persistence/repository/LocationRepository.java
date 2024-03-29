package org.dcsa.skernel.domain.persistence.repository;

import org.dcsa.skernel.domain.persistence.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> { }
