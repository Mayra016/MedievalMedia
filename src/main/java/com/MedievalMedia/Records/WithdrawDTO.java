package com.MedievalMedia.Records;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawDTO(UUID userId, BigDecimal money, String pixKey) {

}
