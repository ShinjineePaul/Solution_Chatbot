package com.org.mainframechatbot.service;

import com.org.mainframechatbot.dto.TicketSummaryDto;

import java.util.List;

public interface TicketService {

    List<TicketSummaryDto> getMatchingTickets(List<String> keywords);

    String buildNotesContext(List<TicketSummaryDto> tickets);
}