package com.meli.challenge.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@PropertySource("classpath:encrypted.properties")
@EnableCaching
public class ChallengeConfig {
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    Cache cache1 = new ConcurrentMapCache("countryBasedOnIP");
    Cache cache2 = new ConcurrentMapCache("countryInfo");
    Cache cache3 = new ConcurrentMapCache("currency");
    cacheManager.setCaches(Arrays.asList(cache1, cache2, cache3));
    return cacheManager;
  }
}
