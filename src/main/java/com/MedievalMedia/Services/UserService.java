package com.MedievalMedia.Services;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.MedievalMedia.Configurations.CustomUserDetails;
import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;
import com.MedievalMedia.Repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {
	private final UserRepository userRepository;
    private Logger log = LoggerFactory.getLogger(UserService.class);
    private JwtTokenService jwtTokenService;
    
    @Autowired
	public UserService(UserRepository userRepository, JwtTokenService jwtTokenService) {
		this.userRepository = userRepository;
		this.jwtTokenService = jwtTokenService;
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
}
