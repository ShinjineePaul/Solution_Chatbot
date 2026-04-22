package com.org.mainframechatbot.exception;

public class TicketFileNotFoundException extends RuntimeException {
    public TicketFileNotFoundException(String path) {
        super("Ticket data file not found at path: " + path);
    }
}