package com.example.aitools.model.response;

import lombok.Data;

@Data
public class ExtractResponse {
    private String platform;
    private String author;
    private String content;
    private Integer likes;
    private Integer comments;
    private String publishTime;
} 