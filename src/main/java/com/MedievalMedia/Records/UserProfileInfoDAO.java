package com.MedievalMedia.Records;

import java.time.LocalDate;
import java.util.UUID;

import com.MedievalMedia.Enums.Language;

public record UserProfileInfoDAO(UUID id, String name, String[] titles, String reign, Language appLanguage, LocalDate birthday) {

}
