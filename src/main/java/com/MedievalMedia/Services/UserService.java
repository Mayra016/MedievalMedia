package com.MedievalMedia.Services;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Records.PostDAO;
import com.MedievalMedia.Records.UserDAO;
import com.MedievalMedia.Records.UserProfileInfoDAO;
import com.MedievalMedia.Repositories.UserRepository;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService implements UserDetailsService {
	private final UserRepository userRepository;
    private Logger log = LoggerFactory.getLogger(UserService.class);
    private JwtTokenService jwtTokenService;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    
    @Autowired
	public UserService(UserRepository userRepository, JwtTokenService jwtTokenService,
			PasswordEncoder passwordEncoder, EmailService emailService) {
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}
    
    @Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
    
    public UUID getCurrentUserId(String email) {
    	User user = this.userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		return user.getId();
    }
    
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> searchUser = userRepository.findByEmail(email);
		if (searchUser.isPresent()) {
			User user = searchUser.get();
	        return new CustomUserDetails(
	            user.getId(),
	            user.getUsername(),
	            user.getEmail(),
	            user.getPassword(),
	            user.getAppLanguage(),
	            user.getLanguages(),
	            user.getCountry()
	        );
        } else {
    		this.log.error("USER SERVICE DOESN'T FOUND THIS USER: " + email);
    		System.err.println("ERROR NULL USER");
        	throw new UsernameNotFoundException(email);
        }
	}

	public Language setLanguageWithLocale() {
		Locale userLocale = LocaleContextHolder.getLocale();
		
		if (userLocale.getLanguage().startsWith("de")) {
			return Language.DEUTSCH;
		}
		
		if (userLocale.getLanguage().startsWith("es")) {
			return Language.ESPAÑOL;
		}
		
		if (userLocale.getLanguage().startsWith("pt")) {
			return Language.PORTUGUÊS;
		}
		
		return Language.ENGLISH;
	}
	
	public String isUserAuthenticated(String token) {
		
		String email = this.jwtTokenService.extractEmail(token);
		
		if (this.jwtTokenService.validateToken(token)) {
			// load user		
			UserDetails userDetails = loadUserByUsername(email);
			
			UsernamePasswordAuthenticationToken authentication =
	                new UsernamePasswordAuthenticationToken(
	                    userDetails, null, userDetails.getAuthorities()
	                );
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
			if (this.jwtTokenService.isTokenAboutToExpire(token)) {
				token = JwtTokenService.generateToken(email);
			}
		}
		
		return token;
	}
	
	public ResponseEntity<String> authenticateUser(UserDAO credentials) {
		try {
			Optional<User> searchUser = this.userRepository.findByEmail(credentials.email());
			
			if (searchUser.isPresent()) {
				User user = searchUser.get();
				if (passwordEncoder.matches(credentials.password(), user.getPassword())) {
					String token = JwtTokenService.generateToken(credentials.email());
					return ResponseEntity.status(HttpStatus.OK).body(token);
				} else {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong credentials");
				}
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Unknow error authenticating login");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unknow error authenticating login");
			
		}
	}

	public ResponseEntity<String> changeCredentials(String email) {
		try {
			Optional<User> searchUser = this.userRepository.findByEmail(email);

			if (searchUser.isPresent()) {
				User user = searchUser.get();
				
				String token = JwtTokenService.generateToken(email);
				this.emailService.sendChangeCredentials(email, token, user.getAppLanguage());
				
				return ResponseEntity.status(HttpStatus.OK).body("Change password email successfuly sended");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			this.log.error("Error changing credentials");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email to change password");
		}
	}

	/**
	 * Get user info
	 *
	 * @param userId The user id of the user the information must be retrieved
	 * @return UserProfileInfoDAO with the user informations
	 * @throws ResponseStatusException 
	 * 	NOT_FOUND: if user not found
	 */
	public UserProfileInfoDAO getUserInfo(UUID userId) {
		User user = this.userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		UserProfileInfoDAO userInfo = new UserProfileInfoDAO(user.getId(), user.getUsername(), user.getTitles(), user.getReign(), user.getAppLanguage(), user.getBirthday());
		return userInfo;
	}
}
