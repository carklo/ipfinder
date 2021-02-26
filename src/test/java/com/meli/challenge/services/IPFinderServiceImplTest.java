package com.meli.challenge.services;

import com.meli.challenge.domain.IP;
import com.meli.challenge.repositories.CurrencyRepository;
import com.meli.challenge.repositories.IPRepository;
import com.meli.challenge.repositories.ISOCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class IPFinderServiceImplTest {

  private IPFinderServiceImpl internalIPFinderServiceImpl;
  private RestTemplate restTemplate;
  private IPRepository ipRepository;
  private CurrencyRepository currencyRepository;
  private ISOCodeRepository isoCodeRepository;
  private IPFinderServiceImpl ipFinderService;

  @BeforeEach
  void setUp() {
    internalIPFinderServiceImpl = Mockito.mock(IPFinderServiceImpl.class);
    restTemplate = Mockito.mock(RestTemplate.class);
    ipRepository = Mockito.mock(IPRepository.class);
    currencyRepository = Mockito.mock(CurrencyRepository.class);
    isoCodeRepository = Mockito.mock(ISOCodeRepository.class);
    ipFinderService = new IPFinderServiceImpl(internalIPFinderServiceImpl,restTemplate,ipRepository, currencyRepository, isoCodeRepository);
  }

  @Test
  void getCountryBasedOnIP() {
    Mockito.when(restTemplate.getForObject(anyString(), any())).thenReturn(Collections.emptyMap());
    org.springframework.test.util.ReflectionTestUtils.setField(ipFinderService, "ip2countryInfoURL", "%s");
    assertNotNull(ipFinderService.getCountryBasedOnIP(""));
  }

  @Test
  void getCountryInfo() {
    Mockito.when(restTemplate.getForObject(anyString(), any())).thenReturn(Collections.emptyMap());
    org.springframework.test.util.ReflectionTestUtils.setField(ipFinderService, "restCountriesURL", "%s");
    assertNotNull(ipFinderService.getCountryInfo(""));
  }

  @Test
  void getFixerInfo() {
    Mockito.when(restTemplate.getForObject(anyString(), any())).thenReturn(Collections.emptyMap());
    org.springframework.test.util.ReflectionTestUtils.setField(ipFinderService, "fixerApikey", "%s");
    org.springframework.test.util.ReflectionTestUtils.setField(ipFinderService, "fixerURL", "%s");
    assertNotNull(ipFinderService.getFixerInfo(""));
  }

  //Case 1 new banned IP
  //Case 2 Ban existing IP
  //Case 3 return banned IP

  @Test
  void banIPCreateBannedIP() {
    IP ipInfo = IP
        .builder()
        .banned(true)
        .build();
    Mockito.when(ipRepository.findByIp(anyString())).thenReturn(null);
    Mockito.when(ipRepository.save(any())).thenReturn(ipInfo);
    IP actual = ipFinderService.banIP("1.1.1.1");
    assertNotNull(actual);
    assertTrue(actual.isBanned());
  }

  @Test
  void banIPExistingIP() {
    IP ipInfo = IP
        .builder()
        .banned(false)
        .build();
    Mockito.when(ipRepository.findByIp(anyString())).thenReturn(ipInfo);
    Mockito.when(ipRepository.save(any())).thenReturn(ipInfo);
    IP actual = ipFinderService.banIP("1.1.1.1");
    assertNotNull(actual);
    assertTrue(ipInfo.isBanned());
  }

  @Test
  void banIPAlreadyBanned() {
    IP ipInfo = IP
        .builder()
        .banned(true)
        .build();
    Mockito.when(ipRepository.findByIp(anyString())).thenReturn(ipInfo);
    IP actual = ipFinderService.banIP("1.1.1.1");
    assertNotNull(actual);
    assertTrue(ipInfo.isBanned());
  }

  //Case 1 Already on DB
  //Case 2 Already on DB and banned return null
  //Case 3 Create IP info and store

  @Test
  void getIPCompleteInfoAlreadyOnDB() {
    IP ip = IP.builder()
        .ip("1.1.1.1")
        .country("Colombia")
        .build();
    Mockito.when(ipRepository.findByIp(anyString())).thenReturn(ip);
    IP ipCompleteInfo = ipFinderService.getIPCompleteInfo("1.1.1.1");
    assertEquals(ip.getIp(), ipCompleteInfo.getIp());
  }

  @Test
  void getIPCompleteInfoAlreadyOnDBAndBanned() {
    IP ip = IP.builder()
        .ip("1.1.1.1")
        .country("Colombia")
        .banned(true)
        .build();
    Mockito.when(ipRepository.findByIp(anyString())).thenReturn(ip);
    IP ipCompleteInfo = ipFinderService.getIPCompleteInfo("1.1.1.1");
    assertNull(ipCompleteInfo);
  }

  @Test
  void getIPCompleteInfoCreateAndStoreIPInfo() {
    Map<String, String> countryBasedOnIP = new HashMap<>();
    countryBasedOnIP.put("countryCode", "DE");

    Map<String, Object> currencies = new HashMap<>();
    currencies.put("code", "EUR");
    currencies.put("name", "Euro");
    currencies.put("symbol", "€");

    Map<String, Object> countryInfo = new HashMap<>();
    countryInfo.put("name", "Germany");
    countryInfo.put("alpha3Code", "DEU");
    countryInfo.put("numericCode", "276");
    countryInfo.put("currencies", Collections.singletonList(currencies));

    Map<String, Object> rates = new HashMap<>();
    rates.put("EUR", 1.23);

    Map<String, Object> fixerInfo = new HashMap<>();
    fixerInfo.put("base", "USD");
    fixerInfo.put("rates", rates);

    Mockito.when(ipRepository.findByIp(anyString())).thenReturn(null);
    Mockito.when(internalIPFinderServiceImpl.getCountryBasedOnIP(anyString())).thenReturn(countryBasedOnIP);
    Mockito.when(internalIPFinderServiceImpl.getCountryInfo(anyString())).thenReturn(countryInfo);
    Mockito.when(internalIPFinderServiceImpl.getFixerInfo(anyString())).thenReturn(fixerInfo);
    Mockito.when(isoCodeRepository.findByalphaCode2(anyString())).thenReturn(null);
    Mockito.when(currencyRepository.findByCode(anyString())).thenReturn(null);
    IP ipCompleteInfo = ipFinderService.getIPCompleteInfo("1.1.1.1");
    assertNotNull(ipCompleteInfo);
    assertEquals(ipCompleteInfo.getIsoCode().getAlphaCode2(), countryBasedOnIP.get("countryCode"));
    assertEquals(ipCompleteInfo.getLocalCurrency().getQuotation(), "1.23 USD");
  }

}