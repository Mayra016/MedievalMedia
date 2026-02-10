package com.MedievalMedia.Controllers;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Records.UserDAO;
import com.MedievalMedia.Records.UserProfileInfoDAO;
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
	
	/**
	* Change credentials
	*
	* @param email The email 
	* @param request The http request to retrieve user info
	* @return ResponseEntity with the http status code 
	*
	* Http response codes:
	*
	* 200: if credential change email was successful changed
	* 401: if the token or user is not valid
	* 404: if the user was not found
	* 500: if an unknow server error occurred
	**/
	@PostMapping("/change-credentials")
	public ResponseEntity<String> changeCredentials(@RequestBody String email, HttpRequest request) {
	    String token = request.getHeaders().get("Authorization").toString().replace("Bearer: ", "");
	    
	    if (!this.jwtService.validateToken(token)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized");
	    }
	    
	    String userEmail = this.jwtService.extractEmail(token);
	    
	    if (!userEmail.equals(email)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized");
	    }
	
		return this.userService.changeCredentials(email);
	}
	
	/**
	* Login authentication 
	*
	* @param credentials The user's credential
	* @return ResponseEntity with the petition status code and user informations
	* 
	* Http response code:
	*
	* 200: if the authentication was successful
	* 403: if the authentication failed
	* 404: if this user was not found
	* 500: if an unknow server error occurred
	**/
	@PostMapping("/login-authentication")
	public ResponseEntity<String> authenticateUserFromLogin(@RequestBody UserDAO credentials) {
		try {
		    this.userService.authenticateUser(credentials);
		}
		catch (ResponseStatusException e) {
		    this.log.error(e.getReason());
		    
		    return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
		}
		catch (Exception e) {
		    this.log.error("Unknow server error " + credentials.email());
		    
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}
	
	/**
	* Verify if user is authenticated
	*
	* @param token The user authentication token
	* @return ResponseEntity with the petition status code and user informations
	*
	* Http response codes:
	*
	* 200: verify authentication and revalidate token if it is about to expire
	* 404: if user not found
	* 500: if an unknow server error occurred
	*
	**/
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
	
	/**
	 * Get user info
	 *
	 * @param userId The user id of the user the information must be retrieved
	 * @param request Access headers to verify jwt token
	 * @return ResponseEntity with the petition status code and user informations
	 * @throws ResponseStatusException if user not found
	 * 
	 * Http response status:
	 *
	 * 200: if user information 
	 * 404: if user was not found
	 * 500: if an unknow error occurred
	 */
	@GetMapping("/get-user-info")
	public ResponseEntity<UserProfileInfoDAO> getUserInfo(@RequestBody UUID userId, HttpRequest request) {
		try {
			String token = request.getHeaders().get("Authorization").toString().replace("Bearer: ", "");
			
			if (!this.jwtTokenService.validateToken(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			
			UserProfileInfoDAO userInfo = this.userService.getUserInfo(userId);
			
			return ResponseEntity.status(HttpStatus.OK).body(userInfo);
			
		} catch (ResponseStatusException e) {
			this.log.error("User not found ", e);
			
			return ResponseEntity.status(e.getStatusCode()).build();
		} catch (Exception e) {
			this.log.error("Unknow error getting user info ", e);
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
    /**
    * Create new user
    *
    * @param user A User object containing user information
    * @return ResponseEntity with the http status code and a custom response
    *
    * Http response codes:
    *
    * 200: New user successful created
    * 500: if an unknow server error occurred
    **/
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
	
	/**
	* Delete user
	*
	* @param userId The UUID of user that must be deleted
	* @return ResponseEntity with the http status code and a custom response
	*
	* Http response codes: 
	*
	* 200: user was successful deleted
	* 500: if unknow server error
	**/
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
