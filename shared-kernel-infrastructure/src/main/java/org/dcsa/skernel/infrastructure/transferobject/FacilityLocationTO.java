package org.dcsa.skernel.infrastructure.transferobject;

import org.dcsa.skernel.infrastructure.transferobject.enums.FacilityCodeListProvider;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

sealed public interface FacilityLocationTO extends UNLocationLocationTO permits LocationTO {
  @NotBlank
  @Size(max = 6)
  String facilityCode();

  @NotNull
  FacilityCodeListProvider facilityCodeListProvider();

}
