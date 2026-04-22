package com.org.mainframechatbot.service;

import com.org.mainframechatbot.dto.ChatRequestDto;

import java.util.List;

public interface OpenAiChatService {
    String chat(String userQuery,
            String notesContext,
            List<ChatRequestDto.MessageDto> history);
}