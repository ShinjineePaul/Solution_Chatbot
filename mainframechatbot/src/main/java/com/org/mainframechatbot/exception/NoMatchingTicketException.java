package com.org.mainframechatbot.exception;

public class NoMatchingTicketException extends RuntimeException {
    public NoMatchingTicketException(String keywords) {
        super("No resolved IMT TEAM tickets found matching keyword(s): " + keywords);
    }
}