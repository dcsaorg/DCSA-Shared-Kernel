package org.dcsa.skernel.model.mapper;

import org.dcsa.skernel.model.Party;
import org.dcsa.skernel.model.transferobjects.PartyTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PartyMapper {
  PartyTO partyToDTO(Party party);

  Party dtoToParty(PartyTO partyTO);
}
