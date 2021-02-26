package com.meli.challenge.repositories;

import com.meli.challenge.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
  Currency findByCode(String code);
}
