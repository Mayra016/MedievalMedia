package com.MedievalMedia.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {
	PENDANT,
	APPROVED,
	CANCELED,
	FAILED,
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
            default:
                throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
