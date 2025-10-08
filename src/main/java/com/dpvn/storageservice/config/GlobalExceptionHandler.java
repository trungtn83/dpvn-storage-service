package com.dpvn.storageservice.config;

import com.dpvn.shared.exception.ApiError;
import com.dpvn.shared.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleBadRequest(BadRequestException ex, HttpServletRequest request) {
    return new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());
  }
}
