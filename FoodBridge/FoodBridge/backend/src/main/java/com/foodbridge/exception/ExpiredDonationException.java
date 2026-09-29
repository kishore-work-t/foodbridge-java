package com.foodbridge.exception;

public class ExpiredDonationException extends RuntimeException {

    public ExpiredDonationException(String message) {
        super(message);
    }
}