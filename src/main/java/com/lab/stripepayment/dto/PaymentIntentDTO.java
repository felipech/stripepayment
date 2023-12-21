package com.lab.stripepayment.dto;

public record PaymentIntentDTO(Long ammount,
                               String currency,
                               String customerId) {
}
