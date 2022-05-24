package org.dcsa.skernel.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dcsa.core.util.MappingUtils;
import org.dcsa.skernel.model.base.AbstractParty;
import org.dcsa.skernel.model.transferobjects.PartyTO;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table("party")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Party extends AbstractParty {

  @Column("address_id")
  private UUID addressID;
}
