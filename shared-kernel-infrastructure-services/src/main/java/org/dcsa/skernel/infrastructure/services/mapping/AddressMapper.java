package org.dcsa.skernel.infrastructure.services.mapping;

import org.dcsa.skernel.domain.persistence.entity.Address;
import org.dcsa.skernel.infrastructure.transferobject.AddressTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
  AddressTO toDTO(Address address);
  Address toDAO(AddressTO address);
}
