package org.dcsa.skernel.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "address")
public class Address {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "street", length = 100)
  private String street;

  @Column(name = "street_number", length = 50)
  private String streetNumber;

  @Column(name = "floor", length =  50)
  private String floor;

  @Column(name = "postal_code", length = 10)
  private String postalCode;

  @Column(name = "city", length = 65)
  private String city;

  @Column(name = "state_region", length = 75)
  private String stateRegion;

  @Column(name = "country", length = 75)
  private String country;
}
