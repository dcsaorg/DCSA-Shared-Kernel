package org.dcsa.skernel.infrastructure.transferobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

sealed public interface UNLocationLocationTO permits FacilityLocationTO, LocationTO {
  @Size(max = 100)
  String locationName();

  @NotBlank
  @Size(max = 5)
  @JsonProperty("UNLocationCode")
  String UNLocationCode();

}

