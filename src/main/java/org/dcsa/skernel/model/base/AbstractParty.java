package org.dcsa.skernel.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class AbstractParty {

  @Id
  @JsonIgnore
  private UUID id;

  @Column("party_name")
  @Size(max = 100)
  private String partyName;

  @Column("tax_reference_1")
  @Size(max = 20)
  private String taxReference1;

  @Column("tax_reference_2")
  @Size(max = 20)
  private String taxReference2;

  @Column("public_key")
  @Size(max = 500)
  private String publicKey;
}
