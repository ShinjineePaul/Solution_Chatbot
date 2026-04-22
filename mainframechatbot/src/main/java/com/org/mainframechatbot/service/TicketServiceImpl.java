package com.org.mainframechatbot.service;

import com.org.mainframechatbot.dto.TicketSummaryDto;
import com.org.mainframechatbot.model.Ticket;
import com.org.mainframechatbot.repository.TicketFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketFileRepository ticketFileRepository;

    @Value("${app.tickets.max-results:3}")
    private int maxResults;

    @Override
    public List<TicketSummaryDto> getMatchingTickets(List<String> keywords) {
        List<Ticket> tickets = ticketFileRepository.findResolvedByKeywords(keywords);

        if (tickets.isEmpty()) {
            log.info("No matching tickets found for keywords: {}", keywords);
            return List.of();
        }

        return tickets.stream()
                .limit(maxResults)
                .map(this::toDTO)
                .toList();
    }

    @Override
    public String buildNotesContext(List<TicketSummaryDto> tickets) {
        if (tickets.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("=== RESOLVED TICKET NOTES (IMT TEAM) ===\n\n");

        for (TicketSummaryDto t : tickets) {
            sb.append("Ticket No : ").append(t.getTicketNo()).append("\n")
                    .append("SL No     : ").append(t.getSlNo()).append("\n")
                    .append("Issue     : ").append(t.getDescription()).append("\n")
                    .append("Resolution Steps:\n").append(t.getNotes())
                    .append("\n\n---\n\n");
        }

        return sb.toString();
    }

    private TicketSummaryDto toDTO(Ticket t) {
        return TicketSummaryDto.builder()
                .slNo(t.getSlNo())
                .ticketNo(t.getTicketNo())
                .assignedTo(t.getAssignedTo())
                .status(t.getStatus())
                .description(t.getDescription())
                .notes(t.getNotes())
                .build();
    }
}