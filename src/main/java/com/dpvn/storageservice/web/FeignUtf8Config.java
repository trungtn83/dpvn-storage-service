package com.dpvn.storageservice.web;

import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class FeignUtf8Config {
  @Bean
  public Decoder feignDecoder() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setDefaultCharset(StandardCharsets.UTF_8);

    return new OptionalDecoder(
        new ResponseEntityDecoder(new SpringDecoder(() -> new HttpMessageConverters(converter))));
  }
}
