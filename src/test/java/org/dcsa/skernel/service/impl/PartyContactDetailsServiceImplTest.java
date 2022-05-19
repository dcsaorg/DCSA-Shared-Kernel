package org.dcsa.skernel.service.impl;

import org.dcsa.skernel.model.PartyContactDetails;
import org.dcsa.skernel.model.mapper.PartyContactDetailsMapper;
import org.dcsa.skernel.model.transferobjects.PartyContactDetailsTO;
import org.dcsa.skernel.repositority.PartyContactDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test for PartyContactDetailsService implementation")
class PartyContactDetailsServiceImplTest {

  @Mock private PartyContactDetailsRepository partyContactDetailsRepository;

  @Spy
  private PartyContactDetailsMapper partyContactDetailsMapper =
      Mappers.getMapper(PartyContactDetailsMapper.class);

  @InjectMocks PartyContactDetailsServiceImpl partyContactDetailsService;

  PartyContactDetails partyContactDetails;
  PartyContactDetailsTO partyContactDetailsTO;

  @BeforeEach
  public void init() {
    partyContactDetails = new PartyContactDetails();
    partyContactDetails.setPartyID("PartyID1");
    partyContactDetails.setName("partyName");
    partyContactDetails.setEmail("party1@party.org");
    partyContactDetails.setPhone("0123456789");
    partyContactDetails.setUrl("party.org");

    partyContactDetailsTO = new PartyContactDetailsTO();
    partyContactDetailsTO.setEmail(partyContactDetails.getEmail());
    partyContactDetailsTO.setName(partyContactDetails.getName());
    partyContactDetailsTO.setUrl(partyContactDetails.getUrl());
    partyContactDetailsTO.setPhone(partyContactDetails.getPhone());
  }

  @Test
  @DisplayName("New partyContactDetails are saved.")
  void testEnsureResolvableSavesNewPartyContactDetails() {
    when(partyContactDetailsRepository.findByContent(any()))
        .thenReturn(Mono.empty());
    when(partyContactDetailsRepository.save(any())).thenReturn(Mono.just(partyContactDetails));

    StepVerifier.create(
            partyContactDetailsService.ensureResolvable(
                partyContactDetailsTO, partyContactDetails.getPartyID()))
        .assertNext(
            partyContactDetailsTOResponse -> {
              verify(partyContactDetailsRepository, times(1)).save(any());
              assertThat(partyContactDetailsTOResponse)
                  .usingRecursiveComparison()
                  .isEqualTo(partyContactDetailsTO);
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("Existing partyContactDetails should not be saved.")
  void testEnsureResolvableDoesNotSaveExistingPartyContactDetails() {
    when(partyContactDetailsRepository.findByContent(any()))
      .thenReturn(Mono.just(partyContactDetails));

    StepVerifier.create(
        partyContactDetailsService.ensureResolvable(
          partyContactDetailsTO, partyContactDetails.getPartyID()))
      .assertNext(
        partyContactDetailsTOResponse -> {
          verify(partyContactDetailsRepository, never()).save(any());
          assertThat(partyContactDetailsTOResponse)
            .usingRecursiveComparison()
            .isEqualTo(partyContactDetailsTO);
        })
      .verifyComplete();
  }

  @Test
  @DisplayName("Finding partyContactDetailsTo by ID should return PartyContactDetailsTO")
  void testFindTOByIDShouldReturnTO() {
    when(partyContactDetailsRepository.findByPartyID(partyContactDetails.getPartyID())).thenReturn(Flux.just(partyContactDetails));

    StepVerifier.create(partyContactDetailsService.findTOByPartyID(partyContactDetails.getPartyID()))
      .assertNext(partyContactDetailsTOResponse -> {
        assertThat(partyContactDetailsTOResponse)
          .usingRecursiveComparison()
          .isEqualTo(partyContactDetailsTO);
      })
      .verifyComplete();
  }
}
