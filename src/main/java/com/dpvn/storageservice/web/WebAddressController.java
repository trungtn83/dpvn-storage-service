package com.dpvn.storageservice.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/address")
public class WebAddressController {
  private final WebAddressService webAddressService;

  public WebAddressController(WebAddressService webAddressService) {
    this.webAddressService = webAddressService;
  }

  @PostMapping("/sync-all")
  public void syncAll() {
    webAddressService.syncAll();
  }
}
