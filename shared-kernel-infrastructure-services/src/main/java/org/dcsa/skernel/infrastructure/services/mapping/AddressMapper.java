package org.dcsa.skernel.infrastructure.services.mapping;

import org.dcsa.skernel.domain.persistence.entity.Address;
import org.dcsa.skernel.infrastructure.transferobject.AddressTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AddressMapper {
  AddressTO toDTO(Address address);
  @Mapping(target = "id", ignore = true)
  Address toDAO(AddressTO address);
}
