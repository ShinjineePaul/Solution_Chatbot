package com.org.mainframechatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.mainframechatbot.dto.ChatRequestDto;
import com.org.mainframechatbot.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiChatServiceImpl implements OpenAiChatService {

    private final FeedbackService feedbackService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.openai.api-key}")
    private String geminiApiKey;

    @Value("${app.openai.model}")
    private String model;

    @Value("${app.openai.max-tokens}")
    private int maxTokens;

    @Value("${app.chat.system-prompt}")
    private String systemPrompt;

    /**
     * Removes all internal ticket ID references (e.g. "Ticket No: IMT3468016",
     * "referencing Ticket No: IMT3468016", "(referencing Ticket No: IMT3468016)")
     * from context before it reaches the model.
     */
    private String sanitizeContext(String context) {
        if (context == null)
            return "";
        return context
                // Removes patterns like: (referencing Ticket No: IMT3468016)
                .replaceAll("(?i)\\(?\\s*referencing\\s+Ticket\\s+No[:\\s]+IMT\\d+\\s*\\)?", "")
                // Removes patterns like: Ticket No: IMT3468016
                .replaceAll("(?i)Ticket\\s+No[:\\s]+IMT\\d+", "")
                // Removes any leftover bare IMT IDs
                .replaceAll("\\bIMT\\d+\\b", "")
                .trim();
    }

    @Override
    public String chat(String userQuery, String notesContext, List<ChatRequestDto.MessageDto> history) {
        try {
            // ── Build full prompt ─────────────────────────────────────────
            StringBuilder promptBuilder = new StringBuilder();

            // [1] System role
            promptBuilder.append(systemPrompt).append("\n\n");

            // [2] Feedback learnings
            String learnings = feedbackService.buildFeedbackLearnings();
            if (learnings != null && !learnings.isBlank()) {
                promptBuilder.append("=== PREVIOUSLY HELPFUL ANSWERS ===\n")
                        .append(learnings).append("\n\n");
            }

            // [3] Ticket notes context — sanitized to remove all internal ticket IDs
            promptBuilder.append(sanitizeContext(notesContext)).append("\n\n");

            // [4] Conversation history
            if (history != null && !history.isEmpty()) {
                promptBuilder.append("=== CONVERSATION HISTORY ===\n");
                history.forEach(h -> promptBuilder.append(h.getRole().toUpperCase())
                        .append(": ")
                        .append(h.getContent())
                        .append("\n"));
                promptBuilder.append("\n");
            }

            // [5] Current user query
            promptBuilder.append("USER: ").append(userQuery);

            String fullPrompt = promptBuilder.toString();

            // ── Build Gemini request body ─────────────────────────────────
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", fullPrompt)))),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "candidateCount", 1,
                            "maxOutputTokens", maxTokens));

            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + geminiApiKey;
            log.info("Calling Gemini API — URL: {}", geminiUrl);

            // ── Call Gemini ───────────────────────────────────────────────
            String responseJson = WebClient.builder()
                    .baseUrl(geminiUrl)
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .post()
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class).map(errorBody -> {
                                log.error("Gemini error body: {}", errorBody);
                                return new AiServiceException("Gemini rejected request: " + errorBody, null);
                            }))
                    .bodyToMono(String.class)
                    .block();

            // ── Parse response ────────────────────────────────────────────
            Map<String, Object> raw = mapper.readValue(responseJson,
                    new com.fasterxml.jackson.core.type.TypeReference<>() {
                    });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) raw.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new AiServiceException("Gemini returned no candidates", null);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            String answer = (String) parts.get(0).get("text");
            if (answer == null || answer.isBlank()) {
                throw new AiServiceException("Gemini returned empty text", null);
            }

            log.info("Gemini responded successfully — {} chars", answer.length());
            return answer;

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed — {}", e.getMessage(), e);
            throw new AiServiceException("Gemini API call failed for query: " + userQuery, e);
        }
    }
}