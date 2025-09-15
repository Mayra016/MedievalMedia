package com.MedievalMedia.Services;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

	private final static SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private final static long EXPIRATION_TIME = 259200000; // 3 days
	
	public static String generateToken(String email) {
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SECRET_KEY, SignatureAlgorithm.HS256)
				.compact();
	}
	
	public boolean validateToken(String token) {
		return !isTokenExpired(token);
	}
	
	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date(System.currentTimeMillis()));
	}
	
	private Date extractExpiration(String token) {
		JwtParser jwtParser = Jwts.parserBuilder()
				.setSigningKey(SECRET_KEY)
				.build();
		
		return (Date) jwtParser.parseClaimsJws(token)
				.getBody()
				.getExpiration();
	}
	
	public String extractEmail(String token) {
		JwtParser jwtParser = Jwts.parserBuilder()
				.setSigningKey(SECRET_KEY)
				.build();
		
		return jwtParser.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
}
