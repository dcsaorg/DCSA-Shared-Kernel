package org.dcsa.skernel.infrastructure.transferobject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

sealed public interface AddressLocationTO permits LocationTO {


  @Size(max = 100)
  String locationName();

  @Valid
  @NotNull
  AddressTO address();

}
