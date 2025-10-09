package com.MedievalMedia.Controllers;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.UserDAO;
import com.MedievalMedia.Repositories.UserRepository;
import com.MedievalMedia.Services.JwtTokenService;
import com.MedievalMedia.Services.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserRestAPI {
	private Logger log = LoggerFactory.getLogger(UserRestAPI.class);
	private UserRepository userRepository;
	private JwtTokenService jwtTokenService; 
	private UserService userService;
	
	@Autowired
	public UserRestAPI(UserRepository userRepository, JwtTokenService jwtTokenService,
			UserService userService) {
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
		this.userService = userService;
	}
	
	// change user credentials
	
	@PostMapping("/change-credentials")
	public ResponseEntity<String> changeCredentials(@RequestBody String email) {
		return this.userService.changeCredentials(email);
	}
	
	@PostMapping("/login-authentication")
	public ResponseEntity<String> authenticateUserFromLogin(@RequestBody UserDAO credentials) {
		return this.userService.authenticateUser(credentials);
	}
	
	@PostMapping("/auth")
	public ResponseEntity<String> isUserAuthenticated(@RequestBody String token) {
		try {
			token = userService.isUserAuthenticated(token);

			return ResponseEntity.status(HttpStatus.OK).body(token);
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Error verifying if user is already authenticated");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(token);
		}
	}

	@PostMapping("/create")
	public ResponseEntity<String> createUser(@RequestBody User user) {
		try {
			this.userRepository.save(user);
			
			return ResponseEntity.status(HttpStatus.OK).body("Success creating new user");
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error creating user: " + user.getEmail());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
		}
	}
	
	@PostMapping("/delete")
	public ResponseEntity<String> deleteUser(@RequestBody UUID userId) {
		try {
			this.userRepository.deleteById(userId);
			
			return ResponseEntity.status(HttpStatus.OK).body("Success by deleting user");
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error deleting user");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
		}
	}
}
