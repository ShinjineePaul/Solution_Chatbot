package com.org.mainframechatbot.service;

import java.util.List;

public interface KeywordExtractorService {

    List<String> extract(String query);
}