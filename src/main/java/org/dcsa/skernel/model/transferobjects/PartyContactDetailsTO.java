package org.dcsa.skernel.model.transferobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dcsa.skernel.model.PartyContactDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyContactDetailsTO {
  private String name;
  private String phone;
  private String email;
  private String url;

  /**
   * Use a mapper instead
   */
  @Deprecated
  public PartyContactDetails toPartyContactDetails(String partyID) {
    PartyContactDetails partyContactDetails = new PartyContactDetails();
    partyContactDetails.setName(this.getName());
    partyContactDetails.setEmail(this.getEmail());
    partyContactDetails.setPhone(this.getPhone());
    partyContactDetails.setUrl(this.getUrl());
    partyContactDetails.setPartyID(partyID);
    return partyContactDetails;
  }
}
