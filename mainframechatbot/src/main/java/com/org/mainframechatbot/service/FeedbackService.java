package com.org.mainframechatbot.service;

import com.org.mainframechatbot.dto.FeedbackRequestDto;

public interface FeedbackService {

    void save(FeedbackRequestDto dto);

    String buildFeedbackLearnings();
}