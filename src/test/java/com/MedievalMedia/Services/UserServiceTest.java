package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.UserDAO;
import com.MedievalMedia.Repositories.UserRepository;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	    @Mock
	    private UserRepository userRepository;

	    @Mock
	    private PasswordEncoder passwordEncoder;

	    @InjectMocks
	    private UserService userService;

	    @Test
	    void testAuthenticateUserSuccess() {
	        UserDAO credentials = new UserDAO("test@test.com", "123456");
	        User user = new User();
	        user.setEmail("test@test.com");
	        user.setPassword("$2a$10$hashedPassword");

	        when(userRepository.findByEmail("test@test.com"))
	            .thenReturn(Optional.of(user));

	        when(passwordEncoder.matches("123456", user.getPassword()))
	            .thenReturn(true);

	        ResponseEntity<String> response = userService.authenticateUser(credentials);

	        assertEquals(HttpStatus.OK, response.getStatusCode());
	        assertNotNull(response.getBody()); 
	    }

	    @Test
	    void testAuthenticateUserWrongPassword() {
	        UserDAO credentials = new UserDAO("test@example.com", "wrong");
	        User user = new User();
	        user.setEmail("test@example.com");
	        user.setPassword("$2a$10$hashedPassword");

	        when(userRepository.findByEmail("test@example.com"))
	            .thenReturn(Optional.of(user));

	        when(passwordEncoder.matches("wrong", user.getPassword()))
	            .thenReturn(false);

	        ResponseEntity<String> response = userService.authenticateUser(credentials);

	        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	    }

	    @Test
	    void testAuthenticateUserUserNotFound() {
	        UserDAO credentials = new UserDAO("notfound@example.com", "123456");

	        when(userRepository.findByEmail("notfound@example.com"))
	            .thenReturn(Optional.empty());

	        ResponseEntity<String> response = userService.authenticateUser(credentials);

	        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    }
	}

