package com.meli.challenge.services;

import com.meli.challenge.domain.Currency;
import com.meli.challenge.domain.IP;
import com.meli.challenge.domain.ISOCode;
import com.meli.challenge.repositories.CurrencyRepository;
import com.meli.challenge.repositories.IPRepository;
import com.meli.challenge.repositories.ISOCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IPFinderServiceImpl implements IPFinderService {

  private final IPFinderServiceImpl ipFinderService;

  private final RestTemplate restTemplate;

  private final IPRepository ipRepository;

  private final CurrencyRepository currencyRepository;

  private final ISOCodeRepository isoCodeRepository;

  @Value("${ip2.country.info.url:https://api.ip2country.info/ip?%s}")
  private String ip2countryInfoURL;

  @Value("${rest.countries.url:https://restcountries.eu/rest/v2/alpha/%s}")
  private String restCountriesURL;

  @Value("${fixer.apikey}")
  private String fixerApikey;

  @Value("${fixer.url:http://data.fixer.io/api/latest?access_key=%s&base=%s&symbols=%s&format=1}")
  private String fixerURL;

  @Autowired
  public IPFinderServiceImpl(IPFinderServiceImpl ipFinderService, RestTemplate restTemplate,
                             IPRepository ipRepository, CurrencyRepository currencyRepository, ISOCodeRepository isoCodeRepository) {
    this.ipFinderService = ipFinderService;
    this.restTemplate = restTemplate;
    this.ipRepository = ipRepository;
    this.currencyRepository = currencyRepository;
    this.isoCodeRepository = isoCodeRepository;
  }

  @Override
  @Cacheable(value = "countryBasedOnIP", key = "#ip")
  public Map<String, String> getCountryBasedOnIP(String ip) {
    return restTemplate.getForObject(String.format(ip2countryInfoURL, ip), Map.class);
  }

  @Override
  @Cacheable(value = "countryInfo", key = "#countryCode")
  public Map<String, Object> getCountryInfo(String countryCode) {
    return restTemplate.getForObject(String.format(restCountriesURL, countryCode), Map.class);
  }

  @Override
  @Cacheable(value = "currency", key = "#currencyCode")
  public Map<String, Object> getFixerInfo(String currencyCode) {
    String base = "USD";
    if(currencyCode.equals("USD")) {
      base = "EUR";
    }
    return restTemplate.getForObject(String.format(fixerURL, fixerApikey, base, currencyCode), Map.class);
  }

  @Override
  public IP banIP(String ip) {
    Optional<IP> ipDB = Optional.ofNullable(ipRepository.findByIp(ip));
    if (ipDB.isEmpty()) {
      IP ipInfo = IP
          .builder()
          .ip(ip)
          .banned(true)
          .build();
      return ipRepository.save(ipInfo);
    } else {
      IP ip1 = ipDB.get();
      if(!ip1.isBanned()) {
        ip1.setBanned(true);
        return ipRepository.save(ip1);
      } else {
        return ip1;
      }
    }
  }

  @Override
  public IP getIPCompleteInfo(String ip) {
    Optional<IP> ipDB = Optional.ofNullable(ipRepository.findByIp(ip));
    if (ipDB.isEmpty()) {
      Map<String, String> countryBasedOnIP = ipFinderService.getCountryBasedOnIP(ip);
      String countryCode = countryBasedOnIP.get("countryCode");
      Map<String, Object> countryInfo = ipFinderService.getCountryInfo(countryCode);
      List<Map<String, Object>> currencies = (List<Map<String, Object>>) countryInfo.get("currencies");
      String currencyCode = (String) currencies.stream().findFirst().get().get("code");
      Map<String, Object> fixerCurrencyInfo = ipFinderService.getFixerInfo(currencyCode);
      Map<String, Object> rates = (Map<String, Object>) fixerCurrencyInfo.get("rates");
      Double price = (Double) rates.get(currencyCode);
      ISOCode isoCodeEntity;
      Currency currencyEntity;
      Optional<ISOCode> isoCode = Optional.ofNullable(isoCodeRepository.findByalphaCode2(countryCode));
      Optional<Currency> currency = Optional.ofNullable(currencyRepository.findByCode(currencyCode));
      if (isoCode.isEmpty()) {
        isoCodeEntity = ISOCode.builder()
            .alphaCode2(countryCode)
            .alphaCode3(String.valueOf(countryInfo.get("alpha3Code")))
            .numericCode(String.valueOf(countryInfo.get("numericCode")))
            .build();
        isoCodeRepository.save(isoCodeEntity);
      } else {
        isoCodeEntity = isoCode.get();
      }
      if (currency.isEmpty()) {
        currencyEntity = Currency.builder()
            .code(currencyCode)
            .name((String) currencies.stream().findFirst().get().get("name"))
            .symbol((String) currencies.stream().findFirst().get().get("symbol"))
            .quotation(price + " " + fixerCurrencyInfo.get("base"))
            .build();
        currencyRepository.save(currencyEntity);
      } else {
        currencyEntity = currency.get();
      }
      IP ipInfo = IP.builder()
          .ip(ip)
          .isoCode(isoCodeEntity)
          .country(String.valueOf(countryInfo.get("name")))
          .localCurrency(currencyEntity)
          .banned(false)
          .build();
      ipRepository.save(ipInfo);
      return ipInfo;
    } else {
      if(ipDB.get().isBanned()) {
        return null;
      } else {
        return ipDB.get();
      }
    }
  }
}
