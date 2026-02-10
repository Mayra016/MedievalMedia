package com.MedievalMedia.Services;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Controllers.PayPalRestController;
import com.MedievalMedia.Entities.Interactions;
import com.MedievalMedia.Entities.Payment;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Status;
import com.MedievalMedia.Models.PixItauToken;
import com.MedievalMedia.Records.CobDTO;
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
	@Value("${PIX_TOKEN_SANDBOX}") private String token;
	@Value("${PIX_RECEIVER}") private String receiverKey;
	@Value("${ITAU_API_TOKEN}") private String itauApiToken;
	@Value("${PIX_URL_ITAU}") private String apiPixRecebimentos;
	@Value("${ITAU_PIX_CLIENT_ID}") private String itauPixClientId;
	@Value("${ITAU_PIX_CLIENT_SECRET}") private String itauPixClientSecret;
	
	private Interactions interactions = new Interactions();
	private Logger log = LoggerFactory.getLogger(PixService.class);
	
	private String price = "";
	private PixItauToken tokenModel = new PixItauToken();
	private PaymentRepository paymentRepository;
	private UserRepository userRepository;
	
	public PixService(PaymentRepository paymentRepository, UserRepository userRepository) {
		this.paymentRepository = paymentRepository;
		this.userRepository = userRepository;
	}
	
	public PixService(PaymentRepository paymentRepository, String pixUrl, String token, String receiverKey, String itauPixClientId, String itauClientSecret) {
		this.paymentRepository = paymentRepository;
		this.pixUrl = pixUrl;
		this.token = token;
		this.receiverKey = receiverKey;
		this.itauPixClientId = itauPixClientId;
		this.itauPixClientSecret = itauClientSecret;
	}
	
	//https://devportal.itau.com.br/nossas-apis/itau-ep9-gtw-pix-recebimentos-ext-v2?tab=especificacaoTecnica
	public String createQRCodeSimple(PixDTO pix, String payerId, String finalUserId, String donationType) throws URISyntaxException, IOException, InterruptedException {
		String newToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxNThkY2Q2OS1jZTc2LTM3MWMtYjJlYy0zMGZkMzUwYzJmMDAiLCJleHAiOjE3NjIzNzA0NjgsImlhdCI6MTc2MjM3MDE2OCwic291cmNlIjoic3RzLXNhbmRib3giLCJlbnYiOiJQIiwiZmxvdyI6IkNDIiwic2NvcGUiOiJjYXNobWFuYWdlbWVudC1jb25zdWx0YWJvbGV0b3MtdjEtYXdzLXNjb3BlIiwidXNlcm5hbWUiOiJtYXlyYTE2dmZ4QGdtYWlsLmNvbSIsIm9yZ2FuaXphdGlvbk5hbWUiOiJBdXRvIENhZGFzdHJvIn0.8yeK8ZbLLvFxrnbcXNpiJonN93k9b38nEP5_-ptESSs"; //this.authenticate();
		
		CobDTO cob = new CobDTO(
                Map.of("original", "10.00"),
                "11963372215"
        );

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(cob);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://sandbox.devportal.itau.com.br/itau-ep9-gtw-pix-recebimentos-ext-v2/v2"))
                .headers(
                        "Content-Type", "application/json",
                        "x-itau-apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxNThkY2Q2OS1jZTc2LTM3MWMtYjJlYy0zMGZkMzUwYzJmMDAiLCJleHAiOjE3NjIzNzI5ODMsImlhdCI6MTc2MjM3MjY4Mywic291cmNlIjoic3RzLXNhbmRib3giLCJlbnYiOiJQIiwiZmxvdyI6IkNDIiwic2NvcGUiOiJjYXNobWFuYWdlbWVudC1jb25zdWx0YWJvbGV0b3MtdjEtYXdzLXNjb3BlIiwidXNlcm5hbWUiOiJtYXlyYTE2dmZ4QGdtYWlsLmNvbSIsIm9yZ2FuaXphdGlvbk5hbWUiOiJBdXRvIENhZGFzdHJvIn0.2vmm2dd2OxHL_HiW7QEJ_9Vg0AfcECM2SASwdp02814",
                        "x-itau-correlationID", UUID.randomUUID().toString()
                )
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        
        Thread.sleep(4000);

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            
            JsonNode root = mapper.readTree(response.body());
            String pixCopiaECola = root.path("pixCopiaECola").asText();
            String txid = root.path("txid").asText();

            System.out.println("TXID: " + txid);
            System.out.println("Pix Copia e Cola: " + pixCopiaECola);

            return pixCopiaECola; // QR Code
        } else {
            throw new RuntimeException("Error creating payment cobrança: " + response.statusCode() + " - " + response.body());
        }
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
	        paymentId = root.path("txid").asText();	
	        
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
			System.out.println("#################" + response.toString());
			this.log.error("Error creating payment with pix: " + response.body());
			throw new ResponseStatusException(HttpStatus.valueOf(response.statusCode()), response.body());
		}
		       
		return "";
	}
	
	/**
	 * Verify pendant payments in data base and verify its status in paypal
	 * 
	 * @throws URISyntaxException 
	 * @throws NOT_FOUND If the PIX API URL is invalid
	 * @throws IOException If the HTTP request to the PIX API fails
	 * @throws InterruptedException If the HTTP request is interrupted
	 * @throws ResponseStatusException If payment generation fails or business rules are violated

	 * Itaú API Docs: https://devportal.itau.com.br/nossas-apis/itau-ep9-gtw-pix-recebimentos-ext-v2?tab=especificacaoTecnica#operation/get/cob/{txid}
	 */
	@Scheduled(fixedDelay = 86400000)
	public void verifyPendantPayments() throws URISyntaxException, IOException, InterruptedException {
		
		List<Payment> payments = this.paymentRepository.findByStatus(Status.PENDANT);
		Set<Payment> toDelete = new HashSet<>();
		String acess_token = this.authenticate();
		
		for (Payment payment : payments) {
			// call api at /cob/{idSolicRec} endpoint to get payment status and update payment status  
			HttpRequest request = HttpRequest.newBuilder()
					  .uri(new URI(this.apiPixRecebimentos + "/cob/" + payment.getId()))
					  .headers("Content-Type", "application/json", "Authorization", "Bearer " + acess_token,
						        "x-itau-correlationID", UUID.randomUUID().toString(), "x-itau-apikey", this.itauPixClientId)
					  .GET()
					  .build();

			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(response.body());
	        
	        String link = root.path("status").asText();
			
	        if (link.equals("APROVADA")) {
	        	payment.setStatus(Status.APPROVED);
	        	this.paymentRepository.save(payment);
	        }
	        
	        if (link.equals("COMPLETADA")) {
	        	payment.setStatus(Status.COMPLETED);
	        	this.paymentRepository.save(payment);
	        }
			
			User finalUser = new User();
						
			if (payment.getStatus() == Status.COMPLETED) {
				// update payment status in data base
				Optional<User> searchUser = this.userRepository.findById(UUID.fromString(payment.getId_final_user()));
				
				if (searchUser.isPresent()) {
				    finalUser = searchUser.get();
				    finalUser.setMoney(finalUser.getMoney().add(payment.getTotal().min((payment.getTotal().multiply(BigDecimal.valueOf(0.5))))));	
				    
				    this.userRepository.save(finalUser);
				}
				
				
			} else {
				if (payment.getStatus() == Status.PENDANT && LocalDateTime.now().minusDays(3).isAfter(LocalDateTime.parse(payment.getDate()))) {
					toDelete.add(payment);
				}
				
			}	
		}
		
		this.paymentRepository.deleteAll(toDelete);
	
	}
	
	/*
	*   Authenticate
	*   Docs: https://devportal.itau.com.br/autenticacao-documentacao#client-jwt-tls
	
	* @return acess_token
	*/
	protected String authenticate() throws URISyntaxException, IOException, InterruptedException {
		if (!this.tokenModel.isValid()) {
			this.tokenModel.authenticate();
		}
		
		return this.tokenModel.getToken();
	}
	
	/**
	*   Withdraw payment using Pix
	*   
	*   @param userId the id of the user that requested the withdraw
	*   @param value The total value of the transaction
	*   @param key The pix key of the user
	*   @throws NOT_FOUND if the user who requested the transaction
	*/
	
	public void withdrawPayment(UUID userId, BigDecimal value, String key) {
		User user = this.userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		
		if (user.getMoney().compareTo(value) < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You haven't enough money");
		}
		
		if (value.compareTo(BigDecimal.valueOf(20)) < 0) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Withdraws lesser than R$ 20,00 are not allowed");
		}
		
		Payment payment = new Payment();
		payment.setId(UUID.randomUUID().toString());
		payment.setDate(LocalDateTime.now().toString());
		payment.setId_final_user(userId.toString());
		payment.setId_payer("MEDIEVAL_MEDIA");
		payment.setStatus(Status.WITHDRAW_REQUESTED_PIX);
		payment.setTotal(value);
		
		user.setMoney(user.getMoney().min(value));
		
		this.paymentRepository.save(payment);
		this.userRepository.save(user);
	}
	
	/**
	 *  Update withdraw status after the requested withdraw were completed
	 *  
	 *  @param paymentId The id of the payment that has been successfully withdrawn
	 *  @throws BAD_REQUEST: if paymentIds is empty
	 */
	
	public void updateWithdraw(List<String> paymentIds) {
		if (paymentIds.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data");
		}
		
		List<Payment> payments = this.paymentRepository.findAllByIdAndStatus(paymentIds, Status.WITHDRAW_REQUESTED_PIX);
		
		for (Payment payment : payments) {
			payment.setStatus(Status.WITHDRAW_COMPLETED);
		}
				
		this.paymentRepository.saveAll(payments);
	}
}
