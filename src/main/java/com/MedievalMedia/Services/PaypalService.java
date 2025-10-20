package com.MedievalMedia.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.MedievalMedia.Entities.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

@Service
public class PaypalService {
	@Value("${PAYPAL_CLIENT_ID}") private String clientId;
	@Value("${PAYPAL_CLIENT_SECRET}") private String clientSecret;
	@Value("${PAYPAL_MODE}") private String mode;
	
	private final APIContext apiContext;
	private Logger log = LoggerFactory.getLogger(PaypalService.class);
	
	// get authentication
	public String getAccessToken() {
	    try {
	        String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://api-m.sandbox.paypal.com/v1/oauth2/token"))
	                .header("Authorization", "Basic " + auth)
	                .header("Content-Type", "application/x-www-form-urlencoded")
	                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
	                .build();

	        HttpClient client = HttpClient.newHttpClient();
	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        System.out.println("Paypal response: " + response.body());

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode jsonNode = mapper.readTree(response.body());
	        JsonNode tokenNode = jsonNode.get("access_token");

	        if (tokenNode == null) {
	            throw new RuntimeException("No access_token in Paypal response");
	        }

	        return tokenNode.asText();
	    } catch (Exception e) {
	        e.printStackTrace();
	        this.log.error("Error getting Paypal access token: " + e.getMessage());
	        return "";
	    }
	}

	// generate payment
	public Payment createPayment(Double total, String currency, String method, String intent, String description, String cancelUrl, String successUrl) 
	 throws PayPalRESTException {
		Amount amount = new Amount();
		amount.setCurrency(currency);
		amount.setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total));
		
		Transaction transaction = new Transaction();
		transaction.setDescription(description);
		transaction.setAmount(amount);
		
		List<Transaction> transactions = new ArrayList<>();
		transactions.add(transaction);
		
		Payer payer = new Payer();
		payer.setPaymentMethod(method);
		
		
		Payment payment = new Payment();
		payment.setIntent(intent);
		payment.setPayer(payer);
		payment.setTransactions(transactions);
		
		RedirectUrls redirections = new RedirectUrls();
		redirections.setCancelUrl(cancelUrl);
		redirections.setReturnUrl(successUrl);
		
		payment.setRedirectUrls(redirections);
		
		return payment.create(apiContext);
		
	}
	
	public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
		Payment payment = new Payment();
		payment.setId(paymentId);
		
		PaymentExecution execution = new PaymentExecution();
		execution.setPayerId(payerId);
		
		return payment.execute(this.apiContext, execution);	
	}
	
	// search payment
	
	// get payment
	
	// update payment 
	
	// generate qr code 
}
