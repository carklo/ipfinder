package com.meli.challenge.services;

import com.meli.challenge.domain.IP;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

public interface IPFinderService {
  Map<String, String> getCountryBasedOnIP(String ip);
  Map<String, Object> getCountryInfo(String countryCode);
  IP getIPCompleteInfo(String ip) throws HttpClientErrorException;
  Map<String, Object> getFixerInfo(String currencyCode);
  IP banIP(String ip);
}
