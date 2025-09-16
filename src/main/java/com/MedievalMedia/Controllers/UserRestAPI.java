package com.MedievalMedia.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Repositories.UserRepository;
//038
@RestController
@RequestMapping("/api/v1/user")
public class UserRestAPI {
	private Logger log = LoggerFactory.getLogger(UserRestAPI.class);
	private UserRepository userRepository;
	
	public UserRestAPI(UserRepository userRepository) {
		this.userRepository = userRepository;
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
}
