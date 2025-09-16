package com.MedievalMedia.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.MedievalMedia.Enums.Language;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="users")
@NoArgsConstructor
public class User {
	@Id
    @GeneratedValue
    private UUID id;
	private String email;
	private String username;
	private String password;
	private List<Language> languages;
	private Language appLanguage;
	private LocalDate birthday;
	private LocalDate enterDate;
	private String country;
	private String[] titles = new String[1];
	@OneToMany(mappedBy = "follows")
	private List<User> follow = new ArrayList<>();
	
	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	public User(String email, String username, String password, List<Language> languages, Language appLanguage, LocalDate birthday, String country, String title) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.languages = languages;
		this.appLanguage = appLanguage;
		this.birthday = birthday;
		this.enterDate = LocalDate.now();
		this.country = country;
		titles[0] = title;
	}

}
