package com.example.aitools.controller;

import com.example.aitools.common.R;
import com.example.aitools.model.request.ExtractRequest;
import com.example.aitools.model.response.ExtractResponse;
import com.example.aitools.service.ExtractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/extract")
@RequiredArgsConstructor
public class ExtractController {
    
    private final ExtractService extractService;
    
    @PostMapping("/content")
    public R<ExtractResponse> extractContent(@RequestBody ExtractRequest request) {
        try {
            ExtractResponse response = extractService.extractContent(request.getUrl());
            return R.success(response);
        } catch (Exception e) {
            return R.error(500, e.getMessage());
        }
    }
} 