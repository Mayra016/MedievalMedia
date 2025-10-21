package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Records.PixDAO;
import com.MedievalMedia.Records.RecurrencyPix;
import com.MedievalMedia.Repositories.PaymentRepository;

@DataJpaTest
@TestPropertySource(properties = {
	    "PIX_BASE_URL=https://sandbox.devportal.itau.com.br/itau-ep9-api-qrcode-pix-automatico-v1-externo/v1/",
	    "PIX_CLIENT_ID=id",
	    "PIX_CLIENT_SECRET=secret",
	    "PIX_RECEIVER=recei",
	    "PIX_TOKEN_SANDBOX=token",
	    "USER_PASSWORD=098"
	})
class PixServiceUnitTest {
	@Autowired
    private PaymentRepository repository;

    private PixService service;
    
    private String pixUrl = "https://sandbox.devportal.itau.com.br/itau-ep9-api-qrcode-pix-automatico-v1-externo/v1/";
	@Value("${PIX_TOKEN}") private String token;
	@Value("${PIX_RECEIVER}") private String receiverKey;
	
    @Autowired
    public PixServiceUnitTest() {
    	this.service = new PixService(this.repository);
    }
	
	@BeforeEach
	public void initialize() {
		this.service = new PixService(this.repository, this.pixUrl, this.token, this.receiverKey);
	}
	
	@Test
	void createQRCodeOK() throws ResponseStatusException, URISyntaxException, IOException, InterruptedException {
		PixDAO pix = new PixDAO(Map.of("subtipo", "IMEDIATO"), Map.of("original", "1"), "", Map.of("nome", "Username", "cnpj", ""), new RecurrencyPix(Map.of("contrato", "", "objeto", "Donation at Medieval Media"), Map.of("dataInicial", LocalDateTime.now().toString(), "dataFinal", LocalDateTime.now().toString()), "NAO_PERMITE"));
		
		String link = this.service.createQRCode(pix, "payer", "finalUser", "coin");
		
		Mockito.verify(this.service.getPrice()).equals("5");
		
		assertNotNull(link);
		assertTrue(!link.equals(""));
	}
}
