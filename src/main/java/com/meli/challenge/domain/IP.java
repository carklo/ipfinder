package com.meli.challenge.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IP {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private int id;
  private String ip;
  private String country;
  @ManyToOne(cascade = CascadeType.MERGE)
  private ISOCode isoCode;
  @ManyToOne(cascade = CascadeType.MERGE)
  private Currency localCurrency;
  private boolean banned;

}
