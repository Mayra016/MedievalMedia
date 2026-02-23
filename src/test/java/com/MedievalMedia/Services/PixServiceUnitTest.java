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

import com.MedievalMedia.Records.PixDTO;
import com.MedievalMedia.Records.RecurrencyPix;
import com.MedievalMedia.Repositories.PaymentRepository;
import com.MedievalMedia.Repositories.UserRepository;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class PixServiceUnitTest {
	@Autowired
    private PaymentRepository repository;
	@Autowired
	private UserRepository userRepository;

    private PixService service;
    
    private String pixUrl = "https://devportal.itau.com.br/sandboxapi/itau-ep9-api-qrcode-pix-automatico-v1-externo/v1";
	@Value("${PIX_TOKEN_SANDBOX}") 
	private String token;
	@Value("${PIX_RECEIVER}") 
	private String receiverKey;
	@Value("${PIX_CLIENT_ID}") 
	private String itauPixClientId;
	@Value("${PIX_CLIENT_SECRET}") 
	private String itauClientSecret;
	
	@BeforeEach
	public void initialize() throws InterruptedException {
		this.service = new PixService(this.repository, this.pixUrl, this.token, this.receiverKey, this.itauPixClientId, this.itauClientSecret);
	}
	
	@Test
	void authenticateOK() throws URISyntaxException, IOException, InterruptedException {
		String token = this.service.authenticate();
		
		assertNotNull(token);
		assertTrue(token!="");
	}
	
	@Test
	void createQRCodeSimpleOK() throws ResponseStatusException, URISyntaxException, IOException, InterruptedException {
		PixDTO pix = new PixDTO(Map.of("subtipo", "IMEDIATO"), Map.of("original", "1"), "", Map.of("nome", "Username", "cnpj", ""), new RecurrencyPix(Map.of("contrato", "", "objeto", "Donation at Medieval Media"), Map.of("dataInicial", LocalDateTime.now().toString(), "dataFinal", LocalDateTime.now().toString()), "NAO_PERMITE"));
		
		String link = this.service.createQRCodeSimple(pix, "payer", "finalUser", "coin");
		System.out.println("####TEST######" + link);
		Thread.sleep(2000);
		assertNotNull(link);
		assertTrue(!link.equals(""));
	}
	
	@Test
	void createQRCodeOK() throws ResponseStatusException, URISyntaxException, IOException, InterruptedException {
		PixDTO pix = new PixDTO(Map.of("subtipo", "IMEDIATO"), Map.of("original", "1"), "", Map.of("nome", "Username", "cnpj", ""), new RecurrencyPix(Map.of("contrato", "", "objeto", "Donation at Medieval Media"), Map.of("dataInicial", LocalDateTime.now().toString(), "dataFinal", LocalDateTime.now().toString()), "NAO_PERMITE"));
		
		String link = this.service.createQRCode(pix, "payer", "finalUser", "coin");
		
		Mockito.verify(this.service.getPrice()).equals("5");
		
		assertNotNull(link);
		assertTrue(!link.equals(""));
	}
}
