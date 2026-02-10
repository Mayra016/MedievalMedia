package com.MedievalMedia.Services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.MedievalMedia.Entities.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Docs https://www.mercadopago.com.ar/developers/es/reference/oauth/_oauth_token/post

@Service 
public class MercadoPagoService {
	
	//@Value("app.base-url")
	private String appBaseUrl = "http://localhost:8080";
	private Logger log = LoggerFactory.getLogger(MercadoPagoService.class);


	// authenticate user
	public String authenticateUser() throws IOException, InterruptedException {

		try {
			String body = """
					{
					  "client_id": "YOUR_CLIENT_ID",
					  "client_secret": "YOUR_CLIENT_SECRET",
					  "grant_type": "client_credentials"
					}
					""";

			    HttpRequest request = HttpRequest.newBuilder()
			            .uri(URI.create("https://api.mercadopago.com/oauth/token"))
			            .header("Content-Type", "application/json")
			            .POST(HttpRequest.BodyPublishers.ofString(body))
			            .build();

			    HttpClient client = HttpClient.newHttpClient();
			    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			    ObjectMapper mapper = new ObjectMapper();
			    JsonNode jsonNode = mapper.readTree(response.body());
			    String accessToken = jsonNode.get("access_token").asText();
			    
			    return accessToken;
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error getting token from Mercado Pago");
			
			return "Error getting token from Mercado Pago";
		}
	}



	// generate payment
	public PaymentDAO generatePayment(User user) {
		
	}
	
	
	// search payment
	
	// get payment
	
	// update payment 
	
	// generate qr code 
}
