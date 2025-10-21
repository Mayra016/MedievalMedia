package com.MedievalMedia.Records;

import java.util.Map;

public record PixDAO(Map<String, String> qrcodePix, Map<String, String> valor, String chave, Map<String, String> devedor, RecurrencyPix recorrencia) {

}
