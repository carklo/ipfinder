package com.meli.challenge.repositories;

import com.meli.challenge.domain.ISOCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISOCodeRepository extends JpaRepository<ISOCode, String> {
  ISOCode findByalphaCode2(String alphaCode2);
}
