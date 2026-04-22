package com.org.mainframechatbot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketSummaryDto {

    private int slNo;

    private String ticketNo;

    private String assignedTo;

    private String status;

    private String description;

    private String notes;
}