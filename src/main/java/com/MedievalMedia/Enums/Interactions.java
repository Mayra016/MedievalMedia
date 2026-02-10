package com.MedievalMedia.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Interactions {
	SWORD,
	HEART,
	SHIELD,
	CROWN,
	COIN;
	
	@JsonCreator
    public static Interactions fromString(String value) {
        switch (value) {
            case "SWORD":
                return SWORD;
            case "HEART":
                return HEART;
            case "SHIELD":
                return SHIELD;
            case "CROWN":
                return CROWN;
            case "COIN":
                return COIN;
            default:
                throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
