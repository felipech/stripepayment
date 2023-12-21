package com.lab.stripepayment.dto;

public record PriceDTO(String productId,
                       Long ammount,
                       String currency) {
}
