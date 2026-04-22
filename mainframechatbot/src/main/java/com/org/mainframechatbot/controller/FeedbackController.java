package com.org.mainframechatbot.controller;

import com.org.mainframechatbot.dto.FeedbackRequestDto;
import com.org.mainframechatbot.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<String> submitFeedback(@Valid @RequestBody FeedbackRequestDto dto) {
        log.info("Feedback received — positive: {}, query: '{}'",
                dto.isPositive(), dto.getUserQuery());

        feedbackService.save(dto);

        String message = dto.isPositive()
                ? "Thank you! Your feedback helps improve future answers."
                : "Thank you for the feedback. We will work on improving this answer.";

        return ResponseEntity.ok(message);
    }
}