package com.org.mainframechatbot.service;

import com.org.mainframechatbot.dto.FeedbackRequestDto;
import com.org.mainframechatbot.model.Feedback;
import com.org.mainframechatbot.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    private static final int MAX_LEARNING_EXAMPLES = 5;

    @Override
    public void save(FeedbackRequestDto dto) {
        Feedback feedback = Feedback.builder()
                .userQuery(dto.getUserQuery())
                .aiResponse(dto.getAiResponse())
                .positive(dto.isPositive())
                .comment(dto.getComment())
                .build();

        feedbackRepository.save(feedback);
        log.info("Feedback saved — positive: {}, query: '{}'",
                dto.isPositive(), dto.getUserQuery());
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String buildFeedbackLearnings() {
        List<Feedback> topRated = feedbackRepository
                .findAllPositiveOrderByCreatedAtDesc()
                .stream()
                .limit(MAX_LEARNING_EXAMPLES)
                .toList();

        if (topRated.isEmpty()) {
            log.debug("No positive feedback found — skipping learning enrichment");
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Feedback f : topRated) {
            sb.append("Q: ").append(f.getUserQuery()).append("\n")
                    .append("A: ").append(f.getAiResponse()).append("\n")
                    .append("---\n");
        }

        log.debug("Injecting {} positive feedback examples into GPT prompt", topRated.size());
        return sb.toString();
    }
}