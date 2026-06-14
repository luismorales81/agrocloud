package com.agrocloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/porcinos/formulas")
public class FormulaController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllFormulas(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearFormula(@RequestBody Map<String, Object> formula, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(new java.util.HashMap<>());
    }
}







