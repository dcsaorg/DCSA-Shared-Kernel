package org.dcsa.skernel.model.mapper;

import org.dcsa.skernel.model.PartyContactDetails;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PartyContactDetailsMapper {

  PartyContactDetailsTO partyContactDetailsToDTO(PartyContactDetails partyContactDetails);

  PartyContactDetails dtoToPartyContactDetails(PartyContactDetailsTO partyContactDetailsTO);

  @Mapping(source = "partyID", target = "partyID")
  PartyContactDetails dtoToPartyContactDetails(PartyContactDetailsTO partyContactDetailsTO, UUID partyID);
}
