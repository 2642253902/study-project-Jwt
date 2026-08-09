package org.example.controller.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ValidationController {

    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> validateException(ValidationException e) {
        log.warn("Resolve [{}:{}]", e.getClass().getName(), e.getMessage());
        return RestBean.failure(400, "参数校验失败");
    }
}
