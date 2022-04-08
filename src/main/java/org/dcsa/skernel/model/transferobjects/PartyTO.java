package org.dcsa.skernel.model.transferobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.base.AbstractParty;
import org.dcsa.skernel.model.enums.DCSAResponsibleAgencyCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PartyTO extends AbstractParty {

  private String nmftaCode;

  private Address address;

  @NotNull(message = "PartyContactDetails is required.")
  @NotEmpty(message = "PartyContactDetails is required.")
  private List<PartyContactDetailsTO> partyContactDetails;

  private List<IdentifyingCode> identifyingCodes;

  public void adjustIdentifyingCodesIfNmftaIsPresent(){
    if (StringUtils.isNotEmpty(this.getNmftaCode())) {
      if (null != identifyingCodes
        && !identifyingCodes.isEmpty()
        && identifyingCodes.stream()
        .anyMatch(
          idc ->
            DCSAResponsibleAgencyCode.SCAC
              .getLegacyAgencyCode()
              .equals(idc.getCodeListResponsibleAgencyCode()))) {

        for (IdentifyingCode idc : this.identifyingCodes) {
          if(DCSAResponsibleAgencyCode.SCAC
            .getLegacyAgencyCode()
            .equals(idc.getCodeListResponsibleAgencyCode())){
            idc.setPartyCode(this.getNmftaCode());
          }
        }

      } else if (null == identifyingCodes || identifyingCodes.isEmpty()) {
        this.identifyingCodes =
          Collections.singletonList(
            IdentifyingCode.builder()
              .codeListResponsibleAgencyCode(DCSAResponsibleAgencyCode.SCAC.getLegacyAgencyCode())
              .partyCode(this.getNmftaCode())
              .build());
      } else {
        identifyingCodes.add(
          IdentifyingCode.builder()
            .codeListResponsibleAgencyCode(DCSAResponsibleAgencyCode.SCAC.getLegacyAgencyCode())
            .partyCode(this.getNmftaCode())
            .build());
      }
    }
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class IdentifyingCode {
    @JsonProperty("DCSAResponsibleAgencyCode")
    private DCSAResponsibleAgencyCode dcsaResponsibleAgencyCode;
    private String codeListResponsibleAgencyCode;
    private String partyCode;
    private String codeListName;
  }
}
