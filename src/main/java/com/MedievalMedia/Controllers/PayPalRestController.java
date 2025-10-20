package com.MedievalMedia.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MedievalMedia.Records.PaymentRequest;
import com.MedievalMedia.Services.PaypalService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

@RestController
@RequestMapping("/paypal")
public class PayPalRestController {
	private PaypalService paypalService;
	private Logger log = LoggerFactory.getLogger(PayPalRestController.class);
	

	@PostMapping("/create-payment")
	public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequest request ) {
		try {
			Payment payment = this.paypalService.createPayment(request.total(), request.currency(), request.method(), request.intent(), request.description(), request.cancelUrl(), request.successUrl());
			return ResponseEntity.status(HttpStatus.OK).body(payment);
		} catch (PayPalRESTException e) {
			this.log.error("Error communicating with PayPal RESTApi to create payment: " + e);
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		} catch (Exception e) {
			this.log.error("Error creating payment: " + e);
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
