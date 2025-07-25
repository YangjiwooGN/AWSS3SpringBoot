package com.example.awss3test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

// 인스턴스를 자동으로 생성해주는 어노테이션
@Component
// RestController 클래스에 앞과 뒤에서 수행하는 동작을 정의할 수 있도록 해주는 어노테이션
@RestControllerAdvice
public class FileUploadFailedException {
    // 파일 업로드 도중 최대 크기를 초과하는 예외가 발생했을 때 동작하는 핸들러
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        ErrorResponse response = ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, "용량 초과").build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
