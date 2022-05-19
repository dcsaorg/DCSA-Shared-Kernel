package org.dcsa.skernel.service.impl;

import org.dcsa.skernel.model.Address;
import org.dcsa.skernel.model.Party;
import org.dcsa.skernel.model.PartyIdentifyingCode;
import org.dcsa.skernel.model.enums.DCSAResponsibleAgencyCode;
import org.dcsa.skernel.model.mapper.PartyMapper;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.model.transferobjects.PartyTO;
import org.dcsa.skernel.repositority.PartyIdentifyingCodeRepository;
import org.dcsa.skernel.repositority.PartyRepository;
import org.dcsa.skernel.service.AddressService;
import org.dcsa.skernel.service.PartyContactDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test for PartyContactDetailsService implementation")
class PartyServiceImplTest {

  @Mock private PartyRepository partyRepository;
  @Mock private AddressService addressService;
  @Mock private PartyIdentifyingCodeRepository partyCodeListResponsibleAgencyRepository;
  @Mock private PartyContactDetailsService partyContactDetailsService;
  @Spy private PartyMapper partyMapper = Mappers.getMapper(PartyMapper.class);

  @InjectMocks private PartyServiceImpl partyService;

  Party party;
  PartyTO partyTO;
  Address address;
  PartyContactDetailsTO partyContactDetailsTO;
  PartyIdentifyingCode partyIdentifyingCode;
  PartyTO.IdentifyingCode identifyingCode;

  @BeforeEach
  void init() {
    party = new Party();
    party.setId("partyID1");
    party.setPartyName("partyName");
    party.setPublicKey("pubKey");
    party.setTaxReference1("TaxRef1");
    party.setTaxReference2("TaxRef2");
    party.setAddressID(UUID.randomUUID());

    address = new Address();
    address.setId(party.getAddressID());
    address.setCountry("Netherlands");
    address.setCity("Amsterdam");

    partyContactDetailsTO = new PartyContactDetailsTO();
    partyContactDetailsTO.setPhone("0123456789");
    partyContactDetailsTO.setUrl("party.org");
    partyContactDetailsTO.setName("party");
    partyContactDetailsTO.setEmail("party@party.org");

    partyIdentifyingCode = new PartyIdentifyingCode();
    partyIdentifyingCode.setPartyCode("PartyCode");
    partyIdentifyingCode.setPartyID(party.getId());
    partyIdentifyingCode.setId(UUID.randomUUID());
    partyIdentifyingCode.setDcsaResponsibleAgencyCode(DCSAResponsibleAgencyCode.DCSA);

    partyTO = partyMapper.partyToDTO(party);
    partyTO.setAddress(address);
    partyTO.setPartyContactDetails(List.of(partyContactDetailsTO));
    identifyingCode = PartyTO.IdentifyingCode.builder()
      .dcsaResponsibleAgencyCode(partyIdentifyingCode.getDcsaResponsibleAgencyCode())
      .partyCode(partyIdentifyingCode.getPartyCode())
      .build();
    partyTO.setIdentifyingCodes(List.of(identifyingCode));
  }

  @Test
  @DisplayName("Create PartyByTO should save a new party and resolve child objects")
  void testCreatePartyByTO() {
    when(partyRepository.save(any())).thenReturn(Mono.just(party));
    when(addressService.ensureResolvable(any())).thenReturn(Mono.just(address));
    when(partyContactDetailsService.ensureResolvable(any(), any())).thenReturn(Mono.just(partyContactDetailsTO));
    when(partyCodeListResponsibleAgencyRepository.save(any())).thenReturn(Mono.just(partyIdentifyingCode));

    StepVerifier.create(partyService.createPartyByTO(partyTO))
      .assertNext(partyTOResponse -> {
        verify(partyRepository, times(1)).save(any());
        verify(addressService, times(1)).ensureResolvable(any());
        verify(partyContactDetailsService, times(1)).ensureResolvable(any(), eq(party.getId()));
        verify(partyCodeListResponsibleAgencyRepository, times(1)).save(any());

        assertThat(partyTOResponse)
          .usingRecursiveComparison()
          .isEqualTo(partyTO);
      })
      .verifyComplete();
  }
}
