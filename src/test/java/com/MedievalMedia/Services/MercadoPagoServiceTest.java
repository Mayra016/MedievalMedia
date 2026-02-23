package com.MedievalMedia.Services;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MercadoPagoServiceTest {

	@InjectMocks
	MercadoPagoService service;
	
	@Test
	public void getAuthenticated() throws IOException, InterruptedException, NullPointerException {
		String token = this.service.authenticateUser();
		System.out.println("TOKEN : " + token);
	}
}
