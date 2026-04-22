package com.org.mainframechatbot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KeywordExtractorServiceImpl implements KeywordExtractorService {

    private static final List<String> DOMAIN_PHRASES = List.of(
            "dispensing fee issue",
            "ingredient cost issue",
            "ingredient issue",
            "dispensing fee",
            "reprice issue",
            "copay issue",
            "rej 70",
            "rej mr");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "it", "in", "on", "at", "to", "for", "of", "and", "or",
            "my", "we", "our", "i", "me", "you", "they", "can", "this", "that", "how", "what",
            "when", "why", "get", "fix", "help", "with", "from", "has", "have", "was", "been",
            "are", "not", "do", "does", "did", "its", "their", "there", "please", "hi", "hello",
            "facing", "getting", "seeing", "having", "problem", "issue", "error", "ticket",
            "claim", "team", "support");

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<String> extract(String query) {
        if (query == null || query.isBlank())
            return List.of();

        String lower = query.toLowerCase().trim();
        List<String> keywords = new ArrayList<>();
        String remaining = lower;

        for (String phrase : DOMAIN_PHRASES) {
            if (remaining.contains(phrase)) {
                keywords.add(phrase);
                remaining = remaining.replace(phrase, " ");
            }
        }

        List<String> tokens = Arrays.stream(remaining.split("[\\s\\p{Punct}]+"))
                .map(String::trim)
                .filter(w -> w.length() >= 3)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toList());

        keywords.addAll(tokens);

        return keywords.stream().distinct().collect(Collectors.toList());
    }
}