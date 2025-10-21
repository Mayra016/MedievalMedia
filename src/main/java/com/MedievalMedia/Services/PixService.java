package com.MedievalMedia.Services;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.time.LocalDateTime;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Controllers.PayPalRestController;
import com.MedievalMedia.Entities.Interactions;
import com.MedievalMedia.Entities.Payment;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Status;
import com.MedievalMedia.Records.PixDTO;
import com.MedievalMedia.Records.RecurrencyPix;
import com.MedievalMedia.Repositories.PaymentRepository;
import com.MedievalMedia.Repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Service
public class PixService {

	@Value("${PIX_BASE_URL}") private String pixUrl;
	@Value("${PIX_TOKEN}") private String token;
	@Value("${PIX_RECEIVER}") private String receiverKey;
	
	private Interactions interactions = new Interactions();
	private Logger log = LoggerFactory.getLogger(PixService.class);
	
	@Getter
	private String price = "";
	private PaymentRepository paymentRepository;
	private UserRepository userRepository;
	
	public PixService(PaymentRepository paymentRepository, UserRepository userRepository) {
		this.paymentRepository = paymentRepository;
		this.userRepository = userRepository;
	}
	
	public PixService(PaymentRepository paymentRepository, String pixUrl, String token, String receiverKey) {
		this.paymentRepository = paymentRepository;
		this.pixUrl = pixUrl;
		this.token = token;
		this.receiverKey = receiverKey;
	}
	
	/**
	 * Creates a PIX QR code payment and stores the payment in the database.
	 *
	 * @param pix The DTO containing payment information matching Itaú API structure
	 * @param payerId The ID of the user who will pay (donor)
	 * @param finalUserId The ID of the user who will receive the payment
	 * @param donationType Type of donation, e.g., "coin" or "crown", used to calculate the amount
	 * @return String The generated QR code link to perform the PIX payment
	 * @throws URISyntaxException If the PIX API URL is invalid
	 * @throws IOException If the HTTP request to the PIX API fails
	 * @throws InterruptedException If the HTTP request is interrupted
	 * @throws ResponseStatusException If payment generation fails or business rules are violated
	 */

	public String createQRCode(PixDTO pix, String payerId, String finalUserId, String donationType) throws URISyntaxException, IOException, InterruptedException, ResponseStatusException {
		if (payerId.equals(finalUserId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Users can't donate for themselves");
		}

		if (donationType.equals("coin")) {
			this.price = this.interactions.getCoinPrices().get("reais");
		} else {
			this.price = this.interactions.getCrownPrices().get("reais");
		}
		
		// create request
		String paymentId = UUID.randomUUID().toString().replace("-", "");
	
		PixDTO finalPix = new PixDTO(Map.of("subtipo", "IMEDIATO"), Map.of("original", price), receiverKey, pix.devedor(), new RecurrencyPix(Map.of("contrato", paymentId, "objeto", "Donation at Medieval Media"), Map.of("dataInicial", pix.recorrencia().calendario().get("dataInicial"), "dataFinal", pix.recorrencia().calendario().get("dataFinal")), "NAO_PERMITE"));
		
		HttpRequest request = HttpRequest.newBuilder()
				  .uri(new URI(this.pixUrl + "/cobrancas"))
				  .headers("Content-Type", "application/json", "Authorization", "Bearer " + this.token,
					        "x-itau-correlationID", UUID.randomUUID().toString())
				  .POST(HttpRequest.BodyPublishers.ofString(finalPix.toString()))
				  .build();

		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		if (response.statusCode() == 201) {
			// verify link
			ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(response.body());
	        
	        String link = root.path("data").path("emv").asText();
	        
	        if (link != "") {
	        	// save payment in database
				Payment payment = new Payment();
				payment.setId(paymentId);	
				payment.setId_payer(payerId);
				payment.setId_final_user(finalUserId);
				payment.setTotal(BigDecimal.valueOf(Long.valueOf(this.price)));
				payment.setStatus(Status.PENDANT);
				payment.setDate(pix.recorrencia().calendario().get("dataInicial"));
				
				this.paymentRepository.save(payment);
				
				
				
				return link;
	        }

					
		} else {
			this.log.error("Error creating payment with pix: " + response.body());
			throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()), response.body());
		}
		       
		return "";
	}

}
