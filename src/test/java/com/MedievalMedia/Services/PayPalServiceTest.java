package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Records.PaymentRequest;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalServiceTest {
	private PaypalService service;
	

	@BeforeEach
	void initialize() {
		this.service = new PaypalService();		
	}
	
	@Test
	public void createPaymentOK() throws PayPalRESTException {
		PaymentRequest request = new PaymentRequest(Double.valueOf(0.02), "USD", "PAYPAL", "ORDER", "Test", "", "");
		Payment payment = this.service.createPayment(request.total(), request.currency(), request.method(), request.intent(), request.description());
		
		assertNotNull(payment);
		assertTrue(payment.getTransactions().size() > 0);
	}
	
	@Test
	public void createPaymentError() throws PayPalRESTException {
		PaymentRequest request = new PaymentRequest(Double.valueOf(0.02), "USD", "invalid method", "ORDER", "Test", "", "");
		
		PayPalRESTException exception = assertThrows(
				PayPalRESTException.class,
    			() -> this.service.createPayment(request.total(), request.currency(), request.method(), request.intent(), request.description())
        );
		assertNotNull(exception);
		assertTrue(exception instanceof PayPalRESTException);
	}
}
