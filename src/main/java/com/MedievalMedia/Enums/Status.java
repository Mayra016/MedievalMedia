package com.MedievalMedia.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
	PENDANT,
	APPROVED,
	CANCELED,
	FAILED,
	WITHDRAW_REQUESTED,
	WITHDRAW_REQUESTED_PIX,
	WITHDRAW_COMPLETED,
	INVALID_BALANCE_PAYPAL,
	COMPLETED;
	
	@JsonCreator
    public static Status fromString(String value) {
        switch (value) {
            case "PENDANT":
                return PENDANT;
            case "APPROVED":
                return APPROVED;
            case "CANCELED":
                return CANCELED;
            case "FAILED":
                return FAILED;
            case "COMPLETED":
                return COMPLETED;
            case "WITHDRAW_REQUESTED":
                return WITHDRAW_REQUESTED;
            case "WITHDRAW_COMPLETED":
                return WITHDRAW_COMPLETED;
            case "INVALID_BALANCE_PAYPAL":
            	return INVALID_BALANCE_PAYPAL;
            default:
                throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
