package com.taskflow.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResultError(LocalDateTime dateResult,
                          int status,
                          String error,
                          String message,
                          Map<String, String> fieldErrors) {
}
