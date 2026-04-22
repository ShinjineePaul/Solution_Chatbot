package com.org.mainframechatbot.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestDto {

    @NotBlank(message = "Query must not be blank")
    private String query;

    private List<MessageDto> history;

    @Data
    public static class MessageDto {
        private String role;
        private String content;
    }
}