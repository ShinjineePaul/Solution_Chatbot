package com.org.mainframechatbot.repository;

import com.org.mainframechatbot.exception.TicketFileNotFoundException;
import com.org.mainframechatbot.model.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TicketFileRepository {

    private static final int COL_SL_NO = 0;
    private static final int COL_TICKET_NO = 1;
    private static final int COL_ASSIGNED_TO = 2;
    private static final int COL_STATUS = 3;
    private static final int COL_DESCRIPTION = 4;
    private static final int COL_NOTES = 5;
    private static final int EXPECTED_COLS = 6;

    private static final String DELIMITER = "\\|";
    private static final String RESOLVED_STATUS = "resolved";
    private static final String IMT_TEAM = "imt team";

    @Value("${app.tickets.file-path}")
    private String filePath;

    public List<Ticket> findResolvedByKeywords(List<String> keywords) {
        Path path = Path.of(filePath);
        if (!Files.exists(path))
            throw new TicketFileNotFoundException(filePath);

        try {
            // Pre-filter resolved + IMT TEAM lines first (raw string check — no object
            // creation)
            // This reduces the working set before any parsing happens
            List<String> resolvedLines = Files.lines(path)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .filter(this::isResolvedByImtTeamRaw)
                    .collect(Collectors.toList());

            // Try all keywords match first for highest relevance
            List<Ticket> allMatch = resolvedLines.stream()
                    .map(this::parseLine)
                    .filter(t -> t != null)
                    .filter(t -> descriptionMatchesAllKeywords(t.getDescription(), keywords))
                    .sorted((a, b) -> Integer.compare(b.getSlNo(), a.getSlNo()))
                    .toList();

            if (!allMatch.isEmpty()) {
                log.info("Found {} ticket(s) matching ALL keywords", allMatch.size());
                return allMatch;
            }

            // Fall back to any keyword match
            List<Ticket> anyMatch = resolvedLines.stream()
                    .map(this::parseLine)
                    .filter(t -> t != null)
                    .filter(t -> descriptionMatchesAnyKeyword(t.getDescription(), keywords))
                    .sorted((a, b) -> Integer.compare(b.getSlNo(), a.getSlNo()))
                    .toList();

            log.info("Fallback: found {} ticket(s) matching ANY keyword", anyMatch.size());
            return anyMatch;

        } catch (IOException e) {
            log.error("Failed to read ticket file at: {}", filePath, e);
            return Collections.emptyList();
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isResolvedByImtTeamRaw(String line) {
        String[] cols = line.split(DELIMITER, -1);
        if (cols.length < EXPECTED_COLS)
            return false;
        return RESOLVED_STATUS.equalsIgnoreCase(cols[COL_STATUS].trim())
                && IMT_TEAM.equalsIgnoreCase(cols[COL_ASSIGNED_TO].trim());
    }

    private boolean isResolvedByImtTeam(Ticket t) {
        return RESOLVED_STATUS.equalsIgnoreCase(t.getStatus())
                && IMT_TEAM.equalsIgnoreCase(t.getAssignedTo());
    }

    private Ticket parseLine(String line) {
        String[] cols = line.split(DELIMITER, -1);
        if (cols.length < EXPECTED_COLS) {
            log.warn("Skipping malformed line: {}", line);
            return null;
        }
        try {
            return Ticket.builder()
                    .slNo(Integer.parseInt(cols[COL_SL_NO].trim()))
                    .ticketNo(cols[COL_TICKET_NO].trim())
                    .assignedTo(cols[COL_ASSIGNED_TO].trim())
                    .status(cols[COL_STATUS].trim())
                    .description(cols[COL_DESCRIPTION].trim())
                    .notes(cols[COL_NOTES].trim())
                    .build();
        } catch (NumberFormatException e) {
            log.warn("Skipping line with non-numeric SL_NO: {}", line);
            return null;
        }
    }

    private boolean descriptionMatchesAllKeywords(String description, List<String> keywords) {
        if (description == null || description.isBlank())
            return false;
        String lower = description.toLowerCase();
        return keywords.stream().allMatch(kw -> lower.contains(kw.toLowerCase()));
    }

    private boolean descriptionMatchesAnyKeyword(String description, List<String> keywords) {
        if (description == null || description.isBlank())
            return false;
        String lower = description.toLowerCase();
        return keywords.stream().anyMatch(kw -> lower.contains(kw.toLowerCase()));
    }
}