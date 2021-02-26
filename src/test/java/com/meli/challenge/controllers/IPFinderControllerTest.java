package com.meli.challenge.controllers;

import com.meli.challenge.domain.IP;
import com.meli.challenge.services.IPFinderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

class IPFinderControllerTest {

  private IPFinderController ipFinderController;
  private IPFinderService ipFinderService;

  @BeforeEach
  void setUp() {
    ipFinderService = Mockito.mock(IPFinderService.class);
    ipFinderController = new IPFinderController(ipFinderService);
  }

  //Case 1 IP not valid
  //Case 2 Exception
  //Case 3 BannedIP
  //Case 4 Success

  @Test
  void findIPInfoInvalidIP() {
    ResponseEntity ipInfo = ipFinderController.findIPInfo("2323df");
    assertEquals(ipInfo.getBody(), "The IP 2323df is not valid");
  }

  @Test
  void findIPInfoHttpClientErrorException() {
    Mockito.when(ipFinderService.getIPCompleteInfo(anyString())).thenThrow((HttpClientErrorException.class));
    ResponseEntity ipInfo = ipFinderController.findIPInfo("1.1.1.1");
    assertEquals(ipInfo.getBody(), "It wasn't possible to retrieve the country's information for IP 1.1.1.1");
  }

  @Test
  void findIPInfoBannedIP() {
    Mockito.when(ipFinderService.getIPCompleteInfo(anyString())).thenReturn(null);
    ResponseEntity ipInfo = ipFinderController.findIPInfo("1.1.1.1");
    assertEquals(ipInfo.getBody(), "The IP 1.1.1.1 is banned");

  }

  @Test
  void findIPInfoSuccess() {
    IP ip = IP.builder()
        .ip("1.1.1.1")
        .country("Colombia")
        .build();
    Mockito.when(ipFinderService.getIPCompleteInfo(anyString())).thenReturn(ip);
    ResponseEntity<IP> ipInfo = ipFinderController.findIPInfo("1.1.1.1");
    assertEquals(ipInfo.getBody().getCountry(), "Colombia");
  }

  @Test
  void banIPInvalidIP() {
    ResponseEntity ipInfo = ipFinderController.banIP("2323df");
    assertEquals(ipInfo.getBody(), "The IP 2323df is not valid");
  }

  @Test
  void banIPError() {
    Mockito.when(ipFinderService.banIP(anyString())).thenReturn(null);
    ResponseEntity ipInfo = ipFinderController.banIP("1.1.1.1");
    assertEquals(ipInfo.getStatusCodeValue(), 500);
  }

  @Test
  void banIPSuccess() {
    IP ip = IP.builder()
        .ip("1.1.1.1")
        .country("Colombia")
        .build();
    Mockito.when(ipFinderService.banIP(anyString())).thenReturn(ip);
    ResponseEntity ipInfo = ipFinderController.banIP("1.1.1.1");
    assertEquals(ipInfo.getBody(), "The IP 1.1.1.1 was banned");
  }

}