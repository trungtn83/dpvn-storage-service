package com.dpvn.storageservice.web;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "ban-do-client",
    contextId = "ban-do-client",
    url = "https://sapnhap.bando.com.vn",
    configuration = FeignUtf8Config.class)
public interface BanDoClient {

  // body : id=0
  @PostMapping("/pcotinh")
  String getProvinces(@RequestBody String body);

  // body : id=33
  @PostMapping(value = "/ptracuu", consumes = "application/x-www-form-urlencoded; charset=UTF-8")
  @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
  String getWard(@RequestBody String body);
}
