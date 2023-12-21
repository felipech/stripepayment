package com.lab.stripepayment.dto;

import com.lab.stripepayment.model.ChargeData;

public record PaymentDto(

        String cardNumber,
        Long monthExp,
        Long yearExp,
        String cvc,
        String userName
) {}
