package com.MedievalMedia.Records;

import java.util.List;
import java.util.Map;

public record PayPalWithdrawDTO(
    SenderBatchHeader sender_batch_header,
    List<Item> items
) {
    public record SenderBatchHeader(
        String sender_batch_id,
        String email_subject,
        String email_message
    ) {}

    public record Item(
        String recipient_type,
        Map<String, String> amount,
        String receiver,
        String note,
        String sender_item_id
    ) {}
}
