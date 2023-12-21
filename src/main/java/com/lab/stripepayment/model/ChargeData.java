package com.lab.stripepayment.model;

public record ChargeData(Long ammount,
                         String currency,
                         String cardTok ) {
}
