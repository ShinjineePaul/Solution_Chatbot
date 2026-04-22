package com.org.mainframechatbot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.mainframechatbot.dto.ChatRequestDto;
import com.org.mainframechatbot.dto.ChatResponseDto;
import com.org.mainframechatbot.dto.TicketSummaryDto;
import com.org.mainframechatbot.service.KeywordExtractorService;
import com.org.mainframechatbot.service.OpenAiChatService;
import com.org.mainframechatbot.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

        private final KeywordExtractorService keywordExtractorService;
        private final TicketService ticketService;
        private final OpenAiChatService openAiChatService;

        @PostMapping
        public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {

                log.info("Chat request received. Query: '{}'", request.getQuery());

                // Step 1 — extract meaningful keywords from the raw user query
                List<String> keywords = keywordExtractorService.extract(request.getQuery());
                log.info("Extracted keywords: {}", keywords);

                // Step 2 — find top-N resolved IMT TEAM tickets matching the keywords
                // (throws NoMatchingTicketsException → 404 if nothing found)
                List<TicketSummaryDto> matchedTickets = ticketService.getMatchingTickets(keywords);
                log.info("Matched {} ticket(s) from tickets.txt", matchedTickets.size());

                // Step 3 — build the structured notes context block for GPT
                String notesContext = ticketService.buildNotesContext(matchedTickets);

                // Step 4 — call GPT with system prompt + notes + history + current query
                String answer = openAiChatService.chat(
                                request.getQuery(),
                                notesContext,
                                request.getHistory());

                // Step 5 — assemble and return the response
                return ResponseEntity.ok(
                                ChatResponseDto.builder()
                                                .answer(answer)
                                                .matchedTickets(matchedTickets)
                                                .extractedKeywords(keywords)
                                                .build());
        }
}