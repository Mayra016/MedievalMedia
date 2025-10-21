package com.MedievalMedia.Records;

import java.util.Map;

public record PixDTO(Map<String, String> qrcodePix, Map<String, String> valor, String chave, Map<String, String> devedor, RecurrencyPix recorrencia) {

}
