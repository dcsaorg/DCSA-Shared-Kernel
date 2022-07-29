package org.dcsa.skernel.model;

import lombok.Data;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@Table("party_contact_details")
public class PartyContactDetails {
  @Id private UUID id;

  @Column("party_id")
  private UUID partyID;

  @Size(max = 100)
  @Column("name")
  private String name;

  @Size(max = 100)
  @Column("email")
  private String email;

  @Size(max = 30)
  @Column("phone")
  private String phone;

  @Size(max = 100)
  @Column("url")
  private String url;

  /**
   * Replace with a Mapper
   */
  @Deprecated
  public PartyContactDetailsTO toPartyTO() {
    PartyContactDetailsTO partyContactDetailsTO = new PartyContactDetailsTO();
    partyContactDetailsTO.setEmail(this.getEmail());
    partyContactDetailsTO.setPhone(this.getPhone());
    partyContactDetailsTO.setName(this.getName());

    return partyContactDetailsTO;
  }
}
