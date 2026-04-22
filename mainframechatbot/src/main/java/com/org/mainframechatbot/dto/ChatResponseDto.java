package com.org.mainframechatbot.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDto {

    private String answer;

    private List<TicketSummaryDto> matchedTickets;

    private List<String> extractedKeywords;
}