package com.ddd4j.cloud.web.api;

import com.ddd4j.cloud.core.contract.annotation.RawResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @RawResponse
    @GetMapping("/health")
    public String healthCheck() {
        return "200";
    }
}