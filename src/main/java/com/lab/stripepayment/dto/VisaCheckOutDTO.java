package com.lab.stripepayment.dto;

public record VisaCheckOutDTO(String name,
                              String email,
                              String currency,
                              Long ammount,
                              String subscriptionType) {
}
