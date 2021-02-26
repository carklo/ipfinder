package com.meli.challenge.controllers;

import com.meli.challenge.domain.IP;
import com.meli.challenge.services.IPFinderService;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping(value = "/ipfinder")
public class IPFinderController {

  private final IPFinderService ipFinderService;

  @Autowired
  public IPFinderController(IPFinderService ipFinderService) {
    this.ipFinderService = ipFinderService;
  }

  @RequestMapping(value = "/ip/{ip}", produces = "application/json; charset=UTF-8", method = RequestMethod.GET)
  public ResponseEntity findIPInfo(@PathVariable String ip) {
    if(!InetAddressValidator.getInstance().isValidInet4Address(ip)) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("The IP "+ip+" is not valid");
    } else {
      try {
        IP ipCompleteInfo = ipFinderService.getIPCompleteInfo(ip);
        if (ipCompleteInfo == null) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The IP "+ip+" is banned");
        } else {
          return ResponseEntity.ok(ipCompleteInfo);
        }
      } catch (HttpClientErrorException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body("It wasn't possible to retrieve the country's information for IP "+ip);
      }
    }
  }

  @RequestMapping(value = "/banIp/{ip}", produces = "application/json; charset=UTF-8", method = RequestMethod.POST)
  public ResponseEntity banIP(@PathVariable String ip) {
    if(!InetAddressValidator.getInstance().isValidInet4Address(ip)) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("The IP "+ip+" is not valid");
    } else {
      IP ip1 = ipFinderService.banIP(ip);
      if (ip1 != null) {
        return ResponseEntity.ok("The IP "+ip+" was banned");
      } else {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("There was a problem banning the IP");
      }
    }
  }
}
