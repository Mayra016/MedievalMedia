package com.MedievalMedia.Records;

public record PaymentRequest(BigDecimal total, String currency, String method, String intent, String description, String cancelUrl, String successUrl) {

}
