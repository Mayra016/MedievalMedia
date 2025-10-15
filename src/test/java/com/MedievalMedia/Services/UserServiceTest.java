package com.MedievalMedia.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.MedievalMedia.Entities.Post;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Records.UserDAO;
import com.MedievalMedia.Records.UserProfileInfoDAO;
import com.MedievalMedia.Repositories.PostRepository;
import com.MedievalMedia.Repositories.UserRepository;
import org.mockito.junit.jupiter.MockitoExtension;

@DataJpaTest
public class UserServiceTest {

	    private UserRepository userRepository;

	    private PasswordEncoder passwordEncoder;
	    
	    private UserService userService;
	    
	    private JwtTokenService jwtTokenService  = new JwtTokenService();
		private EmailService emailService = null;
	    
		@Autowired
		public UserServiceTest (UserRepository userRepository) {
			this.userRepository = userRepository;
		}
		
		@BeforeEach
	    void initialize() {
			this.passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
	    	this.userService = new UserService(userRepository, jwtTokenService, passwordEncoder, emailService);
	    }
	    
	    @Test
	    void getUserInfo() {
	    	User user = new User();
	    	user.setUsername("Test User");
	    	user.setEmail("test@test.com");
	        user.setPassword("$2a$10$hashedPassword");
	    	user.setAppLanguage(Language.DEUTSCH);
	    	user = this.userRepository.save(user);
	    	
	    	UserProfileInfoDAO userInfo = this.userService.getUserInfo(user.getId());
	    	
	    	assertNotNull(userInfo, "Can't be null");
	    	assertEquals(userInfo.name(), user.getUsername());
	    	assertEquals(userInfo.appLanguage(), user.getAppLanguage());
	    }

	    @Test
	    void testAuthenticateUserSuccess() {
	        UserDAO credentials = new UserDAO("test@test.com", "123456");
	        User user = new User();
	        user.setEmail("test@test.com");
	        user.setPassword(this.passwordEncoder.encode(credentials.password()));
	        
	        this.userRepository.save(user);
	        
	        User savedUser = this.userRepository.findByEmail("test@test.com").get();

	        assertNotNull(savedUser);
	        
	        ResponseEntity<String> response = this.userService.authenticateUser(credentials);

	        assertEquals(HttpStatus.OK, response.getStatusCode());
	        assertNotNull(response.getBody()); 
	    }

	    @Test
	    void testAuthenticateUserWrongPassword() {
	        UserDAO credentials = new UserDAO("test@example.com", "wrong");
	        User user = new User();
	        user.setEmail("test@example.com");
	        user.setPassword(this.passwordEncoder.encode("correct"));

	        User savedUser = this.userRepository.save(user);
	        
	        assertNotNull(savedUser);
	        
	        ResponseEntity<String> response = this.userService.authenticateUser(credentials);

	        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	        assertNotNull(response.getBody()); 
	    }

	    @Test
	    void testAuthenticateUserUserNotFound() {
	        UserDAO credentials = new UserDAO("notfound@example.com", "123456");

	        ResponseEntity<String> response = userService.authenticateUser(credentials);

	        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	    }
	}

