package com.agrocloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simple")
public class SimpleTestController {
    
    @GetMapping("/test")
    public String test() {
        return "Simple test controller is working!";
    }
}
