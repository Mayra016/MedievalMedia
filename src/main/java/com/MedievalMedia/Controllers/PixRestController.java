package com.MedievalMedia.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Records.PixRequestDTO;
import com.MedievalMedia.Repositories.PaymentRepository;
import com.MedievalMedia.Services.JwtTokenService;
import com.MedievalMedia.Services.PixService;

@RestController
@RequestMapping("/pix/")
public class PixRestController {
	private PaymentRepository paymentRepository;
	private PixService pixService;
	private JwtTokenService jwtService;
	private Logger log = LoggerFactory.getLogger(PixRestController.class);
	
	
	
	public PixRestController(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
		this.pixService = new PixService(this.paymentRepository);
		this.jwtService = new JwtTokenService();
	}

	@PostMapping("/create-payment")
	public ResponseEntity<String> createPayment(@RequestBody PixRequestDTO pix, HttpRequest request) {
		try {
			String token = request.getHeaders().get("Authorization").toString().replace("Bearer: ", "");
			
			if (!this.jwtService.validateToken(token)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be loged in to donate");
			}
			
			String link = this.pixService.createQRCode(pix.pix(), pix.payerId(), pix.finalUserId(), pix.donationType());
			
			return ResponseEntity.status(HttpStatus.OK).body(link);
		} catch (ResponseStatusException e) {
			this.log.error("Unknow error creating pix qr code payment " + e);
			
			return ResponseEntity.status(e.getStatusCode()).body(e.getBody().toString());
		} catch (Exception e) {
			this.log.error("Unknow error creating pix qr code payment " + e);
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknow error creating pix qr code payment");
		}
	}
}
