package org.dcsa.skernel.model.mapper;

import org.dcsa.skernel.model.PartyContactDetails;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartyContactDetailsMapper {

  PartyContactDetailsTO partyContactDetailsToDTO(PartyContactDetails partyContactDetails);

  PartyContactDetails dtoToPartyContactDetails(PartyContactDetailsTO partyContactDetailsTO);
}
