package com.mrblrrd.cachecontrolplayground;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class HelloWorldController {

    private static final String ETAG_VALUE = "W/\"qwerty\"";

    @GetMapping("/helloworld")
    ResponseEntity<String> getHelloWorld() {
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.empty().cachePrivate())
                .eTag(ETAG_VALUE)
                .body("Hello, World!");
    }

    @PostMapping("/helloworld")
    ResponseEntity<String> postHelloWorld(@RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false) String inputETag) {
        if (Objects.equals(inputETag, ETAG_VALUE)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(CacheControl.empty().cachePrivate())
                    .eTag(ETAG_VALUE)
                    .build();
        }
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noCache().cachePrivate())
                .eTag(ETAG_VALUE)
                .body("Hello, World!");
    }
}
