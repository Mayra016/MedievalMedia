package com.MedievalMedia.Records;

public record PixRequestDTO(
	    PixDTO pix,
	    String payerId,
	    String finalUserId,
	    String donationType
	) {

}
