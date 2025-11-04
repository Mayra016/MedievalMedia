package com.MedievalMedia.Records;

import java.util.Map;

public record PayPalWithdrawDTO(Map<String, String> amount, Map<String, String> sender_batch_header) {

}
