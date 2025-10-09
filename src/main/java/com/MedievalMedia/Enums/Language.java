package com.MedievalMedia.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Language {
	ENGLISH,
	ESPAÑOL,
	DEUTSCH,
	PORTUGUÊS;
	
	@JsonCreator
    public static Language fromString(String value) {
        switch (value) {
            case "ENGLISH":
                return ENGLISH;
            case "PORTUGUÊS":
                return PORTUGUÊS;
            case "ESPAÑOL":
                return ESPAÑOL;
            case "DEUTSCH":
                return DEUTSCH;
            default:
                throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}