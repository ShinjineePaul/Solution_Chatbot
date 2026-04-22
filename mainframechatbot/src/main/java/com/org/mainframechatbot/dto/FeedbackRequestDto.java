package com.org.mainframechatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeedbackRequestDto {

    @NotBlank(message = "User query must not be blank")
    private String userQuery;

    @NotBlank(message = "AI response must not be blank")
    private String aiResponse;

    private boolean positive;

    private String comment;
}