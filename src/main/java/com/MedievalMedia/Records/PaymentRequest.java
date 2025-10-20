package com.MedievalMedia.Records;

public record PaymentRequest(Double total, String currency, String method, String intent, String description, String cancelUrl, String successUrl) {

}
