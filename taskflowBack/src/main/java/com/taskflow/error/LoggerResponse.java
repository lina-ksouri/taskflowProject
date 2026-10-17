package com.taskflow.error;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class LoggerResponse {
    private final ObjectMapper objectMapper;

    public LoggerResponse(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void logger(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String message) throws IOException {
        ResultError body = new ResultError(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
