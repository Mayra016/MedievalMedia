package com.MedievalMedia.Configurations;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.MedievalMedia.Entities.User;
import com.MedievalMedia.Enums.Language;

public class CustomUserDetails extends User implements UserDetails {
    private final UUID id;
    private List<Language> languages;
    private Language appLanguage;
    private String country;

    public CustomUserDetails(UUID id, String username, String email, String password, Language appLanguage, List<Language> languages, String country) {
        super(email, password);
        this.id = id;
        this.languages = languages;
        this.appLanguage = appLanguage;
        this.country = country;
    }

    public UUID getId() {
        return id;
    }
    
    public Language getLanguage() {
    	return this.appLanguage;
    }

	public List<Language> getLanguages() {
		return this.languages;
	}
	
	public String getCountry() {
		return this.country;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}
}
