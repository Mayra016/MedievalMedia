package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.PaymentRequest;
import com.MedievalMedia.Repositories.PaymentRepository;
import com.MedievalMedia.Repositories.UserRepository;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;


@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PayPalServiceTest {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	private PaypalService service;
	private PaymentService paymentService;


	@Value("${paypal.mode}") 
	private String mode;
	@Value("${paypal.client-id}") 
	private String clientId;
	@Value("${paypal.client-secret}") 
	private String clientSecret;
	@Value("${paypal.withdraw.url}")
	private String withdrawUrl;
	@Value("${paypal.balance.url}") 
	String balanceUrl;
	@Value("${paypal.auth.url}") 
	String authUrl;
	@Value("${paypal.email.client}") 
	String emailTest;

	
	
	@BeforeEach
	void initialize() {
		this.paymentService = new PaymentService(this.paymentRepository);
		this.service = new PaypalService(this.userRepository, this.clientId, this.clientSecret, this.mode, this.paymentService, this.withdrawUrl, this.balanceUrl, this.authUrl);
	}

	@Test
	public void withdrawPayment() throws ResponseStatusException, URISyntaxException, IOException, InterruptedException {
		User user = new User();
		user.setMoney(BigDecimal.valueOf(10));
		user = userRepository.save(user);
		
		this.service.withdrawPayment(user.getId(), BigDecimal.valueOf(10), this.emailTest, "BRL");
		
		user = this.userRepository.findById(user.getId()).get();
		
		assertTrue(user.getMoney().compareTo(BigDecimal.valueOf(10)) == 0);
	}
	
	@Test
	public void verifyBalanceOK() throws URISyntaxException, IOException, InterruptedException {
		String token = this.service.authenticate();
		boolean result = this.service.verifyBalance(BigDecimal.valueOf(0), token, "BRL");

		assertNotNull(token);
		assertTrue(result);
	}
	
	@Test
	public void createPaymentOK() throws PayPalRESTException {
		PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(100), "BRL", "", "", "Test", "", "");
		Payment payment = this.service.createPayment(request.total(), request.currency(), request.description());

		assertNotNull(payment);
		assertTrue(payment.getTransactions().size() > 0);
	}
}
