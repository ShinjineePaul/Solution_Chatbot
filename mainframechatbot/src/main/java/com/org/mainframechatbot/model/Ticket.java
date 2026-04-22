package com.org.mainframechatbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    private int slNo;

    private String ticketNo;

    private String assignedTo;

    private String status;

    private String description;

    private String notes;
}
